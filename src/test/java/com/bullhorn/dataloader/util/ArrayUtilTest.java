package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ArrayUtilTest {

    @Test
    public void testPrepend() throws IOException {
        final String[] original = new String[]{"a", "b", "c"};
        final String[] actual = ArrayUtil.prepend("x", original);

        Assert.assertEquals(actual.length, 4);
        Assert.assertEquals(actual[0], "x");
        Assert.assertEquals(actual[1], "a");
        Assert.assertEquals(actual[2], "b");
        Assert.assertEquals(actual[3], "c");
        Assert.assertEquals(original.length, 3);
    }

    @Test
    public void testAppend() throws IOException {
        final String[] original = new String[]{"a", "b", "c"};
        final String[] actual = ArrayUtil.append("x", original);

        Assert.assertEquals(actual.length, 4);
        Assert.assertEquals(actual[0], "a");
        Assert.assertEquals(actual[1], "b");
        Assert.assertEquals(actual[2], "c");
        Assert.assertEquals(actual[3], "x");
        Assert.assertEquals(original.length, 3);
    }
}
