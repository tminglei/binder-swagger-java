/*
 * Copyright (C) 2014 BigTesting.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tminglei.swagger.route;

import io.swagger.models.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis Antunes
 */
public class TreeRouterImpl implements Router {
    private TreeNode root;

    public synchronized void add(Route route) {
        RouteImpl routeImpl = (RouteImpl) route;
        List<PathElement> pathElements = routeImpl.getPathElements();
        if (!pathElements.isEmpty() && routeImpl.endsWithPathSeparator()) {
            pathElements.add(
                new PathStaticElement(RouteHelper.PATH_ELEMENT_SEPARATOR, pathElements.size() - 1));
        }

        if (root == null) {
            root = new TreeNode(new PathStaticElement(RouteHelper.PATH_ELEMENT_SEPARATOR, 0));
        }

        TreeNode currentNode = root;
        for (PathElement elem : pathElements) {
            TreeNode matchingNode = currentNode.getMatchingChild(elem);
            if (matchingNode == null) {
                TreeNode newChild = new TreeNode(elem);
                currentNode.addChild(newChild);
                currentNode = newChild;
            } else {
                currentNode = matchingNode;
            }
        }
        currentNode.addRoute(route);
    }

    /**
     * Returns a Route that matches the given URL path.
     * Note that the path is expected to be an undecoded URL path.
     * The router will handle any decoding that might be required.
     *
     *  @param path an undecoded URL path
     *  @return the matching route, or null if none is found
     */
    public Route route(HttpMethod method, String path) {
        List<String> searchTokens = getPathAsSearchTokens(path);

        /* handle the case where path is '/' and route '/*' exists */
        if (searchTokens.isEmpty() && root.containsSplatChild() && !root.hasRoute()) {
            return root.getSplatChild().getRoute(method);
        }

        TreeNode currentMatchingNode = root;
        for (String token : searchTokens) {
            TreeNode matchingNode = currentMatchingNode.getMatchingChild(token);
            if (matchingNode == null) return null;

            currentMatchingNode = matchingNode;
            if (currentMatchingNode.isSplat() &&
                !currentMatchingNode.hasChildren()) {
                return currentMatchingNode.getRoute(method);
            }
        }

        return currentMatchingNode.getRoute(method);
    }

    private List<String> getPathAsSearchTokens(String path) {
        List<String> tokens = new ArrayList<>();

        path = RouteHelper.urlDecodeForRouting(path);
        String[] pathElements = RouteHelper.getPathElements(path);
        for (int i = 0; i < pathElements.length; i++) {
            String token = pathElements[i];
            if (token != null && token.trim().length() > 0) {
                tokens.add(token);
            }
        }
        if (!tokens.isEmpty() &&
            path.trim().endsWith(RouteHelper.PATH_ELEMENT_SEPARATOR)) {
            tokens.add(RouteHelper.PATH_ELEMENT_SEPARATOR);
        }

        return tokens;
    }

    public TreeNode getRoot() {
        return root;
    }
}
