package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.Timer;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.bullhorn.dataloader.util.AssociationFilter.isInteger;

/**
 * Responsible for loading a single row from a CSV input file.
 */
public class LoadTask implements Runnable {
    private static final Logger log = LogManager.getLogger(LoadTask.class);

    private String entityName;
    private final BullhornAPI bhApi;
    private final BullhornApiAssociator bhApiAssociator;
    private JsonRow data;
    private final LoadingCache<EntityQuery, Result> associationCache;
    private CsvFileWriter csvFileWriter;
    private PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil = new PrintUtil();
    private final ActionTotals actionTotals = new ActionTotals();
    private static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    public LoadTask(String entityName,
                    BullhornAPI bhApi,
                    BullhornApiAssociator bhApiAssociator,
                    JsonRow data,
                    LoadingCache<EntityQuery, Result> associationCache,
                    CsvFileWriter csvFileWriter,
                    PropertyFileUtil propertyFileUtil) {
        this.entityName = entityName;
        this.bhApi = bhApi;
        this.bhApiAssociator = bhApiAssociator;
        this.data = data;
        this.associationCache = associationCache;
        this.csvFileWriter = csvFileWriter;
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Run method on this runnable object called by the thread manager.
     * <p>
     * The createOrGetEntity method performs the update/insert in REST.
     * The results of this are passed to the csvFileWriter
     */
    @Override
    public void run() {
        Timer timer = new Timer();

        String entityBase = bhApi.getRestURL() + StringConsts.ENTITY_SLASH + entityName;
        String restToken = StringConsts.END_BH_REST_TOKEN + bhApi.getBhRestToken();
        try {
            Map<String, Object> toOneIdentifiers = upsertPreprocessingActions();
            Result result = createOrGetEntity(toOneIdentifiers);

            if (result.isSuccess()) {
                updateEntity(entityBase, restToken, result.getBullhornId());
                saveToMany(result.getBullhornId(), entityName, data.getDeferredActions());
            }

            csvFileWriter.writeRow(data.getValues(), result);

            if(result.getAction().equals(Result.Action.INSERT)) {
                actionTotals.incrementTotalInsert();
            } else if(result.getAction().equals(Result.Action.UPDATE)){
                actionTotals.incrementTotalUpdate();
            } else {
                actionTotals.incrementTotalError();
            }

            rowProcessedCount.incrementAndGet();
            if(rowProcessedCount.intValue() % 111 == 0) {
                printUtil.printAndLog("Processed: " + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
            }

        } catch (IOException | ExecutionException e) {
            System.out.println(e);
            log.error(e);
        }

        log.debug("Loaded row " + data.getRowNumber() + " in " + timer.getDurationStringSec());
    }

    private Map<String, Object> upsertPreprocessingActions() throws ExecutionException {
        Map<String, Object> preprocessingActions = data.getPreprocessingActions();
        Map<String, Object> toOneIdentifiers = Maps.newHashMap();
        for (Map.Entry<String, Object> entityEntry : preprocessingActions.entrySet()) {
            EntityQuery entityQuery = new EntityQuery(entityEntry.getKey(), entityEntry.getValue());
            addSearchFields(entityQuery, (Map<String, Object>) entityEntry.getValue());
            Result toOneResult = associationCache.get(entityQuery);
            if (toOneResult.isSuccess()) {
                Map<String, Integer> toOneAssociation = Maps.newHashMap();
                toOneAssociation.put(StringConsts.ID, toOneResult.getBullhornId());
                toOneIdentifiers.put(entityEntry.getKey(), toOneAssociation);
            } else {
                log.error("Failed to upsert to-one association " + entityQuery);
            }
        }
        return toOneIdentifiers;
    }

    private void updateEntity(String entityBase, String restToken, Integer optionalEntityId) throws IOException {
        String postUrl = entityBase + "/" + optionalEntityId + restToken;
        bhApi.saveNonToMany(data.getImmediateActions(), postUrl, "POST");
    }

    private Result createOrGetEntity(Map<String, Object> toOneIdentifiers) throws ExecutionException {
        Object nestJson = mergeObjects(toOneIdentifiers, data.getImmediateActions());
        EntityQuery entityQuery = new EntityQuery(entityName, nestJson);
        addSearchFields(entityQuery, data.getImmediateActions());
        if (bhApi.containsFields(StringConsts.IS_DELETED)) {
            entityQuery.addFieldWithoutCount(StringConsts.IS_DELETED, "false");
        }

        // Query the EntityCache for the current entity. It will handle uploading the data inside the load method.
        return associationCache.get(entityQuery);
    }

    /**
     * Search for the entity using the ID and Name field if they exist, and then add the entityExist property.
     */
    private void addSearchFields(EntityQuery entityQuery, Map<String, Object> actions) {
        ifPresentPut(entityQuery::addInt, StringConsts.ID, actions.get(StringConsts.ID));
        ifPresentPut(entityQuery::addString, StringConsts.NAME, actions.get(StringConsts.NAME));

        Optional<String> entityLabel = bhApi.getLabelByName(entityQuery.getEntity());
        if (entityLabel.isPresent()) {
            Optional<List<String>> entityExistFields = propertyFileUtil.getEntityExistFields(entityLabel.get());
            existsFieldSearch(entityQuery, actions, entityExistFields);
        }

        Optional<List<String>> parentEntityExistFields = propertyFileUtil.getEntityExistFields(entityQuery.getEntity());
        existsFieldSearch(entityQuery, actions, parentEntityExistFields);
    }

    /**
     * Tries to use meta to determine how to search on the propertyExistsField. Default to String if error occurs.
     */
    private void existsFieldSearch(EntityQuery entityQuery, Map<String, Object> actions, Optional<List<String>> entityExistFields) {
        if (entityExistFields.isPresent()) {
            for (String entityExistField : entityExistFields.get()) {
                String entityName = bhApi.getLabelByName(entityQuery.getEntity()).orElse(entityQuery.getEntity());
                try {
                    Optional<String> dataType = bhApi.getMetaDataTypes(entityName).getDataTypeByFieldName(entityExistField);
                    if (dataType.isPresent() && isInteger(dataType.get())) {
                        ifPresentPut(entityQuery::addInt, entityExistField, actions.get(entityExistField));
                    } else {
                        ifPresentPut(entityQuery::addString, entityExistField, actions.get(entityExistField));
                    }
                } catch (IOException e) {
                    log.debug("Error retrieving meta information for: " + entityName, e);
                    ifPresentPut(entityQuery::addString, entityExistField, actions.get(entityExistField));
                }
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
            Set<Integer> validIds = getValidToManyIds(toManyEntry);

            String entityName = toManyEntry.getKey();
            String idList = validIds.stream()
                    .map(it -> it.toString())
                    .collect(Collectors.joining(","));

            EntityInstance associationEntity = new EntityInstance(idList, entityName);
            bhApiAssociator.dissociateEverything(parentEntity, associationEntity);
            bhApiAssociator.associate(parentEntity, associationEntity);
        }
    }

    private Set<Integer> getValidToManyIds(Map.Entry<String, Object> toManyEntry) throws IOException, ExecutionException {
        Set<Integer> validIds = Sets.newHashSet();
        Map<String, Object> entityFieldFilters = (Map) toManyEntry.getValue();
        for (String fieldName : entityFieldFilters.keySet()) {
            List<Object> values = (List<Object>) entityFieldFilters.get(fieldName);
            if (values == null) {
                values = Lists.newArrayList();
            }

            for (Object value : values) {
                EntityQuery entityQuery = new EntityQuery(toManyEntry.getKey(), toManyEntry.getValue());
                String entityLabel = bhApi.getLabelByName(toManyEntry.getKey()).get();

                if (hasPrivateLabel(entityLabel)) {
                    entityQuery.addMemberOfWithoutCount(StringConsts.PRIVATE_LABELS, String.valueOf(bhApi.getPrivateLabel()));
                }

                // should use meta if any more fields are needed
                if (fieldName.equals(StringConsts.ID)) {
                    ifPresentPut(entityQuery::addInt, fieldName, value);
                } else {
                    ifPresentPut(entityQuery::addString, fieldName, value);
                }

                Result association;
                if (propertyFileUtil.shouldFrontLoadEntity(entityLabel)) {
                    association = queryFrontLoaded(entityLabel, fieldName, value.toString());
                } else {
                    association = associationCache.get(entityQuery);
                }

                if (association.isSuccess()) {
                    validIds.add(association.getBullhornId());
                }
            }
        }
        return validIds;
    }

    private boolean hasPrivateLabel(String entityLabel) throws IOException {
        return bhApi.entityContainsFields(entityLabel, StringConsts.PRIVATE_LABELS);
    }

    private Result queryFrontLoaded(String entity, String fieldName, String value) {
        if (StringConsts.ID.equals(fieldName)) {
            return bhApi.hasFrontLoadedEntity(entity, value);
        } else {
            return bhApi.getFrontLoadedFromKey(entity, value);
        }
    }

    private static void ifPresentPut(BiConsumer<String, String> consumer, String fieldName, Object value) {
        if (value != null) {
            consumer.accept(fieldName, value.toString());
        }
    }
}
