package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import io.swagger.models.*;
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

    public static Path path(String path) {
        checkNotEmpty(path, "'path' CAN'T be null or empty!!!");

        synchronized (swagger) {
            if (swagger.getPath(path) == null) {
                logger.info(">>> adding path - '" + path + "'");
                swagger.path(path, new Path());
            }
            return swagger.getPath(path);
        }
    }
    public static ExOperation operation(String httpMethod, String path) {
        checkNotEmpty(path, "'path' CAN'T be null or empty!!!");
        checkNotEmpty(httpMethod, "'method' CAN'T be null or empty!!!");

        HttpMethod method = HttpMethod.valueOf(httpMethod.toUpperCase());
        synchronized (swagger) {
            if (path(path).getOperationMap().get(method) == null) {
                logger.info(">>> adding operation - " + method + " '" + path + "'");
                path(path).set(httpMethod.toLowerCase(), new ExOperation());
            }
            return (ExOperation) path(path).getOperationMap().get(method);
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
    public static Property prop(Framework.Mapping<?> mapping) {
        return mHelper.mToProperty(mapping);
    }

    ///////////////////////////////////////////////////////////////////////

    static MSwaggerHelper mHelper = new MSwaggerHelper(); // extend and replace when necessary
    public static void setMHelper(MSwaggerHelper mHelper) {
        SwaggerContext.mHelper = mHelper;
    }
}
