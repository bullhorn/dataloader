package com.bullhorn.dataloader.rest;


import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornRestCredentials;

/**
 * Dependency Injected Wrapper for obtaining the SDK-REST BullhornData class using DataLoader's properties
 * <p>
 * Contains all logic surrounding the creation of a REST Connection and returning the RestApi object.
 * Creates a single instance of the RestApi, so that the cost of authenticating is only paid once per session.
 */
public class RestSession {

    final private RestApiExtension restApiExtension;
    final private PropertyFileUtil propertyFileUtil;
    private RestApi restApi = null;

    public RestSession(RestApiExtension restApiExtension, PropertyFileUtil propertyFileUtil) {
        this.restApiExtension = restApiExtension;
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Authenticates and creates the Bullhorn REST API Session
     * @return A new RestApi object
     */
    public RestApi getRestApi() {
        if (restApi == null) {
            BullhornRestCredentials bullhornRestCredentials = getBullhornRestCredentials(propertyFileUtil);
            restApi = new RestApi(bullhornRestCredentials, restApiExtension);
        }
        return restApi;
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
