package com.github.tminglei.swagger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Some util methods
 */
public class SwaggerUtils {

    public static boolean isEmpty(Object value) {
        return (value instanceof String && ((String) value).trim().length() == 0)
            || value == null;
    }

    public static Object notEmpty(Object value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        if (value instanceof String && "".equals(((String) value).trim())) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static String joinPaths(String... paths) {
        return Arrays.asList(paths).stream()
            .filter(s -> !isEmpty(s))
            .collect(Collectors.joining("/"))
            .replaceAll("/+", "/");
    }

}
