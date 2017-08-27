package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ArrayUtilTest {

    @Test
    public void testConstructor() throws IOException {
        ArrayUtil arrayUtil = new ArrayUtil();
        Assert.assertNotNull(arrayUtil);
    }

    @Test
    public void testPrepend() throws IOException {
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
    public void testAppend() throws IOException {
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
    public void testGetMatchingStringIgnoreCase() throws IOException {
        List<String> strings = Arrays.asList("name", "firstName", "lastName", "middleName", "last");

        Assert.assertEquals("name", ArrayUtil.getMatchingStringIgnoreCase(strings, "name"));
        Assert.assertEquals("name", ArrayUtil.getMatchingStringIgnoreCase(strings, "NAME"));
        Assert.assertEquals(null, ArrayUtil.getMatchingStringIgnoreCase(strings, "nam"));
        Assert.assertEquals(null, ArrayUtil.getMatchingStringIgnoreCase(strings, "first"));
    }
}
