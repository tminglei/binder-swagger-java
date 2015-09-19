package com.github.tminglei.swagger;

import io.swagger.models.Swagger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Some util methods
 */
public class SwaggerUtils {

    // make Map.Entry
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry(key, value);
    }

    // make a map
    public static <K, V> Map<K, V> newmap(Map.Entry<K, V>... entries) {
        Map<K, V> result = new HashMap<>();
        for(Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // (recursively) find class names under specified base package
    // inspired by: http://www.uofr.net/~greg/java/get-resource-listing.html
    public static List<String> scan(Class<?> loaderClazz, String pkgOrClassName) throws URISyntaxException, IOException {
        pkgOrClassName = pkgOrClassName.replace(".", "/").replaceAll("^/", "").replaceAll("/$", "") + "/";

        Set<String> result = new HashSet<>();
        boolean found = false;

        //1. first, let's try to treat it as package
        Enumeration<URL> dirURLs = loaderClazz.getClassLoader().getResources(pkgOrClassName);
        while (dirURLs.hasMoreElements()) {
            found = true;
            URL dirURL = dirURLs.nextElement();

            if (dirURL.getProtocol().equals("file")) {
                /* A file path: easy enough */
                String[] names = new File(dirURL.toURI()).list();
                for(String name : names) {
                    if (name.endsWith(".class") && !name.contains("$")) { //filter out inner classes
                        result.add(pkgOrClassName + name);
                    } else {
                        File f = new File(dirURL.getPath() + name);
                        if (f.isDirectory()) { //recursively finding
                            result.addAll(scan(loaderClazz, pkgOrClassName + name));
                        }
                    }
                }
            }

            if (dirURL.getProtocol().equals("jar")) {
                /* A JAR path */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while(entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(pkgOrClassName) && name.endsWith(".class") && !name.contains("$")) {
                        result.add(name);
                    }
                }
            }
        }

        //2. if not found, let's try to treat it as class
        if (!found) {
            pkgOrClassName = pkgOrClassName.replaceAll("/$", "").replaceAll("/class", ".class");
            if (!pkgOrClassName.endsWith(".class")) pkgOrClassName = pkgOrClassName + ".class";
            URL clsURL = loaderClazz.getClassLoader().getResource(pkgOrClassName);
            if (clsURL != null) result.add(pkgOrClassName);
        }

        return result.stream().map(
                n -> n.replaceAll("\\.class$", "").replace(File.separator, ".")
            ).collect(Collectors.toList());
    }

    public static Swagger check(Swagger swagger) {
        //todo
        return swagger;
    }

    ///
    static boolean isEmpty(Object value) {
        return (value instanceof String && ((String) value).trim().length() == 0)
                || value == null;
    }

    static void checkNotEmpty(Object value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        if (value instanceof String && "".equals(((String) value).trim())) {
            throw new IllegalArgumentException(message);
        }
    }

    static String joinPaths(String... paths) {
        return Arrays.asList(paths).stream()
                .filter(s -> !isEmpty(s))
                .collect(Collectors.joining("/"))
                .replaceAll("/+", "/");
    }

}
