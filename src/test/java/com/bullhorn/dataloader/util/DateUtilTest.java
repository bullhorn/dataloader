package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilTest {

    @Test
    public void testConstructor() {
        DateUtil dateUtil = new DateUtil();
        Assert.assertNotNull(dateUtil);
    }

    @Test
    public void testGetTimestamp() {
        final String originalTimestamp = DateUtil.getTimestamp();
        final String newTimestamp = DateUtil.getTimestamp();

        Assert.assertEquals(originalTimestamp, newTimestamp);
    }
}
