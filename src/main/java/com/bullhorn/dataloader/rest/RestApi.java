package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.type.AllRecordsEntity;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.parameter.AssociationParams;
import com.bullhornsdk.data.model.parameter.QueryParams;
import com.bullhornsdk.data.model.parameter.SearchParams;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.file.FileApiResponse;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulation of the standard SDK-REST BullhornData class for interacting with Bullhorn's REST API Provides an extra layer of functionality needed
 * by DataLoader surrounding SDK-REST calls. This class calls the RestApiExtension to handle any additional behavior on top of the current REST API
 * calls.
 */
public class RestApi {

    private static final Integer MAX_RECORDS_TO_RETURN_IN_ONE_PULL = 500;
    private static final Integer MAX_RECORDS_TO_RETURN_TOTAL = 20000;
    private final StandardBullhornData bullhornData;
    private final RestApiExtension restApiExtension;
    private final PrintUtil printUtil;

    public RestApi(StandardBullhornData bullhornData, RestApiExtension restApiExtension, PrintUtil printUtil) {
        this.bullhornData = bullhornData;
        this.restApiExtension = restApiExtension;
        this.printUtil = printUtil;
    }

    // region Getters
    String getBhRestToken() {
        return bullhornData.getBhRestToken();
    }

    String getRestUrl() {
        return bullhornData.getRestUrl();
    }
    // endregion

    // region Meta Operations
    public <T extends BullhornEntity> MetaData<T> getMetaData(Class<T> type,
                                                              MetaParameter metaParameter,
                                                              Set<String> fieldSet) {
        return bullhornData.getMetaData(type, metaParameter, fieldSet);
    }
    // endregion

    // region Lookup Calls

    // TODO: Refactor to:
    // <T extends BullhornEntity> List<T> findEntities(SearchCriteria entitySearch, Set<String> fieldSet)
    // Where SearchCriteria contains the EntityType, and query/where clause builder logic, and this method does the
    // check to determine whether to call search or query on bullhornData.

    // The search/query calls that DataLoader uses to lookup existing data
    public <T extends SearchEntity> List<T> searchForList(Class<T> type,
                                                          String query,
                                                          Set<String> fieldSet,
                                                          SearchParams params) {
        printUtil.log(Level.DEBUG, "Find(" + type.getSimpleName() + " Search): " + query);
        Boolean isSupportedEntity = type != JobOrder.class && type != Lead.class && type != Opportunity.class;
        String externalId = SearchCriteria.getExternalIdValue(query);
        if (isSupportedEntity && !externalId.isEmpty()) {
            SearchResult<T> searchResult = restApiExtension.getByExternalId(this, type, externalId, fieldSet);
            if (searchResult.getSuccess()) {
                return searchResult.getList();
            }
        }
        List<T> list = new ArrayList<>();
        params.setCount(MAX_RECORDS_TO_RETURN_IN_ONE_PULL);
        recursiveSearchPull(list, type, query, fieldSet, params);
        return list;
    }

    public <T extends QueryEntity> List<T> queryForList(Class<T> type,
                                                        String where,
                                                        Set<String> fieldSet,
                                                        QueryParams params) {
        printUtil.log(Level.DEBUG, "Find(" + type.getSimpleName() + " Query): " + where);
        List<T> list = new ArrayList<>();
        params.setCount(MAX_RECORDS_TO_RETURN_IN_ONE_PULL);
        recursiveQueryPull(list, type, where, fieldSet, params);
        return list;
    }

    <T extends QueryEntity & AllRecordsEntity> List<T> queryForAllRecordsList(Class<T> type,
                                                                              String where,
                                                                              Set<String> fieldSet,
                                                                              QueryParams params) {
        printUtil.log(Level.DEBUG, "Find(" + type.getSimpleName() + " Query): " + where);
        ListWrapper<T> listWrapper = bullhornData.queryForAllRecords(type, where, fieldSet, params);
        return listWrapper == null ? Collections.emptyList() : listWrapper.getData();
    }
    // endregion

    // region CRUD Operations
    // The methods DataLoader uses to Create, Read, Update, and Delete.
    public <C extends CrudResponse, T extends CreateEntity> C insertEntity(T entity) {
        printUtil.log(Level.DEBUG, "Insert(" + entity.getClass().getSimpleName() + ")");
        C crudResponse = bullhornData.insertEntity(entity);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }

    public <C extends CrudResponse, T extends UpdateEntity> C updateEntity(T entity) {
        printUtil.log(Level.DEBUG, "Update(" + entity.getClass().getSimpleName() + "): #" + entity.getId());
        C crudResponse = bullhornData.updateEntity(entity);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }

    /**
     * Performs postDelete operations defined in the RestApiExtension.
     */
    public <C extends CrudResponse, T extends DeleteEntity> C deleteEntity(Class<T> type, Integer id) {
        printUtil.log(Level.DEBUG, "Delete(" + type.getSimpleName() + "): #" + id);
        C crudResponse = bullhornData.deleteEntity(type, id);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        C postDeleteCrudResponse = restApiExtension.postDelete(this, crudResponse);
        restApiExtension.checkForRestSdkErrorMessages(postDeleteCrudResponse);
        return postDeleteCrudResponse;
    }
    // endregion

    // region Association Methods
    // The methods DataLoader uses for creating or deleting associations.
    public <T extends AssociationEntity, E extends BullhornEntity> List<E> getAllAssociationsList(
        Class<T> type, Set<Integer> entityIds, AssociationField<T, E> associationName, Set<String> fieldSet,
        AssociationParams params) {
        printUtil.log(Level.DEBUG, "FindAssociations(" + type.getSimpleName() + "): #" + entityIds + " - "
            + associationName.getAssociationFieldName());
        ListWrapper<E> listWrapper = bullhornData.getAllAssociations(type, entityIds, associationName, fieldSet, params);
        return listWrapper == null ? Collections.emptyList() : listWrapper.getData();
    }

    public <C extends CrudResponse, T extends AssociationEntity> C associateWithEntity(
        Class<T> type, Integer entityId, AssociationField<T, ? extends BullhornEntity> associationName,
        Set<Integer> associationIds) {
        printUtil.log(Level.DEBUG, "Associate(" + type.getSimpleName() + "): #" + entityId + " - " + associationName.getAssociationFieldName());
        C crudResponse = bullhornData.associateWithEntity(type, entityId, associationName, associationIds);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }

    public <C extends CrudResponse, T extends AssociationEntity> C disassociateWithEntity(
        Class<T> type, Integer entityId, AssociationField<T, ? extends BullhornEntity> associationName,
        Set<Integer> associationIds) {
        printUtil.log(Level.DEBUG, "Disassociate(" + type.getSimpleName() + "): #" + entityId + " - " + associationName.getAssociationFieldName());
        C crudResponse = bullhornData.disassociateWithEntity(type, entityId, associationName, associationIds);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }
    // endregion

    // region File Methods
    // The methods DataLoader uses for attachments
    public List<FileMeta> getFileMetaData(Class<? extends FileEntity> type, Integer entityId) {
        return bullhornData.getFileMetaData(type, entityId);
    }

    public FileContent getFileContent(Class<? extends FileEntity> type, Integer entityId, Integer fileId) {
        return bullhornData.getFileContent(type, entityId, fileId);
    }

    public FileWrapper addFile(Class<? extends FileEntity> type, Integer entityId, FileMeta fileMeta) {
        return bullhornData.addFile(type, entityId, fileMeta);
    }

    public FileWrapper updateFile(Class<? extends FileEntity> type, Integer entityId, FileMeta fileMeta) {
        return bullhornData.updateFile(type, entityId, fileMeta);
    }

    public FileApiResponse deleteFile(Class<? extends FileEntity> type, Integer entityId, Integer fileId) {
        return bullhornData.deleteFile(type, entityId, fileId);
    }
    // endregion

    // region Methods used by RestApiExtension
    <T> T performGetRequest(String url, Class<T> returnType, Map<String, String> uriVariables) {
        return bullhornData.performGetRequest(url, returnType, uriVariables);
    }
    // endregion

    /**
     * The recursive search pull for more than 500 records, applied to all entities, not just AllRecordsEntity entities.
     */
    private <T extends SearchEntity> void recursiveSearchPull(List<T> allEntities,
                                                              Class<T> type,
                                                              String query,
                                                              Set<String> fieldSet,
                                                              SearchParams params) {
        ListWrapper<T> onePull = bullhornData.search(type, query, fieldSet, params);
        allEntities.addAll(onePull.getData());
        if (shouldPullMoreRecords(onePull)) {
            params.setStart(allEntities.size());
            recursiveSearchPull(allEntities, type, query, fieldSet, params);
        }
    }

    /**
     * The recursive query pull for more than 500 records, applied to all entities, not just AllRecordsEntity entities.
     */
    private <T extends QueryEntity> void recursiveQueryPull(List<T> allEntities,
                                                            Class<T> type,
                                                            String where,
                                                            Set<String> fieldSet,
                                                            QueryParams params) {
        ListWrapper<T> onePull = bullhornData.query(type, where, fieldSet, params);
        allEntities.addAll(onePull.getData());
        if (shouldPullMoreRecords(onePull)) {
            params.setStart(allEntities.size());
            recursiveQueryPull(allEntities, type, where, fieldSet, params);
        }
    }

    private boolean shouldPullMoreRecords(ListWrapper<?> response) {
        Integer total = response.getTotal();
        Integer start = response.getStart();
        Integer count = response.getCount();

        // Handle missing values
        if (start == null) {
            start = 0;
        }
        if (total == null) {
            total = count;
        }

        Integer nextStart = start + count;
        Integer nextEnd = Math.min(nextStart + MAX_RECORDS_TO_RETURN_IN_ONE_PULL, total);
        if (nextStart < total && count != 0 && nextStart < MAX_RECORDS_TO_RETURN_TOTAL) {
            printUtil.log(Level.DEBUG, "--> Follow On Find(" + nextStart + " - " + nextEnd + ")");
            return true;
        }
        return false;
    }
}
