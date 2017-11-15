package com.bullhorn.dataloader.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility for low level date related methods used in DataLoader
 */
public class DateUtil {

    private static String timestamp = null;

    /**
     * Returns a timestamp that is set to the time when DataLoader was started. This allows for the same timestamp to be
     * used throughout the same session.
     *
     * @return The timestamp string
     */
    public static String getTimestamp() {
        if (timestamp == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
            timestamp = dateFormat.format(new Date());
        }
        return timestamp;
    }
}
