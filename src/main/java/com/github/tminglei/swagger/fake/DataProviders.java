package com.github.tminglei.swagger.fake;

import com.github.javafaker.Faker;
import com.mifmif.common.regex.Generex;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.tminglei.swagger.SimpleUtils.isEmpty;
import static com.github.tminglei.swagger.SwaggerContext.entry;
import static com.github.tminglei.swagger.SwaggerContext.gen;

/**
 * Created by minglei on 4/15/17.
 */
public class DataProviders {
    private static final Logger logger = LoggerFactory.getLogger(DataProviders.class);

    private static DataProviders INSTANCE = new DataProviders();

    private Faker faker = new Faker();

    public static DataProviders getInstance() {
        return DataProviders.INSTANCE;
    }
    public static void setInstance(DataProviders instance) {
        DataProviders.INSTANCE = instance;
    }

    /**
     * collect data providers from schema
     *
     * @param swagger the hosting swagger object
     * @param schema  the schema object
     * @param clean   whether to clean data providers in the schema
     * @return  organized data provider
     */
    public DataProvider collect(Swagger swagger, Property schema, boolean clean) {
        if (schema == null) return new ConstDataProvider(null);

        Object example = schema.getExample();
        if (example instanceof DataProvider) {
            DataProvider provider = (DataProvider) example;
            if (clean) {
                provider.setRequired(true);
                schema.setExample(provider.get());
            }
            provider.setRequired(schema.getRequired());
            return provider;
        } else if (example != null) {
            DataProvider dataProvider = new ConstDataProvider(example);
            dataProvider.setRequired(schema.getRequired());
            return dataProvider;
        }

        ///---
        DataProvider dataProvider = null;
        if (schema instanceof RefProperty) {
            dataProvider = collectRefProperty(swagger, (RefProperty) schema, clean);
        }
        else if (schema instanceof ObjectProperty) {
            dataProvider = collectObjectProperty(swagger, (ObjectProperty) schema, clean);
        }
        else if (schema instanceof MapProperty) {
            dataProvider = collectMapProperty(swagger, (MapProperty) schema, clean);
        }
        else if (schema instanceof ArrayProperty) {
            dataProvider = collectArrayProperty(swagger, (ArrayProperty) schema, clean);
        }
        else if (schema instanceof AbstractNumericProperty) {
            dataProvider = collectNumericProperty(swagger, (AbstractNumericProperty) schema, clean);
        }
        else if (schema instanceof ByteArrayProperty || schema instanceof BinaryProperty) {
            dataProvider = collectByteProperty(swagger, schema, clean);
        }
        else if (schema instanceof DateProperty || schema instanceof DateTimeProperty) {
            dataProvider = collectDateProperty(swagger, schema, clean);
        }
        else if (schema instanceof EmailProperty) {
            dataProvider = collectEmailProperty(swagger, (EmailProperty) schema, clean);
        }
        else if (schema instanceof FileProperty) {
            dataProvider = collectFileProperty(swagger, (FileProperty) schema, clean);
        }
        else if (schema instanceof UUIDProperty) {
            dataProvider = collectUUIDProperty(swagger, (UUIDProperty) schema, clean);
        }
        else if (schema instanceof BooleanProperty) {
            dataProvider = collectBooleanProperty(swagger, (BooleanProperty) schema, clean);
        }
        else if (schema instanceof StringProperty) {
            dataProvider = collectStringProperty(swagger, (StringProperty) schema, clean);
        }
        else if (schema instanceof PasswordProperty) {
            dataProvider = collectPasswordProperty(swagger, (PasswordProperty) schema, clean);
        }

        if (dataProvider != null) {
            dataProvider.setRequired(schema.getRequired());
            return dataProvider;
        }

        throw new IllegalArgumentException("Unsupported property type: " + schema.getClass());
    }

    protected DataProvider collectRefProperty(Swagger swagger, RefProperty schema, boolean clean) {
        Model model = swagger.getDefinitions() != null ? swagger.getDefinitions().get(schema.getSimpleRef()) : null;
        if (model == null) throw new IllegalArgumentException("CAN'T find model for " + schema.getSimpleRef());

        if (model instanceof ArrayModel) {
            DataProvider itemProvider = collect(swagger, ((ArrayModel) model).getItems(), clean);
            return new ListDataProvider(itemProvider, schema.getSimpleRef());
        } else if (model instanceof ModelImpl) {
            Map<String, DataProvider> fields =
                (model.getProperties() != null ? model.getProperties() : Collections.<String, Property>emptyMap()).entrySet().stream()
                    .map(e -> entry(e.getKey(), collect(swagger, e.getValue(), clean)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            return new ObjectDataProvider(fields, schema.getSimpleRef());
        }

        throw new IllegalArgumentException("Unsupported model type: " + model.getClass());
    }

    protected DataProvider collectObjectProperty(Swagger swagger, ObjectProperty schema, boolean clean) {
        Map<String, DataProvider> fields =
            (schema.getProperties() != null ? schema.getProperties() : Collections.<String, Property>emptyMap()).entrySet().stream()
                .map(e -> entry(e.getKey(), collect(swagger, e.getValue(), clean)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ObjectDataProvider(fields);
    }

    protected DataProvider collectMapProperty(Swagger swagger, MapProperty schema, boolean clean) {
        DataProvider valueProvider = collect(swagger, schema.getAdditionalProperties(), clean);
        return new MapDataProvider(valueProvider);
    }

    protected DataProvider collectArrayProperty(Swagger swagger, ArrayProperty schema, boolean clean) {
        DataProvider itemProvider = collect(swagger, schema.getItems(), clean);
        return new ListDataProvider(itemProvider);
    }

    protected DataProvider collectNumericProperty(Swagger swagger, AbstractNumericProperty schema, boolean clean) {
        long min = schema.getMinimum() != null ? schema.getMinimum().longValue() : Long.MIN_VALUE;
        long max = schema.getMaximum() != null ? schema.getMaximum().longValue() : Long.MAX_VALUE;
        boolean noBetween = (min == Long.MIN_VALUE && max == Long.MAX_VALUE);

        if (schema instanceof BaseIntegerProperty)
            return gen(() -> noBetween ? faker.number().randomNumber() : faker.number().numberBetween(min, max));
        else if (schema instanceof DecimalProperty)
            return gen(() -> faker.number().randomDouble(10, min, max));

        throw new IllegalArgumentException("Unsupported property type: " + schema.getClass());
    }

    protected DataProvider collectByteProperty(Swagger swagger, Property schema, boolean clean) {
        if (schema instanceof ByteArrayProperty)
            return new ConstDataProvider("[ByteArray]");
        else if (schema instanceof BinaryProperty)
            return new ConstDataProvider("[Binary]");

        throw new IllegalArgumentException("Unsupported property type: " + schema.getClass());
    }

    protected DataProvider collectDateProperty(Swagger swagger, Property schema, boolean clean) {
        if (schema instanceof DateTimeProperty)
            return gen(() -> {
                Date dateTime = faker.date().future((int) faker.random().nextDouble(), TimeUnit.DAYS);
                return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.of("UTC"));
            });
        else if (schema instanceof DateProperty)
            return gen(() -> {
                Date dateTime = faker.date().future((int) faker.random().nextDouble(), TimeUnit.DAYS);
                return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.of("UTC")).toLocalDate();
            });

        throw new IllegalArgumentException("Unsupported property type: " + schema.getClass());
    }

    protected DataProvider collectEmailProperty(Swagger swagger, EmailProperty schema, boolean clean) {
        return gen(() -> faker.internet().emailAddress());
    }

    protected DataProvider collectFileProperty(Swagger swagger, FileProperty schema, boolean clean) {
        return gen(() -> faker.file().fileName());
    }

    protected DataProvider collectUUIDProperty(Swagger swagger, UUIDProperty schema, boolean clean) {
        return gen(() -> UUID.randomUUID());
    }

    protected DataProvider collectBooleanProperty(Swagger swagger, BooleanProperty schema, boolean clean) {
        return gen(() -> faker.random().nextBoolean());
    }

    protected DataProvider collectStringProperty(Swagger swagger, StringProperty schema, boolean clean) {
        return gen(() -> {
            StringProperty.Format uriFormat = StringProperty.Format.fromName(schema.getFormat());
            if (uriFormat == StringProperty.Format.URI || uriFormat == StringProperty.Format.URL)
                return faker.internet().url();
            else if (!isEmpty(schema.getPattern())) {
                Generex generex = new Generex(schema.getPattern());
                return generex.random();
            } else
                return faker.lorem().word();
        });
    }

    protected DataProvider collectPasswordProperty(Swagger swagger, PasswordProperty schema, boolean clean) {
        return new ConstDataProvider("*********");
    }
}
