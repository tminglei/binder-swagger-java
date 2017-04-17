package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import com.github.tminglei.swagger.bind.Attachment;
import com.github.tminglei.swagger.bind.MParamBuilder;
import com.github.tminglei.swagger.bind.MappingConverterImpl;
import com.github.tminglei.swagger.bind.MappingConverter;
import com.github.tminglei.swagger.fake.*;
import com.github.tminglei.swagger.route.RouteFactory;
import com.github.tminglei.swagger.route.Router;
import com.github.tminglei.swagger.route.RouteFactoryImpl;
import com.github.tminglei.swagger.route.TreeRouterImpl;
import io.swagger.models.*;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.github.tminglei.swagger.SimpleUtils.*;

/**
 * Context class to hold swagger instance and related helper methods
 */
public class SwaggerContext {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerContext.class);

    private static SwaggerContext INSTANCE = new SwaggerContext(
        new Swagger(), new MappingConverterImpl(), new TreeRouterImpl(), new RouteFactoryImpl(), new DataWriterImpl());

    private Swagger swagger;
    private MappingConverter mConverter;
    private Router router;
    private RouteFactory routeFactory;
    private DataWriter dataWriter;

    private Map<Map.Entry<HttpMethod, String>, Boolean> implemented;

    public SwaggerContext(Swagger swagger, MappingConverter mConverter,
                          Router router, RouteFactory routeFactory, DataWriter dataWriter) {
        this.swagger = swagger;
        this.mConverter = mConverter;
        this.router = router;
        this.routeFactory = routeFactory;
        this.dataWriter = dataWriter;
        this.implemented = new HashMap<>();
    }

    public static SwaggerContext getInstance() {
        return SwaggerContext.INSTANCE;
    }
    public static void setInstance(SwaggerContext instance) {
        SwaggerContext.INSTANCE = instance;
    }

    ///---

    public Swagger getSwagger() {
        return this.swagger;
    }

    public MappingConverter getMappingConverter() {
        return this.mConverter;
    }
    public void setMappingConverter(MappingConverter mConverter) {
        this.mConverter = mConverter;
    }

    public Router getRouter() {
        return this.router;
    }
    public void setRouter(Router router) {
        this.router = router;
    }

    public RouteFactory getRouteFactory() {
        return this.routeFactory;
    }
    public void setRouteFactory(RouteFactory routeFactory) {
        this.routeFactory = routeFactory;
    }

    public DataWriter getDataWriter() {
        return this.dataWriter;
    }
    public void setDataWriter(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    ///---

    public SharingHolder mkSharing() {
        return new SharingHolder(this);
    }

    public ExOperation mkOperation(HttpMethod method, String path) {
        notEmpty(path, "'path' CAN'T be null or empty!!!");
        notEmpty(method, "'method' CAN'T be null or empty!!!");

        synchronized (swagger) {
            if (swagger.getPath(path) == null) {
                logger.info(">>> adding path - '" + path + "'");
                swagger.path(path, new Path());
            }

            Path pathObj = swagger.getPath(path);
            if (pathObj.getOperationMap().get(method) != null) {
                throw new IllegalArgumentException("DUPLICATED operation - " + method + " '" + path + "'");
            }

            logger.info(">>> adding operation - " + method + " '" + path + "'");
            pathObj.set(method.name().toLowerCase(), new ExOperation(this, method, path));
            implemented.put(entry(method, path), true);     // set implemented by default
            return (ExOperation) pathObj.getOperationMap().get(method);
        }
    }

    public MParamBuilder mkParamBuilder(Framework.Mapping<?> mapping) {
        return new MParamBuilder(this, mapping);
    }

    public Property mkProperty(Framework.Mapping<?> mapping) {
        scanAndRegisterNamedModels(mapping);
        return mConverter.mToProperty(mapping);
    }

    public Model mkModel(Framework.Mapping<?> mapping) {
        scanAndRegisterNamedModels(mapping);
        return mConverter.mToModel(mapping);
    }

    public Response mkResponse(Framework.Mapping<?> mapping) {
        scanAndRegisterNamedModels(mapping);
        return mConverter.mToResponse(mapping);
    }

    public void scanAndRegisterNamedModels(Framework.Mapping<?> mapping) {
        synchronized (swagger) {
            mConverter.scanModels(mapping).forEach(p -> {
                Model existed = swagger.getDefinitions() == null ? null : swagger.getDefinitions().get(p.getKey());
                if (existed == null) swagger.model(p.getKey(), p.getValue());
                else if (!existed.equals(p.getValue())) {
                    throw new IllegalArgumentException("CONFLICTED model definitions for '" + p.getKey() + "'!!!");
                }
            });
        }
    }

    public void markNotImplemented(HttpMethod method, String path) {
        Boolean prevValue = implemented.put(entry(method, path), true);
        if (prevValue == null) throw new IllegalStateException(method + " " + path + "NOT defined!!!");
    }

    public Boolean isImplemented(HttpMethod method, String path, boolean errIfAbsent) {
        Boolean value = implemented.get(entry(method, path));
        if (value == null && errIfAbsent) throw new IllegalStateException(method + " " + path + "NOT defined!!!");
        return value;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///                      static convenient methods
    ///////////////////////////////////////////////////////////////////////////

    public static Swagger swagger() {
        return INSTANCE.getSwagger();
    }

    public static SharingHolder sharing() {
        return INSTANCE.mkSharing();
    }

    public static ExOperation operation(HttpMethod method, String path) {
        return INSTANCE.mkOperation(method, path);
    }

    public static MParamBuilder param(Framework.Mapping<?> mapping) {
        return INSTANCE.mkParamBuilder(mapping);
    }
    public static Property prop(Framework.Mapping<?> mapping) {
        return INSTANCE.mkProperty(mapping);
    }
    public static Model model(Framework.Mapping<?> mapping) {
        return INSTANCE.mkModel(mapping);
    }
    public static Response response(Framework.Mapping<?> mapping) {
        return INSTANCE.mkResponse(mapping);
    }
    public static Response response() {
        return new Response();
    }

    public static Info info() {
        return new Info();
    }
    public static Tag tag(String name) {
        return new Tag().name(name);
    }
    public static Contact contact() {
        return new Contact();
    }
    public static License license() {
        return new License();
    }
    public static BasicAuthDefinition basicAuth() {
        return new BasicAuthDefinition();
    }
    public static ApiKeyAuthDefinition apiKeyAuth(String name, In in) {
        return new ApiKeyAuthDefinition(name, in);
    }
    public static OAuth2Definition oAuth2() {
        return new OAuth2Definition();
    }
    public static ExternalDocs externalDocs() {
        return new ExternalDocs();
    }

    ///---

    public static <T> Attachment.Builder<T> $(Framework.Mapping<T> mapping) {
        return new Attachment.Builder<>(mapping);
    }

    public static DataProvider gen(Supplier provider) {
        return new AbstractDataProvider() {
            @Override
            protected Object create() {
                return provider.get();
            }
        };
    }

    public static DataProvider gen(String paramKey) {
        return new ParamDataProvider(paramKey);
    }

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry(key, value);
    }

    public static <K, V> Map<K, V> hashmap(Map.Entry<K, V>... entries) {
        Map<K, V> result = new HashMap<>();
        for(Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
