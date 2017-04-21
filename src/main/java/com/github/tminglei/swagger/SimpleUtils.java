package com.github.tminglei.swagger;

/**
 * Some util methods
 */
public class SimpleUtils {

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
