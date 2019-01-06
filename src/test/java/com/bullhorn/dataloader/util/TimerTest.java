package com.bullhorn.dataloader.util;

import org.junit.Assert;
import org.junit.Test;

public class TimerTest {

    @Test
    public void testGetStartTime() {
        Timer timer = new Timer();
        Assert.assertNotEquals(0, timer.getStartTime());
    }

    @Test
    public void testGetDurationStringHMS() {
        Timer timer = new Timer();
        String durationString = timer.getDurationStringHms();
        Assert.assertEquals("00:00:00", durationString);
    }

    @Test
    public void testGetDurationStringSec() {
        Timer timer = new Timer();
        String durationString = timer.getDurationStringSec();
        Assert.assertEquals("0.0 sec", durationString);
    }
}
