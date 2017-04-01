package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class DateUtilTest {

    @Test
    public void testConstructor() throws IOException {
        DateUtil dateUtil = new DateUtil();
        Assert.assertNotNull(dateUtil);
    }

    @Test
    public void testGetTimestamp() throws IOException {
        final String originalTimestamp = DateUtil.getTimestamp();
        final String newTimestamp = DateUtil.getTimestamp();

        Assert.assertEquals(originalTimestamp, newTimestamp);
    }
}
