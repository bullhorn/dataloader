package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.util.StringConsts;

/**
 * Class that encapsulates the search criteria for looking up existing entities in the system.
 * TODO: Encapsulate all query/where clause building into this class, which will get passed to the RestApi object's
 * EntityList findEntity(entitySearch, fieldSet) call.
 */
class SearchCriteria {

    private static final String EXTERNAL_ID_START = StringConsts.EXTERNAL_ID + ":\"";
    private static final String EXTERNAL_ID_END = "\"";

    /**
     * Convenience method that extracts the externalID lookup if it exists.
     *
     * TODO: Remove the need for this once SearchCriteria is used to encapsulate the search criteria
     *
     * @return empty string if it does not exist or is blank
     */
    static String getExternalIdValue(String searchString) {
        String externalID = "";
        if (searchString.contains(EXTERNAL_ID_START)) {
            externalID = searchString.substring(searchString.indexOf(EXTERNAL_ID_START) + EXTERNAL_ID_START.length());
            externalID = externalID.substring(0, externalID.indexOf(EXTERNAL_ID_END));
        }
        return externalID;
    }
}
