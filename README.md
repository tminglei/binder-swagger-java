# binder-swagger-java

[![Build Status](https://travis-ci.org/tminglei/binder-swagger-java.svg?branch=master)](https://travis-ci.org/tminglei/binder-swagger-java)

`binder-swagger-java` is a simple api management solution, which let api maintainence and dev based on api easily.


## Features
- lightweight, less than 3000 line codes (framework + built-in route/fake data generating)
- based on `form-binder-java`, allowing dynamic objects in operation's parameter/response definitions
- directly integrate with `swagger-models`, allowing to operate swagger object when necessary
- can generate mock response w/ fake data on demand for unimplemented api operations
- high customizable, you can replace almost all of the core components


## How it works
You define the api meta data in classes' static code blocks, then it was collected to a static global swagger object when class scan/loading, so when requested, the program can serve it right now.

![binder-swagger description](https://raw.githubusercontent.com/tminglei/binder-swagger-java/master/binder-swagger-java.png)

> _p.s. `binder-swagger-java` based on [`form-binder-java`](https://github.com/tminglei/form-binder-java) and [`swagger-models`](https://github.com/swagger-api/swagger-core), allow to define dynamic data structures and operate the swagger object directly when necessary, so it's more expressive in theory._


## How to use it
#### 0) add the dependency to your project:
```xml
<dependency>
    <groupId>com.github.tminglei</groupId>
    <artifactId>binder-swagger-java</artifactId>
    <version>0.8.0</version>
</dependency>
```
#### 1) define and register your api operations:
```java
// in `PetResource.java`
static Mapping<?> petStatus = $(text(oneOf(Arrays.asList("available", "pending", "sold"))))
    .desc("pet status in the store").example("available").$$;
static Mapping<?> pet = $(mapping(
    field("id", $(vLong()).desc("pet id").example(gen("petId").or(gen(() -> faker.number().randomNumber()))).$$),
    field("name", $(text(required())).desc("pet name").$$),
    field("category", attach(required()).to($(mapping(
          field("id", vLong(required())),
          field("name", text(required()))
    )).refName("category").desc("category belonged to").$$)),
    field("photoUrls", $(list(text())).desc("pet's photo urls").example(Arrays.asList("http://example.com/photo1")).$$),
    field("tags", $(list(text())).desc("tags for the pet").example(Arrays.asList("tag1", "tag2")).$$),
    field("status", petStatus)
)).refName("pet").desc("pet info").$$;

static SharingHolder sharing = sharing().pathPrefix("/pet").tag("pet");

static {
    sharing.operation(GET, "/{petId}")
        .summary("get pet by id")
        .parameter(param(longv()).in("path").name("petId").example(1l))
        .response(200, response(pet))
        .response(404, response().description("pet not found"))
        .notImplemented() // MARK IT `notImplemented`, THEN `binder-swagger-java` WILL GENERATE MOCK RESPONSE FOR YOU
    ;
}
@GET
@Path("/{petId}")
public Response getPetById(@PathParam("petId") String petId) throws NotFoundException, SQLException {
...
```
#### 2) supplement your other swagger info:
```java
// in `Bootstrap.java`
static {  // for swagger
    swagger().info(info()
        .title("Swagger Sample App")
        .description("This is a sample server Petstore server.  You can find out more about Swagger " +
              "at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, " +
              "you can use the api key `special-key` to test the authorization filters.")
        .termsOfService("http://swagger.io/terms/")
        .contact(contact().email("apiteam@swagger.io"))
        .license(license().name("Apache 2.0")
              .url("http://www.apache.org/licenses/LICENSE-2.0.html")
        )
    ).host("localhost:8002")
    .basePath("/api")
    .consumes("application/json")
    .produces("application/json")
    .securityDefinition("api_key", apiKeyAuth("api_key", In.HEADER))
    .securityDefinition("petstore_auth", oAuth2()
          .implicit("http://petstore.swagger.io/api/oauth/dialog")
          .scope("read:pets", "read your pets")
          .scope("write:pets", "modify pets in your account")
    ).tag(tag("pet").description("Everything about your Pets")
          .externalDocs(externalDocs().description("Find out more").url("http://swagger.io"))
    ).tag(tag("store").description("Access to Petstore orders")
    ).tag(tag("user").description("Operations about user")
          .externalDocs(externalDocs().description("Find out more about our store").url("http://swagger.io"))
    );
}
```
#### 3) configure the filter, which will serv the `swagger.json`:
```xml
// in `web.xml`
<filter>
    <filter-name>SwaggerFilter</filter-name>
    <filter-class>com.github.tminglei.swagger.SwaggerFilter</filter-class>

    <!-- enable/disable swagger, default value: true
    <init-param>
        <param-name>enabled</param-name>
        <param-value>false</param-value>
    </init-param>
    -->

    <init-param>
        <param-name>scan-packages-and-classes</param-name>
        <param-value>com.example.resource; com.example.Bootstrap</param-value>
    </init-param>

    <!-- specify the requestURI relative to base path, to fetch your swagger json, default '/swagger.json'
    <init-param>
        <param-name>swagger-uri</param-name>
        <param-value>/swagger.json</param-value>
    </init-param>
    -->

    <!-- configure your custom mapping converter
    <init-param>
        <param-name>mapping-converter</param-name>
        <param-value>com.company.pkg.MyMappingConverter</param-value>
    </init-param>
    -->

    <!-- enable/disable mock data generation, default value: true
    <init-param>
        <param-name>fake-enabled</param-name>
        <param-value>false</param-value>
    </init-param>
    -->

    <!-- configure your custom url router used by `binder-swagger-java`
    <init-param>
        <param-name>url-router</param-name>
        <param-value>com.company.pkg.MyRouter</param-value>
    </init-param>
    -->

    <!-- configure your custom data writer used by `binder-swagger-java`
    <init-param>
        <param-name>data-writer</param-name>
        <param-value>com.company.pkg.MyDataWriter</param-value>
    </init-param>
    -->
</filter>
<filter-mapping>
    <filter-name>SwaggerFilter</filter-name>
    <url-pattern>/api/*</url-pattern>
</filter-mapping>
...
```


##### That's all. Enjoy it!


> For more usage details, pls check the example project [here](https://github.com/tminglei/binder-swagger-java/tree/master/example/java-jaxrs).


## Q & A
**Q:** Why use static code blocks to associate/register operation meta info instead of annotations?  
**A:** Well, because we can't use annotations here. Annotation requires static defined data types, but we didn't define java beans in our project.  
_(p.s. because of this, we can't also use existing frameworks, like `springfox`.)_


## License
The BSD License, Minglei Tu &lt;tmlneu@gmail.com&gt;
