package com.bullhorn.dataloader.service;

import org.json.JSONObject;

import com.bullhorn.dataloader.domain.Candidate;
import com.bullhorn.dataloader.service.util.GUIDService;

class CandidateImportService extends AbstractEntityImportService {

    public void run() {
        try {
            candidate();
       } catch (Exception e) {
            log.error(e);
        }
    }

    void candidate() throws Exception {
        Candidate candidate = (Candidate) obj;

        // Check if record exists in BH and get postURL
        String[] postInfo = bhapi.getPostURL(candidate);
        String type = postInfo[0];
        String postURL = postInfo[1];

        // Set username/password properties (which will not be defined in the CSV)
        if (type.equalsIgnoreCase("put")) {
            GUIDService.inflateUser(candidate);
        }
        candidate.setIsDeleted("false");
        candidate.setIsEditable("true");

        //Save
        JSONObject jsResp = bhapi.save(candidate, postURL, type);

        // Get ID of the created/updated record
        int candidateID = jsResp.getInt("changedEntityId");

        // If it's an insert and you need to add associations, do it now
        // Instantiate new master data service as association functions are encapsulated in that service
        // Remember to pass it a MasterData object so that it uses the cached object

        // Note: associations are expicitly excluded from serialization as they need to be handled separately
        if (candidate.getCategories() != null && candidate.getCategories().length() > 0) {
            masterDataService.associateCategories(candidateID, candidate.getCategories(), "Candidate");
        }
    }

}
