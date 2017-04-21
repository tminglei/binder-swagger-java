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


import java.util.List;

import com.github.tminglei.swagger.fake.ConstDataProvider;
import io.swagger.models.HttpMethod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luis Antunes
 */
public class TestRoute {

    private RouteImpl newRoute(String path) {
        return new RouteImpl(HttpMethod.GET, path, true, new ConstDataProvider(""));
    }

    @Test
    public void newRoute_NullPathThrowsException() {
        try {
            newRoute(null);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void equals_NotEqual() {
        Route r1 = newRoute("/");
        Route r2 = newRoute("/cntrl");

        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_NotEqual_WithController() {
        Route r1 = newRoute("/cntrl");
        Route r2 = newRoute("/cntrl2");

        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_NotEqual_WithControllerAndAction() {
        Route r1 = newRoute("/cntrl/actn");
        Route r2 = newRoute("/cntrl/actn2");

        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_NotEqual_WithControllerAndActionAndParams() {
        Route r1 = newRoute("/cntrl/actn/:id");
        Route r2 = newRoute("/cntrl/actn2/:id");

        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_Equal_Root() {
        Route r1 = newRoute("/");
        Route r2 = newRoute("/");

        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_Equal_WithController() {
        Route r1 = newRoute("/cntrl");
        Route r2 = newRoute("/cntrl");

        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_Equal_WithControllerAndAction() {
        Route r1 = newRoute("/cntrl/actn");
        Route r2 = newRoute("/cntrl/actn");

        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void equals_Equal_WithControllerAndActionAndParams() {
        Route r1 = newRoute("/cntrl/actn/:id");
        Route r2 = newRoute("/cntrl/actn/:id");

        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }

    @Test
    public void toString_WithController_NoAction_NoParamPath() {
        Route r = newRoute("/cntrl");
        assertEquals("/cntrl", r.getPath());
    }

    @Test
    public void toString_WithController_WithAction_NoParamPath() {
        Route r = newRoute("/cntrl/actn");
        assertEquals("/cntrl/actn", r.getPath());
    }

    @Test
    public void toString_WithController_WithAction_WithParamPath() {
        Route r = newRoute("/cntrl/actn/clients/:id");
        assertEquals("/cntrl/actn/clients/:id", r.getPath());
    }

    @Test
    public void toString_WithSplat() {
        RouteImpl r = newRoute("/*");
        assertEquals("/*", r.getPath());
    }

    @Test
    public void getNamedParameterElements_NoneExist() {
        RouteImpl r = newRoute("/actn");
        List<PathNamedParamElement> params = r.getNamedParameterElements();

        assertTrue(params.isEmpty());
    }

    @Test
    public void getNamedParameterElements_OneExistsWithAction() {
        RouteImpl r = newRoute("/actn/:id");

        List<PathNamedParamElement> params = r.getNamedParameterElements();
        assertEquals(1, params.size());

        PathNamedParamElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(1, elem.index());
        assertNull(elem.regex());
    }

    @Test
    public void getNamedParameterElements_OneExistsAlone() {
        RouteImpl r = newRoute("/:id");

        List<PathNamedParamElement> params = r.getNamedParameterElements();
        assertEquals(1, params.size());

        PathNamedParamElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(0, elem.index());
        assertNull(elem.regex());
    }

    @Test
    public void getNamedParameterElements_ManyExistAlone() {
        RouteImpl r = newRoute("/:id/:name");

        List<PathNamedParamElement> params = r.getNamedParameterElements();
        assertEquals(2, params.size());

        PathNamedParamElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(0, elem.index());
        assertNull(elem.regex());

        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(1, elem.index());
        assertNull(elem.regex());
    }

    @Test
    public void getNamedParameterElements_ManyExistWithControllerAndAction() {
        RouteImpl r = newRoute("/cntrl/actn/:id/:name");

        List<PathNamedParamElement> params = r.getNamedParameterElements();
        assertEquals(2, params.size());

        PathNamedParamElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(2, elem.index());
        assertNull(elem.regex());

        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
        assertNull(elem.regex());
    }

    @Test
    public void getNamedParameterElements_ManyExistWithRegexWithControllerAndAction() {
        RouteImpl r = newRoute("/cntrl/actn/:id<[0-9]+>/:name<[a-z]+>");

        List<PathNamedParamElement> params = r.getNamedParameterElements();
        assertEquals(2, params.size());

        PathNamedParamElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(2, elem.index());
        assertEquals("[0-9]+", elem.regex());

        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
        assertEquals("[a-z]+", elem.regex());
    }

    @Test
    public void getNamedParameterElements_OneExistsWithRegexWithSlashWithControllerAndAction() {
        RouteImpl r = newRoute("/cntrl/actn/:id<[^/]+>/:name<[a-z]+>");

        List<PathNamedParamElement> params = r.getNamedParameterElements();
        assertEquals(2, params.size());

        PathNamedParamElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(2, elem.index());
        assertEquals("[^/]+", elem.regex());

        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
        assertEquals("[a-z]+", elem.regex());
    }

    @Test
    public void getNamedParameter() {
        RouteImpl route = newRoute("/customer/:id");
        String path = "/customer/1";

        assertEquals("1", route.getNamedParameter("id", path));
    }

    @Test
    public void getNamedParameter_HandlesUnicodeCorrectly() {
        RouteImpl route = newRoute("/customer/:id");
        String path = "/customer/f%C3%B6%C3%B6";

        assertEquals("föö", route.getNamedParameter("id", path));
    }

    @Test
    public void getNamedParameter_HandlesEncodedSlashesCorrectly() {
        RouteImpl route = newRoute("/:test");
        String path = "/foo%2Fbar";

        assertEquals("foo/bar", route.getNamedParameter("test", path));
    }

    @Test
    public void getNamedParameter_HandlesEncodedSlashesCorrectlyWhenInterjected() {
        RouteImpl route = newRoute("/hello/:test/there");
        String path = "/hello/foo%2Fbar/there";

        assertEquals("foo/bar", route.getNamedParameter("test", path));
    }

    @Test
    public void getNamedParameter_WithMultipleParameters() {
        RouteImpl route = newRoute("/customer/:id/named/:name");
        String path = "/customer/1/named/John";

        assertEquals("1", route.getNamedParameter("id", path));
        assertEquals("John", route.getNamedParameter("name", path));
    }

    @Test
    public void getNamedParameter_NotFound() {
        RouteImpl route = newRoute("/customer/:id");
        String path = "/customer/1";

        assertNull(route.getNamedParameter("name", path));
    }

    @Test
    public void splat_NoWildcards() {
        RouteImpl route = newRoute("/");
        String path = "/";

        assertEquals(0, route.splat(path).length);
    }

    @Test
    public void getSplatParameterElements_NoneOccurring() {
        RouteImpl route = newRoute("/");

        List<PathSplatParamElement> params = route.getSplatParameterElements();
        assertEquals(0, params.size());
    }

    @Test
    public void getSplatParameterElements_GeneralWildcard() {
        RouteImpl route = newRoute("/*");

        List<PathSplatParamElement> params = route.getSplatParameterElements();
        assertEquals(1, params.size());

        PathSplatParamElement elem = params.get(0);
        assertEquals(0, elem.index());
    }

    @Test
    public void getSplatParameterElements_WithPrecedingResource() {
        RouteImpl route = newRoute("/protected/*");

        List<PathSplatParamElement> params = route.getSplatParameterElements();
        assertEquals(1, params.size());

        PathSplatParamElement elem = params.get(0);
        assertEquals(1, elem.index());
    }

    @Test
    public void getSplatParameterElements_InterjectedBetweenResources() {
        RouteImpl route = newRoute("/protected/*/content");

        List<PathSplatParamElement> params = route.getSplatParameterElements();
        assertEquals(1, params.size());

        PathSplatParamElement elem = params.get(0);
        assertEquals(1, elem.index());
    }

    @Test
    public void getSplatParameterElements_OccurringMultipleTimes() {
        RouteImpl route = newRoute("/say/*/to/*");

        List<PathSplatParamElement> params = route.getSplatParameterElements();
        assertEquals(2, params.size());

        PathSplatParamElement elem = params.get(0);
        assertEquals(1, elem.index());

        elem = params.get(1);
        assertEquals(3, elem.index());
    }

    @Test
    public void getStaticPathElements_NoneOccurring() {
        RouteImpl route = newRoute("/");

        List<PathStaticElement> elems = route.getStaticPathElements();
        assertEquals(0, elems.size());
    }

    @Test
    public void getStaticPathElements_OneOccurring() {
        RouteImpl route = newRoute("/cntrl");

        List<PathStaticElement> elems = route.getStaticPathElements();
        assertEquals(1, elems.size());

        PathStaticElement elem = elems.get(0);
        assertEquals("cntrl", elem.name());
        assertEquals(0, elem.index());
    }

    @Test
    public void getStaticPathElements_TwoOccurring() {
        RouteImpl route = newRoute("/cntrl/actn");

        List<PathStaticElement> elems = route.getStaticPathElements();
        assertEquals(2, elems.size());

        PathStaticElement elem = elems.get(0);
        assertEquals("cntrl", elem.name());
        assertEquals(0, elem.index());

        elem = elems.get(1);
        assertEquals("actn", elem.name());
        assertEquals(1, elem.index());
    }

    @Test
    public void getStaticPathElements_TwoOccurringWithParamElements() {
        RouteImpl route = newRoute("/cntrl/*/actn/:name");

        List<PathStaticElement> elems = route.getStaticPathElements();
        assertEquals(2, elems.size());

        PathStaticElement elem = elems.get(0);
        assertEquals("cntrl", elem.name());
        assertEquals(0, elem.index());

        elem = elems.get(1);
        assertEquals("actn", elem.name());
        assertEquals(2, elem.index());
    }

    @Test
    public void getPathElements_NoneOccurring() {
        RouteImpl route = newRoute("/");

        List<PathElement> elems = route.getPathElements();
        assertEquals(0, elems.size());
    }

    @Test
    public void getPathElements_MultipleOccurring() {
        RouteImpl route = newRoute("/say/*/to/:name/:times<[0-9]+>/*");

        List<PathElement> elems = route.getPathElements();
        assertEquals(6, elems.size());

        PathElement elem = elems.get(0);
        assertTrue(elem instanceof PathStaticElement);
        assertEquals("say", elem.name());
        assertEquals(0, elem.index());

        elem = elems.get(1);
        assertTrue(elem instanceof PathSplatParamElement);
        assertEquals(1, elem.index());

        elem = elems.get(2);
        assertTrue(elem instanceof PathStaticElement);
        assertEquals("to", elem.name());
        assertEquals(2, elem.index());

        elem = elems.get(3);
        assertTrue(elem instanceof PathNamedParamElement);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
        assertNull(((PathNamedParamElement)elem).regex());

        elem = elems.get(4);
        assertTrue(elem instanceof PathNamedParamElement);
        assertEquals("times", elem.name());
        assertEquals(4, elem.index());
        assertEquals("[0-9]+", ((PathNamedParamElement)elem).regex());

        elem = elems.get(5);
        assertTrue(elem instanceof PathSplatParamElement);
        assertEquals(5, elem.index());
    }

    @Test
    public void splat_GeneralWildcard() {
        RouteImpl route = newRoute("/*");

        String path = "/hello/there";
        assertEquals(1, route.splat(path).length);
        assertEquals("hello/there", route.splat(path)[0]);

        path = "/hello";
        assertEquals(1, route.splat(path).length);
        assertEquals("hello", route.splat(path)[0]);

        path = "/hello/";
        assertEquals(1, route.splat(path).length);
        assertEquals("hello/", route.splat(path)[0]);
    }

    @Test
    public void splat_HandlesEncodedSlashesCorrectly() {
        RouteImpl route = newRoute("/*");

        String path = "/foo%2Fbar";
        assertEquals(1, route.splat(path).length);
        assertEquals("foo/bar", route.splat(path)[0]);
    }

    @Test
    public void splat_HandlesEncodedSlashesCorrectlyWhenInterjected() {
        RouteImpl route = newRoute("/hello/*/there");

        String path = "/hello/foo%2Fbar/there";
        assertEquals(1, route.splat(path).length);
        assertEquals("foo/bar", route.splat(path)[0]);
    }

    @Test
    public void splat_HandlesUnicodeCorrectly() {
        RouteImpl route = newRoute("/*");

        String path = "/f%C3%B6%C3%B6";
        assertEquals(1, route.splat(path).length);
        assertEquals("föö", route.splat(path)[0]);
    }

    @Test
    public void splat_WithPrecedingResource() {
        RouteImpl route = newRoute("/protected/*");

        String path = "/protected/1";
        assertEquals(1, route.splat(path).length);
        assertEquals("1", route.splat(path)[0]);

        path = "/protected/1/2";
        assertEquals(1, route.splat(path).length);
        assertEquals("1/2", route.splat(path)[0]);
    }

    @Test
    public void splat_InterjectedBetweenResources() {
        RouteImpl route = newRoute("/protected/*/content");

        String path = "/protected/1/content";
        assertEquals(1, route.splat(path).length);
        assertEquals("1", route.splat(path)[0]);

        path = "/protected/blah/content";
        assertEquals(1, route.splat(path).length);
        assertEquals("blah", route.splat(path)[0]);
    }

    @Test
    public void splat_OccurringMultipleTimes() {
        RouteImpl route = newRoute("/say/*/to/*");

        String path = "/say/hello/to/world";
        assertEquals(2, route.splat(path).length);
        assertEquals("hello", route.splat(path)[0]);
        assertEquals("world", route.splat(path)[1]);

        path = "/say/bye/to/Tim";
        assertEquals(2, route.splat(path).length);
        assertEquals("bye", route.splat(path)[0]);
        assertEquals("Tim", route.splat(path)[1]);

        path = "/say/hello/to/John/Doe";
        assertEquals(2, route.splat(path).length);
        assertEquals("hello", route.splat(path)[0]);
        assertEquals("John/Doe", route.splat(path)[1]);
    }

    @Test
    public void splat_VariousPathParams() {
        RouteImpl route = newRoute("/say/*/to/:name/:times<[0-9]+>/*");

        String path = "/say/hello/to/Tim/1/time";
        assertEquals(2, route.splat(path).length);
        assertEquals("hello", route.splat(path)[0]);
        assertEquals("time", route.splat(path)[1]);
        List<PathNamedParamElement> params = route.getNamedParameterElements();
        assertEquals(2, params.size());
        PathNamedParamElement elem = params.get(0);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
        assertNull(elem.regex());
        elem = params.get(1);
        assertEquals("times", elem.name());
        assertEquals(4, elem.index());
        assertEquals("[0-9]+", elem.regex());
        assertEquals("Tim", route.getNamedParameter("name", path));
        assertEquals("1", route.getNamedParameter("times", path));

        path = "/say/hello/to/Tim/1/time/thanks";
        assertEquals(2, route.splat(path).length);
        assertEquals("hello", route.splat(path)[0]);
        assertEquals("time/thanks", route.splat(path)[1]);
        params = route.getNamedParameterElements();
        assertEquals(2, params.size());
        elem = params.get(0);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
        assertNull(elem.regex());
        elem = params.get(1);
        assertEquals("times", elem.name());
        assertEquals(4, elem.index());
        assertEquals("[0-9]+", elem.regex());
        assertEquals("Tim", route.getNamedParameter("name", path));
        assertEquals("1", route.getNamedParameter("times", path));
    }

    @Test
    public void splat_ReturnsEmptyStringWithEmptyTerminalSplat() {
        RouteImpl route = newRoute("/hello/*");

        String path = "/hello/";
        assertEquals(1, route.splat(path).length);
        assertEquals("", route.splat(path)[0]);
    }

    @Test
    public void urlDecodesNamedParametersAndSplats() {
        RouteImpl route = newRoute("/:foo/*");

        String path = "/hello%20world/how%20are%20you";

        assertEquals("hello world", route.getNamedParameter("foo", path));
        assertEquals(1, route.splat(path).length);
        assertEquals("how are you", route.splat(path)[0]);
    }

    @Test
    public void doesNotCovertPlusSignIntoSpaceAsTheValueOfANamedParam() {
        RouteImpl route = newRoute("/:test");

        String path = "/bob+ross";

        assertEquals("bob+ross", route.getNamedParameter("test", path));
    }

    @Test
    public void doesNotCovertPlusSignIntoSpaceAsTheValueOfASplatParam() {
        RouteImpl route = newRoute("/hello/*");

        String path = "/hello/bob+ross";

        assertEquals("bob+ross", route.splat(path)[0]);
    }

    @Test
    public void hasPathElementsReturnsTrueIfItDoes() {
        RouteImpl route = newRoute("/hello/*");
        assertTrue(route.hasPathElements());
    }

    @Test
    public void hasPathElementsReturnsTrueIfItDoesWithJustASplat() {
        RouteImpl route = newRoute("/*");
        assertTrue(route.hasPathElements());
    }

    @Test
    public void hasPathElementsReturnsTrueIfItDoesWithJustANamedParameter() {
        RouteImpl route = newRoute("/:named");
        assertTrue(route.hasPathElements());
    }

    @Test
    public void hasPathElementsReturnsFalseIfItDoesNot() {
        RouteImpl route = newRoute("/");
        assertFalse(route.hasPathElements());
    }

    @Test
    public void getSplatParameter() {
        RouteImpl route = newRoute("/customer/*");
        String path = "/customer/1";

        assertEquals("1", route.getSplatParameter(0, path));
    }

    @Test
    public void getSplatParameter_HandlesUnicodeCorrectly() {
        RouteImpl route = newRoute("/customer/*");
        String path = "/customer/f%C3%B6%C3%B6";

        assertEquals("föö", route.getSplatParameter(0, path));
    }

    @Test
    public void getSplatParameter_HandlesEncodedSlashesCorrectly() {
        RouteImpl route = newRoute("/*");
        String path = "/foo%2Fbar";

        assertEquals("foo/bar", route.getSplatParameter(0, path));
    }

    @Test
    public void getSplatParameter_HandlesEncodedSlashesCorrectlyWhenInterjected() {
        RouteImpl route = newRoute("/hello/*/there");
        String path = "/hello/foo%2Fbar/there";

        assertEquals("foo/bar", route.getSplatParameter(0, path));
    }

    @Test
    public void getSplatParameter_WithMultipleParameters() {
        RouteImpl route = newRoute("/customer/*/named/*");
        String path = "/customer/1/named/John";

        assertEquals("1", route.getSplatParameter(0, path));
        assertEquals("John", route.getSplatParameter(1, path));
    }

    @Test
    public void getSplatParameter_NotFound() {
        RouteImpl route = newRoute("/customer/*");
        String path = "/customer/1";

        assertNull(route.getSplatParameter(1, path));
    }
}