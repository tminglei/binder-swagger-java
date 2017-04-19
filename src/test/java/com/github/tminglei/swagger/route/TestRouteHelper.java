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

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Luis Antunes
 */
public class TestRouteHelper {

    @Test
    // returns an empty string if given a path with just '/'
    public void getPathElementsTest1() {
        String[] expected = new String[]{""};
        String[] actual = RouteHelper.getPathElements("/");
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    // returns the correct strings if given a path with multiple static elements and a named parameter
    public void getPathElementsTest2() {
        String[] expected = new String[]{"cntrl","actn","clients",":id"};
        String[] actual = RouteHelper.getPathElements("/cntrl/actn/clients/:id");
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    // returns the correct strings even if the path does not start with '/'
    public void getPathElementsTest2b() {
        String[] expected = new String[]{"cntrl","actn","clients",":id"};
        String[] actual = RouteHelper.getPathElements("cntrl/actn/clients/:id");
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    // returns the correct strings if given a path with a single static element and a named parameter
    public void getPathElementsTest3() {
        String[] expected = new String[]{"clients",":id"};
        String[] actual = RouteHelper.getPathElements("/clients/:id");
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    // returns the correct strings if given a path with a single static element and a named parameter with custom regex
    public void getPathElementsTest4() {
        String[] expected = new String[]{"clients",":id<[0-9]+>"};
        String[] actual = RouteHelper.getPathElements("/clients/:id<[0-9]+>");
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    // escapes non-custom regex
    public void escapeNonCustomRegex() {
        String path = "/cntrl/[](){}*^?$.\\/a+b/:id<[^/]+>/:name<[a-z]+>";
        String expected = "/cntrl/\\[\\]\\(\\)\\{\\}\\*\\^\\?\\$\\.\\\\/a\\+b/:id<[^/]+>/:name<[a-z]+>";
        String actual = RouteHelper.escapeNonCustomRegex(path);
        assertEquals(expected, actual);
    }

    @Test
    // url decodes for routing correctly
    public void urlDecodeForRoutingTest() {
        String path = "/hello";
        assertEquals("/hello", RouteHelper.urlDecodeForRouting(path));

        path = "/hello/foo%2Fbar/there";
        assertEquals("/hello/foo%2fbar/there", RouteHelper.urlDecodeForRouting(path));

        path = "/hello/foo%2fbar/there";
        assertEquals("/hello/foo%2fbar/there", RouteHelper.urlDecodeForRouting(path));

        path = "/hello/foo%2Fbar/there/foo%2Bbar/a+space";
        assertEquals("/hello/foo%2fbar/there/foo+bar/a space", RouteHelper.urlDecodeForRouting(path));
    }

    @Test
    // url decodes for path params correctly
    public void urlDecodeForPathParamsTest() {
        String param = "hello";
        assertEquals("hello", RouteHelper.urlDecodeForPathParams(param));

        param = "foo%2Fbar";
        assertEquals("foo/bar", RouteHelper.urlDecodeForPathParams(param));

        param = "foo+bar";
        assertEquals("foo+bar", RouteHelper.urlDecodeForPathParams(param));

        param = "foo%2Bbar";
        assertEquals("foo+bar", RouteHelper.urlDecodeForPathParams(param));

        param = "foo%20bar";
        assertEquals("foo bar", RouteHelper.urlDecodeForPathParams(param));
    }

}
