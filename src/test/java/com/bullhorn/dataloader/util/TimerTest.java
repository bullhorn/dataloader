package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class TimerTest {

    @Test
    public void testGetDurationStringHMS() throws InterruptedException {
        final Timer timer = new Timer();
        final String durationString = timer.getDurationStringHMS();

        Assert.assertEquals("00:00:00", durationString);
    }

    @Test
    public void testGetDurationStringSec() throws InterruptedException {
        final Timer timer = new Timer();
        final String durationString = timer.getDurationStringSec();

        Assert.assertEquals("0.0 sec", durationString);
    }
}
