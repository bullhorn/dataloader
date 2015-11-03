package com.bullhorn.dataloader.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Test;

import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.service.util.ImportServiceUtils;
import com.bullhorn.dataloader.util.BullhornAPI;

public class ClientCorporationImportServiceTest {

    @Test
    public void testClientCorporation() throws Exception {
        // setup
        ImportServiceUtils importServiceUtils = new ImportServiceUtils();
        mockNonAssociatedClientCorporation(importServiceUtils);
        ClientCorporationImportService clientCorporationImportService = new ClientCorporationImportService();
        importServiceUtils.injectFakeDependencies(clientCorporationImportService);
        clientCorporationImportService.setObj(getClientCorporation());

        // execution
        Integer clientCorporationID = clientCorporationImportService.clientCorporation();

        // verification
        assertEquals(clientCorporationID, Integer.valueOf(42));
        verifyNonAssociatedClientCorporation(importServiceUtils);
    }

    private void mockNonAssociatedClientCorporation(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        when(bullhornAPI.getPostURL(any(ClientCorporation.class)))
                .thenReturn(new String[]{"put", "url"});

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("changedEntityId", 42);

        when(bullhornAPI.save(any(ClientCorporation.class), eq("url"), eq("put")))
                .thenReturn(jsonObject);

    }

    private void verifyNonAssociatedClientCorporation(ImportServiceUtils importServiceUtils) {

    }

    private ClientCorporation getClientCorporation() {
        return new ClientCorporation();
    }

}