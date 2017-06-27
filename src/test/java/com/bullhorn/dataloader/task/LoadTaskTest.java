package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.CandidateEducation;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.NoteEntity;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Skill;
import com.bullhornsdk.data.model.enums.ChangeType;
import org.apache.logging.log4j.Level;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadTaskTest {

    private ActionTotals actionTotalsMock;
    private RestApi restApiMock;
    private CsvFileWriter csvFileWriterMock;
    private Preloader preloaderMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;

    private ArgumentCaptor<Result> resultArgumentCaptor;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = mock(ActionTotals.class);
        restApiMock = mock(RestApi.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        preloaderMock = mock(Preloader.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);

        String dateFormatString = "yyyy-MM-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Optional.empty());

        Map<String, Integer> countryNameToIdMap = new LinkedHashMap<>();
        countryNameToIdMap.put("United States", 1);
        countryNameToIdMap.put("Canada", 2216);
        when(preloaderMock.getCountryNameToIdMap()).thenReturn(countryNameToIdMap);

        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    // TODO: Stop testing this non-public method
    @Test
    public void insertAttachmentToDescriptionCandidateTest() throws Exception {
        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getDescription());

        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        LoadTask task = spy(new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock));
        task.entity = candidate;

        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn(TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html"));
        task.insertAttachmentToDescription();

        Assert.assertNotNull(candidate.getDescription());
    }

    // TODO: Stop testing this non-public method
    @Test
    public void insertAttachmentToDescriptionClientCorporationTest() throws Exception {
        Row row = TestUtils.createRow("externalID,name","11,DL Technologoes");
        ClientCorporation corporation = new ClientCorporation();

        // ClientCorporation uses companyDescription field instead of description
        Assert.assertNull(corporation.getCompanyDescription());

        LoadTask task = spy(new LoadTask(EntityInfo.CLIENT_CORPORATION, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock));
        task.entity = corporation;
        when(task.getAttachmentFilePath("ClientCorporation", "11")).thenReturn(TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html"));
        task.insertAttachmentToDescription();

        Assert.assertNotNull(corporation.getCompanyDescription());
    }

    // TODO: Stop testing this non-public method
    @Test
    public void getAttachmentFilePathTest() throws IOException {
        String entityName = "Candidate";
        String externalID = "123";
        String expected = "convertedAttachments/Candidate/123.html";

        Row row = TestUtils.createRow("firstName,lastName,email", "John,Bullhorn,jbullhorn@gmail.com");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String actual = task.getAttachmentFilePath(entityName, externalID);

        Assert.assertEquals(expected, actual);
    }

    // TODO: Stop testing this non-public method
    @Test
    public void getDescriptionMethodWithDescriptionMethodInMapTest() throws IOException {
        String expected = "description";
        Row row = TestUtils.createRow("firstName,lastName,email", "John,Bullhorn,jbullhorn@gmail.com");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(expected, actual);
    }

    // TODO: Stop testing this non-public method
    @Test
    public void getDescriptionMethodWithDescriptionSubstringMethodInMapTest() throws IOException {
        // ClientCorporation uses companyDescription field instead of description
        String expected = "companydescription";
        Row row = TestUtils.createRow("firstName,lastName,email", "John,Bullhorn,jbullhorn@gmail.com");
        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(expected, actual);
    }

    // TODO: Stop testing this non-public method
    @Test
    public void getDescriptionMethodWithNoDescriptionMethodInMapTest() throws IOException {
        // Placement does not have a description field
        String expected = "";

        Row row = TestUtils.createRow("firstName,lastName,email", "John,Bullhorn,jbullhorn@gmail.com");
        LoadTask task = new LoadTask(EntityInfo.PLACEMENT, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String actual = task.getDescriptionMethod();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void run_InsertSuccess() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        when(propertyFileUtilMock.getEntityExistFields("Candidate")).thenReturn(Optional.of(Arrays.asList("firstName", "lastName", "email")));
        when(restApiMock.search(eq(Candidate.class), eq("firstName:\"Data\" AND lastName:\"Loader\" AND email:\"dloader@bullhorn.com\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(restApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(restApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertNewCorp_ExternalID() throws Exception {
        when(propertyFileUtilMock.getEntityExistFields("ClientCorporation")).thenReturn(Optional.of(Collections.singletonList("externalID")));
        Row row = TestUtils.createRow("id,externalID", "1,JAMCORP123");

        // Mock out all existing reference entities
        ClientCorporation clientCorporation = new ClientCorporation(1);
        clientCorporation.setExternalID("JAMCORP123");

        ClientContact clientContact = new ClientContact(1);
        clientContact.setExternalID("defaultContactJAMCORP123");

        when(restApiMock.search(eq(ClientCorporation.class), eq("externalID:\"JAMCORP123\""), any(), any())).thenReturn(TestUtils.getListWrapper(ClientCorporation.class));
        when(restApiMock.query(eq(ClientCorporation.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(clientCorporation));
        when(restApiMock.query(eq(ClientContact.class), eq("clientCorporation.id=1 AND status='Archive'"), any(), any())).thenReturn(TestUtils.getListWrapper(ClientContact.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(restApiMock).updateEntity(eq(clientContact));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void run_InsertSuccess_MultipleAssociations() throws Exception {
        // Associate with multiple primarySkills
        Row row = TestUtils.createRow(
            "externalID,firstName,lastName,email,primarySkills.id",
            "11,Data,Loader,dloader@bullhorn.com,1;2;3");
        when(propertyFileUtilMock.getEntityExistFields("Candidate")).thenReturn(Optional.of(Collections.singletonList("externalID")));
        Set associationIdSet = new HashSet<>(Arrays.asList(1, 2, 3));
        when(restApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(restApiMock.query(eq(Skill.class), eq("id=1 OR id=2 OR id=3"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2, 3));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        // Verify that only one association call got made for all of the associated primarySkills
        verify(restApiMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdSet));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertSuccess_NoExistField() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        when(restApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(restApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertSuccess_Note() throws Exception {
        Row row = TestUtils.createRow(
            "candidates.externalID,clientContacts.externalID,leads.customText1,jobOrders.externalID,opportunities.externalID,placements.customText1",
            "1;2,3,4,5,6,7");

        // Mock out all existing reference entities
        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        when(restApiMock.search(eq(Candidate.class), eq("externalID:\"1\" OR externalID:\"2\""), any(), any())).thenReturn(TestUtils.getListWrapper(candidate1, candidate2));

        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        when(restApiMock.search(eq(ClientContact.class), eq("externalID:\"3\""), any(), any())).thenReturn(TestUtils.getListWrapper(clientContact));

        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        when(restApiMock.search(eq(Lead.class), eq("customText1:\"4\""), any(), any())).thenReturn(TestUtils.getListWrapper(lead));

        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        when(restApiMock.search(eq(JobOrder.class), eq("externalID:\"5\""), any(), any())).thenReturn(TestUtils.getListWrapper(jobOrder));

        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        when(restApiMock.search(eq(Opportunity.class), eq("externalID:\"6\""), any(), any())).thenReturn(TestUtils.getListWrapper(opportunity));

        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        when(restApiMock.search(eq(Placement.class), eq("customText1:\"7\""), any(), any())).thenReturn(TestUtils.getListWrapper(placement));

        // Do not mock out any existing note entities
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        verify(restApiMock, times(8)).insertEntity(any());
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertError_Note_MissingRecords() throws Exception {
        Row row = TestUtils.createRow("candidates.id", "1;2");
        when(restApiMock.search(any(), eq("id:1 OR id:2"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Error occurred: candidates does not exist with id of the following values:\n\t2");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_UpdateSuccess() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        when(propertyFileUtilMock.getEntityExistFields("Candidate")).thenReturn(Optional.of(Collections.singletonList("externalID")));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));
        when(restApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(restApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(restApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void run_UpdateSuccess_Note() throws Exception {
        Row row = TestUtils.createRow(
            "id,candidates.externalID,clientContacts.externalID,leads.customText1,jobOrders.externalID,opportunities.externalID,placements.customText1",
            "1,1;2,3,4,5,6,7");
        when(propertyFileUtilMock.getEntityExistFields("Note")).thenReturn(Optional.of(Collections.singletonList("id")));
        when(restApiMock.search(eq(Note.class), eq("noteID:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Note.class, 1));

        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        when(restApiMock.search(eq(Candidate.class), eq("externalID:\"1\" OR externalID:\"2\""), any(), any())).thenReturn(TestUtils.getListWrapper(candidate1, candidate2));

        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        when(restApiMock.search(eq(ClientContact.class), eq("externalID:\"3\""), any(), any())).thenReturn(TestUtils.getListWrapper(clientContact));

        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        when(restApiMock.search(eq(Lead.class), eq("customText1:\"4\""), any(), any())).thenReturn(TestUtils.getListWrapper(lead));

        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        when(restApiMock.search(eq(JobOrder.class), eq("externalID:\"5\""), any(), any())).thenReturn(TestUtils.getListWrapper(jobOrder));

        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        when(restApiMock.search(eq(Opportunity.class), eq("externalID:\"6\""), any(), any())).thenReturn(TestUtils.getListWrapper(opportunity));

        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        when(restApiMock.search(eq(Placement.class), eq("customText1:\"7\""), any(), any())).thenReturn(TestUtils.getListWrapper(placement));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        // Ensure that no inserts happen by returning the duplicate error message from SDK-REST, should an insert call be made
        RestApiException exception = new RestApiException("{\"errorMessage\" : \"error persisting an entity of type: NoteEntity\",\"errors\" : [ {\"propertyName\" : null,\"severity\" : \"ERROR\",\"type\" : \"DUPLICATE_VALUE\"} ],\"entityName\" : \"NoteEntity\"}");
        when(restApiMock.insertEntity(any(NoteEntity.class))).thenThrow(exception);

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        verify(restApiMock, times(1)).updateEntity(any(Note.class));
        verify(restApiMock, times(7)).insertEntity(any(NoteEntity.class));

        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to Candidate entity 1001 already exists.");
        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to Candidate entity 1002 already exists.");
        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to ClientContact entity 1003 already exists.");
        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to Lead entity 1004 already exists.");
        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to JobOrder entity 1005 already exists.");
        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to Opportunity entity 1006 already exists.");
        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Note entity 1 to Placement entity 1007 already exists.");
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void run_invalidField() throws Exception {
        Row row = TestUtils.createRow("bogus","This should fail with meaningful error because the field bogus does not exist on Candidate.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid field: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidAddressField() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,city","Data,Loader,Failsville");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid address field format: 'city' Must use 'address.city' in csv header");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_validAddressField() throws Exception {
        Row row = TestUtils.createRow("id,city","1,Successville");
        when(propertyFileUtilMock.getEntityExistFields("CandidateEducation")).thenReturn(Optional.of(Collections.singletonList("id")));
        when(restApiMock.query(eq(CandidateEducation.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CandidateEducation.class, 1));
        when(restApiMock.query(eq(CandidateEducation.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CandidateEducation.class, 1));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_EDUCATION, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void run_validAddressField_CountryName() throws Exception {
        Row row = TestUtils.createRow("externalID,firstName,lastName,address.countryName","11,Data,Loader,Canada");
        when(propertyFileUtilMock.getEntityExistFields("Candidate")).thenReturn(Optional.of(Collections.singletonList("externalID")));
        when(restApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(restApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(restApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        // Verify the candidate's countryID
        ArgumentCaptor<Candidate> entityArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(restApiMock).insertEntity(entityArgumentCaptor.capture());
        Candidate actualCandidate = entityArgumentCaptor.getValue();
        Assert.assertEquals(Integer.valueOf(2216), actualCandidate.getAddress().getCountryID());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_validAddressField_CountryID() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,address.countryID","Data,Loader,2216"); // 2216 = Canada
        when(restApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(restApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        // Verify the candidate's countryID
        ArgumentCaptor<Candidate> entityArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(restApiMock).insertEntity(entityArgumentCaptor.capture());
        Candidate actualCandidate = entityArgumentCaptor.getValue();
        Assert.assertEquals(Integer.valueOf(2216), actualCandidate.getAddress().getCountryID());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_invalidAddressField_CountryID() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,address.countryName","Data,Loader,BOGUS");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.NumberFormatException: For input string: \"BOGUS\"");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidToOneAssociation() throws Exception {
        Row row = TestUtils.createRow("bogus.id","This should fail with meaningful error because bogus does not exist.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: To-One Association: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidToOneAssociationField() throws Exception {
        Row row = TestUtils.createRow("owner.bogus","This should fail with meaningful error because the field bogus does not exist on the owner to-one association.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: 'owner.bogus': 'bogus' does not exist on CorporateUser");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidToOneAddressAssociationField() throws Exception {
        Row row = TestUtils.createRow("secondaryAddress.bogus", "This should fail with meaningful error because the field bogus does not exist on the address to-one association.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid field: 'secondaryAddress.bogus' - 'bogus' does not exist on the Address object");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    // TODO: Stop testing this internal method
    @Test
    public void getGetMethodTestCatch() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        boolean exceptionWasThrown = false;
        try {
            LoadTask task = new LoadTask(EntityInfo.CANDIDATE_REFERENCE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
            task.getGetMethod(CandidateAssociations.getInstance().businessSectors(), "nothing");
        } catch (NoSuchMethodException e) {
            exceptionWasThrown = true;
        }

        Assert.assertThat(true, new ReflectionEquals(exceptionWasThrown));
    }

    @Test
    public void getNewAssociationIdListTest() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,primarySkills.id","Data,Loader,1;2;3");
        when(restApiMock.query(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        String expectedExceptionMessage = "Row 1: Error occurred: primarySkills does not exist with id of the following values:\n\t3";

        String actualExceptionMessage = "";
        try {
            LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
            task.getNewAssociationIdList("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            actualExceptionMessage = e.getMessage();
        }

        Assert.assertThat(expectedExceptionMessage, new ReflectionEquals(actualExceptionMessage));
    }

    @Test
    public void run_TestCatch() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.NullPointerException");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void testRunMultipleExistingRecords() throws Exception {
        Row row = TestUtils.createRow("externalID,firstName,lastName","11,Data,Loader");
        when(propertyFileUtilMock.getEntityExistFields("Candidate")).thenReturn(Optional.of(Collections.singletonList("externalID")));
        when(restApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1, 2));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Update - Multiple Records Exist. Found 2 Candidate records with the same ExistField criteria of: {externalID=11}");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    // TODO: Stop testing internals - do this through the run method
    @Test
    public void addAssociationToEntityTestCatchNoThrow() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        Set associationIdList = new HashSet<>(Collections.singletonList(1));
        RestApiException thrownException = new RestApiException("an association between Candidate 1 and Skill 1 already exists");
        when(restApiMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        when(restApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.entityID = 1;
        task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());

        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Candidate entity 1 to Skill entities [1] already exists.");
    }

    // TODO: Stop testing internals - do this through the run method
    @Test
    public void addAssociationToEntityTestCatchThrow() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        RestApiException thrownException = new RestApiException("nope");
        Set associationIdList = new HashSet<>(Collections.singletonList(1));
        when(restApiMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        when(restApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(restApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        boolean wasExceptionThrown = false;
        try {
            LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
            task.entityID = 1;
            task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            wasExceptionThrown = true;
        }

        Assert.assertThat(true, new ReflectionEquals(wasExceptionThrown));
    }

    // TODO: Stop testing internals - do this through the run method
    @Test
    public void addAssociationToEntityTestCatchThrowDuplicateAssociation() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryName,owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,United States,1,");
        RestApiException thrownException = new RestApiException("nope");
        Set associationIdList = new HashSet<>();
        associationIdList.add(1);
        when(restApiMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        when(restApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        when(restApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        String errorMessage = "";

        boolean wasExceptionThrown = false;
        try {
            LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
            task.entityID = 1;
            task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            errorMessage = e.getMessage();
            wasExceptionThrown = true;
        }

        Assert.assertTrue(wasExceptionThrown);
        Assert.assertTrue(errorMessage.contains("duplicate To-Many Associations"));
    }

    // TODO: Stop testing internals - do this through the run method
    @Test
    public void findEntityTest_search() throws Exception {
        Row row = TestUtils.createRow("clientCorporation.id", "1");
        when(restApiMock.search(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);

        verify(restApiMock, times(1)).search(any(), any(), any(), any());
    }

    // TODO: Stop testing internals - do this through the run method
    @Test
    public void findEntityTest_note() throws Exception {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.search(eq(Note.class), eq("noteID:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Note.class, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("id", "id", Note.class, Integer.class);

        verify(restApiMock, times(1)).search(any(), any(), any(), any());
    }

    // TODO: Stop testing internals - do this through the run method
    @Test(expected = RestApiException.class)
    public void findEntityTest_searchReturnsEmptyList() throws Exception {
        Row row = TestUtils.createRow("clientCorporation.id", "1");
        when(restApiMock.search(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CONTACT, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);
    }

    // TODO: Stop testing internals - do this through the run method
    @Test(expected = RestApiException.class)
    public void findEntityTest_duplicates() throws Exception {
        Row row = TestUtils.createRow("clientCorporation.id","1");
        when(restApiMock.search(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1, 2));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CONTACT, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);

        verify(restApiMock, times(1)).search(any(), any(), any(), any());
    }

    // TODO: Move to query builder
    @Test(expected = RestApiException.class)
    public void getWhereStatement_integer() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CLIENT_CONTACT, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String actual = task.getWhereStatement("id", "99", int.class);

        Assert.assertEquals("id=99", actual);
    }

    // TODO: Move to query builder
    @Test(expected = RestApiException.class)
    public void getWhereStatement_unsupportedType() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CLIENT_CONTACT, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.getWhereStatement("comments", "my comment", double.class);
    }

    // TODO: Move to query builder
    @Test(expected = RestApiException.class)
    public void getQueryStatement_unsupportedType() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.getQueryStatement("doubleField", "doubleValue", double.class, Note.class);
    }

    // TODO: Move to query builder
    @Test
    public void updateRowProcessedCountsTest() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.rowProcessedCount.set(110);
        task.updateRowProcessedCounts();

        verify(printUtilMock, times(1)).printAndLog("Processed: 111 records.");
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_IntegerEmptyValue() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("id"), "");

        Assert.assertEquals(0, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_Integer() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("id"), "1");

        Assert.assertEquals(1, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_Double() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Method method = getClass().getMethod("convertStringToClassTest_DoubleTestMethod", Double.class);
        Object convertedString = task.convertStringToClass(method, "1");

        Assert.assertEquals(1.0, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_DoubleEmptyValue() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Method method = getClass().getMethod("convertStringToClassTest_DoubleTestMethod", Double.class);
        Object convertedString = task.convertStringToClass(method, "");

        Assert.assertEquals(0.0, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_Boolean() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("isdeleted"), "true");

        Assert.assertEquals(true, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_BooleanNullValue() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("isdeleted"), "");

        Assert.assertEquals(false, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_DateTime() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        DateTime now = DateTime.now();
        String dateFormatString = "MM/dd/yyyy HH:mm:ss.SSS";
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(dateFormatString);
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("dateadded"), now.toString(dateTimeFormat));

        Assert.assertEquals(now, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_EmptyDateTime() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("dateadded"), "");
        Assert.assertEquals(null, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_UnknownValue() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("tearsheets"), "");
        Assert.assertEquals(null, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_BigDecimal() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("dayrate"), "1");

        Assert.assertEquals(BigDecimal.ONE, convertedString);
    }

    // TODO: Move to query builder
    @Test
    public void convertStringToClassTest_BigDecimalEmptyValue() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        Object convertedString = task.convertStringToClass(methodMap.get("dayrate"), "");

        Assert.assertTrue(BigDecimal.ZERO.setScale(1).equals(convertedString));
    }

    // TODO: Move to query builder
    @Test
    public void getDateQueryTest() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName","Data,Loader");
        String dateFormatString = "MM/dd/yyyy";
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_EDUCATION, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String actualResult = task.getDateQuery("08/01/2016");

        String expectedResult = "2016-08-01 00:00:00.000";
        Assert.assertEquals(expectedResult, actualResult);
    }

    /**
     * Used by the convertStringToClass_Double tests above
     */
    public void convertStringToClassTest_DoubleTestMethod(Double test) {
    }
}
