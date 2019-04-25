package com.bullhorn.dataloader.rest;


import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;

/**
 * Dependency Injected Wrapper for obtaining the SDK-REST BullhornData class using DataLoader's properties Contains all
 * logic surrounding the creation of a REST Connection and returning the RestApi object. Creates a single instance of
 * the RestApi, so that the cost of authenticating is only paid once per session.
 */
public class RestSession {

    private final RestApiExtension restApiExtension;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;
    private RestApi restApi = null;

    public RestSession(RestApiExtension restApiExtension, PropertyFileUtil propertyFileUtil, PrintUtil printUtil) {
        this.restApiExtension = restApiExtension;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
    }

    /**
     * Authenticates and creates the Bullhorn REST API Session
     *
     * @return A new RestApi object
     */
    public RestApi getRestApi() {
        if (restApi == null) {
            restApi = new RestApi(createRestSession(), restApiExtension, propertyFileUtil, printUtil);
        }
        return restApi;
    }

    /**
     * Creates the rest session - the StandardBullhornData constructor performs the REST authentication.
     *
     * @return a BullhornData object that is used to make rest calls.
     */
    private StandardBullhornData createRestSession() {
        BullhornRestCredentials bullhornRestCredentials = getBullhornRestCredentials(propertyFileUtil);
        return new StandardBullhornData(bullhornRestCredentials);
    }

    /**
     * Returns the authentication object based on the DataLoader properties
     *
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
