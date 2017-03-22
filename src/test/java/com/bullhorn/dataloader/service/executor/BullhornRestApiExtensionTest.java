package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.TestUtils;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class BullhornRestApiExtensionTest {

    private BullhornRestApi bullhornRestApiMock;

    @Before
    public void setup() {
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
    }

    @Test
    public void testPostDelete_JobSubmission() throws InstantiationException, IllegalAccessException {
        //When soft-deleting a JobSubmission, JobSubmissionHistory records should also be hard-deleted
        List<JobSubmissionHistory> jshList = TestUtils.getListWrapper(JobSubmissionHistory.class, 1, 2, 3).getData();
        CrudResponse crudResponse_jsArg = getDeleteCrudResponse("JobSubmission", 1, "UPDATE", null);
        CrudResponse crudResponse_jshArg = getDeleteCrudResponse("JobSubmissionHistory", 1, "DELETE", null);
        Mockito.when(bullhornRestApiMock.queryForList(eq(JobSubmissionHistory.class), any(), any(), any())).thenReturn(jshList);
        Mockito.when(bullhornRestApiMock.deleteEntity(eq(JobSubmissionHistory.class), any())).thenReturn(crudResponse_jshArg);

        CrudResponse actualCrudResponse = BullhornRestApiExtension.postDelete(bullhornRestApiMock, crudResponse_jsArg);

        Assert.assertTrue(actualCrudResponse.getMessages().isEmpty());
    }

    @Test
    public void testPostDelete_JobSubmission_FAIL() throws InstantiationException, IllegalAccessException {
        //When (soft-)deleting NOT a JobSubmission, History records should NOT also be hard-deleted
        List<JobSubmissionHistory> jshList = TestUtils.getListWrapper(JobSubmissionHistory.class, 1, 2, 3).getData();
        CrudResponse crudResponse_jsArg = getDeleteCrudResponse("JobSubmission", 1, "UPDATE", null);
        CrudResponse crudResponse_jshArg = getDeleteCrudResponse("JobSubmissionHistory", 1, "DELETE", "This text makes it fail.");
        Mockito.when(bullhornRestApiMock.queryForList(eq(JobSubmissionHistory.class), any(), any(), any())).thenReturn(jshList);
        Mockito.when(bullhornRestApiMock.deleteEntity(eq(JobSubmissionHistory.class), any())).thenReturn(crudResponse_jshArg);

        CrudResponse actualCrudResponse = BullhornRestApiExtension.postDelete(bullhornRestApiMock, crudResponse_jsArg);

        Assert.assertTrue(actualCrudResponse.isError());
    }

    @Test
    public void testPostDelete_Not_JobSubmission() throws InstantiationException, IllegalAccessException {
        CrudResponse crudResponse = getDeleteCrudResponse("Candidate", 1, "UPDATE", null);

        CrudResponse actualCrudResponse = BullhornRestApiExtension.postDelete(bullhornRestApiMock, crudResponse);

        Assert.assertThat(crudResponse, new ReflectionEquals(actualCrudResponse));
        Assert.assertTrue(!actualCrudResponse.isError());
        Mockito.verify(bullhornRestApiMock, Mockito.never()).deleteEntity(eq(JobSubmissionHistory.class), any());
    }

    private CrudResponse getDeleteCrudResponse(String changedEntityType, Integer changedEntityId, String changeType, String message) {
        CrudResponse crudResponse = new DeleteResponse();
        crudResponse.setChangedEntityType(changedEntityType);
        crudResponse.setChangeType(changeType);
        List<Message> messages = new ArrayList<>();
        if(null != message) {
            Message newMessage = new Message();
            newMessage.setDetailMessage(message);
            newMessage.setType("ERROR");
            newMessage.setSeverity("ERROR");
            messages.add(newMessage );
        }
        crudResponse.setMessages(messages);
        crudResponse.setChangedEntityId(changedEntityId);

        return crudResponse;
    }
}
