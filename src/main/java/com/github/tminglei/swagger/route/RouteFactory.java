package com.github.tminglei.swagger.route;

import com.github.tminglei.swagger.fake.DataProvider;
import io.swagger.models.HttpMethod;

/**
 * route factory
 */
public interface RouteFactory {

    /**
     *
     * @param method        http method
     * @param pathPattern   url pattern
     * @param implemented   whether it is implemented
     * @param dataProvider  data provider used to generate fake data
     * @return  route implementation object
     */
    Route create(HttpMethod method,
                 String pathPattern,
                 boolean implemented,
                 DataProvider dataProvider);

}
