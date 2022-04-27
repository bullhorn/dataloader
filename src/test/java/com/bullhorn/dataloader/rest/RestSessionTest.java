package com.bullhorn.dataloader.rest;


import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;

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
    public void testConnectExistingSession() throws NoSuchFieldException, IllegalAccessException {
        RestSession restSession = new RestSession(restApiExtensionMock, propertyFileUtilMock, printUtilMock);
        Field privateField = restSession.getClass().getDeclaredField("restApi");
        privateField.setAccessible(true);
        privateField.set(restSession, restApiMock);

        RestApi restApi = restSession.getRestApi();

        Assert.assertEquals(restApi, restApiMock);
    }
}
