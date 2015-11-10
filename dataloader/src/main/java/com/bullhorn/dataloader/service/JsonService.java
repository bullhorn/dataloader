package com.bullhorn.dataloader.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.query.AssociationQuery;
import com.bullhorn.dataloader.util.BullhornAPI;
import com.google.common.cache.LoadingCache;

public class JsonService implements Runnable {
    private final LoadingCache<AssociationQuery, Optional<Integer>> associationCache;
    private BullhornAPI bhapi;
    private String entity;
    private JsonRow data;

    private final Log log = LogFactory.getLog(JsonService.class);

    public JsonService(String entity,
                       BullhornAPI bullhornApi,
                       JsonRow data,
                       LoadingCache<AssociationQuery, Optional<Integer>> associationCache) {
        this.bhapi = bullhornApi;
        this.entity = entity;
        this.data = data;
        this.associationCache = associationCache;
    }

    @Override
    public void run() {
        // Post to BH
        String entityBase = bhapi.getRestURL() + "entity/" + getEntity();
        String restToken = "?BhRestToken=" + bhapi.getBhRestToken();
        String postURL = entityBase + restToken;
        JSONObject response = null;
        try {
            response = bhapi.saveNonToMany(data.getNonToManyProperties(), postURL, "PUT");
            // ResponseId is
            // response.getInt("changedEntityId");
            saveToMany(data.getToManyProperties());
        } catch (Exception e) {
            log.error("Error saving entity", e);
            log.error(response);
        }
    }

    private void saveToMany(Map<String, Object> toManyProperties) throws ExecutionException {
        for (String entity : toManyProperties.keySet()) {
            AssociationQuery associationQuery = new AssociationQuery(entity);
            Map<String, Object> entityFieldFilters = (Map) toManyProperties.get(entity);
            for (String key : entityFieldFilters.keySet()) {
                associationQuery.addCondition(key, entityFieldFilters.get(key).toString());
            }
            associationCache.get(associationQuery);
        }
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
