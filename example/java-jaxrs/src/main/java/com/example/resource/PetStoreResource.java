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

import com.example.data.StoreData;
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

import static io.swagger.models.HttpMethod.*;
import static com.github.tminglei.swagger.SwaggerContext.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Constraints.*;
import static com.github.tminglei.bind.Processors.*;

@Path("/store")
@Produces({"application/json", "application/xml"})
public class PetStoreResource {
    static StoreData storeData = new StoreData();
    private ResourceBundle bundle = ResourceBundle.getBundle("bind-messages");
    private Messages messages = (key) -> bundle.getString(key);

    static Mapping<?> orderStatus = $(text(oneOf(Arrays.asList("placed", "approved", "delivered"))))
            .desc("order status").$$;
    static Mapping<?> order = $(mapping(
            field("id", $(longv()).desc("order id").$$),
            field("petId", $(longv(required())).desc("pet id").$$),
            field("quantity", $(intv(required())).desc("number to be sold").$$),
            field("shipDate", $(datetime()).desc("delivery time").$$),
            field("status", orderStatus)
        )).refName("Order").desc("order info").$$;

    static SharingHolder sharing = sharing().pathPrefix("/store").tag("store");

    ///
    static {
        sharing.operation(GET, "/order/{orderId}")
            .summary("get order by id")
            .parameter(param(longv()).in("path").name("orderId").desc("order id"))
            .response(200, response(order))
            .response(404, response().description("order not found"))
        ;
    }
    @GET
    @Path("/order/{orderId}")
    public Response getOrderById(@PathParam("orderId") String orderId)
            throws NotFoundException, SQLException {
        Map<String, Object> order = storeData.findOrderById(Long.parseLong(orderId));
        if (null != order) {
            return Response.ok().entity(order).build();
        } else {
            throw new NotFoundException(404, "Order not found");
        }
    }

    static {
        sharing.operation(POST, "/order")
            .summary("add an order")
            .parameter(param(order).in("body"))
            .response(200, response())
        ;
    }
    @POST
    @Path("/order")
    public Response placeOrder(String data) throws BadRequestException, SQLException {
        BindObject bindObj = new FormBinder(messages).bind(
                attach(expandJson()).to(order),
                hashmap(entry("", data)));
        if (bindObj.errors().isPresent()) {
            throw new BadRequestException(400, "invalid pet");
        } else {
            storeData.placeOrder(bindObj);
            return Response.ok().entity("").build();
        }
    }

    static {
        sharing.operation(DELETE, "/order/{orderId}")
            .summary("delete specified order")
            .parameter(param(longv()).in("path").name("orderId").desc("order id"))
            .response(200, response())
        ;
    }
    @DELETE
    @Path("/order/{orderId}")
    public Response deleteOrder(@PathParam("orderId") String orderId) throws SQLException {
        storeData.deleteOrder(Long.parseLong(orderId));
        return Response.ok().entity("").build();
    }
}
