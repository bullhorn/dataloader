package com.bullhorn.dataloader.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Test;

import com.bullhorn.dataloader.domain.Candidate;
import com.bullhorn.dataloader.service.util.ImportServiceUtils;
import com.bullhorn.dataloader.util.BullhornAPI;

public class CandidateImportServiceTest {

    @Test
    public void testNonAssociatedCandidate() throws Exception {
        // setup
        ImportServiceUtils importServiceUtils = new ImportServiceUtils();
        mockNonAssociatedCandidates(importServiceUtils);
        CandidateImportService candidateImportService = new CandidateImportService();
        importServiceUtils.injectFakeDependencies(candidateImportService);
        candidateImportService.setObj(getCandidate());

        // execution
        candidateImportService.candidate();

        //verification
        verifyNonAssociatedCandidates(importServiceUtils);
    }

    private void mockNonAssociatedCandidates(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        when(bullhornAPI.getPostURL(any(Candidate.class)))
                .thenReturn(new String[]{"put", "url"});

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("changedEntityId", 42);

        when(bullhornAPI.save(any(Candidate.class), eq("url"), eq("put")))
                .thenReturn(jsonObject);
    }

    public Candidate getCandidate() {
        Candidate candidate = new Candidate();
        candidate.setLastName("lastName");
        return candidate;
    }


    private void verifyNonAssociatedCandidates(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        verify(bullhornAPI).save(any(Candidate.class), eq("url"), eq("put"));
    }
}