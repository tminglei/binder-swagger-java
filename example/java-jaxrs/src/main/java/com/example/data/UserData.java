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

public class UserData {

    public Map<String, Object> findUserByName(String username) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );

        return run.query("select id, user_name, first_name, last_name, email, phone, status from user where user_name=?",
                H2DB.mkResultSetHandler(
                        "id", "username", "firstName", "lastName", "email", "phone", "status"
                ), username).stream().findFirst().orElse(null);
    }

    public void addUser(BindObject bindObj) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        run.update("insert into user(id, user_name, first_name, last_name, email, password, phone, status) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?)",
                bindObj.get("id"),
                bindObj.get("username"),
                bindObj.get("firstName"),
                bindObj.get("lastName"),
                bindObj.get("email"),
                bindObj.get("password"),
                bindObj.get("phone"),
                bindObj.get("status"));
    }

    public void removeUser(String username) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        run.update("delete from user where user_name=?", username);
    }
}