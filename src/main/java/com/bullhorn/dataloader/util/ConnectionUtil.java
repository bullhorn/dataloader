package com.bullhorn.dataloader.util;


import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhornsdk.data.api.BullhornRestCredentials;

/**
 * Dependency Injected Wrapper for obtaining the SDK-REST BullhornData class using DataLoader's properties
 * <p>
 * Contains all logic surrounding the creation of a REST Connection and returning the BullhornRestApi object.
 * Creates a single instance of the BullhornRestApi, so that the cost of authenticating is only paid once per session.
 */
public class ConnectionUtil {

    final private PropertyFileUtil propertyFileUtil;
    private BullhornRestApi bullhornRestApi = null;

    public ConnectionUtil(PropertyFileUtil propertyFileUtil) {
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Authenticates and creates the Bullhorn REST API Session
     * @return A new BullhornRestApi object
     */
    public BullhornRestApi getSession() {
        if (bullhornRestApi == null) {
            bullhornRestApi = new BullhornRestApi(getBullhornRestCredentials(propertyFileUtil));
        }
        return bullhornRestApi;
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
