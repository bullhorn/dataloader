package com.bullhorn.dataloader.rest;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

public class RestApiTest {
    private BullhornRestCredentials bullhornRestCredentials;
    private RestApiExtension restApiExtensionMock;

    @Before
    public void setup() {
        bullhornRestCredentials = new BullhornRestCredentials();
        restApiExtensionMock = Mockito.mock(RestApiExtension.class);
    }

    @Test
    public void testConstructor_NoCredentials() {
        RestApiException expectedException = new RestApiException("Failed to create rest session");
        RestApiException actualException = null;

        try {
            new RestApi(bullhornRestCredentials, restApiExtensionMock);
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testDeleteEntity() {
        RestApi restApiPartialMock = Mockito.mock(RestApi.class);
        Whitebox.setInternalState(restApiPartialMock, "restApiExtension", restApiExtensionMock);
        Mockito.when(restApiPartialMock.deleteEntity(Mockito.any(), Mockito.any())).thenCallRealMethod();

        restApiPartialMock.deleteEntity(Candidate.class, 1);

        Mockito.verify(restApiPartialMock, Mockito.times(1)).deleteEntity(Mockito.eq(Candidate.class), Mockito.eq(1));
        Mockito.verify(restApiExtensionMock, Mockito.times(1)).postDelete(Mockito.eq(restApiPartialMock), Mockito.any());
    }
}
