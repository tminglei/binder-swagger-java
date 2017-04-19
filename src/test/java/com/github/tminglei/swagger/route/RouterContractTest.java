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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Luis Antunes
 */
public abstract class RouterContractTest<R extends Router> {

    protected R router;

    @Before
    public void beforeEachTest() {
        router = newRouter();
    }

    protected abstract R newRouter();

    protected abstract Route newRoute(String path);

    protected Route route(String path) {
        return router.route(HttpMethod.GET, path);
    }

    ///---

    @Test
    // matches the root route
    public void routeTest1() {
        Route r1 = newRoute("/");
        router.add(r1);
        assertEquals(r1, route("/"));
    }

    @Test
    // distinguishes between similar static and named param routes; case 1
    public void routeTest2() {
        Route r1 = newRoute("/clients/all");
        Route r2 = newRoute("/clients/:id");

        router.add(r1);
        router.add(r2);

        assertEquals(r1, route("/clients/all"));
    }

    @Test
    // distinguishes between similar static and named param routes; case 2
    public void routeTest3() {
        Route r1 = newRoute("/clients/all");
        Route r2 = newRoute("/clients/:id");

        router.add(r1);
        router.add(r2);

        assertEquals(r2, route("/clients/123"));
    }

    @Test
    // distinguishes between dissimilar static and named param routes; case 1
    public void routeTest4() {
        Route r1 = newRoute("/cntrl");
        Route r2 = newRoute("/cntrl/clients/:id");

        router.add(r1);
        router.add(r2);

        assertEquals(r1, route("/cntrl"));
    }

    @Test
    // distinguishes between dissimilar static and named param routes; case 2
    public void routeTest5() {
        Route r1 = newRoute("/cntrl");
        Route r2 = newRoute("/cntrl/clients/:id");

        router.add(r1);
        router.add(r2);

        assertEquals(r2, route("/cntrl/clients/23455"));
    }

    @Test
    // distinguishes between two different static routes
    public void routeTest6() {
        Route r1 = newRoute("/cntrl");
        Route r2 = newRoute("/actn");

        router.add(r1);
        router.add(r2);

        assertEquals(r2, route("/actn"));
    }

    @Test
    // returns null when no route is found
    public void routeTest7() {
        Route r1 = newRoute("/cntrl");
        Route r2 = newRoute("/actn");

        router.add(r1);
        router.add(r2);

        assertNull(route("/test"));
    }

    @Test
    // distinguishes between routes with multiple named path parameters; case 1
    public void routeTest8() {
        Route r1 = newRoute("/cntrl/actn/:id");
        Route r2 = newRoute("/cntrl/actn/:id/:name");

        router.add(r1);
        router.add(r2);

        assertEquals(r2, route("/cntrl/actn/123/bob"));
    }

    @Test
    // distinguishes between routes with multiple named path parameters; case 2
    public void routeTest9() {
        Route r1 = newRoute("/cntrl/actn/:id");
        Route r2 = newRoute("/cntrl/actn/:id/:name");

        router.add(r1);
        router.add(r2);

        assertEquals(r1, route("/cntrl/actn/123"));
    }

    @Test
    // distinguishes between named parameters with custom regex; alpha case
    public void routeTest10() {
        Route r1 = newRoute("/cntrl/actn/:id<[0-9]+>");
        Route r2 = newRoute("/cntrl/actn/:id<[a-z]+>");

        router.add(r1);
        router.add(r2);

        assertEquals(r2, route("/cntrl/actn/bob"));
        assertNull(route("/cntrl/actn/bob/"));
    }

    @Test
    // distinguishes between named parameters with custom regex; numeric case
    public void routeTest11() {
        Route r1 = newRoute("/cntrl/actn/:id<[0-9]+>");
        Route r2 = newRoute("/cntrl/actn/:id<[a-z]+>");

        router.add(r1);
        router.add(r2);

        assertEquals(r1, route("/cntrl/actn/123"));
        assertNull(route("/cntrl/actn/123/"));
    }

    @Test
    // matches a named parameter, but not if there is an extra path element after it
    public void routeTest12() {
        Route r1 = newRoute("/cntrl/:name");

        router.add(r1);

        assertEquals(r1, route("/cntrl/Tim"));
        assertNull(route("/cntrl/Tim/blah"));
    }

    @Test
    // handles the splat path parameter for all requests
    public void routeTest13() {
        Route r1 = newRoute("/*");
        Route r2 = newRoute("/specific");
        Route r3 = newRoute("/:id<[0-9]+>");

        router.add(r1);
        router.add(r2);
        router.add(r3);

        assertEquals(r1, route("/"));
        assertEquals(r1, route("/*"));
        assertEquals(r1, route("/cntrl"));
        assertEquals(r1, route("/actn"));
        assertEquals(r1, route("/cntrl/actn"));
        assertEquals(r1, route("/specific"));
        assertEquals(r1, route("/123"));
        assertEquals(r1, route("/hello/"));
    }

    @Test
    // handles splat parameter for all requests with root route present
    public void routeTest14() {
        Route r0 = newRoute("/");
        Route r1 = newRoute("/*");

        router.add(r0);
        router.add(r1);

        assertEquals(r0, route("/"));
        assertEquals(r1, route("/blah"));
    }

    @Test
    // handles splat parameters with a preceding resource
    public void routeTest15() {
        Route r1 = newRoute("/protected/*");
        Route r2 = newRoute("/protected/:id<[0-9]+>");
        Route r3 = newRoute("/:name<[a-z]+>");

        router.add(r1);
        router.add(r2);
        router.add(r3);

        assertEquals(r1, route("/protected/content"));
        assertEquals(r1, route("/protected/123"));
        assertEquals(r3, route("/john"));
        assertEquals(r1, route("/protected/"));
    }

    @Test
    // handles splat parameters interjected between resources
    public void routeTest16() {
        Route r1 = newRoute("/protected/*/content");
        Route r2 = newRoute("/protected/user/content");

        router.add(r1);
        router.add(r2);

        assertNull(route("/hello"));
        assertEquals(r1, route("/protected/1/content"));
        assertEquals(r1, route("/protected/blah/content"));
        assertNull(route("/protected/blah/content/"));
        assertNull(route("/protected/1/blah/content"));
        assertEquals(r1, route("/protected/user/content"));
    }

    @Test
    // handles paths with splat parameters occurring multiple times
    public void routeTest17() {
        Route r1 = newRoute("/say/*/to/*");

        router.add(r1);

        assertNull(route("/hello"));
        assertEquals(r1, route("/say/hello/to/world"));
        assertEquals(r1, route("/say/bye/to/Tim"));
        assertNull(route("/say/bye/bye/to/Tim"));
        assertEquals(r1, route("/say/bye/to/John/Doe"));
        assertNull(route("/say/hello/to"));
        assertEquals(r1, route("/say/hello/to/"));
    }

    @Test
    // handles splat path params that are part of paths with various path params
    public void routeTest18() {
        Route r1 = newRoute("/say/*/to/:name/:times<[0-9]+>/*");

        router.add(r1);

        assertNull(route("/hello"));
        assertNull(route("/say/hello/to/John"));
        assertNull(route("/say/hello/to/John/1"));
        assertEquals(r1, route("/say/hello/to/John/1/"));
        assertEquals(r1, route("/say/hello/to/Tim/1/time"));
        assertEquals(r1, route("/say/hello/to/Tim/1/time/thanks"));
    }

    @Test
    // handles paths containing regex symbols
    public void routeTest19() {
        Route r = newRoute("/hello$.html");
        router.add(r);
        assertEquals(r, route("/hello$.html"));
    }

    @Test
    // allows using unicode
    public void routeTest20() {
        Route r = newRoute("/föö");
        router.add(r);
        assertEquals(r, route("/f%C3%B6%C3%B6"));
    }

    @Test
    // handles encoded '/' correctly
    public void routeTest21() {
        Route r = newRoute("/foo/bar");
        router.add(r);
        assertNull(route("/foo%2Fbar"));
    }

    @Test
    // handles encoded '/' correctly with named params
    public void routeTest22() {
        Route r = newRoute("/:test");
        router.add(r);
        assertEquals(r, route("/foo%2Fbar"));
    }

    @Test
    // handles encoded '/' correctly with splat
    public void routeTest22b() {
        Route r = newRoute("/*");
        router.add(r);
        assertEquals(r, route("/foo%2Fbar"));
    }

    @Test
    // handles encoded '/' correctly with named params if it is interjected between resources
    public void routeTest22c() {
        Route r = newRoute("/hello/:test/there");
        router.add(r);
        assertEquals(r, route("/hello/foo%2Fbar/there"));
    }

    @Test
    // handles encoded '/' correctly with splat if it is interjected between resources
    public void routeTest22d() {
        Route r = newRoute("/hello/*/there");
        router.add(r);
        assertEquals(r, route("/hello/foo%2Fbar/there"));
    }

    @Test
    // literally matches '+' in path
    public void routeTest23() {
        Route r = newRoute("/foo+bar");
        router.add(r);
        assertEquals(r, route("/foo%2Bbar"));
    }

    @Test
    // literally matches '$' in path
    public void routeTest24() {
        Route r = newRoute("/test$/");
        router.add(r);
        assertEquals(r, route("/test$/"));
    }

    @Test
    // literally matches '.' in path
    public void routeTest25() {
        Route r = newRoute("/test.bar");
        router.add(r);
        assertEquals(r, route("/test.bar"));
    }

    @Test
    // matches paths that include spaces encoded with '%20'
    public void routeTest26() {
        Route r = newRoute("/path with spaces");
        router.add(r);
        assertEquals(r, route("/path%20with%20spaces"));
    }

    @Test
    // matches paths that include spaces encoded with '+'
    public void routeTest27() {
        Route r = newRoute("/path with spaces");
        router.add(r);
        assertEquals(r, route("/path+with+spaces"));
    }

    @Test
    // matches paths that include ampersands
    public void routeTest28() {
        Route r = newRoute("/:name");
        router.add(r);
        assertEquals(r, route("/foo&bar"));
    }

    @Test
    // matches a dot as part of a named param
    public void routeTest29() {
        Route r = newRoute("/:foo/:bar");
        router.add(r);
        assertEquals(r, route("/user@example.com/name"));
    }

    @Test
    // literally matches parens in path
    public void routeTest30() {
        Route r = newRoute("/test(bar)/");
        router.add(r);
        assertEquals(r, route("/test(bar)/"));
    }

    @Test
    // matches paths that end with '/' that occur within route paths
    public void routeTest31() {
        Route r1 = newRoute("/hello");
        Route r2 = newRoute("/hello/");
        Route r3 = newRoute("/hello/world");

        router.add(r1);
        router.add(r2);
        router.add(r3);

        assertEquals(r1, route("/hello"));
        assertEquals(r2, route("/hello/"));
        assertEquals(r3, route("/hello/world"));
    }
}
