package com.bullhorn.dataloader.service;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.CustomObject;

class CustomObjectImportService extends AbstractEntityImportService {

    public void run() {

        try {
            customObject();
        } catch (Exception e) {
            log.error(e);
        }
    }

    void customObject() throws Exception {
        CustomObject co = (CustomObject) obj;
        co.setCustomObjectName(co.getCustomObjectName() + "s");

        // If there's an ID, disassociate then re-create
        if (co.getId() != null && co.getId().length() > 0) {
            String postURL = bhapi.getRestURL() + "entity/" + co.getEntity() + "/" + co.getEntityID() + "/" + co.getCustomObjectName() + "/" + co.getId();
            postURL = postURL + "?BhRestToken=" + bhapi.getBhRestToken();
            bhapi.delete(postURL);
        }

        JSONObject coObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(bhapi.serialize(co)));

        coObj.put(co.getCustomObjectName(), jsonArray);
        String jsString = coObj.toString();

        // Post to BH
        String postURL = bhapi.getRestURL() + "entity/" + co.getEntity() + "/" + co.getEntityID();
        postURL = postURL + "?BhRestToken=" + bhapi.getBhRestToken();

        bhapi.save(jsString, postURL, "post");
    }

}
