package com.bullhorn.dataloader.service;

import org.json.JSONObject;

import com.bullhorn.dataloader.domain.ID;
import com.bullhorn.dataloader.domain.Lead;

class LeadImportService extends AbstractEntityImportService {

    public void run() {
        try {

            lead();

        } catch (Exception e) {
            log.error(e);
        }

    }

    void lead() throws Exception {
        Lead lead = (Lead) obj;

        // Check if record exists in BH and get postURL
        String[] postInfo = bhapi.getPostURL(lead);
        String type = postInfo[0];
        String postURL = postInfo[1];

        // Determine owner. If owner isn't passed in, it uses session user
        ID ownerID = new ID();
        // If an ID is passed in
        if (lead.getOwnerID() != null && lead.getOwnerID().length() > 0) {
            ownerID.setId(lead.getOwnerID());
            lead.setOwner(ownerID);
            // Else look up by name
        } else if (lead.getOwnerName() != null && lead.getOwnerName().length() > 0) {
            ownerID.setId(masterDataService.getOwnerID(lead.getOwnerName()));
            lead.setOwner(ownerID);
        }

        // Save
        JSONObject jsResp = bhapi.save(lead, postURL, type);

        // Get ID of the created/updated record
        int leadID = jsResp.getInt("changedEntityId");

        // Note: associations are expicitly excluded from serialization as they need to be handled separately
        if (lead.getCategories() != null && lead.getCategories().length() > 0) {
            masterDataService.associateCategories(leadID, lead.getCategories(), "Lead");
        }
    }


}
