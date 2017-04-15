package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by tminglei on 9/10/15.
 */
public class SwaggerScanTest {

    @Test
    public void testScanPackage() throws IOException, URISyntaxException {
        SwaggerFilter filter = new SwaggerFilter();
        List<String> classes = filter.scan(SwaggerUtils.class, "com.github.tminglei");
        assertTrue(classes.contains(SwaggerContext.class.getName())); //in folder
        assertTrue(classes.contains(Framework.class.getName()));      //in jar
        ///
        List<String> classes1 = filter.scan(SwaggerUtils.class, "com/github/tminglei/");
        assertTrue(classes1.contains(SwaggerContext.class.getName())); //in folder
        assertTrue(classes1.contains(Framework.class.getName()));      //in jar
    }

    @Test
    public void testScanClass() throws IOException, URISyntaxException {
        SwaggerFilter filter = new SwaggerFilter();
        List<String> classes = filter.scan(SwaggerUtils.class, "com.github.tminglei.swagger.SwaggerContext");
        assertEquals(classes.size(), 1);
        assertEquals(classes.get(0), SwaggerContext.class.getName());
        ///
        List<String> classes1 = filter.scan(SwaggerUtils.class, "/com/github/tminglei/swagger/SwaggerContext.class");
        assertEquals(classes1.size(), 1);
        assertEquals(classes1.get(0), SwaggerContext.class.getName());
    }
}
