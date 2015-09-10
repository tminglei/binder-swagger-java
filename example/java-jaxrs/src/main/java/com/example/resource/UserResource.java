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

package com.example.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.example.data.UserData;
import com.example.exception.NotFoundException;
import com.example.exception.ApiException;
import com.example.model.User;

import java.util.Arrays;

import static com.github.tminglei.swagger.SwaggerContext.*;
import static com.github.tminglei.swagger.SwaggerExtensions.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Constraints.*;

@Path("/user")
@Produces({"application/json", "application/xml"})
public class UserResource {
    static UserData userData = new UserData();

    static Mapping<?> user = mapping(
            field("id", vLong().$ext(o -> ext(o).desc("user id"))),
            field("username", text(required()).$ext(o -> ext(o).desc("user name"))),
            field("firstName", text().$ext(o -> ext(o).desc("user's first name"))),
            field("lastName", text().$ext(o -> ext(o).desc("user's last name"))),
            field("email", text(email()).$ext(o -> ext(o).desc("user's email"))),
            field("password", text().$ext(o -> ext(o).format("password").desc("password"))),
            field("phone", text(pattern("[\\d]{3}-[\\d]{4}-[\\d]{2}")).$ext(o -> ext(o).desc("phone number"))),
            field("status", vInt(oneOf(Arrays.asList("1", "2", "3"))).$ext(o -> ext(o).desc("user's status")))
        ).$ext(o -> ext(o).desc("user info"));

    ///
    static {
        operation("post", "/user")
                .summary("create a user")
                .tag("user")
                .parameter(param(user).in("body"))
                .response(200, new io.swagger.models.Response())
        ;
    }
    @POST
    public Response createUser(User user) {
        userData.addUser(user);
        return Response.ok().entity("").build();
    }

    static {
        operation("post", "/user/createWithArray")
                .summary("create multiple users")
                .tag("user")
                .parameter(param(list(user)).in("body"))
                .response(200, new io.swagger.models.Response())
        ;
    }
    @POST
    @Path("/createWithArray")
    public Response createUsersWithArrayInput(User[] users) {
        for (User user : users) {
            userData.addUser(user);
        }
        return Response.ok().entity("").build();
    }

    static {
        operation("post", "/user/createWithList")
                .summary("create multiple users")
                .tag("user")
                .parameter(param(list(user)).in("body"))
                .response(200, new io.swagger.models.Response())
        ;
    }
    @POST
    @Path("/createWithList")
    public Response createUsersWithListInput(java.util.List<User> users) {
        for (User user : users) {
            userData.addUser(user);
        }
        return Response.ok().entity("").build();
    }

    static {
        operation("put", "/user/{username}")
                .summary("update user")
                .tag("user")
                .parameter(param(text()).in("path").name("username").desc("user name"))
                .parameter(param(user).in("body"))
                .response(200, new io.swagger.models.Response())
        ;
    }
    @PUT
    @Path("/{username}")
    public Response updateUser(@PathParam("username") String username, User user) {
        userData.addUser(user);
        return Response.ok().entity("").build();
    }

    static {
        operation("delete", "/user/{username}")
                .summary("delete user")
                .tag("user")
                .parameter(param(text()).in("path").name("username").desc("user name"))
                .response(200, new io.swagger.models.Response())
        ;
    }
    @DELETE
    @Path("/{username}")
    public Response deleteUser(String username) {
        userData.removeUser(username);
        return Response.ok().entity("").build();
    }

    static {
        operation("get", "/user/{username}")
                .summary("get specified user")
                .tag("user")
                .parameter(param(text()).in("path").name("username").desc("user name"))
                .response(200, response(user))
                .response(404, new io.swagger.models.Response()
                        .description("user not found")
                )
        ;
    }
    @GET
    @Path("/{username}")
    public Response getUserByName(@PathParam("username") String username)
            throws ApiException {
        User user = userData.findUserByName(username);
        if (null != user) {
            return Response.ok().entity(user).build();
        } else {
            throw new NotFoundException(404, "User not found");
        }
    }

    static {
        operation("post", "/user/login")
                .summary("login user")
                .tag("user")
                .parameter(param(text(required())).in("form").name("username"))
                .parameter(param(text(required())).in("form").name("password"))
                .response(200, new io.swagger.models.Response())
        ;
    }
    @POST
    @Path("/login")
    public Response loginUser(@FormParam("username") String username, @FormParam("password") String password) {
        return Response.ok()
                .entity("logged in user session:" + System.currentTimeMillis())
                .build();
    }

    static {
        operation("get", "/user/logout")
                .summary("logout user")
                .tag("user")
                .response(200, new io.swagger.models.Response())
        ;
    }
    @GET
    @Path("/logout")
    public Response logoutUser() {
        return Response.ok().entity("").build();
    }
}
