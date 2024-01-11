package com.bullhorn.dataloader.service;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.paybill.optionslookup.SimplifiedOptionsLookup;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.WorkersCompensationRate;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.google.common.collect.Sets;

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

        // Mock out Candidate meta fields
        Field idField = TestUtils.createField("id", "ID", null, null, "ID", "Integer");
        Field nameField = TestUtils.createField("name", "Name", "", "", "SCALAR", "String");
        Field emailField = TestUtils.createField("email", "Email", "", "", "SCALAR", "String");
        Field commentsField = TestUtils.createField("comments", "Comments", "General Comments",
            "Place for general comments about the record", "SCALAR", "String");
        Field customTextField = TestUtils.createField("customText1", "Favorite Food", "What is the person's favorite food?",
            "Useful sometimes", "SCALAR", "String");
        Field customIntField = TestUtils.createField("customInt100", "Brand new field", "", "", "SCALAR", "Integer");
        Field ownerField = TestUtils.createField("owner", "Recruiter", "", "", "TO_ONE", "");
        Field startDateField = TestUtils.createField("startDate", "Start Date", null, null, "SCALAR", "Timestamp");
        StandardMetaData<CorporateUser> corporateUserMeta = new StandardMetaData<>();
        corporateUserMeta.setEntity("CorporateUser");
        corporateUserMeta.setLabel("Recruiter");
        corporateUserMeta.setFields(new ArrayList<>(Arrays.asList(idField, nameField)));
        ownerField.setAssociatedEntity(corporateUserMeta);
        Field addressField = TestUtils.createField("address", "Address", "", "", "COMPOSITE", "ADDRESS_BLOCK");
        Field address1Field = TestUtils.createField("address1", "Street Address", "", "", "SCALAR", "String");
        Field cityField = TestUtils.createField("city", "City", "", "", "SCALAR", "String");
        addressField.setFields(new ArrayList<>(Arrays.asList(address1Field, cityField)));

        // Mock out Candidate meta
        StandardMetaData<Candidate> candidateMeta = new StandardMetaData<>();
        candidateMeta.setEntity("Candidate");
        candidateMeta.setLabel("Employee");
        candidateMeta.setFields(new ArrayList<>(Arrays.asList(idField, emailField, commentsField, customTextField, customIntField, ownerField, addressField)));

        // Mock out Placement meta fields
        Field bteSyncStatusField = TestUtils.createField("bteSyncStatus", "Bte Sync Status", "A lookup field", "", "TO_ONE", "SimplifiedOptionsLookup");
        StandardMetaData<SimplifiedOptionsLookup> bteSyncStatusMeta = new StandardMetaData<>();
        bteSyncStatusMeta.setEntity("BteSyncStatusLookup");
        bteSyncStatusMeta.setLabel("Bte Sync Status Lookup");
        bteSyncStatusMeta.setFields(new ArrayList<>(Arrays.asList(idField, nameField)));
        bteSyncStatusField.setAssociatedEntity(bteSyncStatusMeta);

        // Mock out Placement meta
        StandardMetaData<Placement> placementMeta = new StandardMetaData<>();
        placementMeta.setEntity("Placement");
        placementMeta.setLabel("Placement");
        placementMeta.setFields(new ArrayList<>(Arrays.asList(idField, bteSyncStatusField, startDateField)));

        // Mock out WorkersCompensationRate meta
        StandardMetaData<WorkersCompensationRate> workersCompensationRateMeta = new StandardMetaData<>();
        workersCompensationRateMeta.setEntity("WorkersCompensationRate");
        workersCompensationRateMeta.setLabel("Workers Compensation Rate");
        workersCompensationRateMeta.setFields(new ArrayList<>(Arrays.asList(idField, startDateField)));

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.getMetaData(eq(Candidate.class), eq(MetaParameter.FULL), eq(Sets.newHashSet(StringConsts.ALL_FIELDS))))
            .thenReturn(candidateMeta);
        when(restApiMock.getMetaData(eq(Placement.class), eq(MetaParameter.FULL), eq(Sets.newHashSet(StringConsts.ALL_FIELDS))))
            .thenReturn(placementMeta);
        when(restApiMock.getMetaData(eq(WorkersCompensationRate.class), eq(MetaParameter.FULL), eq(Sets.newHashSet(StringConsts.ALL_FIELDS))))
            .thenReturn(workersCompensationRateMeta);
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

    @Test
    public void testRunPlacement() {
        String[] testArgs = {Command.META.getMethodName(), EntityInfo.PLACEMENT.getEntityName()};
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        metaService.run(testArgs);

        verify(printUtilMock, times(1)).log("Getting meta for Placement...");
        verify(printUtilMock, times(1)).log("Done generating meta for Placement");
        verify(printUtilMock, times(1)).print(stringCaptor.capture());

        String jsonPrinted = stringCaptor.getValue();
        JSONObject meta = new JSONObject(jsonPrinted);
        Assert.assertEquals(meta.get("entity"), "Placement");
        Assert.assertEquals(meta.get("label"), "Placement");
        JSONArray fields = meta.getJSONArray("fields");
        TestUtils.checkJsonObject(fields.getJSONObject(0), "name", "id");

        JSONObject bteSyncStatusField = fields.getJSONObject(1);
        TestUtils.checkJsonObject(bteSyncStatusField, "name,label,description,type", "bteSyncStatus,Bte Sync Status,A lookup field,SCALAR");
        Assert.assertFalse(bteSyncStatusField.has("associatedEntity"));
    }

    @Test
    public void testRunWorkersCompensationRate() {
        String[] testArgs = {Command.META.getMethodName(), EntityInfo.WORKERS_COMPENSATION_RATE.getEntityName()};
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        metaService.run(testArgs);

        verify(printUtilMock, times(1)).log("Getting meta for WorkersCompensationRate...");
        verify(printUtilMock, times(1)).log("Added WorkersCompensationRate field: privateLabel that was not in Meta.");
        verify(printUtilMock, times(1)).log("Done generating meta for WorkersCompensationRate");
        verify(printUtilMock, times(1)).print(stringCaptor.capture());

        String jsonPrinted = stringCaptor.getValue();
        JSONObject meta = new JSONObject(jsonPrinted);
        Assert.assertEquals(meta.get("entity"), "WorkersCompensationRate");
        Assert.assertEquals(meta.get("label"), "Workers Compensation Rate");
        JSONArray fields = meta.getJSONArray("fields");
        TestUtils.checkJsonObject(fields.getJSONObject(0), "name,label,type,dataType", "id,ID,ID,Integer");
        TestUtils.checkJsonObject(fields.getJSONObject(1), "name,label,type,dataType", "startDate,Start Date,SCALAR,Timestamp");

        JSONObject privateLabelField = fields.getJSONObject(2);
        TestUtils.checkJsonObject(privateLabelField, "name,label,type", "privateLabel,Private Label,TO_ONE");
        JSONObject ownerAssociation = privateLabelField.getJSONObject("associatedEntity");
        Assert.assertEquals(ownerAssociation.getString("entity"), "PrivateLabel");
        JSONArray ownerAssociationFields = ownerAssociation.getJSONArray("fields");
        TestUtils.checkJsonObject(ownerAssociationFields.getJSONObject(0), "name,label,type,dataType", "id,ID,ID,Integer");
    }

    @Test(expected = RestApiException.class)
    public void testRunBadConnection() {
        when(restSessionMock.getRestApi()).thenThrow(new RestApiException());

        metaService.run(new String[]{Command.META.getMethodName(), "Candidate"});

        verify(printUtilMock, times(1)).printAndLog("Failed to create REST session.");
    }

    @Test
    public void testRunMetaCallException() {
        when(restApiMock.getMetaData(eq(Candidate.class), eq(MetaParameter.FULL), eq(Sets.newHashSet(StringConsts.ALL_FIELDS))))
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
