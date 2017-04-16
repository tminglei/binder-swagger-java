package com.github.tminglei.swagger.fake;

/**
 * Used to generate fake data
 */
public interface DataProvider {

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
