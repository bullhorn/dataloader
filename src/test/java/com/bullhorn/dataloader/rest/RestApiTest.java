package com.bullhorn.dataloader.rest;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestApiTest {
    private BullhornRestCredentials bullhornRestCredentials;
    private RestApiExtension restApiExtensionMock;

    @Before
    public void setup() {
        bullhornRestCredentials = new BullhornRestCredentials();
        restApiExtensionMock = mock(RestApiExtension.class);
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
        RestApi restApiPartialMock = mock(RestApi.class);
        Whitebox.setInternalState(restApiPartialMock, "restApiExtension", restApiExtensionMock);
        when(restApiPartialMock.deleteEntity(any(), any())).thenCallRealMethod();

        restApiPartialMock.deleteEntity(Candidate.class, 1);

        verify(restApiPartialMock, times(1)).deleteEntity(eq(Candidate.class), eq(1));
        verify(restApiExtensionMock, times(1)).postDelete(eq(restApiPartialMock), any());
    }
}
