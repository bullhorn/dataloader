package com.bullhorn.dataloader.service.executor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JsonService implements Runnable {
    private final LoadingCache<EntityQuery, Optional<Integer>> associationCache;
    private final Set<List<EntityInstance>> seenFlag;
    private BullhornAPI bhapi;
    private String entity;
    private JsonRow data;

    private static final Log log = LogFactory.getLog(JsonService.class);

    public JsonService(String entity,
                       BullhornAPI bullhornApi,
                       JsonRow data,
                       LoadingCache<EntityQuery, Optional<Integer>> associationCache,
                       Set<List<EntityInstance>> seenFlag) {
        this.bhapi = bullhornApi;
        this.entity = entity;
        this.data = data;
        this.associationCache = associationCache;
        this.seenFlag = seenFlag;
    }

    @Override
    public void run() {
        String entityBase = bhapi.getRestURL() + StringConsts.ENTITY_SLASH + getEntity();
        String restToken = StringConsts.END_BH_REST_TOKEN + bhapi.getBhRestToken();
        try {
            Map<String, Object> toOneIdentifiers = upsertPreprocessingActions();
            Optional<Integer> optionalEntityId = createOrGetEntity(toOneIdentifiers);
            if (optionalEntityId.isPresent()) {
                updateEntity(entityBase, restToken, optionalEntityId.get());
                saveToMany(optionalEntityId.get(), entity, data.getDeferredActions());
            }
        } catch (IOException | ExecutionException e) {
            log.error(e);
        }
    }

    private Map<String, Object> upsertPreprocessingActions() throws ExecutionException {
        Map<String, Object> preprocessingActions = data.getPreprocessingActions();
        Map<String, Object> toOneIdentifiers = Maps.newHashMap();
        for (Map.Entry<String, Object> entityEntry : preprocessingActions.entrySet()) {
            EntityQuery entityQuery = new EntityQuery(entityEntry.getKey(), entityEntry.getValue());
            addSearchFields(entityQuery, (Map<String, Object>) entityEntry.getValue());
            Optional<Integer> toOneId = associationCache.get(entityQuery);
            if (toOneId.isPresent()) {
                Map<String, Integer> toOneAssociation = Maps.newHashMap();
                toOneAssociation.put(StringConsts.ID, toOneId.get());
                toOneIdentifiers.put(entityEntry.getKey(), toOneAssociation);
            } else {
                log.error("Failed to upsert to-one association " + entityQuery);
            }
        }
        return toOneIdentifiers;
    }

    private void updateEntity(String entityBase, String restToken, Integer optionalEntityId) throws IOException {
        String postUrl = entityBase + "/" + optionalEntityId + restToken;
        bhapi.saveNonToMany(data.getImmediateActions(), postUrl, "POST");
    }

    private Optional<Integer> createOrGetEntity(Map<String, Object> toOneIdentifiers) throws ExecutionException {
        Object nestJson = mergeObjects(toOneIdentifiers, data.getImmediateActions());
        EntityQuery entityQuery = new EntityQuery(getEntity(), nestJson);
        addSearchFields(entityQuery, data.getImmediateActions());
        entityQuery.addInt(StringConsts.IS_DELETED, "false");
        return associationCache.get(entityQuery);
    }

    private void addSearchFields(EntityQuery entityQuery, Map<String, Object> actions) {
        ifPresentPut(entityQuery::addInt, StringConsts.ID, actions.get(StringConsts.ID));
        ifPresentPut(entityQuery::addString, StringConsts.NAME, actions.get(StringConsts.NAME));
        Optional<String> entityExistsFieldsProperty = bhapi.getEntityExistsFieldsProperty(entity);

        if (entityExistsFieldsProperty.isPresent()) {
            for (String propertyFileExistField : entityExistsFieldsProperty.get().split(",")) {
                ifPresentPut(entityQuery::addString, propertyFileExistField, actions.get(propertyFileExistField));
            }
        }
    }

    private static Map<String, Object> mergeObjects(Map<String, Object> toOneIdentifiers, Map<String, Object> immediateActions) {
        immediateActions.putAll(toOneIdentifiers);
        return immediateActions;
    }

    private void saveToMany(Integer entityId, String entity, Map<String, Object> toManyProperties) throws ExecutionException, IOException {
        EntityInstance parentEntity = new EntityInstance(String.valueOf(entityId), entity);

        for (Map.Entry<String, Object> toManyEntry : toManyProperties.entrySet()) {
            EntityQuery entityQuery = new EntityQuery(toManyEntry.getKey(), toManyEntry.getValue());
            Map<String, Object> entityFieldFilters = (Map) toManyEntry.getValue();
            ifPresentPut(entityQuery::addInt, StringConsts.ID, entityFieldFilters.get(StringConsts.ID));
            ifPresentPut(entityQuery::addString, StringConsts.NAME, entityFieldFilters.get(StringConsts.NAME));
            Optional<Integer> associatedId = associationCache.get(entityQuery);
            if (associatedId.isPresent()) {
                EntityInstance associationEntity = new EntityInstance(String.valueOf(associatedId.get()), entityQuery.getEntity());
                associate(parentEntity, associationEntity);
            }
        }
    }

    private void associate(EntityInstance parentEntity, EntityInstance associationEntity) throws IOException {
        List<EntityInstance> nestedEntities = Lists.newArrayList(parentEntity, associationEntity);
        if (!seenFlag.contains(nestedEntities)) {
            dissociateEverything(parentEntity, associationEntity);
        }
        bhapi.associateEntity(parentEntity, associationEntity);
    }

    private synchronized void dissociateEverything(EntityInstance parentEntity, EntityInstance associationEntity) throws IOException {
        List<EntityInstance> nestedEntities = Lists.newArrayList(parentEntity, associationEntity);
        if (!seenFlag.contains(nestedEntities)) {
            seenFlag.add(nestedEntities);
            log.debug("Dissociating " + parentEntity + " "+ associationEntity);
            bhapi.dissociateEverything(parentEntity, associationEntity);
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
