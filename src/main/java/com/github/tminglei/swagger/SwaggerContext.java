package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import com.github.tminglei.swagger.bind.Attachment;
import com.github.tminglei.swagger.bind.MParamBuilder;
import com.github.tminglei.swagger.bind.DefaultMappingConverter;
import com.github.tminglei.swagger.bind.MappingConverter;
import io.swagger.models.*;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tminglei.bind.OptionsOps._attachment;
import static com.github.tminglei.swagger.SwaggerUtils.*;

/**
 * Context class to hold swagger instance and related helper methods
 */
public class SwaggerContext {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerContext.class);

    public final static SwaggerContext INSTANCE = new SwaggerContext(new Swagger(), new DefaultMappingConverter());

    private Swagger swagger;
    private MappingConverter mConverter;

    public SwaggerContext(Swagger swagger, MappingConverter mConverter) {
        this.swagger = swagger;
        this.mConverter = mConverter;
    }

    ///---

    public Swagger getSwagger() {
        return this.swagger;
    }

    public MappingConverter getMConverter() {
        return this.mConverter;
    }
    public void setMConverter(MappingConverter mConverter) {
        this.mConverter = mConverter;
    }

    public SharingHolder mkSharing() {
        return new SharingHolder(this);
    }

    public ExOperation mkOperation(String method, String path) {
        notEmpty(path, "'path' CAN'T be null or empty!!!");
        notEmpty(method, "'method' CAN'T be null or empty!!!");

        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        synchronized (swagger) {
            if (swagger.getPath(path) == null) {
                logger.info(">>> adding path - '" + path + "'");
                swagger.path(path, new Path());
            }

            Path pathObj = swagger.getPath(path);
            if (pathObj.getOperationMap().get(httpMethod) != null) {
                throw new IllegalArgumentException("DUPLICATED operation - " + httpMethod + " '" + path + "'");
            }

            logger.info(">>> adding operation - " + httpMethod + " '" + path + "'");
            pathObj.set(method.toLowerCase(), new ExOperation());
            return (ExOperation) pathObj.getOperationMap().get(httpMethod);
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

    ///////////////////////////////////////////////////////////////////////////
    ///                      static convenient methods
    ///////////////////////////////////////////////////////////////////////////

    public static Swagger swagger() {
        return INSTANCE.getSwagger();
    }

    public static SharingHolder sharing() {
        return INSTANCE.mkSharing();
    }

    public static ExOperation operation(String method, String path) {
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
