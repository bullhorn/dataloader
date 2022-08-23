package com.bullhorn.dataloader.rest;

import java.util.Map;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;

/**
 * Overrides parts of the StandardBullhornData (Rest API interface) needed for Data Loader.
 */
public class CustomBullhornData extends StandardBullhornData {

    public CustomBullhornData(BullhornRestCredentials bullhornRestCredentials) {
        super(bullhornRestCredentials);
    }

    /***
     * Makes the otherwise protected post request public for use in lookup by external ID
     */
    public <T> T performPostRequestPublic(String url, Object requestPayLoad, Class<T> returnType, Map<String, String> uriVariables) {
        return performPostRequest(url, requestPayLoad, returnType, uriVariables);
    }
}
