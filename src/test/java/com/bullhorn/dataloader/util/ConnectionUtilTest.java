package com.bullhorn.dataloader.util;


import com.bullhorn.dataloader.rest.BullhornRestApiExtension;
import com.bullhornsdk.data.exception.RestApiException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ConnectionUtilTest {

    private BullhornRestApiExtension bullhornRestApiExtensionMock;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() {
        bullhornRestApiExtensionMock = Mockito.mock(BullhornRestApiExtension.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
    }

    @Test
    public void testConnect_NoCredentials() {
        RestApiException expectedException = new RestApiException("Failed to create rest session");
        RestApiException actualException = null;

        ConnectionUtil connectionUtil = new ConnectionUtil(bullhornRestApiExtensionMock, propertyFileUtilMock);
        try {
            connectionUtil.getSession();
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
