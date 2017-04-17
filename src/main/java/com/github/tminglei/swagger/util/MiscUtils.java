package com.github.tminglei.swagger.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Some util methods
 */
public class MiscUtils {

    public static boolean isEmpty(Object value) {
        return (value instanceof String && ((String) value).trim().length() == 0)
            || value == null;
    }

    public static <T> T notEmpty(T value, String message) {
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

    public static <T> T newInstance(String clazzName) {
        try {
            Class<T> clazz = (Class<T>) Class.forName(clazzName);
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("INVALID class: '" + clazzName + "'!!!");
        } catch (InstantiationException e) {
            throw new RuntimeException("FAILED to instantiate class: '" + clazzName + "'!!!");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
