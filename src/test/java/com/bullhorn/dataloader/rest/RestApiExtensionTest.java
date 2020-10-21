package com.bullhorn.dataloader.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import com.google.common.collect.Sets;

public class RestApiExtensionTest {

    private RestApi restApiMock;
    private PrintUtil printUtilMock;
    private RestApiExtension restApiExtension;

    @Before
    public void setup() {
        restApiMock = mock(RestApi.class);
        printUtilMock = mock(PrintUtil.class);
        restApiExtension = new RestApiExtension(printUtilMock);
    }

    @Test
    public void testCheckForRestSdkErrorMessagesSuccess() {
        CrudResponse crudResponse = TestUtils.getResponse(ChangeType.INSERT, 1, "SuccessField", "");
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
    }

    @Test(expected = RestApiException.class)
    public void testCheckFOrRestSdkErrorMessagesFailure() {
        CrudResponse crudResponse = TestUtils.getResponse(ChangeType.INSERT, null, "FailureField", "Because failed");
        restApiExtension.checkForRestSdkErrorMessages(crudResponse);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetByExternalIdBuildUrl() {
        String externalID = "ext 1";
        when(restApiMock.getRestUrl()).thenReturn("https://rest.bullhorn.com/");
        when(restApiMock.getBhRestToken()).thenReturn("123456789");
        when(restApiMock.performGetRequest(any(), any(), any())).thenReturn("[{id: 1}]");

        Set fieldSet = Sets.newHashSet("name", "id");
        restApiExtension.getByExternalId(restApiMock, Candidate.class, externalID, fieldSet);

        String expectedUrl = "https://rest.bullhorn.com/services/dataLoader/getByExternalID?"
            + "entity={entity}&externalId={externalId}&fields={fields}&BhRestToken={BhRestToken}";
        Map<String, String> expectedUrlVariables = new LinkedHashMap<>();
        expectedUrlVariables.put("entity", "Candidate");
        expectedUrlVariables.put("externalId", "ext 1");
        expectedUrlVariables.put("fields", "name,id");
        expectedUrlVariables.put("BhRestToken", "123456789");
        verify(restApiMock, times(1)).performGetRequest(eq(expectedUrl), eq(String.class), eq(expectedUrlVariables));
    }

    @Test
    public void testGetByExternalIdBuildUrlNullFieldSet() {
        String externalID = "ext 1";
        when(restApiMock.getRestUrl()).thenReturn("https://rest.bullhorn.com/");
        when(restApiMock.getBhRestToken()).thenReturn("123456789");
        when(restApiMock.performGetRequest(any(), any(), any())).thenReturn("[{id: 1}]");

        restApiExtension.getByExternalId(restApiMock, Candidate.class, externalID, null);

        String expectedUrl = "https://rest.bullhorn.com/services/dataLoader/getByExternalID?"
            + "entity={entity}&externalId={externalId}&fields={fields}&BhRestToken={BhRestToken}";
        Map<String, String> expectedUrlVariables = new LinkedHashMap<>();
        expectedUrlVariables.put("entity", "Candidate");
        expectedUrlVariables.put("externalId", "ext 1");
        expectedUrlVariables.put("fields", "id");
        expectedUrlVariables.put("BhRestToken", "123456789");
        verify(restApiMock, times(1)).performGetRequest(eq(expectedUrl), eq(String.class), eq(expectedUrlVariables));
    }

    @Test
    public void testGetByExternalIdEmptyReturn() {
        when(restApiMock.performGetRequest(any(), any(), any())).thenReturn("[]");

        SearchResult searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext 1", new HashSet<>(Collections.singletonList("id")));

        Assert.assertTrue(searchResult.getSuccess());
        Assert.assertTrue(searchResult.getAuthorized());
        Assert.assertTrue(searchResult.getList().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetByExternalIdOneReturn() {
        when(restApiMock.performGetRequest(any(), any(), any())).thenReturn("[{id: 1}]");

        SearchResult searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext 1", new HashSet<>(Collections.singletonList("id")));

        Assert.assertTrue(searchResult.getSuccess());
        Assert.assertTrue(searchResult.getAuthorized());
        Assert.assertEquals(searchResult.getList().size(), 1);

        List<Candidate> candidates = searchResult.getList();
        Candidate candidate = candidates.get(0);
        Assert.assertEquals(1, (int) candidate.getId());
        Assert.assertEquals(candidate.getExternalID(), "ext 1");
    }

    @Test
    public void testGetByExternalIdMultipleReturns() {
        when(restApiMock.performGetRequest(any(), any(), any())).thenReturn("[{id: 1}, {id: 2}]");

        SearchResult searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext 1", new HashSet<>(Collections.singletonList("id")));

        Assert.assertTrue(searchResult.getSuccess());
        Assert.assertTrue(searchResult.getAuthorized());
        Assert.assertEquals(searchResult.getList().size(), 2);
    }

    @Test
    public void testGetByExternalIdUnauthorized() {
        RestApiException restApiException = new RestApiException("Missing entitlement: SI DataLoader Administration");
        when(restApiMock.performGetRequest(any(), any(), any())).thenThrow(restApiException);

        SearchResult searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext 1", new HashSet<>(Collections.singletonList("id")));

        Assert.assertFalse(searchResult.getSuccess());
        Assert.assertFalse(searchResult.getAuthorized());

        // Subsequent calls should not attempt to call the doGetByExternalID method
        searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext 2", new HashSet<>(Collections.singletonList("id")));

        String expected = "WARNING: Cannot perform fast lookup by externalID because the current user is missing the User Action Entitlement: 'SI Dataloader Administration'. Will use regular /search calls that rely on the lucene index.";
        Assert.assertFalse(searchResult.getSuccess());
        Assert.assertFalse(searchResult.getAuthorized());
        verify(restApiMock, times(1)).performGetRequest(any(), any(), any());
        verify(printUtilMock, times(1)).printAndLog(eq(expected));
    }

    @Test
    public void testGetByExternalIdFailure() {
        RestApiException restApiException = new RestApiException("Flagrant System Error");
        when(restApiMock.performGetRequest(any(), any(), any())).thenThrow(restApiException);

        SearchResult searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext1", new HashSet<>(Collections.singletonList("id")));

        String expected = "WARNING: Fast lookup failed for Candidate by externalID: 'ext1'. "
            + "Will use a regular /search call instead. Error Message: Flagrant System Error";
        verify(printUtilMock, times(1)).printAndLog(eq(expected));
        Assert.assertFalse(searchResult.getSuccess());
        Assert.assertTrue(searchResult.getAuthorized());

        // Subsequent calls should attempt to call the doGetByExternalID method, and warn user every time it failed
        searchResult = restApiExtension.getByExternalId(
            restApiMock, Candidate.class, "ext 2", new HashSet<>(Collections.singletonList("id")));

        Assert.assertFalse(searchResult.getSuccess());
        Assert.assertTrue(searchResult.getAuthorized());
        expected = "WARNING: Fast lookup failed for Candidate by externalID: 'ext 2'. "
            + "Will use a regular /search call instead. Error Message: Flagrant System Error";
        verify(printUtilMock, times(1)).printAndLog(eq(expected));
        verify(restApiMock, times(2)).performGetRequest(any(), any(), any());
    }

    @Test
    public void testPostDeleteJobSubmission() throws InstantiationException, IllegalAccessException {
        // When soft-deleting a JobSubmission, JobSubmissionHistory records should also be hard-deleted
        List<JobSubmissionHistory> jshList = TestUtils.getList(JobSubmissionHistory.class, 1, 2, 3);
        CrudResponse crudResponse_jsArg = getDeleteCrudResponse("JobSubmission", "UPDATE", null);
        CrudResponse crudResponse_jshArg = getDeleteCrudResponse("JobSubmissionHistory", "DELETE", null);
        when(restApiMock.queryForList(eq(JobSubmissionHistory.class), any(), any(), any())).thenReturn(jshList);
        when(restApiMock.deleteEntity(eq(JobSubmissionHistory.class), any())).thenReturn(crudResponse_jshArg);

        CrudResponse actualCrudResponse = restApiExtension.postDelete(restApiMock, crudResponse_jsArg);

        Assert.assertTrue(actualCrudResponse.getMessages().isEmpty());
    }

    @Test
    public void testPostDeleteJobSubmissionFailure() throws InstantiationException, IllegalAccessException {
        // When (soft-)deleting NOT a JobSubmission, History records should NOT also be hard-deleted
        List<JobSubmissionHistory> jshList = TestUtils.getList(JobSubmissionHistory.class, 1, 2, 3);
        CrudResponse crudResponse_jsArg = getDeleteCrudResponse("JobSubmission", "UPDATE", null);
        CrudResponse crudResponse_jshArg = getDeleteCrudResponse("JobSubmissionHistory", "DELETE", "This text makes it fail.");
        when(restApiMock.queryForList(eq(JobSubmissionHistory.class), any(), any(), any())).thenReturn(jshList);
        when(restApiMock.deleteEntity(eq(JobSubmissionHistory.class), any())).thenReturn(crudResponse_jshArg);

        CrudResponse actualCrudResponse = restApiExtension.postDelete(restApiMock, crudResponse_jsArg);

        Assert.assertTrue(actualCrudResponse.isError());
    }

    @Test
    public void testPostDeleteNotJobSubmission() {
        CrudResponse crudResponse = getDeleteCrudResponse("Candidate", "UPDATE", null);

        CrudResponse actualCrudResponse = restApiExtension.postDelete(restApiMock, crudResponse);

        Assert.assertTrue(new ReflectionEquals(crudResponse).matches(actualCrudResponse));
        Assert.assertFalse(actualCrudResponse.isError());
        verify(restApiMock, never()).deleteEntity(eq(JobSubmissionHistory.class), any());
    }

    private CrudResponse getDeleteCrudResponse(String changedEntityType, String changeType, String message) {
        CrudResponse crudResponse = new DeleteResponse();
        crudResponse.setChangedEntityType(changedEntityType);
        crudResponse.setChangeType(changeType);
        List<Message> messages = new ArrayList<>();
        if (null != message) {
            Message newMessage = new Message();
            newMessage.setDetailMessage(message);
            newMessage.setType("ERROR");
            newMessage.setSeverity("ERROR");
            messages.add(newMessage);
        }
        crudResponse.setMessages(messages);
        crudResponse.setChangedEntityId(1);

        return crudResponse;
    }
}
