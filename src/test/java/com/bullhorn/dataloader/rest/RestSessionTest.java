package com.bullhorn.dataloader.rest;


import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestSessionTest {

    private RestApi restApiMock;
    private RestApiExtension restApiExtensionMock;
    private PropertyFileUtil propertyFileUtilMock;
    private PrintUtil printUtilMock;

    @Before
    public void setup() {
        restApiMock = mock(RestApi.class);
        restApiExtensionMock = mock(RestApiExtension.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        printUtilMock = mock(PrintUtil.class);
    }

    @Test
    public void testConnect_NoCredentials() {
        RestApiException expectedException = new RestApiException("Failed to create rest session");
        RestApiException actualException = null;

        RestSession restSession = new RestSession(restApiExtensionMock, propertyFileUtilMock, printUtilMock);
        try {
            restSession.getRestApi();
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testConnectExistingSession() {
        RestSession restSessionPartialMock = mock(RestSession.class);
        Whitebox.setInternalState(restSessionPartialMock, "restApi", restApiMock);
        when(restSessionPartialMock.getRestApi()).thenCallRealMethod();

        RestApi restApi = restSessionPartialMock.getRestApi();

        Assert.assertEquals(restApi, restApiMock);
    }
}
