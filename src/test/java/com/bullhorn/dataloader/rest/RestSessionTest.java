package com.bullhorn.dataloader.rest;


import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RestSessionTest {

    private RestApiExtension restApiExtensionMock;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() {
        restApiExtensionMock = Mockito.mock(RestApiExtension.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
    }

    @Test
    public void testConnect_NoCredentials() {
        RestApiException expectedException = new RestApiException("Failed to create rest session");
        RestApiException actualException = null;

        RestSession restSession = new RestSession(restApiExtensionMock, propertyFileUtilMock);
        try {
            restSession.getRestApi();
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
