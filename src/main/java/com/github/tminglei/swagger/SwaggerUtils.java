package com.github.tminglei.swagger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    // (recursive) get class names under specified package bases
    // inspired by: http://www.uofr.net/~greg/java/get-resource-listing.html
    public static List<String> getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException {
        path = path.replace(".", "/").replaceAll("^/", "").replaceAll("/$", "") + "/";

        Enumeration<URL> dirURLs = clazz.getClassLoader().getResources(path);
        Set<String> result = new HashSet<>();
        while (dirURLs.hasMoreElements()) {
            URL dirURL = dirURLs.nextElement();
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                /* A file path: easy enough */
                String[] names = new File(dirURL.toURI()).list();
                for(String name : names) {
                    if (name.endsWith(".class")
                            && !name.contains("$")) {   // filter out inner classes
                        result.add(path + name);
                    } else {
                        File f = new File(dirURL.getPath() + name);
                        if (f.isDirectory()) {
                            result.addAll(getResourceListing(clazz, path + name));
                        }
                    }
                }
            }

            if (dirURL != null && dirURL.getProtocol().equals("jar")) {
                /* A JAR path */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while(entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(path) && name.endsWith(".class") // filter according to the path
                            && !name.contains("$")) {   // filter out inner classes
                        result.add(name);
                    }
                }
            }
        }

        return new ArrayList<>(result);
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

}
