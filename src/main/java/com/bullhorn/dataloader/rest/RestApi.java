package com.bullhorn.dataloader.rest;

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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulation of the standard SDK-REST BullhornData class for interacting with Bullhorn's REST API Provides an extra
 * layer of functionality needed by DataLoader surrounding SDK-REST calls. This class calls the RestApiExtension to
 * handle any additional behavior on top of the current REST API calls.
 */
public class RestApi {

    private final StandardBullhornData bullhornData;
    private final RestApiExtension restApiExtension;

    public RestApi(StandardBullhornData bullhornData, RestApiExtension restApiExtension) {
        this.bullhornData = bullhornData;
        this.restApiExtension = restApiExtension;
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
        Boolean isSupportedEntity = type != JobOrder.class && type != Lead.class && type != Opportunity.class;
        String externalId = SearchCriteria.getExternalIdValue(query);
        if (isSupportedEntity && !externalId.isEmpty()) {
            SearchResult<T> searchResult = restApiExtension.getByExternalId(this, type, externalId, fieldSet);
            if (searchResult.getSuccess()) {
                return searchResult.getList();
            }
        }
        return bullhornData.searchForList(type, query, fieldSet, params);
    }

    public <T extends QueryEntity> List<T> queryForList(Class<T> type,
                                                        String where,
                                                        Set<String> fieldSet,
                                                        QueryParams params) {
        return bullhornData.queryForList(type, where, fieldSet, params);
    }

    <T extends QueryEntity & AllRecordsEntity> List<T> queryForAllRecordsList(Class<T> type,
                                                                              String where,
                                                                              Set<String> fieldSet,
                                                                              QueryParams params) {
        ListWrapper<T> listWrapper = bullhornData.queryForAllRecords(type, where, fieldSet, params);
        return listWrapper == null ? Collections.emptyList() : listWrapper.getData();
    }
    // endregion

    // region CRUD Operations
    // The methods DataLoader uses to Create, Read, Update, and Delete.
    public <C extends CrudResponse, T extends CreateEntity> C insertEntity(T entity) {
        C crudResponse = bullhornData.insertEntity(entity);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }

    public <C extends CrudResponse, T extends UpdateEntity> C updateEntity(T entity) {
        C crudResponse = bullhornData.updateEntity(entity);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }

    /**
     * Performs postDelete operations defined in the RestApiExtension.
     */
    public <C extends CrudResponse, T extends DeleteEntity> C deleteEntity(Class<T> type, Integer id) {
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
        ListWrapper<E> listWrapper = bullhornData.getAllAssociations(type, entityIds, associationName, fieldSet, params);
        return listWrapper == null ? Collections.emptyList() : listWrapper.getData();
    }

    public <C extends CrudResponse, T extends AssociationEntity> C associateWithEntity(
        Class<T> type, Integer entityId, AssociationField<T, ? extends BullhornEntity> associationName,
        Set<Integer> associationIds) {
        C crudResponse = bullhornData.associateWithEntity(type, entityId, associationName, associationIds);
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
        return crudResponse;
    }

    public <C extends CrudResponse, T extends AssociationEntity> C disassociateWithEntity(
        Class<T> type, Integer entityId, AssociationField<T, ? extends BullhornEntity> associationName,
        Set<Integer> associationIds) {
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
}
