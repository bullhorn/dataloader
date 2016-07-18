package com.bullhorn.dataloader.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ArrayUtilTest {

    @Test
    public void testPrepend() throws IOException {
        //arrange
        String[] original = new String[] {"a", "b", "c"};

        //act
        String[] actual = ArrayUtil.prepend("x", original);

        //assert
        Assert.assertEquals(actual.length, 4);
        Assert.assertEquals(actual[0], "x");
        Assert.assertEquals(actual[1], "a");
        Assert.assertEquals(actual[2], "b");
        Assert.assertEquals(actual[3], "c");
        Assert.assertEquals(original.length, 3);
    }
}