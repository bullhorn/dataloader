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

import java.util.ArrayList;
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
        Field nameField = TestUtils.createField("name", "Name", "", "", "SCALAR", "String");
        Field emailField = TestUtils.createField("email", "Email", "", "", "SCALAR", "String");
        Field commentsField = TestUtils.createField("comments", "Comments", "General Comments",
            "Place for general comments about the record", "SCALAR", "String");
        Field customTextField = TestUtils.createField("customText1", "Favorite Food", "What is the person's favorite food?",
            "Useful sometimes", "SCALAR", "String");
        Field customIntField = TestUtils.createField("customInt100", "Brand new field", "", "", "SCALAR", "Integer");
        Field ownerField = TestUtils.createField("owner", "Recruiter", "", "", "TO_ONE", "");
        StandardMetaData<CorporateUser> corporateUserMeta = new StandardMetaData<>();
        corporateUserMeta.setEntity("CorporateUser");
        corporateUserMeta.setLabel("Recruiter");
        corporateUserMeta.setFields(new ArrayList<>(Arrays.asList(idField, nameField)));
        ownerField.setAssociatedEntity(corporateUserMeta);
        Field addressField = TestUtils.createField("address", "Address", "", "", "COMPOSITE", "ADDRESS_BLOCK");
        Field address1Field = TestUtils.createField("address1", "Street Address", "", "", "SCALAR", "String");
        Field cityField = TestUtils.createField("city", "City", "", "", "SCALAR", "String");
        addressField.setFields(new ArrayList<>(Arrays.asList(address1Field, cityField)));

        // Mock out Candidate meta data
        StandardMetaData<Candidate> candidateMeta = new StandardMetaData<>();
        candidateMeta.setEntity("Candidate");
        candidateMeta.setLabel("Employee");
        candidateMeta.setFields(new ArrayList<>(Arrays.asList(idField, emailField, commentsField, customTextField, customIntField, ownerField, addressField)));

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(candidateMeta);
    }

    @Test
    public void testRunCandidate() {
        String[] testArgs = {Command.META.getMethodName(), EntityInfo.CANDIDATE.getEntityName()};
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        metaService.run(testArgs);

        verify(printUtilMock, times(1)).log("Getting meta for Candidate...");
        verify(printUtilMock, times(1)).log("Removed Candidate field: customInt100 that does not exist in SDK-REST.");
        verify(printUtilMock, times(1)).log("Added Candidate field: externalID that was not in Meta.");
        verify(printUtilMock, times(1)).log("Done generating meta for Candidate");
        verify(printUtilMock, times(1)).log("Done generating meta for Candidate");
        verify(printUtilMock, times(1)).print(stringCaptor.capture());

        String jsonPrinted = stringCaptor.getValue();
        JSONObject meta = new JSONObject(jsonPrinted);
        Assert.assertEquals(meta.get("entity"), "Candidate");
        Assert.assertEquals(meta.get("label"), "Employee");
        JSONArray fields = meta.getJSONArray("fields");
        TestUtils.checkJsonObject(fields.getJSONObject(0), "name", "id");
        TestUtils.checkJsonObject(fields.getJSONObject(1), "name,label,description,hint", "email,Email,,");
        TestUtils.checkJsonObject(fields.getJSONObject(2), "name,label,description,hint",
            "comments,Comments,General Comments,Place for general comments about the record");
        TestUtils.checkJsonObject(fields.getJSONObject(3), "name,label,description,hint",
            "customText1,Favorite Food,What is the person's favorite food?,Useful sometimes");

        JSONObject ownerField = fields.getJSONObject(4);
        TestUtils.checkJsonObject(ownerField, "name,label", "owner,Recruiter");
        JSONObject ownerAssociation = ownerField.getJSONObject("associatedEntity");
        Assert.assertEquals(ownerAssociation.getString("entity"), "CorporateUser");
        JSONArray ownerAssociationFields = ownerAssociation.getJSONArray("fields");
        TestUtils.checkJsonObject(ownerAssociationFields.getJSONObject(0), "name", "id");
        TestUtils.checkJsonObject(ownerAssociationFields.getJSONObject(1), "name", "name");

        JSONObject addressField = fields.getJSONObject(5);
        TestUtils.checkJsonObject(addressField, "name,label", "address,Address");
        JSONArray addressFields = addressField.getJSONArray("fields");
        TestUtils.checkJsonObject(addressFields.getJSONObject(0), "name", "address1");
        TestUtils.checkJsonObject(addressFields.getJSONObject(1), "name", "city");

        TestUtils.checkJsonObject(fields.getJSONObject(6), "name", "externalID");
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
