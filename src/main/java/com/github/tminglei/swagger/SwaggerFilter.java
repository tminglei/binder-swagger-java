package com.github.tminglei.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tminglei.bind.Simple;
import com.github.tminglei.swagger.fake.ConstDataProvider;
import com.github.tminglei.swagger.fake.DataProvider;
import com.github.tminglei.swagger.fake.DataProviders;
import com.github.tminglei.swagger.route.Route;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
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

import static com.github.tminglei.swagger.SimpleUtils.*;

/**
 * Filter used to init/scan swagger registering info and serv swagger json
 */
public class SwaggerFilter implements Filter {
    private boolean enabled = true;
    private boolean fakeEnabled = true;
    private String swaggerUri = "/swagger.json";

    private static final Logger logger = LoggerFactory.getLogger(SwaggerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        enabled = Optional.ofNullable(filterConfig.getInitParameter("enabled"))
            .map(Boolean::parseBoolean).orElse(enabled);
        fakeEnabled = Optional.ofNullable(filterConfig.getInitParameter("fake-enabled"))
            .map(Boolean::parseBoolean).orElse(fakeEnabled);

        if (enabled) {
            swaggerUri = Optional.ofNullable(filterConfig.getInitParameter("swagger-uri"))
                .orElse(swaggerUri);

            SwaggerContext swaggerContext = SwaggerContext.getInstance();

            // step 1. setup custom components

            // set user custom mapping converter
            String mappingConverter = filterConfig.getInitParameter("mapping-converter");
            logger.info("swagger config - mapping-converter: {}", mappingConverter);
            if (!isEmpty(mappingConverter)) {
                swaggerContext.setMappingConverter(newInstance(mappingConverter));
            }

            // set user custom url router
            String router = filterConfig.getInitParameter("url-router");
            logger.info("swagger config - url-router: {}", router);
            if (!isEmpty(router)) {
                swaggerContext.setRouter(newInstance(router));
            }

            // set user custom data writer
            String dataWriter = filterConfig.getInitParameter("data-writer");
            logger.info("swagger config - data-writer: {}", dataWriter);
            if (!isEmpty(dataWriter)) {
                swaggerContext.setDataWriter(newInstance(dataWriter));
            }

            // step 2: scan and register swagger api info
            String scanPkgAndClasses = filterConfig.getInitParameter("scan-packages-and-classes");
            if (isEmpty(scanPkgAndClasses)) throw new IllegalArgumentException("`scan-packages-and-classes` NOT configured!!!");
            logger.info("swagger config - scan-packages-and-classes: {}", scanPkgAndClasses);
            String[] scanPkgClasses = scanPkgAndClasses.split(",|;");
            for (String pkgClazz : scanPkgClasses) {
                if (!isEmpty(pkgClazz)) {
                    scan(this.getClass(), pkgClazz.trim()).forEach(clz -> {
                        logger.info("found class: {}", clz);
                        try {
                            Class.forName(clz);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

            // step 3: scan and collect (fake) data providers
            if (fakeEnabled) {
                Map<String, Path> paths = swaggerContext.getSwagger().getPaths();
                if (paths == null) paths = Collections.emptyMap();
                for (String path : paths.keySet()) {
                    Map<HttpMethod, Operation> operations = paths.get(path).getOperationMap();
                    for (HttpMethod method : operations.keySet()) {
                        Map<String, Response> responses = operations.get(method).getResponses();
                        Response response = responses != null ? responses.get("200") : null;
                        DataProvider dataProvider = response != null && response.getSchema() != null
                            ? DataProviders.getInstance().collect(swaggerContext.getSwagger(), response.getSchema(), true)
                            : new ConstDataProvider(null);
                        dataProvider.setRequired(true);
                        boolean implemented = swaggerContext.isImplemented(method, path, true);
                        String origPath = swaggerContext.getOrigPath(method, path, true);
                        Route route = swaggerContext.getRouteFactory().create(method, origPath, implemented, dataProvider);
                        swaggerContext.getRouter().add(route);
                    }
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (enabled) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            SwaggerContext swaggerContext = SwaggerContext.getInstance();

            if (req.getPathInfo().equals(swaggerUri) && "GET".equalsIgnoreCase(req.getMethod())) {
                // enable cross-origin resource sharing
                resp.addHeader("Access-Control-Allow-Origin", "*");
                resp.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, DELETE, HEAD, OPTIONS");
                resp.addHeader("Access-Control-Max-Age", "43200"); // half a day

                String json = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writer().writeValueAsString(swaggerContext.getSwagger());
                resp.getWriter().write(json);
                resp.flushBuffer();
                return;
            }

            if (fakeEnabled) {
                HttpMethod method = HttpMethod.valueOf(req.getMethod().toUpperCase());
                Route route = swaggerContext.getRouter().route(method, req.getPathInfo());
                if (route != null && ! route.isImplemented()) {
                    Map<String, String> params = Simple.data(req.getParameterMap());
                    params.putAll(route.getPathParams(req.getPathInfo()));
                    route.getDataProvider().setRequestParams(params);
                    Object fakeData = route.getDataProvider().get();
                    if (!isEmpty(fakeData)) {
                        String format = req.getHeader("accept");
                        swaggerContext.getDataWriter().write(resp.getWriter(), format, fakeData);
                        resp.flushBuffer();
                    }
                    return;
                }
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
    List<String> scan(Class<?> loaderClazz, String pkgOrClassName) {
        pkgOrClassName = pkgOrClassName.replace(".", "/").replaceAll("^/", "").replaceAll("/$", "") + "/";

        try {
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
                    for (String name : names) {
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

        } catch (URISyntaxException e) {
            throw new RuntimeException("INVALID package or class name: '" + pkgOrClassName + "'!!!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
