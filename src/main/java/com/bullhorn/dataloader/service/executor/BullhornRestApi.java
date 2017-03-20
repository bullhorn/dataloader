package com.bullhorn.dataloader.service.executor;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;

/**
 * Extension of the standard SDK-REST BullhornData class for interacting with Bullhorn's REST API
 * <p>
 * Contains any extra REST behavior needed by DataLoader that is outside of SDK-REST.
 */
public class BullhornRestApi extends StandardBullhornData {

    public BullhornRestApi(BullhornRestCredentials bullhornRestCredentials) {
        super(bullhornRestCredentials);
    }
}
