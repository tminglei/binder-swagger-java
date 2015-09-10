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
import com.example.exception.NotFoundException;
import com.example.model.Pet;

import java.util.Arrays;

import static com.github.tminglei.swagger.SwaggerContext.*;
import static com.github.tminglei.swagger.SwaggerExtensions.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Constraints.*;

@Path("/pet")
@Produces({"application/json", "application/xml"})
public class PetResource {
    static PetData petData = new PetData();
    static JavaRestResourceUtil ru = new JavaRestResourceUtil();

    static Mapping<?> petStatus = text(oneOf(Arrays.asList("available", "pending", "sold")))
            .$ext(o -> ext(o).desc("pet status in the store"));
    static Mapping<?> pet = mapping(
            field("id", vLong().$ext(o -> ext(o).desc("pet id"))),
            field("name", text(required()).$ext(o -> ext(o).desc("pet name"))),
            field("category", mapping(
                    field("id", vLong(required())),
                    field("name", text(required()))
            ).$ext(o -> ext(o).desc("category belonged to"))),
            field("photoUrls", list(text()).$ext(o -> ext(o).desc("pet's photo urls"))),
            field("tags", list(text()).$ext(o -> ext(o).desc("tags for the pet"))),
            field("status", petStatus)
        ).$ext(o -> ext(o).desc("pet info"));

    ///
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
            throws NotFoundException {
        Pet pet = petData.getPetbyId(ru.getLong(0, 100000, 0, petId));
        if (null != pet) {
            return Response.ok().entity(pet).build();
        } else {
            throw new NotFoundException(404, "Pet not found");
        }
    }

    static {
        operation("post", "/pet")
                .summary("create a pet")
                .tag("pet")
                .parameter(param(pet).in("body"))
                .response(200, new io.swagger.models.Response()
                                .description("success")
                )
        ;
    }
    @POST
    public Response addPet(Pet pet) {
        petData.addPet(pet);
        return Response.ok().entity("SUCCESS").build();
    }

    static {
        operation("put", "/pet")
                .summary("update pet")
                .tag("pet")
                .parameter(param(pet).in("body"))
                .response(200, new io.swagger.models.Response()
                                .description("success")
                )
        ;
    }
    @PUT
    public Response updatePet(Pet pet) {
        petData.addPet(pet);
        return Response.ok().entity("SUCCESS").build();
    }

    static {
        operation("get", "/pet/findByStatus")
                .summary("find pets by status")
                .tag("pet")
                .parameter(param(petStatus).in("query").name("status"))
                .response(200, response(list(pet)).description("pet list"))
        ;
    }
    @GET
    @Path("/findByStatus")
    public Response findPetsByStatus(@QueryParam("status") String status) {
        return Response.ok(petData.findPetByStatus(status)).build();
    }

    static {
        operation("get", "/pet/findByTags")
                .summary("find pets by tags")
                .tag("pet")
                .parameter(param(list(text())).in("query").name("tags").desc("pet tags"))
                .response(200, response(list(pet)).description("pet list"));
    }
    @GET
    @Path("/findByTags")
    @Deprecated
    public Response findPetsByTags(@QueryParam("tags") String tags) {
        return Response.ok(petData.findPetByTags(tags)).build();
    }
}
