package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class TimerTest {

    @Test
    public void testGetDurationMillis() throws InterruptedException {
        final Timer timer = new Timer();
        Thread.sleep(1); // Create a 1 msec wait
        final long durationMillis = timer.getDurationMillis();

        Assert.assertTrue(durationMillis > 0);
    }

    @Test
    public void testToString() throws InterruptedException {
        final Timer timer = new Timer();
        Thread.sleep(1); // Create a 1 msec wait
        final String durationString = timer.getDurationStringSec();

        Assert.assertEquals("0.0 sec", durationString);
    }
}