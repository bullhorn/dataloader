package com.bullhorn.dataloader.rest;

import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.parameter.QueryParams;
import com.bullhornsdk.data.model.parameter.standard.StandardQueryParams;
import com.bullhornsdk.data.model.response.crud.CrudResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles implementation details for extensions to the RestApi.
 */
public class RestApiExtension {

    private static final String CHANGETYPE_UPDATE = "UPDATE";
    private static final String FIELDNAME_ID = "id";
    private static final Integer QUERYPARAM_MAX_COUNT = 500;
    private static final String ENTITY_JOBSUBMISSION = "JobSubmission";
    private static final String RESPONSETYPE_DELETERESPONSE = "DeleteResponse";
    private static final String WHERECLAUSETEMPLATE_JOBSUBMISSION_ID = "jobSubmission.id=%s";

    /**
     * Performs additional checks after a record has been deleted. For deleted job records, deletes the job submission
     * history as well.
     *
     * @param restApi The RestApi
     * @param crudResponse    The response from the RestApi
     * @return The updated crud response after additional behavior
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
     * @param restApi The RestApi object
     * @param jobSubmissionId The ID of the JobSubmission that was deleted
     * @return CrudResponse with up to 1 error message if any deletes failed.
     */
    private <C extends CrudResponse> C deleteJobSubmissionHistoryRecords(RestApi restApi, Integer jobSubmissionId) {

        C crudResponse = null;

        // queryForList arg1
        Class jshClass = JobSubmissionHistory.class;

        // queryForList arg2
        String whereClause = String.format(WHERECLAUSETEMPLATE_JOBSUBMISSION_ID, jobSubmissionId);

        // queryForList arg3
        Set<String> fieldSet = new HashSet<String>();
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
