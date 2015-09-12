# binder-swagger-java

[![Build Status](https://travis-ci.org/tminglei/binder-swagger-java.svg?branch=master)](https://travis-ci.org/tminglei/binder-swagger-java)

Given the `swagger.json`, [swagger ui](http://petstore.swagger.io/) can dynamically build the web client, to enable online browsing your APIs, sending request and receiving response from your rest services.

`binder-swagger-java` was designed to help construct the swagger object, corresponding to `swagger.json`, and let it accessible from swagger ui or other http visitors.

_p.s. and, of course, you can use [form-binder-java](https://github.com/tminglei/form-binder-java) mappings, when constructing the swagger object. ;-)_

##How to use
0) add the dependency to your project:
```
<dependency>
  <groupId>com.github.tminglei</groupId>
  <artifactId>binder-swagger-java</artifactId>
  <version>0.3.0</version>
</dependency>
```
1) define and register your api operations
```java
// in `PetResource.java`
static {
    operation("get", "/pet/{petId}")
        .summary("get pet by id")
        .tag("pet")
        .parameter(param(vLong()).in("path").name("petId").example(1l))
        .response(200, response(pet))
        .response(404, new io.swagger.models.Response()
                .description("pet not found")
        )
    ;
}
@GET
@Path("/{petId}")
public Response getPetById(@PathParam("petId") String petId)
...
```
2) supplement your other swagger info
```java
// in `Bootstrap.java`
static {  // for swagger
	swagger().info(new Info()
	      .title("Swagger Sample App")
	      .description("This is a sample server Petstore server.  You can find out more about Swagger " +
		      "at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, " +
		      "you can use the api key `special-key` to test the authorization filters.")
	      .termsOfService("http://swagger.io/terms/")
	      .contact(new Contact().email("apiteam@swagger.io"))
	      .license(new License().name("Apache 2.0")
		              .url("http://www.apache.org/licenses/LICENSE-2.0.html")
	      )
	).host("localhost:8002")
	.basePath("/api")
	.consumes("application/json")
	.produces("application/json")
	.securityDefinition("api_key", new ApiKeyAuthDefinition("api_key", In.HEADER))
	.securityDefinition("petstore_auth",
	      new OAuth2Definition()
		      .implicit("http://petstore.swagger.io/api/oauth/dialog")
		      .scope("read:pets", "read your pets")
		      .scope("write:pets", "modify pets in your account")
	).tag(new Tag().name("pet")
		      .description("Everything about your Pets")
		      .externalDocs(new ExternalDocs("Find out more", "http://swagger.io"))
	).tag(new Tag().name("store")
	      .description("Access to Petstore orders")
	).tag(new Tag().name("user")
	      .description("Operations about user")
	      .externalDocs(new ExternalDocs("Find out more about our store", "http://swagger.io"))
	);
}
```
3) configure the filter, which will serv the `swagger.json`
```
// in `web.xml`
<filter>
    <filter-name>SwaggerFilter</filter-name>
    <filter-class>com.github.tminglei.swagger.SwaggerFilter</filter-class>
    <!-- configure your extended swagger helper
    <init-param>
        <param-name>my-extended-swagger-helper</param-name>
        <param-value>com.mycompany.pkg.MySwaggerHelper</param-value>
    </init-param>
    -->
    <init-param>
        <param-name>scan-packages-and-classes</param-name>
        <param-value>com.example.resource; com.example.Bootstrap</param-value>
    </init-param>
    <!-- specify the path to fetch your swagger json, default '/swagger.json'
    <init-param>
        <param-name>swagger-path</param-name>
        <param-value>/swagger.json</param-value>
    </init-param>
    -->
    <!-- enable/disable swagger, default value: true
    <init-param>
        <param-name>enabled</param-name>
        <param-value>false</param-value>
    </init-param>
    -->
</filter>
<filter-mapping>
    <filter-name>SwaggerFilter</filter-name>
    <url-pattern>/api/*</url-pattern><!-- keep consistent with the jersey servlet mapping -->
</filter-mapping>
...
```
NOTES: if you extend the [MSwaggerHelper](https://github.com/tminglei/binder-swagger-java/blob/master/src/main/java/com/github/tminglei/swagger/MSwaggerHelper.java) and configure it here, pls ensure **SwaggerFilter** to be loaded eariler, that is, ensure the swagger helper to be set to [SwaggerContext](https://github.com/tminglei/binder-swagger-java/blob/master/src/main/java/com/github/tminglei/swagger/SwaggerContext.java) before other class scanner started working.


That's all. Enjoy it!


> Example is a good start, you can check the example project [here](https://github.com/tminglei/binder-swagger-java/tree/master/example/java-jaxrs).


##License
The BSD License, Minglei Tu &lt;tmlneu@gmail.com&gt;
