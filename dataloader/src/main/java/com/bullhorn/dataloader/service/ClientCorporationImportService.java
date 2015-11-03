package com.bullhorn.dataloader.service;

import org.json.JSONObject;

import com.bullhorn.dataloader.domain.Address;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.ID;

class ClientCorporationImportService extends AbstractEntityImportService {

    Integer clientCorporation() {

        try {

            ClientCorporation corp = (ClientCorporation) obj;

            // Check if record exists in BH and get postURL
            String[] postInfo = bhapi.getPostURL(corp);
            String type = postInfo[0];
            String postURL = postInfo[1];

            // Populate address fields
            if (corp.getAddress() == null) {
                Address address = new Address();
                address.setAddress1(corp.getAddress1());
                address.setAddress2(corp.getAddress2());
                address.setCity(corp.getCity());
                address.setCountryID(corp.getCountry());
                address.setState(corp.getState());
                address.setZip(corp.getZip());
                corp.setAddress(address);
            }

            // If there's a parent corporation, associate it
            if (corp.getParentClientCorporation() == null &&
                    corp.getParentClientCorporationID() != null &&
                    corp.getParentClientCorporationID().length() > 0) {
                ID parentClientCorporationID = new ID();
                parentClientCorporationID.setId(corp.getParentClientCorporationID());
                corp.setParentClientCorporation(parentClientCorporationID);
            }

            JSONObject jsResp = bhapi.save(corp, postURL, type);

            // Get ID of the created/updated record
            return jsResp.getInt("changedEntityId");

        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

    public void run() {
        clientCorporation();
    }

}
