package com.github.tminglei.swagger.fake;

import java.util.Map;

/**
 * Used to generate fake data
 */
public interface DataProvider {

    /**
     *
     * @param params    flattened request param map
     */
    default void setRequestParams(Map<String, String> params) {}

    /**
     *
     * @param required  whether target data is required
     */
    default void setRequired(boolean required) {}

    /**
     *
     * @return  data object or null
     */
    Object get();

}
