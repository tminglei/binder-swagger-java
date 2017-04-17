package com.github.tminglei.swagger.route.impl;

import com.github.tminglei.swagger.fake.DataProvider;
import com.github.tminglei.swagger.route.Route;
import com.github.tminglei.swagger.route.RouteFactory;
import io.swagger.models.HttpMethod;

/**
 * Created by minglei on 4/17/17.
 */
public class RouteFactoryImpl implements RouteFactory {

    @Override
    public Route create(HttpMethod method,
                        String pathPattern,
                        boolean implemented,
                        DataProvider dataProvider) {
        return new RouteImpl(method, pathPattern, implemented, dataProvider);
    }

}
