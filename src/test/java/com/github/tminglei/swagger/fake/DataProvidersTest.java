package com.github.tminglei.swagger.fake;

import com.github.tminglei.bind.Framework;
import com.github.tminglei.swagger.SwaggerContext;
import com.github.tminglei.swagger.bind.MappingConverter;
import com.github.tminglei.swagger.bind.MappingConverterImpl;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;
import org.junit.Test;

import java.util.*;

import static com.github.tminglei.bind.Constraints.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.swagger.SwaggerContext.*;
import static org.junit.Assert.*;

/**
 * Created by minglei on 4/18/17.
 */
public class DataProvidersTest {
    private Random random = new Random();

    private List<String> statuses = Arrays.asList("available", "pending", "sold");
    private Framework.Mapping<?> petStatus =
        $(text(oneOf(statuses))).desc("pet status in the store")
            .example(gen(() -> statuses.get(random.nextInt(3)))).$$;

    private Framework.Mapping<?> pet =
        mapping(
            field("id", $(longv(required())).desc("pet id").example(gen("petId")).$$),
            field("name", $(text(required())).desc("pet name").$$),
            field("category", $(mapping(
                field("id", longv(required())),
                field("name", text(required()))
            )).desc("category belonged to").$$),
            field("photoUrls", $(list(text())).desc("pet's photo urls").example(Arrays.asList("http://example.com/photo1")).$$),
            field("tags", $(list(text())).desc("tags for the pet").example(Arrays.asList("tag1", "tag2")).$$),
            field("status", petStatus)
        );
    private Framework.Mapping<?> pet1 =
        $(pet).refName("Pet").desc("pet info").$$;

    private List<String> optionals = Arrays.asList("category", "photoUrls", "tags", "status");
    private List<String> paramVals = Arrays.asList("id");

    private Map<String, Object> expectedSample =
        hashmap(
            entry("id", 101L),
            entry("name", "kitty"),
            entry("category", hashmap(
                entry("id", 202L),
                entry("name", "cat1")
            )),
            entry("photoUrls", Arrays.asList("http://example.com/photo1")),
            entry("tags", Arrays.asList("tag1", "tag2")),
            entry("status", "available")
        );
    private Map<String, Object> exampleVals =
        hashmap(
            entry("id", null),
            entry("name", null),
            entry("category", null),
            entry("photoUrls", Arrays.asList("http://example.com/photo1")),
            entry("tags", Arrays.asList("tag1", "tag2")),
            entry("status", "available")
        );


    private MappingConverter converter = new MappingConverterImpl();

    ///---

    @Test
    public void testCollectDataProviders() {
        Property property = converter.mToProperty(pet);
        DataProvider dataProvider = DataProviders.getInstance().collect(new Swagger(), property, true);

        dataProvider.setRequired(true);
        dataProvider.setRequestParams(hashmap(entry("petId", String.valueOf(101L))));
        Object generated = dataProvider.get();
        checkEquals(expectedSample, generated, "", optionals, paramVals);

        Object collected = collectData(new Swagger(), property);
        checkEquals(exampleVals, collected, "", optionals, paramVals);
    }

    @Test
    public void testCollectDataProviders_withRefModels() {
        SwaggerContext.getInstance().scanAndRegisterNamedModels(pet1);
        Swagger swagger = SwaggerContext.getInstance().getSwagger();

        Property property = converter.mToProperty(pet1);
        DataProvider dataProvider = DataProviders.getInstance().collect(swagger, property, true);

        dataProvider.setRequired(true);
        dataProvider.setRequestParams(hashmap(entry("petId", String.valueOf(101L))));
        Object generated = dataProvider.get();
        checkEquals(expectedSample, generated, "", optionals, paramVals);

        Object collected = collectData(swagger, property);
        checkEquals(exampleVals, collected, "", optionals, paramVals);
    }

    @Test
    public void testCollectDataProviders_clean() {
        Property property1 = converter.mToProperty(pet);
        DataProviders.getInstance().collect(new Swagger(), property1, false);
        assertTrue("id's example object should be type of `DataProvider`",
            ((ObjectProperty) property1).getProperties().get("id").getExample() instanceof DataProvider);
        assertTrue("status's example object shoudl be type of `DataProvider`",
            ((ObjectProperty) property1).getProperties().get("status").getExample() instanceof DataProvider);

        Property property2 = converter.mToProperty(pet);
        DataProviders.getInstance().collect(new Swagger(), property2, true);
        assertTrue("id's example object should be null",
            ((ObjectProperty) property2).getProperties().get("id").getExample() == null);
        assertTrue("status's example object should be type of `String`",
            ((ObjectProperty) property2).getProperties().get("status").getExample() instanceof String);
    }

    ///---

    private Object collectData(Swagger swagger, Property property) {
        if (property.getExample() != null) {
            return property.getExample();
        } else if (property instanceof RefProperty) {
            Model model = swagger.getDefinitions().get(((RefProperty) property).getSimpleRef());
            if (model instanceof ArrayModel) {
                Property itemProperty = ((ArrayModel) model).getItems();
                return Arrays.asList(collectData(swagger, itemProperty));
            } else if (model instanceof ModelImpl) {
                Map map = new HashMap();
                Map<String, Property> fields = model.getProperties();
                for (String field : fields.keySet()) {
                    map.put(field, collectData(swagger, fields.get(field)));
                }
                return map;
            }
            throw new IllegalArgumentException("Unsupported model type: " + model.getClass());
        } else if (property instanceof ObjectProperty) {
            Map map = new HashMap();
            Map<String, Property> fields = ((ObjectProperty) property).getProperties();
            for (String field : fields.keySet()) {
                map.put(field, collectData(swagger, fields.get(field)));
            }
            return map;
        } else if (property instanceof MapProperty) {
            Property valueProperty = ((MapProperty) property).getAdditionalProperties();
            return hashmap(entry("k1", collectData(swagger, valueProperty)));
        } else if (property instanceof ArrayProperty) {
            Property itemProperty = ((ArrayProperty) property).getItems();
            return Arrays.asList(collectData(swagger, itemProperty));
        } else {
            return property.getExample();
        }
    }

    private void checkEquals(Object obj1, Object obj2, String path, List<String> optionals, List<String> paramVals) {
        if (obj1 == null || obj2 == null) {
            if (!optionals.contains(path))
                assertEquals(obj1, obj2);
        } else if (obj1.getClass() != obj2.getClass()) {
            if (paramVals.contains(path) && (obj1 instanceof String || obj2 instanceof String)) ;
            else assertEquals(obj1.getClass(), obj2.getClass());
        } else if (obj1 instanceof Map) {
            Map map1 = (Map) obj1;
            Map map2 = (Map) obj2;

            assertEquals(map1.size(), map2.size());
            for (Object key : map1.keySet()) {
                String path1 = path + (path.isEmpty() ? "" : ".") +key;
                checkEquals(map1.get(key), map2.get(key), path1, optionals, paramVals);
            }
        } else if (obj1 instanceof List) {
            List list1 = new ArrayList((List) obj1);
            List list2 = new ArrayList((List) obj2);

            assertEquals(list1.size(), list2.size());
            Collections.sort(list1);
            Collections.sort(list2);
            for (int i = 0; i < list1.size(); i++) {
                String path1 = path + "[" + i + "]";
                checkEquals(list1.get(i), list2.get(i), path1, optionals, paramVals);
            }
        } else {
            assertEquals(obj1.getClass(), obj2.getClass());
        }
    }
}
