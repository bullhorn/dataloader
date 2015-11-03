package com.bullhorn.dataloader.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Test;

import com.bullhorn.dataloader.domain.Lead;
import com.bullhorn.dataloader.service.util.ImportServiceUtils;
import com.bullhorn.dataloader.util.BullhornAPI;

public class LeadImportServiceTest {

    @Test
    public void testRun() throws Exception {
        // setup
        ImportServiceUtils importServiceUtils = new ImportServiceUtils();
        mockNonAssociatedLead(importServiceUtils);
        LeadImportService leadImportService = new LeadImportService();
        importServiceUtils.injectFakeDependencies(leadImportService);
        leadImportService.setObj(getLead());

        // execution
        leadImportService.lead();

        // verification
        verifyNonAssociatedLead(importServiceUtils);
    }

    public Lead getLead() {
        return new Lead();
    }

    private void mockNonAssociatedLead(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        when(bullhornAPI.getPostURL(any(Lead.class)))
                .thenReturn(new String[]{"put", "url"});

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("changedEntityId", 42);

        when(bullhornAPI.save(any(Lead.class), eq("url"), eq("put")))
                .thenReturn(jsonObject);
    }

    private void verifyNonAssociatedLead(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        verify(bullhornAPI).save(any(Lead.class), eq("url"), eq("put"));
    }
}