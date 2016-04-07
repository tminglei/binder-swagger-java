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
import java.util.*;
import java.util.stream.Collectors;

import com.github.tminglei.bind.BindObject;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.StringUtils;

public class PetData {

    public Map<String, Object> getPetById(long petId) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );

        return run.query("select * from pet where id=?", H2DB.mkResultSetHandler(
                "id", "name", "categoryId", "photoUrls", "tags", "status"
        ), petId).stream().map(m -> {
            m.put("photoUrls", H2DB.strToList((String) m.get("photoUrls")));
            m.put("tags", H2DB.strToList((String) m.get("tags")));
            m.put("category", getCategory(run, (Long) m.get("categoryId")));
            m.remove("categoryId");
            return m;
        }).findFirst().orElse(null);
    }

    private Map<String, Object> getCategory(QueryRunner run, Long categoryId) {
        try {
            List<Map<String, Object>> results = run.query("select * from category where id=?", H2DB.mkResultSetHandler("id", "name"), categoryId);
            return results.isEmpty() ? null : results.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> findPetByStatus(String status) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        String[] statues = status.split(",");
        String statusInStr = StringUtils.join(
                Arrays.asList(statues).stream()
                        .map(s -> "'" + s.trim() + "'")
                        .collect(Collectors.toList()),
                ",");

        if (statues.length > 0) {
            return run.query("select * from pet where status in (" + statusInStr + ")",
                    H2DB.mkResultSetHandler("id", "name", "categoryId", "photoUrls", "tags", "status")
            ).stream().map(m -> {
                m.put("photoUrls", H2DB.strToList((String) m.get("photoUrls")));
                m.put("tags", H2DB.strToList((String) m.get("tags")));
                m.put("category", getCategory(run, (Long) m.get("categoryId")));
                m.remove("categoryId");
                return m;
            }).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> findPetByTags(String tags) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        List<String> tagList = Arrays.asList(tags.split(","));

        if (tagList.isEmpty()) return Collections.EMPTY_LIST;
        else {
            return run.query("select * from pet",
                    H2DB.mkResultSetHandler("id", "name", "categoryId", "photoUrls", "tags", "status")
            ).stream().map(m -> {
                m.put("photoUrls", H2DB.strToList((String) m.get("photoUrls")));
                m.put("tags", H2DB.strToList((String) m.get("tags")));
                m.put("category", getCategory(run, (Long) m.get("categoryId")));
                m.remove("categoryId");
                return m;
            }).filter(m -> {
                if (m.get("tags") == null) return false;
                else {
                    List<String> its = (List<String>) m.get("tags");
                    List<String> tmp = new ArrayList<>(its);
                    tmp.removeAll(tagList);
                    return tmp.size() != its.size();
                }
            }).collect(Collectors.toList());
        }
    }

    public void addPet(BindObject bindObj) throws SQLException {
        QueryRunner run = new QueryRunner( H2DB.getDataSource() );
        run.update("insert into pet(id, category_id, name, photo_urls, tags, status) " +
                "values(?, ?, ?, ?, ?, ?)",
                bindObj.get("id"),
                bindObj.obj("category").get("id"),
                bindObj.get("name"),
                bindObj.get("photoUrls"),
                bindObj.get("tags"),
                bindObj.get("status"));
    }
}