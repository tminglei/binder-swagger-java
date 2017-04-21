package com.github.tminglei.swagger;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by minglei on 4/18/17.
 */
public class SimpleUtilsTest {

    @Test
    public void testIsEmpty() {
        assertEquals(true, SimpleUtils.isEmpty(null));
        assertEquals(true, SimpleUtils.isEmpty(""));
        assertEquals(true, SimpleUtils.isEmpty("   "));
        assertEquals(false, SimpleUtils.isEmpty("ab c"));
        assertEquals(false, SimpleUtils.isEmpty(new Object()));
    }

    @Test
    public void testNotEmpty() {
        assertEquals("abc", SimpleUtils.notEmpty("abc", null));

        Object object = new Object();
        assertEquals(object, SimpleUtils.notEmpty(object, null));

        try {
            assertEquals("  ", SimpleUtils.notEmpty("  ", "value is null or empty"));
        } catch (Exception e) {
            assertEquals("value is null or empty", e.getMessage());
        }

        try {
            assertTrue(null == SimpleUtils.notEmpty(null, "value is null or empty"));
        } catch (Exception e) {
            assertEquals("value is null or empty", e.getMessage());
        }
    }
}
