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

import com.example.data.PetData;
import com.example.exception.BadRequestException;
import com.example.exception.NotFoundException;
import com.github.tminglei.bind.BindObject;
import com.github.tminglei.bind.FormBinder;
import com.github.tminglei.bind.Messages;
import com.github.tminglei.swagger.SharingHolder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;

import static com.github.tminglei.swagger.SwaggerContext.*;
import static com.github.tminglei.swagger.Attachment.*;
import static com.github.tminglei.swagger.SwaggerUtils.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Constraints.*;
import static com.github.tminglei.bind.Processors.*;

@Path("/pet")
@Produces({"application/json", "application/xml"})
public class PetResource {
    static PetData petData = new PetData();
    private ResourceBundle bundle = ResourceBundle.getBundle("bind-messages");
    private Messages messages = (key) -> bundle.getString(key);

    static Mapping<?> petStatus = $(text(oneOf(Arrays.asList("available", "pending", "sold"))))
            .desc("pet status in the store").$$;
    static Mapping<?> pet = $(mapping(
            field("id", $(longv()).desc("pet id").$$),
            field("name", $(text(required())).desc("pet name").$$),
            field("category", attach(required()).to($(mapping(
                    field("id", longv(required())),
                    field("name", text(required()))
            )).desc("category belonged to").$$)),
            field("photoUrls", $(list(text().constraint(pattern("http://.*")))).desc("pet's photo urls").$$),
            field("tags", $(list(text())).desc("tags for the pet").$$),
            field("status", petStatus)
        )).refName("Pet").desc("pet info").$$;

    static SharingHolder sharing = share().commonPath("/pet").tag("pet");

    ///
    static {
        operation("get", "/{petId}", sharing)
                .summary("get pet by id")
                .parameter(param(longv()).in("path").name("petId").example(1l))
                .response(200, response(pet))
                .response(404, response().description("pet not found"))
        ;
    }
    @GET
    @Path("/{petId}")
    public Response getPetById(@PathParam("petId") String petId)
            throws NotFoundException, SQLException {
        Map<String, Object> pet = petData.getPetById(Long.parseLong(petId));
        if (null != pet) {
            return Response.ok().entity(pet).build();
        } else {
            throw new NotFoundException(404, "Pet not found");
        }
    }

    static {
        operation("post", "/", sharing)
                .summary("create a pet")
                .parameter(param(pet).in("body"))
                .response(200, response().description("success"))
                .response(400, response())
        ;
    }
    @POST
    public Response addPet(String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(pet),
                newmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            petData.addPet(bindObj);
            return Response.ok().entity("SUCCESS").build();
        }
    }

    static {
        operation("put", "/", sharing)
                .summary("update pet")
                .parameter(param(pet).in("body"))
                .response(200, response().description("success"))
                .response(400, response())
        ;
    }
    @PUT
    public Response updatePet(String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(pet),
                newmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            petData.addPet(bindObj);
            return Response.ok().entity("SUCCESS").build();
        }
    }

    static {
        operation("get", "/findByStatus", sharing)
                .summary("find pets by status")
                .parameter(param(list(petStatus, required())).in("query").name("status"))
                .response(200, response(list(pet)).description("pet list"))
        ;
    }
    @GET
    @Path("/findByStatus")
    public Response findPetsByStatus(@QueryParam("status") String status) throws SQLException {
        return Response.ok(petData.findPetByStatus(status)).build();
    }

    static {
        operation("get", "/findByTags", sharing)
                .summary("find pets by tags")
                .parameter(param(list(text())).in("query").name("tags").desc("pet tags"))
                .response(200, response(list(pet)).description("pet list"))
                .deprecated(true);
    }
    @GET
    @Path("/findByTags")
    @Deprecated
    public Response findPetsByTags(@QueryParam("tags") String tags) throws SQLException {
        return Response.ok(petData.findPetByTags(tags)).build();
    }
}
