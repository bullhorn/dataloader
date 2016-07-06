package com.bullhorn.dataloader.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class StringConstsTest {

    @Test
    public void testGetTimestamp() throws IOException {
        //arrange

        //act
        String originalTimestamp = StringConsts.getTimestamp();
        String newTimestamp = StringConsts.getTimestamp();

        //assert
        Assert.assertEquals(originalTimestamp, newTimestamp);
    }
}
