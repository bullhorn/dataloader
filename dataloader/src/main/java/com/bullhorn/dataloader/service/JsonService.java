package com.bullhorn.dataloader.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.util.BullhornAPI;

public class JsonService implements Runnable {
    private final Log log = LogFactory.getLog(JsonService.class);

    private BullhornAPI bhapi;
    private String entity;
    private Map<String, Object> data;

    public JsonService(String entity, BullhornAPI bullhornApi, Map<String, Object> data) {
        this.bhapi = bullhornApi;
        this.entity = entity;
        this.data = data;
    }

    @Override
    public void run() {
        // Post to BH
        String postURL = bhapi.getRestURL() + "entity/" + getEntity() + "?BhRestToken=" + bhapi.getBhRestToken();

        try {
            bhapi.save(data, postURL, "PUT");
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
