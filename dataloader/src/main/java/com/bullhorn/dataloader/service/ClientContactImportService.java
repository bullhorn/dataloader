package com.bullhorn.dataloader.service;

import org.json.JSONObject;

import com.bullhorn.dataloader.domain.ClientContact;
import com.bullhorn.dataloader.domain.ID;
import com.bullhorn.dataloader.service.util.GUIDService;

class ClientContactImportService extends AbstractEntityImportService {

    Integer clientContact() throws Exception {
        ClientContact contact = (ClientContact) obj;

        // Check if record exists in BH and get postURL
        String[] postInfo = bhapi.getPostURL(contact);
        String type = postInfo[0];
        String postURL = postInfo[1];

        // Set username/password properties (which will not be defined in the CSV)
        if (type.equalsIgnoreCase("put")) {
            GUIDService.inflateUser(contact);
        }
        contact.setIsDeleted("false");

        // Determine owner. If owner isn't passed in, it uses session user
        ID ownerID = new ID();
        // If an ID is passed in
        if (contact.getOwnerID() != null && contact.getOwnerID().length() > 0) {
            ownerID.setId(contact.getOwnerID());
            contact.setOwner(ownerID);
            // Else look up by name
        } else if (contact.getOwnerName() != null && contact.getOwnerName().length() > 0) {
            ownerID.setId(masterDataService.getOwnerID(contact.getOwnerName()));
            contact.setOwner(ownerID);
        }

        // Save
        JSONObject jsResp = bhapi.save(contact, postURL, type);

        // Get ID of the created/updated record
        int clientContactID = jsResp.getInt("changedEntityId");

        // Note: associations are expicitly excluded from serialization as they need to be handled separately
        if (contact.getCategories() != null && contact.getCategories().length() > 0) {
            masterDataService.associateCategories(clientContactID, contact.getCategories(), "ClientContact");
        }

        return clientContactID;


    }

    public void run() {
        try {
            clientContact();
        } catch (Exception e) {
            log.error("Client Contact Import Failed for reason", e);
        }
    }

}
