package com.bullhorn.dataloader.service.executor;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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

    @Test
    public void testDeleteEntity() {
        BullhornRestApi bullhornRestApiPartialMock = Mockito.mock(BullhornRestApi.class);
        Mockito.when(bullhornRestApiPartialMock.deleteEntity(Mockito.any(), Mockito.any())).thenCallRealMethod();

        bullhornRestApiPartialMock.deleteEntity(Candidate.class, 1);

        Mockito.verify(bullhornRestApiPartialMock, Mockito.times(1)).deleteEntity(Mockito.eq(Candidate.class), Mockito.eq(1));
        // TODO: Allow for testing that postDelete was called on the mock RestApiExtension
        // Mockito.verify(bullhornRestApiExtensionMock, Mockito.times(1)).postDelete(bullhornRestApiPartialMock, Mockito.any());
    }
}
