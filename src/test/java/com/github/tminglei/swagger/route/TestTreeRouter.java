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


import com.github.tminglei.swagger.fake.ConstDataProvider;
import io.swagger.models.HttpMethod;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Luis Antunes
 */
public class TestTreeRouter extends RouterContractTest<TreeRouterImpl> {

    @Override
    protected TreeRouterImpl newRouter() {
        return new TreeRouterImpl();
    }

    @Override
    protected Route newRoute(String path) {
        return new RouteImpl(HttpMethod.GET, path, true, new ConstDataProvider(""));
    }

    ///---

    @Test
    // produces the correct tree when given the root route
    public void treeTest1() {
        Route r = newRoute("/");

        router.add(r);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertEquals(r, root.getRoute(HttpMethod.GET));
        assertEquals(0, root.getChildren().size());

        assertEquals(r, route("/"));
        assertNull(route("/*"));
        assertNull(route("/blah"));
    }

    @Test
    // produces the correct tree when given a single route with a single static path element
    public void treeTest2() {
        Route r = newRoute("/hello");

        router.add(r);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^hello$", child.toString());
        assertEquals(r, child.getRoute(HttpMethod.GET));

        assertNull(route("/"));
        assertEquals(r, route("/hello"));
        assertNull(route("/hello/"));
        assertNull(route("/blah"));
    }

    @Test
    // produces the correct tree when given the same route twice
    public void treeTest3() {
        Route r1 = newRoute("/hello");
        Route r2 = newRoute("/hello");

        router.add(r1);
        router.add(r2);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^hello$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));

        assertNull(route("/"));
        assertEquals(r1, route("/hello"));
        assertNull(route("/hello/"));
        assertNull(route("/blah"));
    }

    @Test
    // produces the correct tree when given a single route with a single static path element ending with a path separator
    public void treeTest4() {
        Route r = newRoute("/hello/");

        router.add(r);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^hello$", child.toString());
        assertNull(child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^/$", child.toString());
        assertEquals(r, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertNull(route("/"));
        assertEquals(r, route("/hello/"));
        assertNull(route("/hello"));
        assertNull(route("/blah"));
    }

    @Test
    // produces the correct tree when given a single route with a single static path element with a regex symbol
    public void treeTest5() {
        Route r = newRoute("/hello$.html");

        router.add(r);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^hello\\$\\.html$", child.toString());
        assertEquals(r, child.getRoute(HttpMethod.GET));

        assertNull(route("/"));
        assertEquals(r, route("/hello$.html"));
        assertNull(route("/hello/"));
        assertNull(route("/blah"));
    }

    @Test
    // produces the correct tree when given the root route and a route with a single static path element
    public void treeTest6() {
        Route r1 = newRoute("/");
        Route r2 = newRoute("/hello");

        router.add(r1);
        router.add(r2);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertEquals(r1, root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^hello$", child.toString());
        assertEquals(r2, child.getRoute(HttpMethod.GET));

        assertEquals(r1, route("/"));
        assertEquals(r2, route("/hello"));
        assertNull(route("/hello/"));
        assertNull(route("/blah"));
    }

    @Test
    // produces the correct tree when given multiple routes with static path elements
    public void treeTest7() {
        Route r1 = newRoute("/");
        Route r2 = newRoute("/hello");
        Route r3 = newRoute("/hello/world");
        Route r4 = newRoute("/protected");

        router.add(r1);
        router.add(r2);
        router.add(r3);
        router.add(r4);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertEquals(r1, root.getRoute(HttpMethod.GET));
        assertEquals(2, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^hello$", child.toString());
        assertEquals(r2, child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^world$", child.toString());
        assertEquals(r3, child.getRoute(HttpMethod.GET));

        child = root.getChildren().get(1);
        assertEquals("^protected$", child.toString());
        assertEquals(r4, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertEquals(r1, route("/"));
        assertEquals(r2, route("/hello"));
        assertNull(route("/hello/"));
        assertEquals(r3, route("/hello/world"));
        assertEquals(r4, route("/protected"));
        assertNull(route("/blah"));
        assertNull(route("/protected/"));
        assertNull(route("/hello/world/"));
    }

    @Test
    // produces the correct tree when given a sigle route with a single named path element
    public void treeTest8() {
        Route r1 = newRoute("/:name");

        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^([^/]+)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertNull(route("/"));
        assertEquals(r1, route("/john"));
        assertNull(route("/john/doe"));
        assertNull(route("/john/"));
    }

    @Test
    // produces the correct tree when given a single route with multiple named path elements
    public void treeTest9() {
        Route r1 = newRoute("/:name/:id");

        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^([^/]+)$", child.toString());
        assertNull(child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^([^/]+)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertNull(route("/"));
        assertNull(route("/john"));
        assertEquals(r1, route("/john/doe"));
        assertNull(route("/john/"));
        assertNull(route("/john/doe/"));
    }

    @Test
    // produces the correct tree when given a single route with a single named parameter with custom regex
    public void treeTest10() {
        Route r1 = newRoute("/:id<[0-9]+>");

        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^([0-9]+)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertNull(route("/"));
        assertEquals(r1, route("/123"));
        assertNull(route("/123/456"));
        assertNull(route("/123/"));
    }

    @Test
    // produces the correct tree when given multiples routes with multiple elements with a named parameter with custom regex
    public void treeTest11() {
        Route r1 = newRoute("/cntrl/actn/:id<[0-9]+>");
        Route r2 = newRoute("/cntrl/actn/:id<[a-z]+>");

        router.add(r1);
        router.add(r2);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode firstChild = root.getChildren().get(0);
        assertEquals("^cntrl$", firstChild.toString());
        assertNull(firstChild.getRoute(HttpMethod.GET));
        assertEquals(1, firstChild.getChildren().size());

        TreeNode secondChild = firstChild.getChildren().get(0);
        assertEquals("^actn$", secondChild.toString());
        assertNull(secondChild.getRoute(HttpMethod.GET));
        assertEquals(2, secondChild.getChildren().size());

        TreeNode child = secondChild.getChildren().get(0);
        assertEquals("^([0-9]+)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        child = secondChild.getChildren().get(1);
        assertEquals("^([a-z]+)$", child.toString());
        assertEquals(r2, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertEquals(r1, route("/cntrl/actn/123"));
        assertNull(route("/cntrl/actn/123/"));
        assertEquals(r2, route("/cntrl/actn/bob"));
        assertNull(route("/cntrl/actn/bob/"));
    }

    @Test
    // produces the correct tree when given a single route with a splat that matches all paths
    public void treeTest12() {
        Route r1 = newRoute("/*");

        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertEquals(r1, route("/"));
        assertEquals(r1, route("/123"));
        assertEquals(r1, route("/123/456"));
        assertEquals(r1, route("/123/"));
    }

    @Test
    // produces the correct tree when given a single route with a splat that matches all paths, and the root route
    public void treeTest13() {
        Route r0 = newRoute("/");
        Route r1 = newRoute("/*");

        router.add(r0);
        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertEquals(r0, root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertEquals(r0, route("/"));
        assertEquals(r1, route("/123"));
        assertEquals(r1, route("/123/456"));
        assertEquals(r1, route("/123/"));
    }

    @Test
    // produces the correct tree when given a single route with a splat with a preceding static element
    public void treeTest14() {
        Route r1 = newRoute("/protected/*");

        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^protected$", child.toString());
        assertNull(child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertNull(route("/protected"));
        assertEquals(r1, route("/protected/content"));
        assertEquals(r1, route("/protected/"));
    }

    @Test
    // produces the correct tree when given multiple routes, one with a splat with a preceding static element
    public void treeTest15() {
        Route r1 = newRoute("/protected/*");
        Route r2 = newRoute("/protected/:id<[0-9]+>");
        Route r3 = newRoute("/:name<[a-z]+>");

        router.add(r1);
        router.add(r2);
        router.add(r3);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(2, root.getChildren().size());

        TreeNode firstChild = root.getChildren().get(0);
        assertEquals("^protected$", firstChild.toString());
        assertNull(firstChild.getRoute(HttpMethod.GET));
        assertEquals(2, firstChild.getChildren().size());

        TreeNode child = firstChild.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        child = firstChild.getChildren().get(1);
        assertEquals("^([0-9]+)$", child.toString());
        assertEquals(r2, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        TreeNode secondChild = root.getChildren().get(1);
        assertEquals("^([a-z]+)$", secondChild.toString());
        assertEquals(r3, secondChild.getRoute(HttpMethod.GET));
        assertEquals(0, secondChild.getChildren().size());

        assertEquals(r1, route("/protected/content"));
        assertEquals(r1, route("/protected/123"));
        assertEquals(r3, route("/john"));
        assertEquals(r1, route("/protected/"));
    }

    @Test
    // produces the correct tree when given a single route with multiple splat elements
    public void treeTest16() {
        Route r1 = newRoute("/say/*/to/*");

        router.add(r1);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertNull(root.getRoute(HttpMethod.GET));
        assertEquals(1, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^say$", child.toString());
        assertNull(child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertNull(child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^to$", child.toString());
        assertNull(child.getRoute(HttpMethod.GET));
        assertEquals(1, child.getChildren().size());

        child = child.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertEquals(r1, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        assertNull(route("/hello"));
        assertEquals(r1, route("/say/hello/to/world"));
        assertEquals(r1, route("/say/bye/to/Tim"));
        assertNull(route("/say/bye/bye/to/Tim"));
        assertEquals(r1, route("/say/bye/to/John/Doe"));
        assertNull(route("/say/hello/to"));
        assertEquals(r1, route("/say/hello/to/"));
    }

    @Test
    // matches a similar route containing a splat before the route containing the static element
    public void treeTest17() {
        Route r1  = newRoute("/abc/*/def");
        Route r2  = newRoute("/abc/123/def");

        router.add(r2);
        router.add(r1);

        assertEquals(r1, route("/abc/123/def"));
    }

    @Test
    // matches a similar route containing a static element before the route containing the named parameter
    public void treeTest18() {
        Route r1  = newRoute("/abc/123/def");
        Route r2  = newRoute("/abc/:name/def");

        router.add(r2);
        router.add(r1);

        assertEquals(r1, route("/abc/123/def"));
    }

    @Test
    // matches a similar route containing a splat before the route containing the named parameter
    public void treeTest19() {
        Route r1  = newRoute("/abc/*/def");
        Route r2  = newRoute("/abc/:name/def");

        router.add(r2);
        router.add(r1);

        assertEquals(r1, route("/abc/123/def"));
    }

    @Test
    // produces a tree with multiple routes with children sorted properly
    public void treeTest20() {
        Route r1  = newRoute("/");
        Route r2  = newRoute("/*");
        Route r3  = newRoute("/1");
        Route r4  = newRoute("/x");
        Route r5  = newRoute("/y");
        Route r6  = newRoute("/:id");

        router.add(r6);
        router.add(r5);
        router.add(r3);
        router.add(r4);
        router.add(r1);
        router.add(r2);

        TreeNode root = router.getRoot();
        assertEquals("^/$", root.toString());
        assertEquals(r1, root.getRoute(HttpMethod.GET));
        assertEquals(5, root.getChildren().size());

        TreeNode child = root.getChildren().get(0);
        assertEquals("^(.*)$", child.toString());
        assertEquals(r2, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        child = root.getChildren().get(1);
        assertEquals("^1$", child.toString());
        assertEquals(r3, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        child = root.getChildren().get(2);
        assertEquals("^x$", child.toString());
        assertEquals(r4, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        child = root.getChildren().get(3);
        assertEquals("^y$", child.toString());
        assertEquals(r5, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());

        child = root.getChildren().get(4);
        assertEquals("^([^/]+)$", child.toString());
        assertEquals(r6, child.getRoute(HttpMethod.GET));
        assertEquals(0, child.getChildren().size());
    }
}