package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class TimerTest {

    @Test
    public void testGetDurationMillis() throws InterruptedException {
        Timer timer = new Timer();
        Thread.sleep(1); // Create a 1 msec wait
        long durationMillis = timer.getDurationMillis();
        Assert.assertTrue(durationMillis > 0);
    }

    @Test
    public void testToString() throws InterruptedException {
        Timer timer = new Timer();
        Thread.sleep(1); // Create a 1 msec wait
        String durationString = timer.getDurationStringSec();
        Assert.assertEquals("0.0 sec", durationString);
    }
}
