package com.bullhorn.dataloader.service.executor;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.exception.RestApiException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BullhornRestApiTest {
    private BullhornRestCredentials bullhornRestCredentials;

    @Before
    public void setup() {
        bullhornRestCredentials = new BullhornRestCredentials();
    }

    @Test
    public void testConstructor_NoCredentials() {
        RestApiException expectedException = new RestApiException("Failed to create rest session");
        RestApiException actualException = null;

        try {
            new BullhornRestApi(bullhornRestCredentials);
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
