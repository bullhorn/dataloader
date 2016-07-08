package com.bullhorn.dataloader.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Global String Constants
 *
 * Only strings that are used in more than one class should go here. If a string constant is only used internal to a
 * class, that class should contain the string constant.
 */
public class StringConsts {
    public static final String UTF = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";
    public static final String BH_REST_TOKEN = "BhRestToken";
    public static final String END_BH_REST_TOKEN = "?BhRestToken=";
    public static final String AND_BH_REST_TOKEN = "&BhRestToken=";
    public static final String ENTITY_SLASH = "entity/";
    public static final String CHANGED_ENTITY_ID = "changedEntityId";
    public static final String ID = "id";
    public static final String PROPERTYFILE_ARG = "propertyfile";
    public static final String COUNT = "count";
    public static final String NAME = "name";
    public static final String DATA = "data";
    public static final String IS_DELETED = "isDeleted";
    public static final String SEARCH = "search/";
    public static final String QUERY = "query/";
    public static final String CANDIDATE = "candidate";
    public static final String PRIVATE_LABELS = "privateLabels";
    public static final String CATEGORY = "Category";
    public static final String STRING = "String";

    private static String timestamp = null;

    /**
     * Returns a timestamp that is set to the time when DataLoader was started. This allows for the same
     * timestamp to be used throughout the same session.
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

    private StringConsts() {
    }
}
