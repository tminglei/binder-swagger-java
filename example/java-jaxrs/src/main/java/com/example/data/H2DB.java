package com.example.data;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

/**
 * Created by tminglei on 9/9/15.
 */
public class H2DB {
    private static DataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("sa");
            dataSource = ds;
        }
        return dataSource;
    }

    public static ResultSetHandler<List<Map<String, Object>>>
            mkResultSetHandler(String... names) {
        return (rs) -> {
            try {
                int columnCount = rs.getMetaData().getColumnCount();
                List<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) {
                    Object[] data = readOneRow(rs, columnCount);
                    result.add(arrayToMap(data, names));
                }
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Object[] readOneRow(ResultSet rs, int columnCount) throws SQLException {
        Object[] result = new Object[columnCount];
        for(int i=1; i<=columnCount; i++) {
            result[i-1] = rs.getObject(i);
        }
        return result;
    }

    public static Map<String, Object> arrayToMap(Object[] data, String... names) {
        if (data == null) throw new IllegalArgumentException("data is null!!!");
        if (data.length != names.length) throw new IllegalArgumentException(
                "data has different length with names!!!");

        Map<String, Object> result = new HashMap<String, Object>();
        for(int i=0; i < names.length; i++) {
            result.put(names[i], data[i]);
        }
        return result;
    }

    static List<String> strToList(String listStr) {
        if (listStr == null) return null;
        List<String> result = new ArrayList<>();
        String[] parts = listStr.split(",");
        for(int i=0; i<parts.length; i++) {
            if (parts[i].trim().length() > 0) {
                result.add(parts[i].trim());
            }
        }
        return result;
    }

    ///
    public static void setupDatabase() {
        QueryRunner run = new QueryRunner( getDataSource() );

        try {
            run.update("create table category(" +
                    "id bigint primary key, " +
                    "name varchar(255)" +
                    ")");
            run.update("create table pet(" +
                    "id bigint primary key, " +
                    "name varchar(255), " +
                    "category_id bigint, " +
                    "photo_urls varchar(2000) default '', " +
                    "tags varchar(500) default '', " +
                    "status varchar(25)" +
                    ")");
            run.update("create table order1(" +
                    "id bigint primary key, " +
                    "pet_id bigint, " +
                    "quantity int, " +
                    "ship_date timestamp, " +
                    "status varchar(25)" +
                    ")");
            run.update("create table user(" +
                    "id bigint primary key, " +
                    "user_name varchar(50), " +
                    "first_name varchar(50), " +
                    "last_name varchar(50), " +
                    "email varchar(100), " +
                    "password varchar(250), " +
                    "phone varchar(50), " +
                    "status int" +
                    ")");

            run.batch("insert into category(id, name) values(?, ?)", new Object[][]{
                    new Object[]{1, "Dogs"},
                    new Object[]{2, "Cats"},
                    new Object[]{3, "Rabbits"},
                    new Object[]{4, "Lions"}
            });
            run.batch("insert into pet(id, category_id, name, photo_urls, tags, status) " +
                    "values(?, ?, ?, ?, ?, ?)", new Object[][]{
                    new Object[]{1, 2, "Cat 1", ",url1,url2,", ",tag1,tag2,", "available"},
                    new Object[]{2, 2, "Cat 2", ",url1,url2,", ",tag2,tag3,", "available"},
                    new Object[]{3, 2, "Cat 3", ",url1,url2,", ",tag3,tag4,", "pending"},

                    new Object[]{4, 1, "Dog 1", ",url1,url2,", ",tag1,tag2,", "available"},
                    new Object[]{5, 1, "Dog 2", ",url1,url2,", ",tag2,tag3,", "sold"},
                    new Object[]{6, 1, "Dog 3", ",url1,url2,", ",tag3,tag4,", "pending"},

                    new Object[]{7, 4, "Lion 1", ",url1,url2,", ",tag1,tag2,", "available"},
                    new Object[]{8, 4, "Lion 1", ",url1,url2,", ",tag2,tag3,", "available"},
                    new Object[]{9, 4, "Lion 1", ",url1,url2,", ",tag3,tag4,", "available"},

                    new Object[]{10, 3, "Rabbit 1", ",url1,url2,", ",tag3,tag4,", "available"}
            });

            run.batch("insert into order1(id, pet_id, quantity, ship_date, status) " +
                    "values(?, ?, ?, ?, ?)", new Object[][]{
                    new Object[]{1, 1, 2, new Date(), "placed"},
                    new Object[]{2, 1, 2, new Date(), "delivered"},
                    new Object[]{3, 2, 2, new Date(), "placed"},
                    new Object[]{4, 2, 2, new Date(), "delivered"},
                    new Object[]{5, 3, 2, new Date(), "placed"},
                    new Object[]{11, 3, 2, new Date(), "placed"},
                    new Object[]{12, 3, 2, new Date(), "placed"},
                    new Object[]{13, 3, 2, new Date(), "placed"},
                    new Object[]{14, 3, 2, new Date(), "placed"},
                    new Object[]{15, 3, 2, new Date(), "placed"}
            });

            run.batch("insert into user(id, user_name, first_name, last_name, email, password, phone, status) " +
                    "values(?, ?, ?, ?, ?, ?, ?, ?)", new Object[][]{
                    new Object[]{1, "user1", "fname1", "lname1", "email1@test.com", "XXXXXXXX", "123-456-7890", 1},
                    new Object[]{2, "user2", "fname2", "lname2", "email2@test.com", "XXXXXXXX", "123-456-7890", 2},
                    new Object[]{3, "user3", "fname3", "lname3", "email3@test.com", "XXXXXXXX", "123-456-7890", 3},
                    new Object[]{4, "user4", "fname4", "lname4", "email4@test.com", "XXXXXXXX", "123-456-7890", 1},
                    new Object[]{5, "user5", "fname5", "lname5", "email5@test.com", "XXXXXXXX", "123-456-7890", 2},
                    new Object[]{6, "user6", "fname6", "lname6", "email6@test.com", "XXXXXXXX", "123-456-7890", 3},
                    new Object[]{7, "user7", "fname7", "lname7", "email7@test.com", "XXXXXXXX", "123-456-7890", 1},
                    new Object[]{8, "user8", "fname8", "lname8", "email8@test.com", "XXXXXXXX", "123-456-7890", 2},
                    new Object[]{9, "user9", "fname9", "lname9", "email9@test.com", "XXXXXXXX", "123-456-7890", 3},
                    new Object[]{10, "user10", "fname10", "lname10", "email10@test.com", "XXXXXXXX", "123-456-7890", 1},
                    new Object[]{11, "user11", "fname11", "lname11", "email11@test.com", "XXXXXXXX", "123-456-7890", 1}
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
