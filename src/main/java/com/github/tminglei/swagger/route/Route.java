package com.github.tminglei.swagger.route;

import com.github.tminglei.swagger.fake.DataProvider;
import io.swagger.models.HttpMethod;

import java.util.Map;

/**
 * Used to hold some related info/objects
 */
public interface Route {

    /**
     *
     * @return binded http method
     */
    HttpMethod getMethod();

    /**
     *
     * @return binded path pattern
     */
    String getPath();

    /**
     *
     * @param path request path, should match the binded path pattern
     * @return
     */
    Map<String, String> getPathParams(String path);

    /**
     *
     * @return whether target operation is implemented
     */
    boolean isImplemented();

    /**
     *
     * @return data provider used to generate fake data
     */
    DataProvider getDataProvider();

}
