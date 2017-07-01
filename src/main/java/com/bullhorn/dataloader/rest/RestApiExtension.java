package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.api.helper.RestJsonConverter;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.QueryParams;
import com.bullhornsdk.data.model.parameter.standard.StandardQueryParams;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs additional REST behavior details for operations in the RestApi.
 */
public class RestApiExtension {

    private static final String CHANGETYPE_UPDATE = "UPDATE";
    private static final String FIELDNAME_ID = "id";
    private static final Integer QUERYPARAM_MAX_COUNT = 500;
    private static final String ENTITY_JOBSUBMISSION = "JobSubmission";
    private static final String RESPONSETYPE_DELETERESPONSE = "DeleteResponse";
    private static final String WHERECLAUSETEMPLATE_JOBSUBMISSION_ID = "jobSubmission.id=%s";

    final private PrintUtil printUtil;
    final private RestJsonConverter restJsonConverter;

    // Whether or not the current user being used to load data is authorized with the 'SI Dataloader Administration'
    // User Action Entitlement. Assume we are, until proven otherwise the first time through when a call that is
    // hidden behind this entitlement fails.
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
                sb.append("\tError occurred on field ").append(message.getPropertyName()).
                    append(" due to the following: ").append(message.getDetailMessage()).append("\n");
            }
            throw new RestApiException("Error occurred when making " + response.getChangeType() + " REST call:\n" + sb.toString());
        }
    }

    /**
     * Searches for results by externalID. If the call ever fails due to being unauthorized, don't try again in
     * the same DataLoader session.
     *
     * @param type       the entity type
     * @param externalID the string field to search for
     * @param fieldSet   the fields to return in the results for each entity found with the given externalID
     * @param <S>        the search entity
     * @return SearchResults for checking if the call succeeded and the list of results
     */
    <S extends SearchEntity> SearchResult<S> getByExternalID(RestApi restApi, Class<S> type, String externalID, Set<String> fieldSet) {
        SearchResult<S> searchResult = new SearchResult<>();
        searchResult.setSuccess(false);
        searchResult.setAuthorized(authorized);
        if (authorized) {
            searchResult = doGetByExternalID(restApi, type, externalID, fieldSet);
            authorized = searchResult.getAuthorized();
        }
        return searchResult;
    }

    /**
     * Internal method that performs the search by externalID and returns the resulting list of entities
     */
    private <S extends SearchEntity> SearchResult<S> doGetByExternalID(RestApi restApi, Class<S> type, String externalID, Set<String> fieldSet) {
        SearchResult<S> searchResult = new SearchResult<>();

        try {
            String encodedExternalID = URLEncoder.encode(externalID, "UTF-8");
            String restUrl = restApi.getRestUrl() + "services/dataLoader/getByExternalID/" + encodedExternalID;
            String bhRestToken = restApi.getBhRestToken();

            URIBuilder uriBuilder = new URIBuilder(restUrl);
            uriBuilder.addParameter("BhRestToken", bhRestToken);
            uriBuilder.addParameter("entity", type.getSimpleName());
            uriBuilder.addParameter("fields", String.join(",", fieldSet));
            String url = uriBuilder.toString();

            String jsonString = restApi.performGetRequest(url, String.class, new HashMap<>());

            List<S> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                list.add(restJsonConverter.jsonToEntityDoNotUnwrapRoot(jsonObject.toString(), type));
            }
            searchResult.setList(list);
        } catch (Exception e) {
            if (e.getMessage().contains("SI DataLoader Administration")) {
                printUtil.printAndLog("WARNING: Cannot perform fast lookup by externalID because the current user is missing the User Action Entitlement: 'SI Dataloader Administration'. Will use regular /search calls that rely on the lucene index.");
                searchResult.setAuthorized(false);
            } else if (e.getMessage().contains("Unknown or badly structured command")) {
                printUtil.printAndLog("WARNING: Cannot perform fast lookup by externalID: '" + externalID + "' because the externalID is limited to no special characters, spaces or dashes. Will use a regular /search call instead.");
                searchResult.setSuccess(false);
            } else {
                printUtil.printAndLog("WARNING: Fast lookup failed for " + type.getSimpleName() + " by externalID: '" + externalID + "'. Will use a regular /search call instead. Error Message: " + e.getMessage());
                searchResult.setSuccess(false);
            }
        }

        return searchResult;
    }

    /**
     * Performs additional checks after a record has been deleted. For deleted job records, deletes the job submission
     * history as well.*
     *
     * @param restApi      the bullhorn rest api
     * @param crudResponse the response from the RestApi
     * @return the updated crud response after additional behavior
     */
    <C extends CrudResponse> C postDelete(RestApi restApi, C crudResponse) {
        if (null != crudResponse &&                                                         // CrudResponse is not null
            crudResponse.getMessages().isEmpty() &&                                         // and has no errors
            CHANGETYPE_UPDATE.equals(crudResponse.getChangeType()) &&                       // if we updated
            RESPONSETYPE_DELETERESPONSE.equals(crudResponse.getClass().getSimpleName()) &&  // when we tried to delete, then it was a soft delete
            ENTITY_JOBSUBMISSION.equals(crudResponse.getChangedEntityType())                // and we soft deleted a Job Submission record
            ) {
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

        // queryForList arg1
        Class jshClass = JobSubmissionHistory.class;

        // queryForList arg2
        String whereClause = String.format(WHERECLAUSETEMPLATE_JOBSUBMISSION_ID, jobSubmissionId);

        // queryForList arg3
        Set<String> fieldSet = new HashSet<>();
        fieldSet.add(FIELDNAME_ID);

        // queryForList arg4
        QueryParams queryParams = StandardQueryParams.getInstance();
        queryParams.setCount(QUERYPARAM_MAX_COUNT);
        queryParams.setOrderBy(FIELDNAME_ID);
        queryParams.setShowTotalMatched(false);
        queryParams.setStart(0);
        queryParams.setUseDefaultQueryFilter(false);

        // List of JobSubmissionHistory records to hard-delete
        @SuppressWarnings("unchecked") List<JobSubmissionHistory> jobSubmissionHistories = restApi.queryForList(jshClass, whereClause, fieldSet, queryParams);

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
