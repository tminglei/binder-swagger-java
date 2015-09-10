package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by tminglei on 9/10/15.
 */
public class SwaggerUtilsTest {

    @Test
    public void testScanPackage() throws IOException, URISyntaxException {
        List<String> classes = SwaggerUtils.scan(SwaggerUtils.class, "com.github.tminglei");
        Assert.assertTrue(classes.contains(SwaggerContext.class.getName())); //in folder
        Assert.assertTrue(classes.contains(Framework.class.getName()));      //in jar
        ///
        List<String> classes1 = SwaggerUtils.scan(SwaggerUtils.class, "com/github/tminglei/");
        Assert.assertTrue(classes1.contains(SwaggerContext.class.getName())); //in folder
        Assert.assertTrue(classes1.contains(Framework.class.getName()));      //in jar
    }

    @Test
    public void testScanClass() throws IOException, URISyntaxException {
        List<String> classes = SwaggerUtils.scan(SwaggerUtils.class, "com.github.tminglei.swagger.SwaggerContext");
        Assert.assertEquals(classes.size(), 1);
        Assert.assertEquals(classes.get(0), SwaggerContext.class.getName());
        ///
        List<String> classes1 = SwaggerUtils.scan(SwaggerUtils.class, "/com/github/tminglei/swagger/SwaggerContext.class");
        Assert.assertEquals(classes1.size(), 1);
        Assert.assertEquals(classes1.get(0), SwaggerContext.class.getName());
    }
}
