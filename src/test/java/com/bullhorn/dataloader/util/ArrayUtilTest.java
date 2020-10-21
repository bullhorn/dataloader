package com.bullhorn.dataloader.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ArrayUtilTest {

    @Test
    public void testConstructor() {
        ArrayUtil arrayUtil = new ArrayUtil();
        Assert.assertNotNull(arrayUtil);
    }

    @Test
    public void testPrepend() {
        String[] original = new String[]{"a", "b", "c"};
        String[] actual = ArrayUtil.prepend("x", original);

        Assert.assertEquals(actual.length, 4);
        Assert.assertEquals(actual[0], "x");
        Assert.assertEquals(actual[1], "a");
        Assert.assertEquals(actual[2], "b");
        Assert.assertEquals(actual[3], "c");
        Assert.assertEquals(original.length, 3);
    }

    @Test
    public void testAppend() {
        String[] original = new String[]{"a", "b", "c"};
        String[] actual = ArrayUtil.append(original, "x");

        Assert.assertEquals(actual.length, 4);
        Assert.assertEquals(actual[0], "a");
        Assert.assertEquals(actual[1], "b");
        Assert.assertEquals(actual[2], "c");
        Assert.assertEquals(actual[3], "x");
        Assert.assertEquals(original.length, 3);
    }

    @Test
    public void testGetMatchingStringIgnoreCase() {
        List<String> strings = Arrays.asList("name", "firstName", "lastName", "middleName", "last");

        Assert.assertEquals("name", ArrayUtil.getMatchingStringIgnoreCase(strings, "name"));
        Assert.assertEquals("name", ArrayUtil.getMatchingStringIgnoreCase(strings, "NAME"));
        Assert.assertNull(ArrayUtil.getMatchingStringIgnoreCase(strings, "nam"));
        Assert.assertNull(ArrayUtil.getMatchingStringIgnoreCase(strings, "first"));
    }
}
