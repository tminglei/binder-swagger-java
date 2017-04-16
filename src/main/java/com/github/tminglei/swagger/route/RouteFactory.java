package com.github.tminglei.swagger.route;

import com.github.tminglei.swagger.fake.DataProvider;
import io.swagger.models.HttpMethod;

/**
 * route factory
 */
public interface RouteFactory {

    Route create(HttpMethod method,
                 String pathPattern,
                 boolean implemented,
                 DataProvider dataProvider);

}
