package com.bullhorn.dataloader.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Test;

import com.bullhorn.dataloader.domain.Opportunity;
import com.bullhorn.dataloader.service.util.ImportServiceUtils;
import com.bullhorn.dataloader.util.BullhornAPI;

public class OpportunityImportServiceTest {

    @Test
    public void testRun() throws Exception {
        // setup
        ImportServiceUtils importServiceUtils = new ImportServiceUtils();
        mockOpportunity(importServiceUtils);
        OpportunityImportService opportunityImportService = new OpportunityImportService();
        importServiceUtils.injectFakeDependencies(opportunityImportService);
        opportunityImportService.setObj(getOpportunity());

        // execution
        opportunityImportService.opportunity();

        // verification
        verifyOpportunity(importServiceUtils);

    }

    private void verifyOpportunity(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        verify(bullhornAPI).save(any(Opportunity.class), eq("url"), eq("put"));
    }

    private Opportunity getOpportunity() {
        return new Opportunity();
    }

    private void mockOpportunity(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        when(bullhornAPI.getPostURL(any(Opportunity.class)))
            .thenReturn(new String[] {"put", "url"});

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("changedEntityId", 42);

        when(bullhornAPI.save(any(Opportunity.class), eq("url"), eq("put")))
            .thenReturn(jsonObject);
    }
}