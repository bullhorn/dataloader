package com.bullhorn.dataloader.service.executor;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.query.AssociationQuery;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.google.common.cache.LoadingCache;

public class JsonService implements Runnable {
    private final String ID = "id";
    private final String NAME = "name";

    private final LoadingCache<AssociationQuery, Optional<Integer>> associationCache;
    private final Set<EntityInstance> seenFlag;
    private BullhornAPI bhapi;
    private String entity;
    private JsonRow data;

    private final static Log log = LogFactory.getLog(JsonService.class);

    public JsonService(String entity,
                       BullhornAPI bullhornApi,
                       JsonRow data,
                       LoadingCache<AssociationQuery, Optional<Integer>> associationCache,
                       Set<EntityInstance> seenFlag) {
        this.bhapi = bullhornApi;
        this.entity = entity;
        this.data = data;
        this.associationCache = associationCache;
        this.seenFlag = seenFlag;
    }

    @Override
    public void run() {
        // Post to BH
        String entityBase = bhapi.getRestURL() + "entity/" + getEntity();
        String restToken = "?BhRestToken=" + bhapi.getBhRestToken();
        String postURL = entityBase + restToken;
        JSONObject response = null;
        try {
            response = bhapi.saveNonToMany(data.getImmediateActions(), postURL, "PUT");
            saveToMany(data.getDeferredActions());
        } catch (Exception e) {
            log.error("Error saving entity", e);
            log.error(response);
        }
    }

    private void saveToMany(Map<String, Object> toManyProperties) throws ExecutionException, IOException {
        for (String toManyKey : toManyProperties.keySet()) {
            AssociationQuery associationQuery = new AssociationQuery(toManyKey, toManyProperties.get(toManyKey));
            Map<String, Object> entityFieldFilters = (Map) toManyProperties.get(toManyKey);
            ifPresentPut(associationQuery::addInt, ID, entityFieldFilters.get(ID));
            ifPresentPut(associationQuery::addString, NAME, entityFieldFilters.get(NAME));
            Optional<Integer> associatedId = associationCache.get(associationQuery);
            log.info(associatedId);
            Optional<String> properEntity = bhapi.getLabelByName(associationQuery.getEntity());
            log.info(properEntity);
        }
    }

    private static void ifPresentPut(BiConsumer<String, String> consumer, String fieldName, Object value) {
        if (value != null) {
            consumer.accept(fieldName, value.toString());
        }
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
