package com.bullhorn.dataloader.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.api.helper.RestJsonConverter;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmission;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.entity.core.standard.PrivateLabel;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import com.google.common.collect.Sets;

/**
 * Performs additional REST behavior details for operations in the RestApi.
 */
public class RestApiExtension {

    private final PrintUtil printUtil;
    private final RestJsonConverter restJsonConverter;

    // Whether the user has the 'SI Dataloader Administration' User Action Entitlement.
    // Assume we are until proven otherwise when a call that is hidden behind this entitlement fails.
    private Boolean authorized = true;

    public RestApiExtension(PrintUtil printUtil) {
        this.printUtil = printUtil;
        this.restJsonConverter = new RestJsonConverter();
    }

    /**
     * Throws exceptions if there are errors in the SDK-REST response
     *
     * @param response the CrudResponse that was returned from SDK-REST
     * @throws RestApiException when there are error messages in the response
     */
    void checkForRestSdkErrorMessages(CrudResponse response) throws RestApiException {
        if (response != null && !response.getMessages().isEmpty() && response.getChangedEntityId() == null) {
            StringBuilder sb = new StringBuilder();
            for (Message message : response.getMessages()) {
                sb.append("\tField ").append(message.getPropertyName()).append(": ").append(message.getDetailMessage()).append("\n");
            }
            throw new RestApiException(response.getChangeType() + " REST call:\n" + sb);
        }
    }

    /**
     * Searches for results by externalID. If the call ever fails due to being unauthorized, don't try again in the same
     * DataLoader session.
     *
     * @param type       the entity type
     * @param externalId the string field to search for
     * @param fieldSet   the fields to return for each entity found with the given externalID
     * @param <S>        the search entity
     * @return SearchResults for checking if the call succeeded and the list of results
     */
    <S extends SearchEntity> SearchResult<S> getByExternalId(RestApi restApi, Class<S> type, String externalId, Set<String> fieldSet) {
        SearchResult<S> searchResult = new SearchResult<>();
        searchResult.setSuccess(false);
        searchResult.setAuthorized(authorized);
        if (authorized) {
            searchResult = doGetByExternalId(restApi, type, externalId, fieldSet);
            authorized = searchResult.getAuthorized();
        }
        return searchResult;
    }

    /**
     * Bypass the fast fail of verifying if an entity is already in the system. If we have an internal id, then skip the
     * lookup, since the rest call itself will verify the id upon creation/update.
     */
    <T extends QueryEntity> List<T> bypassPrivateLabelLookupById(Class<T> type, String where, Set<String> fieldSet) {
        List<T> list = null;
        if (type.equals(PrivateLabel.class)
            && where.startsWith(StringConsts.ID + "=")
            && !fieldSet.isEmpty()
            && fieldSet.stream().findFirst().get().equals(StringConsts.ID)
        ) {
            list = new ArrayList<>();
            PrivateLabel privateLabel = new PrivateLabel();
            Integer id = Integer.valueOf(where.substring(3));
            privateLabel.setId(id);
            list.add((T)privateLabel);
        }
        return list;
    }

    /**
     * Internal method that performs the search by externalID and returns the resulting list of entities
     */
    private <S extends SearchEntity> SearchResult<S> doGetByExternalId(RestApi restApi, Class<S> type, String externalId, Set<String> fieldSet) {
        SearchResult<S> searchResult = new SearchResult<>();
        fieldSet = fieldSet == null ? Sets.newHashSet("id") : fieldSet;

        try {
            String url = restApi.getRestUrl()
                + "services/dataLoader/getByExternalID?entity={entity}&externalId={externalId}&fields={fields}&BhRestToken={BhRestToken}";

            Map<String, String> urlVariables = new LinkedHashMap<>();
            urlVariables.put("entity", type.getSimpleName());
            urlVariables.put("externalId", externalId);
            urlVariables.put("fields", String.join(",", fieldSet));
            urlVariables.put("BhRestToken", restApi.getBhRestToken());

            String jsonString = restApi.performGetRequest(url, String.class, urlVariables);

            List<S> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put(StringConsts.EXTERNAL_ID, externalId);
                list.add(restJsonConverter.jsonToEntity(jsonObject.toString(), type));
            }
            searchResult.setList(list);
        } catch (Exception e) {
            if (e.getMessage().contains("SI DataLoader Administration")) {
                printUtil.printAndLog("WARNING: Cannot perform fast lookup by externalID because the current "
                    + "user is missing the User Action Entitlement: 'SI Dataloader Administration'. "
                    + "Will use regular /search calls that rely on the lucene index.");
                searchResult.setAuthorized(false);
            } else {
                printUtil.printAndLog("WARNING: Fast lookup failed for " + type.getSimpleName()
                    + " by externalID: '" + externalId + "'. Will use a regular /search call instead. Error Message: "
                    + e.getMessage());
                searchResult.setSuccess(false);
            }
        }
        return searchResult;
    }

    /**
     * Performs additional checks after a record has been deleted. For deleted job records, deletes the job submission history records.
     *
     * @param restApi      the bullhorn rest api
     * @param crudResponse the response from the RestApi
     * @return the updated crud response after additional behavior
     */
    <C extends CrudResponse> C postDelete(RestApi restApi, C crudResponse) {
        // If the crud response is an updated response where we soft-deleted the job submission record, then hard delete its history records
        if (crudResponse != null
            && crudResponse.getMessages().isEmpty()
            && crudResponse.getChangeType().equals("UPDATE")
            && crudResponse.getClass().equals(DeleteResponse.class)
            && crudResponse.getChangedEntityType().equals(JobSubmission.class.getSimpleName())) {
            crudResponse = deleteJobSubmissionHistoryRecords(restApi, crudResponse.getChangedEntityId());
        }
        return crudResponse;
    }

    /**
     * Given a jobSubmissionId, delete all associated JobSubmissionHistory records for it.
     *
     * @param restApi         the bullhorn rest api
     * @param jobSubmissionId the ID of the JobSubmission that was deleted
     * @return CrudResponse with up to 1 error message if any deletes failed.
     */
    private <C extends CrudResponse> C deleteJobSubmissionHistoryRecords(RestApi restApi, Integer jobSubmissionId) {
        C crudResponse = null;
        final String filter = "jobSubmission.id=" + jobSubmissionId;
        List<JobSubmissionHistory> jobSubmissionHistories = restApi.queryForList(JobSubmissionHistory.class, filter,
            Sets.newHashSet(StringConsts.ID), ParamFactory.queryParams());

        for (JobSubmissionHistory jsh : jobSubmissionHistories) {
            // Hard Delete JobSubmissionHistory record
            crudResponse = restApi.deleteEntity(JobSubmissionHistory.class, jsh.getId());

            // Stop processing if there are any errors
            if (!crudResponse.getMessages().isEmpty()) {
                break;
            }
        }

        // Return the last received CrudResponse - this will contain the first error encountered if there was one.
        return crudResponse;
    }
}
