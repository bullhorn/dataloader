package com.bullhorn.dataloader.util;


import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhornsdk.data.api.BullhornRestCredentials;

/**
 * Dependency Injected Wrapper for obtaining the SDK-REST BullhornData class using DataLoader's properties
 * <p>
 * Contains all logic surrounding the creation of a REST Connection and returning the BullhornRestApi object.
 */
public class ConnectionUtil {

    final protected PropertyFileUtil propertyFileUtil;

    public ConnectionUtil(PropertyFileUtil propertyFileUtil) {
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Authenticates and creates the Bullhorn REST API Session
     * @return A new BullhornRestApi object
     */
    public BullhornRestApi connect() {
        return new BullhornRestApi(getBullhornRestCredentials(propertyFileUtil));
    }

    /**
     * Returns the authentication object based on the DataLoader properties
     * @param propertyFileUtil The dataloader properties
     * @return The credentials object
     */
    private BullhornRestCredentials getBullhornRestCredentials(PropertyFileUtil propertyFileUtil) {
        BullhornRestCredentials bullhornRestCredentials = new BullhornRestCredentials();
        bullhornRestCredentials.setPassword(propertyFileUtil.getPassword());
        bullhornRestCredentials.setRestAuthorizeUrl(propertyFileUtil.getAuthorizeUrl());
        bullhornRestCredentials.setRestClientId(propertyFileUtil.getClientId());
        bullhornRestCredentials.setRestClientSecret(propertyFileUtil.getClientSecret());
        bullhornRestCredentials.setRestLoginUrl(propertyFileUtil.getLoginUrl());
        bullhornRestCredentials.setRestTokenUrl(propertyFileUtil.getTokenUrl());
        bullhornRestCredentials.setUsername(propertyFileUtil.getUsername());
        bullhornRestCredentials.setRestSessionMinutesToLive("60");
        return bullhornRestCredentials;
    }
}
