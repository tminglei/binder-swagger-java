/**
 *  Copyright 2015 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.example.data.H2DB;
import io.swagger.models.auth.In;

import static com.github.tminglei.swagger.SwaggerContext.*;

public class Bootstrap extends HttpServlet {
  private static final long serialVersionUID = 1L;

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

  @Override
  public void init(ServletConfig config) throws ServletException {
      H2DB.setupDatabase();
  }
}
