package com.github.tminglei.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

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
        String mySwaggerHelper = filterConfig.getInitParameter("my-extended-swagger-helper");
        logger.info("swagger config - my-extended-swagger-helper: " + mySwaggerHelper);
        if (!isEmpty(mySwaggerHelper)) {
            try {
                Class<MSwaggerHelper> clazz = (Class<MSwaggerHelper>) Class.forName(mySwaggerHelper);
                SwaggerContext.setMHelper(clazz.newInstance());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("INVALID swagger helper class: '" + mySwaggerHelper + "'!!!");
            } catch (InstantiationException e) {
                throw new RuntimeException("FAILED to instantiate the swagger helper class: '" + mySwaggerHelper + "'!!!");
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

            // enable cross-origin resource sharing
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, DELETE, HEAD, OPTIONS");
            resp.addHeader("Access-Control-Max-Age", "43200"); // half a day

            if (swaggerPath.equals(req.getPathInfo()) && "GET".equalsIgnoreCase(req.getMethod())) {
                String json = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .writer().writeValueAsString(check(SwaggerContext.swagger()));
                response.getWriter().write(json);
                response.flushBuffer();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
