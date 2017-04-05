package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.AssociationUtil;
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
import com.csvreader.CsvReader;
import org.apache.logging.log4j.Level;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadTaskTest {

    private ExecutorService executorServiceMock;
    private PrintUtil printUtilMock;
    private CsvReader csvReaderMock;
    private CsvFileWriter csvFileWriterMock;
    private BullhornRestApi bullhornRestApiMock;
    private PropertyFileUtil propertyFileUtilMock_CandidateID;
    private PropertyFileUtil propertyFileUtilMock_CandidateExternalID;
    private PropertyFileUtil propertyFileUtilMock_NoteID;
    private PropertyFileUtil propertyFileUtilMock_ClientCorporationExternalID;

    private Map<String, String> dataMap;
    private Map<String, Method> methodMap;
    private Map<String, Integer> countryNameToIdMap;

    private ArgumentCaptor<Result> resultArgumentCaptor;
    private ActionTotals actionTotalsMock;
    private ConcurrencyService concurrencyService;

    @Before
    public void setup() throws Exception {
        executorServiceMock = mock(ExecutorService.class);
        csvReaderMock = mock(CsvReader.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        bullhornRestApiMock = mock(BullhornRestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock_CandidateID = mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = mock(PropertyFileUtil.class);
        propertyFileUtilMock_NoteID = mock(PropertyFileUtil.class);
        propertyFileUtilMock_ClientCorporationExternalID = mock(PropertyFileUtil.class);

        List<String> idExistField = Arrays.asList(new String[]{"id"});
        doReturn(Optional.ofNullable(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Candidate");
        doReturn(";").when(propertyFileUtilMock_CandidateID).getListDelimiter();

        List<String> externalIdExistField = Arrays.asList(new String[]{"externalID"});
        doReturn(Optional.ofNullable(externalIdExistField)).when(propertyFileUtilMock_CandidateExternalID).getEntityExistFields("Candidate");
        doReturn(";").when(propertyFileUtilMock_CandidateExternalID).getListDelimiter();

        concurrencyService = new ConcurrencyService(Command.LOAD_ATTACHMENTS, EntityInfo.CANDIDATE, csvReaderMock, csvFileWriterMock, executorServiceMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);

        methodMap = concurrencyService.createMethodMap(Candidate.class);
        countryNameToIdMap = new LinkedHashMap<>();
        countryNameToIdMap.put("United States", 1);
        countryNameToIdMap.put("Canada", 2216);

        dataMap = new LinkedHashMap<>();
        dataMap.put("externalID", "11");
        dataMap.put("customDate1", "2016-08-30");
        dataMap.put("firstName", "Load");
        dataMap.put("lastName", "Test");
        dataMap.put("primarySkills.id", "1");
        dataMap.put("address.address1", "test");
        dataMap.put("address.countryName", "United States");
        dataMap.put("owner.id", "1");

        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        String dateFormatString = "yyyy-MM-dd";
        when(propertyFileUtilMock_CandidateID.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
        when(propertyFileUtilMock_CandidateExternalID.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
    }

    @Test
    public void insertAttachmentToDescriptionCandidateTest() throws Exception {
        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getDescription());

        LoadTask task = spy(new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock));
        task.init();
        task.entity = candidate;

        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        task.insertAttachmentToDescription();

        Assert.assertNotNull(candidate.getDescription());
    }

    @Test
    public void insertAttachmentToDescriptionClientCorporationTest() throws Exception {
        ClientCorporation corporation = new ClientCorporation();
        methodMap = concurrencyService.createMethodMap(ClientCorporation.class);

        // ClientCorporation uses companyDescription field instead of description
        Assert.assertNull(corporation.getCompanyDescription());

        LoadTask task = spy(new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock));
        task.init();
        task.entity = corporation;
        when(task.getAttachmentFilePath("ClientCorporation", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        task.insertAttachmentToDescription();

        Assert.assertNotNull(corporation.getCompanyDescription());
    }

    @Test
    public void getAttachmentFilePathTest() {
        String entityName = "Candidate";
        String externalID = "123";
        String expected = "convertedAttachments/Candidate/123.html";

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        String actual = task.getAttachmentFilePath(entityName, externalID);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithDescriptionMethodInMapTest() {
        String expected = "description";
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithDescriptionSubstringMethodInMapTest() {
        // ClientCorporation uses companyDescription field instead of description
        String expected = "companydescription";
        methodMap = concurrencyService.createMethodMap(ClientCorporation.class);
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithNoDescriptionMethodInMapTest() {
        // Placement does not have a description field
        String expected = "";
        methodMap = concurrencyService.createMethodMap(Placement.class);

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void run_InsertSuccess() throws Exception {
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertNewCorp_ExternalID() throws Exception {
        List<String> externalIdExistField = Arrays.asList(new String[]{"externalID"});
        doReturn(Optional.ofNullable(externalIdExistField)).when(propertyFileUtilMock_ClientCorporationExternalID).getEntityExistFields("ClientCorporation");
        doReturn(";").when(propertyFileUtilMock_ClientCorporationExternalID).getListDelimiter();

        methodMap = concurrencyService.createMethodMap(ClientCorporation.class);

        dataMap.clear();
        dataMap.put("id", "1");
        dataMap.put("externalID", "JAMCORP123");

        // Mock out all existing reference entities
        ClientCorporation clientCorporation = new ClientCorporation(1);
        clientCorporation.setExternalID("JAMCORP123");

        ClientContact clientContact = new ClientContact(1);
        clientContact.setExternalID("defaultContactJAMCORP123");

        when(bullhornRestApiMock.search(eq(ClientCorporation.class), eq("externalID:\"JAMCORP123\""), any(), any())).thenReturn(TestUtils.getListWrapper(ClientCorporation.class));
        when(bullhornRestApiMock.query(eq(ClientCorporation.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(clientCorporation));
        when(bullhornRestApiMock.query(eq(ClientContact.class), eq("clientCorporation.id=1 AND status='Archive'"), any(), any())).thenReturn(TestUtils.getListWrapper(ClientContact.class, 1));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_ClientCorporationExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(bullhornRestApiMock).updateEntity(eq(clientContact));
    }

    @Test
    public void run_InsertSuccess_MultipleAssociations() throws Exception {
        // Associate with multiple primarySkills
        dataMap.put("primarySkills.id", "1;2;3");
        Set associationIdSet = new HashSet<>(Arrays.asList(1, 2, 3));
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1 OR id=2 OR id=3"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2, 3));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        // Verify that only one association call got made for all of the associated primarySkills
        verify(bullhornRestApiMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdSet));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertSuccess_NoExistField() throws Exception {
        PropertyFileUtil propertyFileUtilMock_NoExistField = mock(PropertyFileUtil.class);
        when(propertyFileUtilMock_NoExistField.getDateParser()).thenReturn(DateTimeFormat.forPattern("yyyy-mm-dd"));
        doReturn(Optional.empty()).when(propertyFileUtilMock_NoExistField).getEntityExistFields("Candidate");
        doReturn(";").when(propertyFileUtilMock_NoExistField).getListDelimiter();
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_NoExistField, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertSuccess_Note() throws Exception {
        dataMap.clear();
        dataMap.put("candidates.externalID", "1;2");
        dataMap.put("clientContacts.externalID", "3");
        dataMap.put("leads.customText1", "4");
        dataMap.put("jobOrders.externalID", "5");
        dataMap.put("opportunities.externalID", "6");
        dataMap.put("placements.customText1", "7");

        doReturn(Optional.empty()).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Note");

        // Mock out all existing reference entities
        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"1\" OR externalID:\"2\""), any(), any())).thenReturn(TestUtils.getListWrapper(candidate1, candidate2));

        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        when(bullhornRestApiMock.search(eq(ClientContact.class), eq("externalID:\"3\""), any(), any())).thenReturn(TestUtils.getListWrapper(clientContact));

        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        when(bullhornRestApiMock.search(eq(Lead.class), eq("customText1:\"4\""), any(), any())).thenReturn(TestUtils.getListWrapper(lead));

        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        when(bullhornRestApiMock.search(eq(JobOrder.class), eq("externalID:\"5\""), any(), any())).thenReturn(TestUtils.getListWrapper(jobOrder));

        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        when(bullhornRestApiMock.search(eq(Opportunity.class), eq("externalID:\"6\""), any(), any())).thenReturn(TestUtils.getListWrapper(opportunity));

        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        when(bullhornRestApiMock.search(eq(Placement.class), eq("customText1:\"7\""), any(), any())).thenReturn(TestUtils.getListWrapper(placement));

        // Do not mock out any existing note entities
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.NOTE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));

        verify(bullhornRestApiMock, times(8)).insertEntity(any());
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_InsertError_Note_MissingRecords() throws Exception {
        dataMap.clear();
        dataMap.put("candidates.id", "1;2");
        doReturn(Optional.empty()).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Note");
        when(bullhornRestApiMock.search(any(), eq("id:1 OR id:2"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.NOTE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Error occurred: candidates does not exist with id of the following values:\n\t2");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_UpdateSuccess() throws Exception {
        when(bullhornRestApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void run_UpdateSuccess_Note() throws Exception {
        dataMap.clear();
        dataMap.put("id", "1");
        dataMap.put("candidates.externalID", "1;2");
        dataMap.put("clientContacts.externalID", "3");
        dataMap.put("leads.customText1", "4");
        dataMap.put("jobOrders.externalID", "5");
        dataMap.put("opportunities.externalID", "6");
        dataMap.put("placements.customText1", "7");

        methodMap = concurrencyService.createMethodMap(Note.class);

        List<String> idExistField = Collections.singletonList("id");
        doReturn(Optional.of(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Note");
        when(bullhornRestApiMock.search(eq(Note.class), eq("noteID:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Note.class, 1));

        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"1\" OR externalID:\"2\""), any(), any())).thenReturn(TestUtils.getListWrapper(candidate1, candidate2));

        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        when(bullhornRestApiMock.search(eq(ClientContact.class), eq("externalID:\"3\""), any(), any())).thenReturn(TestUtils.getListWrapper(clientContact));

        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        when(bullhornRestApiMock.search(eq(Lead.class), eq("customText1:\"4\""), any(), any())).thenReturn(TestUtils.getListWrapper(lead));

        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        when(bullhornRestApiMock.search(eq(JobOrder.class), eq("externalID:\"5\""), any(), any())).thenReturn(TestUtils.getListWrapper(jobOrder));

        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        when(bullhornRestApiMock.search(eq(Opportunity.class), eq("externalID:\"6\""), any(), any())).thenReturn(TestUtils.getListWrapper(opportunity));

        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        when(bullhornRestApiMock.search(eq(Placement.class), eq("customText1:\"7\""), any(), any())).thenReturn(TestUtils.getListWrapper(placement));
        when(bullhornRestApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        // Ensure that no inserts happen by returning the duplicate error message from SDK-REST, should an insert call be made
        RestApiException exception = new RestApiException("{\"errorMessage\" : \"error persisting an entity of type: NoteEntity\",\"errors\" : [ {\"propertyName\" : null,\"severity\" : \"ERROR\",\"type\" : \"DUPLICATE_VALUE\"} ],\"entityName\" : \"NoteEntity\"}");
        when(bullhornRestApiMock.insertEntity(any(NoteEntity.class))).thenThrow(exception);

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.NOTE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        verify(bullhornRestApiMock, times(1)).updateEntity(any(Note.class));
        verify(bullhornRestApiMock, times(7)).insertEntity(any(NoteEntity.class));

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
        dataMap.put("bogus", "This should fail with meaningful error because the field bogus does not exist on Candidate.");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid field: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidAddressField() throws Exception {
        dataMap.put("city", "Failville");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid address field format: 'city' Must use 'address.city' in csv header");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_validAddressField() throws Exception {
        dataMap.clear();
        dataMap.put("id", "1");
        dataMap.put("city", "Successville");
        List<String> idExistField = Collections.singletonList("id");
        doReturn(Optional.of(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("CandidateEducation");
        methodMap = concurrencyService.createMethodMap(CandidateEducation.class);
        when(bullhornRestApiMock.query(eq(CandidateEducation.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CandidateEducation.class, 1));
        when(bullhornRestApiMock.query(eq(CandidateEducation.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CandidateEducation.class, 1));
        when(bullhornRestApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE_EDUCATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void run_validAddressField_CountryName() throws Exception {
        dataMap.put("address.countryName", "Canada");
        when(bullhornRestApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        // Verify the candidate's countryID
        ArgumentCaptor<Candidate> entityArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(bullhornRestApiMock).insertEntity(entityArgumentCaptor.capture());
        Candidate actualCandidate = entityArgumentCaptor.getValue();
        Assert.assertEquals(Integer.valueOf(2216), actualCandidate.getAddress().getCountryID());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_validAddressField_CountryID() throws Exception {
        dataMap.remove("address.countryName");
        dataMap.put("address.countryID", "2216"); // Canada
        when(bullhornRestApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        // Verify the candidate's countryID
        ArgumentCaptor<Candidate> entityArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(bullhornRestApiMock).insertEntity(entityArgumentCaptor.capture());
        Candidate actualCandidate = entityArgumentCaptor.getValue();
        Assert.assertEquals(Integer.valueOf(2216), actualCandidate.getAddress().getCountryID());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void run_invalidAddressField_CountryID() throws Exception {
        dataMap.remove("address.countryName");
        dataMap.put("address.countryID", "BOGUS");
        when(bullhornRestApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.NumberFormatException: For input string: \"BOGUS\"");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidToOneAssociation() throws Exception {
        dataMap.put("bogus.id", "This should fail with meaningful error because bogus does not exist.");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: To-One Association: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidToOneAssociationField() throws Exception {
        dataMap.put("owner.bogus", "This should fail with meaningful error because the field bogus does not exist on the owner to-one association.");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: 'owner.bogus': 'bogus' does not exist on CorporateUser");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void run_invalidToOneAddressAssociationField() throws Exception {
        dataMap.put("secondaryAddress.bogus", "This should fail with meaningful error because the field bogus does not exist on the address to-one association.");
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid field: 'secondaryAddress.bogus' - 'bogus' does not exist on the Address object");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void getAssociationFieldsTestCatch() {
        List expectedResult = new ArrayList<>();

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE_REFERENCE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        List actualResult = AssociationUtil.getAssociationFields(task.entityClass);

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void getGetMethodTestCatch() {
        boolean exceptionWasThrown = false;
        try {
            LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE_REFERENCE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
            task.getGetMethod(CandidateAssociations.getInstance().businessSectors(), "nothing");
        } catch (NoSuchMethodException e) {
            exceptionWasThrown = true;
        }

        Assert.assertThat(true, new ReflectionEquals(exceptionWasThrown));
    }

    @Test
    public void getNewAssociationIdListTest() throws Exception {
        dataMap.put("primarySkills.id", "1;2;3");
        when(bullhornRestApiMock.query(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        when(bullhornRestApiMock.query(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        String expectedExceptionMessage = "Row 1: Error occurred: primarySkills does not exist with id of the following values:\n\t3";

        String actualExceptionMessage = "";
        try {
            LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
            task.getNewAssociationIdList("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            actualExceptionMessage = e.getMessage();
        }

        Assert.assertThat(expectedExceptionMessage, new ReflectionEquals(actualExceptionMessage));
    }

    @Test
    public void run_TestCatch() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.NullPointerException");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void run_createEntityObjectTestCatch() throws Exception {
        when(bullhornRestApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1, 2));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Update - Multiple Records Exist. Found 2 Candidate records with the same ExistField criteria of: {externalID=11}");
        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void addAssociationToEntityTestCatchNoThrow() throws Exception {
        Set associationIdList = new HashSet<>(Collections.singletonList(1));
        RestApiException thrownException = new RestApiException("an association between Candidate 1 and Skill 1 already exists");
        when(bullhornRestApiMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        when(bullhornRestApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.init();
        task.entityID = 1;
        task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());

        verify(printUtilMock, times(1)).log(Level.INFO, "Association from Candidate entity 1 to Skill entities [1] already exists.");
    }

    @Test
    public void addAssociationToEntityTestCatchThrow() throws Exception {
        RestApiException thrownException = new RestApiException("nope");

        Set associationIdList = new HashSet<>();
        associationIdList.add(1);
        when(bullhornRestApiMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        when(bullhornRestApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));

        boolean wasExceptionThrown = false;
        try {
            LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
            task.init();
            task.entityID = 1;
            task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            wasExceptionThrown = true;
        }

        Assert.assertThat(true, new ReflectionEquals(wasExceptionThrown));
    }

    @Test
    public void addAssociationToEntityTestCatchThrowDuplicateAssociation() throws Exception {
        RestApiException thrownException = new RestApiException("nope");
        Set associationIdList = new HashSet<>();
        associationIdList.add(1);
        when(bullhornRestApiMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        when(bullhornRestApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        when(bullhornRestApiMock.query(any(), any(), eq(null), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1, 2));
        String errorMessage = "";

        boolean wasExceptionThrown = false;
        try {
            LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
            task.entityID = 1;
            task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            errorMessage = e.getMessage();
            wasExceptionThrown = true;
        }

        Assert.assertTrue(wasExceptionThrown);
        Assert.assertTrue(errorMessage.contains("duplicate To-Many Associations"));
    }

    @Test
    public void findEntityTest_search() throws Exception {
        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        when(bullhornRestApiMock.search(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);

        verify(bullhornRestApiMock, times(1)).search(any(), any(), any(), any());
    }

    @Test
    public void findEntityTest_note() throws Exception {
        dataMap = new LinkedHashMap<>();
        dataMap.put("id", "1");
        methodMap.clear();
        methodMap = concurrencyService.createMethodMap(Note.class);
        when(bullhornRestApiMock.search(eq(Note.class), eq("noteID:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Note.class, 1));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.NOTE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_NoteID, bullhornRestApiMock,printUtilMock, actionTotalsMock);
        task.findEntity("id", "id", Note.class, Integer.class);

        verify(bullhornRestApiMock, times(1)).search(any(), any(), any(), any());
    }


    @Test(expected = RestApiException.class)
    public void findEntityTest_searchReturnsEmptyList() throws Exception {
        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        when(bullhornRestApiMock.search(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CONTACT, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);
    }

    @Test(expected = RestApiException.class)
    public void findEntityTest_duplicates() throws Exception {
        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        when(bullhornRestApiMock.search(any(), any(), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1, 2));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CONTACT, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);

        verify(bullhornRestApiMock, times(1)).search(any(), any(), any(), any());
    }

    @Test(expected = RestApiException.class)
    public void getWhereStatement_integer() {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CONTACT, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID,bullhornRestApiMock, printUtilMock, actionTotalsMock);
        String actual = task.getWhereStatement("id", "99", int.class);

        Assert.assertEquals("id=99", actual);
    }

    @Test(expected = RestApiException.class)
    public void getWhereStatement_unsupportedType() {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CLIENT_CONTACT, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.getWhereStatement("comments", "my comment", double.class);
    }

    @Test(expected = RestApiException.class)
    public void getQueryStatement_unsupportedType() {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.getQueryStatement("doubleField", "doubleValue", double.class, Note.class);
    }

    @Test
    public void updateRowProcessedCountsTest() {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.rowProcessedCount.set(110);
        task.updateRowProcessedCounts();

        verify(printUtilMock, times(1)).printAndLog("Processed: 111 records.");
    }

    @Test
    public void convertStringToClassTest_Integer() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("id"), "1");

        Assert.assertEquals(1, convertedString);
    }

    @Test
    public void convertStringToClassTest_IntegerEmptyValue() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("id"), "");

        Assert.assertEquals(0, convertedString);
    }

    @Test
    public void convertStringToClassTest_Double() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Method method = getClass().getMethod("convertStringToClassTest_DoubleTestMethod", Double.class);
        Object convertedString = task.convertStringToClass(method, "1");

        Assert.assertEquals(1.0, convertedString);
    }

    @Test
    public void convertStringToClassTest_DoubleEmptyValue() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Method method = getClass().getMethod("convertStringToClassTest_DoubleTestMethod", Double.class);
        Object convertedString = task.convertStringToClass(method, "");

        Assert.assertEquals(0.0, convertedString);
    }

    @Test
    public void convertStringToClassTest_Boolean() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("isdeleted"), "true");

        Assert.assertEquals(true, convertedString);
    }

    @Test
    public void convertStringToClassTest_BooleanNullValue() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("isdeleted"), "");

        Assert.assertEquals(false, convertedString);
    }

    @Test
    public void convertStringToClassTest_DateTime() throws Exception {
        DateTime now = DateTime.now();
        String dateFormatString = "MM/dd/yyyy HH:mm:ss.SSS";
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(dateFormatString);
        when(propertyFileUtilMock_CandidateExternalID.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("dateadded"), now.toString(dateTimeFormat));

        Assert.assertEquals(now, convertedString);
    }

    @Test(expected = DateTimeException.class)
    public void convertStringToClassTest_DateTimeException() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.convertStringToClass(methodMap.get("dateadded"), "");
    }

    @Test
    public void convertStringToClassTest_BigDecimal() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("dayrate"), "1");

        Assert.assertEquals(BigDecimal.ONE, convertedString);
    }

    @Test
    public void convertStringToClassTest_BigDecimalEmptyValue() throws Exception {
        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Object convertedString = task.convertStringToClass(methodMap.get("dayrate"), "");

        Assert.assertTrue(BigDecimal.ZERO.setScale(1).equals(convertedString));
    }

    @Test
    public void getDateQueryTest(){
        String dateFormatString = "MM/dd/yyyy";
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));

        LoadTask task = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE_EDUCATION, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        String actualResult = task.getDateQuery("08/01/2016");

        String expectedResult = "2016-08-01 00:00:00.000";
        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void insertMissingRequiredFieldExceptionTest() throws Exception {
        when(bullhornRestApiMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        when(bullhornRestApiMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(CorporateUser.class, 1));
        when(bullhornRestApiMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Skill.class, 1));
        RestApiException exception = new RestApiException("{\"errorMessage\" : \"error persisting an entity of type: Opportunity\",\"errors\" : [ {\"propertyName\" : null,\"severity\" : \"ERROR\",\"type\" : \"DUPLICATE_VALUE\"} ],\"entityName\" : \"Candidate\"}");
        when(bullhornRestApiMock.insertEntity(any())).thenThrow(exception);

        LoadTask task = spy(new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornRestApiMock, printUtilMock, actionTotalsMock));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        task.run();

        verify(task, times(1)).checkForRequiredFieldsError(exception);
    }

    /**
     * Used by the convertStringToClass_Double tests above
     */
    public void convertStringToClassTest_DoubleTestMethod(Double test){
    }
}
