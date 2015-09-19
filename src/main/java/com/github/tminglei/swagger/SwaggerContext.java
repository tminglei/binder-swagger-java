package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import io.swagger.models.*;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tminglei.swagger.SwaggerUtils.*;

/**
 * Context class to hold swagger instance and related helper methods
 */
public class SwaggerContext {
    private static Swagger swagger = new Swagger();

    private static final Logger logger = LoggerFactory.getLogger(SwaggerContext.class);

    public static Swagger swagger() {
        return swagger;
    }

    public static ExOperation addOperation(String method, String path) {
        checkNotEmpty(path, "'path' CAN'T be null or empty!!!");
        checkNotEmpty(method, "'method' CAN'T be null or empty!!!");

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

    public static MParamBuilder param(Framework.Mapping<?> mapping) {
        return new MParamBuilder(mapping);
    }
    public static Model model(Framework.Mapping<?> mapping) {
        return mHelper.mToModel(mapping);
    }
    public static Response response(Framework.Mapping<?> mapping) {
        return mHelper.mToResponse(mapping);
    }
    public static Response response() {
        return new Response();
    }
    public static Property prop(Framework.Mapping<?> mapping) {
        return mHelper.mToProperty(mapping);
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

    ///////////////////////////////////////////////////////////////////////

    static MSwaggerHelper mHelper = new MSwaggerHelper(); // extend and replace when necessary
    public static void setMHelper(MSwaggerHelper mHelper) {
        SwaggerContext.mHelper = mHelper;
    }
}
