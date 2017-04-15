package com.github.tminglei.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tminglei.swagger.bind.MappingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.github.tminglei.swagger.SwaggerUtils.*;

/**
 * Filter used to init/scan swagger registering info and serv swagger json
 */
public class SwaggerFilter implements Filter {
    private boolean enabled = true;
    private String swaggerPath = "/swagger.json";

    private static final Logger logger = LoggerFactory.getLogger(SwaggerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        enabled = Optional.ofNullable(filterConfig.getInitParameter("enabled"))
                .map(Boolean::parseBoolean).orElse(true);
        swaggerPath = Optional.ofNullable(filterConfig.getInitParameter("swagger-path"))
                .orElse("/swagger.json");

        // set user extended swagger helper
        String mappingConverter = filterConfig.getInitParameter("mapping-converter");
        logger.info("swagger config - mapping-converter: " + mappingConverter);
        if (!isEmpty(mappingConverter)) {
            try {
                Class<MappingConverter> clazz = (Class<MappingConverter>) Class.forName(mappingConverter);
                SwaggerContext.INSTANCE.setMConverter(clazz.newInstance());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("INVALID mapping converter class: '" + mappingConverter + "'!!!");
            } catch (InstantiationException e) {
                throw new RuntimeException("FAILED to instantiate the mapping converter class: '" + mappingConverter + "'!!!");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // scan and register swagger api info
        String[] scanPkgClasses = Optional.ofNullable(filterConfig.getInitParameter("scan-packages-and-classes"))
                .map(s -> s.split(",|;")).orElse(new String[0]);
        logger.info("swagger config - scan-packages-and-classes: " + filterConfig.getInitParameter("scan-packages-and-classes"));
        for(String pkgClazz : scanPkgClasses) {
            if (!isEmpty(pkgClazz)) {
                try {
                    scan(this.getClass(), pkgClazz.trim()).forEach(clz -> {
                        logger.info("found class: " + clz);
                        try {
                            Class.forName(clz);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (URISyntaxException e) {
                    throw new RuntimeException("INVALID package or class name: '" + pkgClazz + "'!!!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (enabled) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;

            if (swaggerPath.equals(req.getPathInfo()) && "GET".equalsIgnoreCase(req.getMethod())) {
                // enable cross-origin resource sharing
                resp.addHeader("Access-Control-Allow-Origin", "*");
                resp.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, DELETE, HEAD, OPTIONS");
                resp.addHeader("Access-Control-Max-Age", "43200"); // half a day

                String json = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .writer().writeValueAsString(SwaggerContext.swagger());
                resp.getWriter().write(json);
                resp.flushBuffer();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    ///---
    // (recursively) find class names under specified base package
    // inspired by: http://www.uofr.net/~greg/java/get-resource-listing.html
    List<String> scan(Class<?> loaderClazz, String pkgOrClassName) throws URISyntaxException, IOException {
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
            n -> n.replaceAll("\\.class$", "").replace(File.separator, ".").replace("/", ".")
        ).collect(Collectors.toList());
    }

}
