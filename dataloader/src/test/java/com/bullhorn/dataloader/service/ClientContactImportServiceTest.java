package com.bullhorn.dataloader.service;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Test;

import com.bullhorn.dataloader.domain.ClientContact;
import com.bullhorn.dataloader.service.util.ImportServiceUtils;
import com.bullhorn.dataloader.util.BullhornAPI;

public class ClientContactImportServiceTest {

    @Test
    public void testClientContact() throws Exception {
        // setup
        ImportServiceUtils importServiceUtils = new ImportServiceUtils();
        mockNonAssociatedClientContact(importServiceUtils);
        ClientContactImportService clientContactImportService = new ClientContactImportService();
        importServiceUtils.injectFakeDependencies(clientContactImportService);
        clientContactImportService.setObj(getClientContact());

        // execution
        Integer clientContactID = clientContactImportService.clientContact();

        // verification
        assertEquals(clientContactID, Integer.valueOf(42));
        verifyNonAssociatedClientContact(importServiceUtils);
    }

    private ClientContact getClientContact() {
        return new ClientContact();
    }

    private void mockNonAssociatedClientContact(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        when(bullhornAPI.getPostURL(any(ClientContact.class)))
                .thenReturn(new String[]{"put", "url"});

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("changedEntityId", 42);

        when(bullhornAPI.save(any(ClientContact.class), eq("url"), eq("put")))
                .thenReturn(jsonObject);
    }

    private void verifyNonAssociatedClientContact(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        verify(bullhornAPI).save(any(ClientContact.class), eq("url"), eq("put"));
    }

}