package com.bullhorn.dataloader.util;

import java.util.concurrent.TimeUnit;

/**
 * Simple timer object used for performance analysis of the DataLoader.
 */
public class Timer {
    private long startTime;

    public long getStartTime() {
        return startTime;
    }

    /**
     * Initializes the timer to the point when the timer is created.
     */
    public Timer() {
        start();
    }

    /**
     * Gets the duration that the timer has been in existence.
     *
     * @return The number of milliseconds since this timer's creation
     */
    public long getDurationMillis() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Returns a formatted string with the current duration
     *
     * @return The string that represents the duration in HH:MM:SS format.
     */
    public String getDurationStringHms() {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(getDurationMillis()),
            TimeUnit.MILLISECONDS.toMinutes(getDurationMillis()) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(getDurationMillis()) % TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * Returns a formatted string with the current duration
     *
     * @return The string that represents the duration in seconds.
     */
    public String getDurationStringSec() {
        return String.format("%.1f", (double) (getDurationMillis()) / 1000) + " sec";
    }

    /**
     * Sets the start of the timer interval
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }
}
