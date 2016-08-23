package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.CandidateReference;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Skill;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.response.crud.CreateResponse;
import com.bullhornsdk.data.model.response.crud.UpdateResponse;
import com.bullhornsdk.data.model.response.list.CandidateListWrapper;
import com.bullhornsdk.data.model.response.list.ClientContactListWrapper;
import com.bullhornsdk.data.model.response.list.JobOrderListWrapper;
import com.bullhornsdk.data.model.response.list.LeadListWrapper;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import com.bullhornsdk.data.model.response.list.OpportunityListWrapper;
import com.bullhornsdk.data.model.response.list.PlacementListWrapper;
import com.bullhornsdk.data.model.response.list.SkillListWrapper;
import com.bullhornsdk.data.model.response.list.StandardListWrapper;
import com.csvreader.CsvReader;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadTaskTest {

    private ExecutorService executorServiceMock;
    private PrintUtil printUtilMock;
    private CsvReader csvReaderMock;
    private CsvFileWriter csvFileWriterMock;
    private BullhornData bullhornDataMock;
    private PropertyFileUtil propertyFileUtilMock_CandidateID;
    private PropertyFileUtil propertyFileUtilMock_CandidateExternalID;

    private LinkedHashMap<String, String> dataMap;
    private Map<String, Method> methodMap;
    private Map<String, Integer> countryNameToIdMap;

    private ArgumentCaptor<Result> resultArgumentCaptor;
    private ActionTotals actionTotalsMock;
    private LoadTask task;
    private ConcurrencyService concurrencyService;

    @Before
    public void setUp() throws Exception {
        executorServiceMock = Mockito.mock(ExecutorService.class);
        csvReaderMock = Mockito.mock(CsvReader.class);
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock_CandidateID = Mockito.mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = Mockito.mock(PropertyFileUtil.class);

        List<String> idExistField = Arrays.asList(new String[]{"id"});
        Mockito.doReturn(Optional.ofNullable(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Candidate");
        Mockito.doReturn(";").when(propertyFileUtilMock_CandidateID).getListDelimiter();

        List<String> externalIdExistField = Arrays.asList(new String[]{"externalID"});
        Mockito.doReturn(Optional.ofNullable(externalIdExistField)).when(propertyFileUtilMock_CandidateExternalID).getEntityExistFields("Candidate");
        Mockito.doReturn(";").when(propertyFileUtilMock_CandidateExternalID).getListDelimiter();

        concurrencyService = new ConcurrencyService(Command.LOAD_ATTACHMENTS, "Candidate", csvReaderMock, csvFileWriterMock, executorServiceMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        methodMap = concurrencyService.createMethodMap(Candidate.class);
        countryNameToIdMap = new LinkedHashMap<String, Integer>();
        countryNameToIdMap.put("United States", 1);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("externalID", "11");
        dataMap.put("firstName", "Load");
        dataMap.put("lastName", "Test");
        dataMap.put("primarySkills.id", "1");
        dataMap.put("address.address1", "test");
        dataMap.put("address.countryName", "USA");
        dataMap.put("owner.id", "1");

        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void insertAttachmentToDescriptionCandidateTest() throws Exception {
        Candidate candidate = new Candidate();
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        task.entity = candidate;
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");

        Assert.assertNull(candidate.getDescription());
        task.insertAttachmentToDescription();
        Assert.assertNotNull(candidate.getDescription());
    }

    @Test
    public void insertAttachmentToDescriptionClientCorporationTest() throws Exception {
        ClientCorporation corporation = new ClientCorporation();
        methodMap = concurrencyService.createMethodMap(ClientCorporation.class);
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientCorporation.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        task.entity = corporation;
        when(task.getAttachmentFilePath("ClientCorporation", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");

        // ClientCorporation uses companyDescription field instead of description
        Assert.assertNull(corporation.getCompanyDescription());
        task.insertAttachmentToDescription();
        Assert.assertNotNull(corporation.getCompanyDescription());
    }

    @Test
    public void getAttachmentFilePathTest() {
        String entityName = "Candidate";
        String externalID = "123";
        String expected = "convertedAttachments/Candidate/123.html";
        task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        String actual = task.getAttachmentFilePath(entityName, externalID);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithDescriptionMethodInMapTest() {
        String expected = "description";
        task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithDescriptionSubstringMethodInMapTest() {
        //client corp uses companyDescription field instead of description
        String expected = "companydescription";
        methodMap = concurrencyService.createMethodMap(ClientCorporation.class);
        task = new LoadTask(Command.LOAD, 1, ClientCorporation.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithNoDescriptionMethodInMapTest() {
        //placement does not have a description field
        String expected = "";
        methodMap = concurrencyService.createMethodMap(Placement.class);
        task = new LoadTask(Command.LOAD, 1, ClientCorporation.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void run_InsertSuccess() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        when(bullhornDataMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(new CandidateListWrapper());
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        CreateResponse response = new CreateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.insertEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));

        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_InsertSuccess_NoExistField() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        PropertyFileUtil propertyFileUtilMock_NoExistField = Mockito.mock(PropertyFileUtil.class);
        Mockito.doReturn(Optional.empty()).when(propertyFileUtilMock_NoExistField).getEntityExistFields("Candidate");
        Mockito.doReturn(";").when(propertyFileUtilMock_NoExistField).getListDelimiter();
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_NoExistField, bullhornDataMock, printUtilMock, actionTotalsMock));
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        CreateResponse response = new CreateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.insertEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_InsertSuccess_Note() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");

        dataMap.clear();
        dataMap.put("candidates.externalID", "1;2");
        dataMap.put("clientContacts.externalID", "3");
        dataMap.put("leads.customText1", "4");
        dataMap.put("jobOrders.externalID", "5");
        dataMap.put("opportunities.externalID", "6");
        dataMap.put("placements.customText1", "7");

        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Note.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateID, bullhornDataMock, printUtilMock, actionTotalsMock));
        Mockito.doReturn(Optional.ofNullable(null)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Note");

        final List<Candidate> candidates = new ArrayList<>();
        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        candidates.add(candidate1);
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        candidates.add(candidate2);
        final ListWrapper<Candidate> candidateListWrapper = new CandidateListWrapper();
        candidateListWrapper.setData(candidates);
        when(bullhornDataMock.search(eq(Candidate.class), eq("externalID:\"1\" OR externalID:\"2\""), any(), any())).thenReturn(candidateListWrapper);

        final List<ClientContact> clientContacts = new ArrayList<>();
        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        clientContacts.add(clientContact);
        final ListWrapper<ClientContact> clientContactListWrapper = new ClientContactListWrapper();
        clientContactListWrapper.setData(clientContacts);
        when(bullhornDataMock.search(eq(ClientContact.class), eq("externalID:\"3\""), any(), any())).thenReturn(clientContactListWrapper);

        final List<Lead> leads = new ArrayList<>();
        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        leads.add(lead);
        final ListWrapper<Lead> leadListWrapper = new LeadListWrapper();
        leadListWrapper.setData(leads);
        when(bullhornDataMock.search(eq(Lead.class), eq("customText1:\"4\""), any(), any())).thenReturn(leadListWrapper);

        final List<JobOrder> jobOrders = new ArrayList<>();
        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        jobOrders.add(jobOrder);
        final ListWrapper<JobOrder> jobOrderListWrapper = new JobOrderListWrapper();
        jobOrderListWrapper.setData(jobOrders);
        when(bullhornDataMock.search(eq(JobOrder.class), eq("externalID:\"5\""), any(), any())).thenReturn(jobOrderListWrapper);

        final List<Opportunity> opportunities = new ArrayList<>();
        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        opportunities.add(opportunity);
        final ListWrapper<Opportunity> opportunityListWrapper = new OpportunityListWrapper();
        opportunityListWrapper.setData(opportunities);
        when(bullhornDataMock.search(eq(Opportunity.class), eq("externalID:\"6\""), any(), any())).thenReturn(opportunityListWrapper);

        final List<Placement> placements = new ArrayList<>();
        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        placements.add(placement);
        final ListWrapper<Placement> placementListWrapper = new PlacementListWrapper();
        placementListWrapper.setData(placements);
        when(bullhornDataMock.search(eq(Placement.class), eq("customText1:\"7\""), any(), any())).thenReturn(placementListWrapper);

        CreateResponse response = new CreateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.insertEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));

        Mockito.verify(bullhornDataMock, Mockito.times(8)).insertEntity(any());

        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_InsertError_Note_MissingRecords() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Error occurred: candidates does not exist with id of the following values:\n\t2");
        dataMap.clear();
        dataMap.put("candidates.id", "1;2");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Note.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateID, bullhornDataMock, printUtilMock, actionTotalsMock));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        Mockito.doReturn(Optional.ofNullable(null)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Note");

        final List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1));

        final ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);

        when(bullhornDataMock.search(any(), eq("id:1 OR id:2"), any(), any())).thenReturn(listWrapper);
        CreateResponse response = new CreateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.insertEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));

        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_UpdateSuccess() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");

        UpdateResponse response = new UpdateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.updateEntity(any())).thenReturn(response);
        when(bullhornDataMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));


        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_invalidField() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("bogus", "This should fail with meaningful error because the field bogus does not exist on Candidate.");
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid field: 'bogus' does not exist on Candidate");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        when(bullhornDataMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        UpdateResponse response = new UpdateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.updateEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_invalidToOneAssociation() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("bogus.id", "This should fail with meaningful error because bogus does not exist.");
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: To-One Association: 'bogus' does not exist on Candidate");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        when(bullhornDataMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        UpdateResponse response = new UpdateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.updateEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_invalidToOneAssociationField() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("owner.bogus", "This should fail with meaningful error because the field bogus does not exist on the owner to-one association.");
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: To-One Association field: 'bogus' does not exist on CorporateUser");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        when(bullhornDataMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        UpdateResponse response = new UpdateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.updateEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_invalidToOneAddressAssociationField() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("secondaryAddress.bogus", "This should fail with meaningful error because the field bogus does not exist on the address to-one association.");
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Invalid field: 'secondaryAddress.bogus' - 'bogus' does not exist on the Address object");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        when(bullhornDataMock.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornDataMock.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornDataMock.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        UpdateResponse response = new UpdateResponse();
        response.setChangedEntityId(1);
        when(bullhornDataMock.updateEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));

        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalsMock, Mockito.times(1)).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalsMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void getAssociationFieldsTestCatch() {
        List expectedResult = new ArrayList<>();
        task = new LoadTask(Command.LOAD, 1, CandidateReference.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        List actualResult = task.getAssociationFields();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void getGetMethodTestCatch() {
        task = new LoadTask(Command.LOAD, 1, CandidateReference.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);

        boolean exceptionWasThrown = false;
        try {
            task.getGetMethod(CandidateAssociations.getInstance().businessSectors(), "nothing");
        } catch (NoSuchMethodException e) {
            exceptionWasThrown = true;
        }

        Assert.assertThat(true, new ReflectionEquals(exceptionWasThrown));
    }

    @Test
    public void getNewAssociationIdListTest() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        dataMap.put("primarySkills.id", "1;2;3");
        task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock);
        List<Skill> primarySkills = new ArrayList<>();
        Skill skillOne = new Skill();
        skillOne.setId(1);
        primarySkills.add(skillOne);
        Skill skillTwo = new Skill();
        skillTwo.setId(2);
        primarySkills.add(skillTwo);
        SkillListWrapper listWrapper = new SkillListWrapper();
        listWrapper.setData(primarySkills);
        when(bullhornDataMock.query(any(), any(), any(), any())).thenReturn(listWrapper);
        String expectedExceptionMessage = "Row 1: Error occurred: primarySkills does not exist with id of the following values:\n\t3";

        String actualExceptionMessage = "";
        try {
            task.getNewAssociationIdList("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            actualExceptionMessage = e.getMessage();
        }

        Assert.assertThat(expectedExceptionMessage, new ReflectionEquals(actualExceptionMessage));
    }

    @Test
    public void run_TestCatch() throws Exception {
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.NullPointerException");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void run_populateFieldOnEntityTestCatch() throws NoSuchMethodException, ParseException, IOException, InvocationTargetException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.text.ParseException: failure");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        Method setExternalIdMethod = Candidate.class.getMethod("setExternalID", String.class);
        when(task.convertStringToClass(eq(setExternalIdMethod), eq("11"))).thenThrow(new ParseException("failure", 1));
        when(bullhornDataMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(new CandidateListWrapper());

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void run_createEntityObjectTestCatch() throws IOException {
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Update - Multiple Records Exist. Found 2 Candidate records with the same ExistField criteria of: {externalID=11}");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        CandidateListWrapper candidateListWrapper = new CandidateListWrapper();
        List<Candidate> candidateList = new ArrayList<>();
        candidateList.add(new Candidate(1));
        candidateList.add(new Candidate(2));
        candidateListWrapper.setData(candidateList);
        when(bullhornDataMock.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(candidateListWrapper);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void addAssociationToEntityTestCatchNoThrow() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        RestApiException thrownException = new RestApiException("an association between Candidate 1 and Skill 1 already exists");
        Set associationIdList = new HashSet<>();
        associationIdList.add(1);
        when(bullhornDataMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        SkillListWrapper skillListWrapper = new SkillListWrapper();
        List<Skill> skillList = new ArrayList<>();
        Skill skill = new Skill();
        skill.setId(1);
        skillList.add(skill);
        skillListWrapper.setData(skillList);
        when(bullhornDataMock.query(any(), any(), eq(null), any())).thenReturn(skillListWrapper);
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        task.entityID = 1;

        boolean wasExceptionThrown = false;
        try {
            task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            wasExceptionThrown = true;
        }

        Assert.assertThat(false, new ReflectionEquals(wasExceptionThrown));
    }

    @Test
    public void addAssociationToEntityTestCatchThrow() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        RestApiException thrownException = new RestApiException("nope");
        Set associationIdList = new HashSet<>();
        associationIdList.add(1);
        when(bullhornDataMock.associateWithEntity(eq(Candidate.class), eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(associationIdList))).thenThrow(thrownException);
        SkillListWrapper skillListWrapper = new SkillListWrapper();
        List<Skill> skillList = new ArrayList<>();
        Skill skill = new Skill();
        skill.setId(1);
        skillList.add(skill);
        skillListWrapper.setData(skillList);
        when(bullhornDataMock.query(any(), any(), eq(null), any())).thenReturn(skillListWrapper);
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        task.entityID = 1;

        boolean wasExceptionThrown = false;
        try {
            task.addAssociationToEntity("primarySkills.id", CandidateAssociations.getInstance().primarySkills());
        } catch (RestApiException e) {
            wasExceptionThrown = true;
        }

        Assert.assertThat(true, new ReflectionEquals(wasExceptionThrown));
    }

    @Test
    public void findEntityTest_search() {
        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientContact.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        CandidateListWrapper candidateListWrapper = new CandidateListWrapper();
        List<Candidate> candidateList = new ArrayList<>();
        candidateList.add(new Candidate(1));
        candidateListWrapper.setData(candidateList);
        when(bullhornDataMock.search(any(), any(), any(), any())).thenReturn(candidateListWrapper);

        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);

        verify(bullhornDataMock, times(1)).search(any(), any(), any(), any());
    }

    @Test(expected = RestApiException.class)
    public void findEntityTest_searchReturnsEmptyList() {
        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientContact.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        CandidateListWrapper candidateListWrapper = new CandidateListWrapper();
        candidateListWrapper.setData(new ArrayList<>());
        when(bullhornDataMock.search(any(), any(), any(), any())).thenReturn(candidateListWrapper);

        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);
    }

    @Test(expected = RestApiException.class)
    public void findEntityTest_duplicates() {
        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        CandidateListWrapper candidateListWrapper = new CandidateListWrapper();
        List<Candidate> candidateList = new ArrayList<>();
        candidateList.add(new Candidate(1));
        candidateList.add(new Candidate(2));
        candidateListWrapper.setData(candidateList);
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientContact.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        when(bullhornDataMock.search(any(), any(), any(), any())).thenReturn(candidateListWrapper);

        task.findEntity("clientCorporation.id", "clientCorporation", ClientCorporation.class, Integer.class);

        verify(bullhornDataMock, times(1)).search(any(), any(), any(), any());
    }

    @Test(expected = RestApiException.class)
    public void getWhereStatement_integer() {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientContact.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        String actual = task.getWhereStatment("id", "99", int.class);

        Assert.assertEquals("id=99", actual);
    }

    @Test(expected = RestApiException.class)
    public void getWhereStatement_unsupportedType() {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientContact.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        task.getWhereStatment("comments", "my comment", double.class);
    }

    @Test(expected = RestApiException.class)
    public void getQueryStatement_unsupportedType() {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        task.getQueryStatement("comments", "my comment", double.class);
    }

    @Test
    public void updateRowProcessedCountsTest() {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        task.rowProcessedCount.set(110);
        task.updateRowProcessedCounts();

        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("Processed: 111 records.");
    }

    @Test
    public void convertStringToClassTest_Integer() throws ParseException {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        Object convertedString = task.convertStringToClass(methodMap.get("id"), "1");

        Assert.assertEquals(1, convertedString);
    }

    @Test
    public void convertStringToClassTest_Double() throws ParseException, NoSuchMethodException {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        Method method = getClass().getMethod("convertStringToClassTest_DoubleTestMethod", Double.class);

        Object convertedString = task.convertStringToClass(method, "1");

        Assert.assertEquals(1.0, convertedString);
    }

    @Test
    public void convertStringToClassTest_Boolean() throws ParseException {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        Object convertedString = task.convertStringToClass(methodMap.get("isdeleted"), "true");

        Assert.assertEquals(true, convertedString);
    }

    @Test
    public void convertStringToClassTest_DateTime() throws ParseException {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));
        DateTime now = DateTime.now();
        String dateFormatString = "MM/dd/yyyy HH:mm:ss.SSS";
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(dateFormatString);
        when(propertyFileUtilMock_CandidateExternalID.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));

        Object convertedString = task.convertStringToClass(methodMap.get("dateadded"), now.toString(dateTimeFormat));

        Assert.assertEquals(now, convertedString);
    }

    @Test
    public void convertStringToClassTest_BigDecimal() throws ParseException {
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, bullhornDataMock, printUtilMock, actionTotalsMock));

        Object convertedString = task.convertStringToClass(methodMap.get("dayrate"), "1");

        Assert.assertEquals(BigDecimal.ONE, convertedString);
    }

    public void convertStringToClassTest_DoubleTestMethod(Double test){
    }

    public <B extends BullhornEntity> ListWrapper<B> getListWrapper(Class<B> entityClass) throws IllegalAccessException, InstantiationException {
        ListWrapper<B> listWrapper = new StandardListWrapper<B>();
        B entity = entityClass.newInstance();
        entity.setId(1);
        listWrapper.setData(Arrays.asList(entity));
        return listWrapper;
    }

}
