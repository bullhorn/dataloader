package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetaServiceTest {

    private PrintUtil printUtilMock;
    private RestApi restApiMock;
    private RestSession restSessionMock;
    private MetaService metaService;

    @Before
    public void setup() {
        printUtilMock = mock(PrintUtil.class);
        restSessionMock = mock(RestSession.class);
        restApiMock = mock(RestApi.class);

        metaService = new MetaService(restSessionMock, printUtilMock);

        // Mock out meta fields
        Field idField = TestUtils.createField("id", null, null, null, "SCALAR", "Integer");
        Field emailField = TestUtils.createField("email", "Email", "", "", "SCALAR", "String");
        Field externalIdField = TestUtils.createField("externalID", "External ID", "", "", "SCALAR", "String");
        Field commentsField = TestUtils.createField("comments", "Comments", "General Comments",
            "Place for general comments about the record", "SCALAR", "String");
        Field customTextField = TestUtils.createField("customText1", "Favorite Food", "What is the person's favorite food?",
            "Useful sometimes", "SCALAR", "String");
        Field ownerField = TestUtils.createField("owner", "Recruiter", "", "", "TO_ONE", "");
        StandardMetaData<CorporateUser> corporateUserMeta = new StandardMetaData<>();
        corporateUserMeta.setEntity("CorporateUser");
        corporateUserMeta.setLabel("Recruiter");
        ownerField.setAssociatedEntity(corporateUserMeta);

        // Mock out Candidate meta data
        StandardMetaData<Candidate> candidateMeta = new StandardMetaData<>();
        candidateMeta.setEntity("Candidate");
        candidateMeta.setLabel("Employee");
        candidateMeta.setFields(Arrays.asList(idField, emailField, externalIdField, commentsField, customTextField, ownerField));

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(candidateMeta);
    }

    @Test
    public void testRunCandidate() {
        String[] testArgs = {Command.META.getMethodName(), EntityInfo.CANDIDATE.getEntityName()};
        ArgumentCaptor stringCaptor = ArgumentCaptor.forClass(String.class);

        metaService.run(testArgs);

        verify(printUtilMock, times(1)).log("Getting meta for Candidate...");
        verify(printUtilMock, times(1)).log("Done generating meta for Candidate");
        verify(printUtilMock, times(1)).print((String) stringCaptor.capture());

        String jsonPrinted = (String) stringCaptor.getValue();
        JSONObject meta = new JSONObject(jsonPrinted);
        Assert.assertEquals(meta.get("entity"), "Candidate");
        Assert.assertEquals(meta.get("label"), "Employee");
        JSONArray fields = meta.getJSONArray("fields");
        TestUtils.checkJsonObject(fields.getJSONObject(0), "name,label,description,hint", "email,Email,,");
        TestUtils.checkJsonObject(fields.getJSONObject(1), "name,label,description,hint", "externalID,External ID,,");
        TestUtils.checkJsonObject(fields.getJSONObject(2), "name,label,description,hint",
            "comments,Comments,General Comments,Place for general comments about the record");
        TestUtils.checkJsonObject(fields.getJSONObject(3), "name,label,description,hint",
            "customText1,Favorite Food,What is the person's favorite food?,Useful sometimes");
        TestUtils.checkJsonObject(fields.getJSONObject(4), "name,label",
            "owner,Recruiter");
        Assert.assertEquals(fields.getJSONObject(4).getJSONObject("associatedEntity").getString("entity"), "CorporateUser");
    }

    @Test(expected = RestApiException.class)
    public void testRunBadConnection() {
        when(restSessionMock.getRestApi()).thenThrow(new RestApiException());

        metaService.run(new String[]{Command.META.getMethodName(), "Candidate"});

        verify(printUtilMock, times(1)).printAndLog("Failed to create REST session.");
    }

    @Test
    public void testRunMetaCallException() {
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null))
            .thenThrow(new RestApiException("Meta Error"));

        metaService.run(new String[]{Command.META.getMethodName(), "Candidate"});

        verify(printUtilMock, times(1)).log("Getting meta for Candidate...");
        verify(printUtilMock, times(1)).printAndLog("ERROR: Failed to get Meta for Candidate");
    }

    @Test
    public void testIsValidArguments() {
        String[] testArgs = {Command.META.getMethodName(), "Candidate"};

        boolean actualResult = metaService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() {
        String[] testArgs = {Command.META.getMethodName()};

        boolean actualResult = metaService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArguments() {
        String[] testArgs = {Command.META.getMethodName(), "Candidate", "tooMany"};

        boolean actualResult = metaService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() {
        String[] testArgs = {Command.META.getMethodName(), "BadActors"};

        boolean actualResult = metaService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyEntity() {
        String[] testArgs = {Command.META.getMethodName(), ""};

        boolean actualResult = metaService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}
