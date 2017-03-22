package com.bullhorn.dataloader.service.executor;

import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.parameter.QueryParams;
import com.bullhornsdk.data.model.parameter.standard.StandardQueryParams;
import com.bullhornsdk.data.model.response.crud.CrudResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BullhornRestApiExtension {

    static String CHANGETYPE_UPDATE = "UPDATE";
    static String FIELDNAME_ID = "id";
    static Integer QUERYPARAM_MAX_COUNT = 500;
    static String ENTITY_JOBSUBMISSION = "JobSubmission";
    static String RESPONSETYPE_DELETERESPONSE = "DeleteResponse";
    static String WHERECLAUSETEMPLATE_JOBSUBMISSION_ID = "jobSubmission.id=%s";

    static public <C extends CrudResponse> C postDelete(BullhornRestApi bullhornRestApi, C crudResponse) {

        if(
            null != crudResponse &&                                                             //crudResponse is not null
                crudResponse.getMessages().isEmpty() &&                                         //and has no errors
                CHANGETYPE_UPDATE.equals(crudResponse.getChangeType()) &&                       //if we updated
                RESPONSETYPE_DELETERESPONSE.equals(crudResponse.getClass().getSimpleName()) &&  //when we tried to delete, then it was a soft delete
                ENTITY_JOBSUBMISSION.equals(crudResponse.getChangedEntityType())                //and we soft deleted a Job Submission record
            ) {
            crudResponse = deleteJobSubmissionHistoryRecords(bullhornRestApi, crudResponse.getChangedEntityId());
        }

        return crudResponse;
    }

    /**
     * Given a jobSubmissionId, delete all associated JobSubmissionHistory records for it.
     * @param bullhornRestApi BullhornRestApi
     * @param jobSubmissionId The id of the JobSubmission
     * @return CrudResponse with up to 1 error message if any deletes failed.
     */
    static private <C extends CrudResponse> C deleteJobSubmissionHistoryRecords(BullhornRestApi bullhornRestApi, Integer jobSubmissionId) {

        C crudResponse = null;

        //queryForList arg1
        Class jshClass = JobSubmissionHistory.class;

        //queryForList arg2
        String whereClause = String.format(WHERECLAUSETEMPLATE_JOBSUBMISSION_ID, jobSubmissionId);

        //queryForList arg3
        Set<String> fieldSet = new HashSet<String>();
        fieldSet.add(FIELDNAME_ID);

        //queryForList arg4
        QueryParams queryParams = StandardQueryParams.getInstance();
        queryParams.setCount(QUERYPARAM_MAX_COUNT);
        queryParams.setOrderBy(FIELDNAME_ID);
        queryParams.setShowTotalMatched(false);
        queryParams.setStart(0);
        queryParams.setUseDefaultQueryFilter(false);

        //List of JobSubmissionHistory records to hard-delete
        @SuppressWarnings("unchecked") List<JobSubmissionHistory> jobSubmissionHistories = bullhornRestApi.queryForList(jshClass, whereClause, fieldSet, queryParams);

        for(JobSubmissionHistory jsh : jobSubmissionHistories) {
            //hard-delete JobSubmissionHistory record
            crudResponse = bullhornRestApi.deleteEntity(JobSubmissionHistory.class, jsh.getId());

            //stop processing if there are any errors
            if(!crudResponse.getMessages().isEmpty()) {
                break;
            }
        }

        //return the last received CrudResponse
        //this will contain the first error encountered if there was one.
        return crudResponse;
    }
}
