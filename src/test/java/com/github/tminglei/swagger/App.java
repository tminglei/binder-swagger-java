package com.github.tminglei.swagger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Hello world!
 *
 */
public class App {

    public static void main( String[] args ) throws Exception {
//        System.out.println("Hello World!");

//        Swagger swagger = new Swagger();
//        swagger.setSwagger("2.0");
//        swagger.info(new Info()
//                .title("api")
//                .description("demo api")
//                .termsOfService("http://swagger.io/terms/")
//                .contact(new Contact()
//                                .name("api support")
//                                .url("http://www.swagger.io/support")
//                                .email("support@swagger.io")
//                ).license(new License()
//                                .name("Apache 2.0")
//                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")
//                ).version("0.1"));
//        swagger.host("localhost")
//                .basePath("/")
//                .scheme(Scheme.HTTP)
//                .consumes("application/json")
//                .produces("application/json");
//        swagger.tag(new Tag().name("tag1"));
//        swagger.tag(new Tag().name("tag2"));
//
//        swagger.path("/pet/{petId}", new Path()
//                .get(new Operation()
//                        .tag("tag1")
//                        .summary("get pet")
//                        .description("test")
//                        .operationId("getPetById")
//                        .parameter(new PathParameter()
//                                .name("petId")
//                                .description("pet id")
//                                .type("integer").format("int64")
//                        ).response(200, new Response()
//                                        .description("pet")
//                                        .schema(new ObjectProperty()
//                                                .properties(newmap(
//                                                        entry("id", new IntegerProperty()),
//                                                        entry("name", new StringProperty())
//                                                ))
//                                        )
//                                )
//                ));
//
//        String json = new ObjectMapper().writer().writeValueAsString(swagger);
//        System.out.println(json);
//        Class<?> clazz = Class.forName("com.github.tminglei.swagger.App");
//        try {
////            clazz.newInstance();
////            clazz.newInstance();
////            new App();
////            new App();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Class<?> c = Class.forName("com.github.tminglei.swagger.Test");

        Class<?> clazz1 = new ArrayList<String>(){}.getClass();
        System.out.println(clazz1.getName());
        System.out.println(realTypeParam(mkList().getClass()));
        System.out.println(realTypeParam(clazz1));

        Enumeration<URL> resources = App.class.getClassLoader().getResources("com/github/tminglei/swagger");
        System.out.println("scanned resources - ");
        while (resources.hasMoreElements()) {
            System.out.println(">>> resource - " + resources.nextElement());
        }
        System.out.println("resource - " + App.class.getResource("/com/github/tminglei/swagger/ExOperation.class"));
        System.out.println("resource - " + App.class.getResource("/com/github/tminglei/swagger"));

        System.out.println("-------------------");
        List<String> classes = SwaggerUtils.getResourceListing(App.class, "com.github.tminglei");
        classes.forEach(cn -> System.out.println(cn));
    }

    static List<Optional<String>> mkList() {
        return new ArrayList<Optional<String>>(){};
    }

    static Type realTypeParam(Class<?> clazz) {
        Type type = ((ParameterizedType)clazz.getGenericSuperclass())
                .getActualTypeArguments()[0];
        return type;
    }

}
