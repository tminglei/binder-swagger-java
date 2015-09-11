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

package com.example.data;

import java.sql.SQLException;
import java.util.Map;

import com.github.tminglei.bind.BindObject;
import org.apache.commons.dbutils.QueryRunner;

public class StoreData {

    public Map<String, Object> findOrderById(long orderId) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );

        return run.query("select * from order where id=?", H2DB.mkResultSetHandler(
                "id", "petId", "quantity", "shipDate", "status"
        ), orderId).stream().findFirst().orElse(null);
    }

    public void placeOrder(BindObject bindObj) throws SQLException {
        deleteOrder(bindObj.get("id"));

        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        run.update("insert into order(id, pet_id, quantity, ship_date, status) " +
                        "values(?, ?, ?, ?, ?)",
                bindObj.get("id"),
                bindObj.get("petId"),
                bindObj.get("quantity"),
                bindObj.get("shipDate"),
                bindObj.get("status"));
    }

    public void deleteOrder(long orderId) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        run.update("delete from order where id=?", orderId);
    }
}