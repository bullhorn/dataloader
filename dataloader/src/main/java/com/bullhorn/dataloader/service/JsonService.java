package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.util.BullhornAPI;

public class JsonService implements Runnable {
    private final Log log = LogFactory.getLog(JsonService.class);

    private BullhornAPI bhapi;
    private String entity;
    private JsonRow data;

    public JsonService(String entity, BullhornAPI bullhornApi, JsonRow data) {
        this.bhapi = bullhornApi;
        this.entity = entity;
        this.data = data;
    }

    @Override
    public void run() {
        // Post to BH
        String entityBase = bhapi.getRestURL() + "entity/" + getEntity();
        String restToken = "?BhRestToken=" + bhapi.getBhRestToken();
        String postURL = entityBase + restToken;

        try {
            JSONObject response = bhapi.saveNonToMany(data.getNonToManyProperties(), postURL, "PUT");
            bhapi.saveToMany(response, data.getToManyProperties(), entityBase, restToken, "PUT");

        } catch (Exception e) {
            log.error("Error saving entity", e);
        }
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
