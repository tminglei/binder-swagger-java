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
import com.example.exception.BadRequestException;
import com.example.exception.NotFoundException;
import com.example.exception.ApiException;
import com.github.tminglei.bind.BindObject;
import com.github.tminglei.bind.FormBinder;
import com.github.tminglei.bind.Messages;
import com.github.tminglei.swagger.SharingHolder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static io.swagger.models.HttpMethod.*;
import static com.github.tminglei.swagger.SwaggerContext.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Constraints.*;
import static com.github.tminglei.bind.Processors.*;

@Path("/user")
@Produces({"application/json", "application/xml"})
public class UserResource {
    static UserData userData = new UserData();
    private ResourceBundle bundle = ResourceBundle.getBundle("bind-messages");
    private Messages messages = (key) -> bundle.getString(key);

    static Mapping<?> user = $(mapping(
            field("id", $(longv()).desc("user id").$$),
            field("username", $(text(required())).desc("user name").$$),
            field("firstName", $(text()).desc("user's first name").$$),
            field("lastName", $(text()).desc("user's last name").$$),
            field("email", $(text(email())).desc("user's email").$$),
            field("password", $(text()).format("password").desc("password").$$),
            field("phone", $(text(pattern("[\\d]{3}-[\\d]{4}-[\\d]{2}"))).desc("phone number").$$),
            field("status", $(intv(oneOf(Arrays.asList("1", "2", "3")))).desc("user's status").$$)
        )).refName("User").desc("user info").$$;

    static SharingHolder sharing = sharing().pathPrefix("/user").tag("user");

    ///
    static {
        sharing.operation(POST, "/")
                .summary("create a user")
                .parameter(param(user).in("body"))
                .response(200, response())
        ;
    }
    @POST
    public Response createUser(String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(user),
                hashmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            userData.addUser(bindObj);
            return Response.ok().entity("").build();
        }
    }

    static {
        sharing.operation(POST, "/createWithArray")
                .summary("create multiple users")
                .parameter(param(list(user)).in("body"))
                .response(200, response())
        ;
    }
    @POST
    @Path("/createWithArray")
    public Response createUsersWithArrayInput(String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(list(user)),
                hashmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            for(BindObject u : (List<BindObject>) bindObj.get()) {
                userData.addUser(u);
            }
        }
        return Response.ok().entity("").build();
    }

    static {
        sharing.operation(POST, "/createWithList")
                .summary("create multiple users")
                .parameter(param(list(user)).in("body"))
                .response(200, response())
        ;
    }
    @POST
    @Path("/createWithList")
    public Response createUsersWithListInput(String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(list(user)),
                hashmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            for(BindObject u : (List<BindObject>) bindObj.get()) {
                userData.addUser(u);
            }
        }
        return Response.ok().entity("").build();
    }

    static {
        sharing.operation(PUT, "/{username}")
                .summary("update user")
                .parameter(param(text()).in("path").name("username").desc("user name"))
                .parameter(param(user).in("body"))
                .response(200, response())
        ;
    }
    @PUT
    @Path("/{username}")
    public Response updateUser(@PathParam("username") String username, String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(user),
                hashmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            userData.removeUser(username);
            userData.addUser(bindObj);
            return Response.ok().entity("").build();
        }
    }

    static {
        sharing.operation(DELETE, "/{username}")
                .summary("delete user")
                .parameter(param(text()).in("path").name("username").desc("user name"))
                .response(200, response())
        ;
    }
    @DELETE
    @Path("/{username}")
    public Response deleteUser(String username) throws SQLException {
        userData.removeUser(username);
        return Response.ok().entity("").build();
    }

    static {
        sharing.operation(GET, "/{username}")
                .summary("get specified user")
                .parameter(param(text()).in("path").name("username").desc("user name"))
                .response(200, response(user))
                .response(404, response().description("user not found"))
        ;
    }
    @GET
    @Path("/{username}")
    public Response getUserByName(@PathParam("username") String username)
            throws ApiException, SQLException {
        Map<String, Object> user = userData.findUserByName(username);
        if (null != user) {
            return Response.ok().entity(user).build();
        } else {
            throw new NotFoundException(404, "User not found");
        }
    }

    static {
        sharing.operation(POST, "/login")
                .summary("login user")
                .parameter(param(text(required())).in("form").name("username"))
                .parameter(param(text(required())).in("form").name("password"))
                .response(200, response())
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
        sharing.operation(GET, "/logout")
                .summary("logout user")
                .response(200, response())
        ;
    }
    @GET
    @Path("/logout")
    public Response logoutUser() {
        return Response.ok().entity("").build();
    }
}
