package com.github.tminglei.swagger.route;

import io.swagger.models.HttpMethod;

/**
 * Used to locate request path to matched route object
 */
public interface Router {

    void add(Route route);

    /**
     * Returns a Route that matches the given URL path.
     * Note that the path may be expected to be an undecoded
     * URL path. This URL encoding requirement is determined
     * by the Router implementation.
     *
     * @param method http method
     * @param path a decoded or undecoded URL path,
     *             depending on the Router implementation
     * @return the matching route, or null if none is found
     */
    Route route(HttpMethod method, String path);

}
