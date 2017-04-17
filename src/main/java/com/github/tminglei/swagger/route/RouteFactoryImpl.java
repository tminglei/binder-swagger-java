package com.github.tminglei.swagger.route;

import com.github.tminglei.swagger.fake.DataProvider;
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
