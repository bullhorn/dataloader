package com.bullhorn.dataloader.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
import com.bullhornsdk.data.model.entity.core.customobjectinstances.clientcorporation.ClientCorporationCustomObjectInstance2;
import com.bullhornsdk.data.model.entity.core.customobjectinstances.person.PersonCustomObjectInstance2;
import com.bullhornsdk.data.model.entity.core.standard.Appointment;
import com.bullhornsdk.data.model.entity.core.standard.AppointmentAttendee;
import com.bullhornsdk.data.model.entity.core.standard.BusinessSector;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.CandidateWorkHistory;
import com.bullhornsdk.data.model.entity.core.standard.Category;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Person;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Skill;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LoadTaskTest {

    private ActionTotals actionTotalsMock;
    private RestApi restApiMock;
    private CsvFileWriter csvFileWriterMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Cache cacheMock;
    private CompleteUtil completeUtilMock;

    @Before
    public void setup() {
        actionTotalsMock = mock(ActionTotals.class);
        restApiMock = mock(RestApi.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        cacheMock = mock(Cache.class);
        completeUtilMock = mock(CompleteUtil.class);

        String dateFormatString = "yyyy-MM-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
        when(propertyFileUtilMock.getProcessEmptyAssociations()).thenReturn(Boolean.FALSE);
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList());
        when(cacheMock.getEntry(any(), any(), any())).thenReturn(null);
    }

    @Test
    public void testRunInsertSuccess() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryID," +
                "owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,1,1,");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Arrays.asList("firstName", "lastName", "email"));
        when(restApiMock.searchForList(eq(Candidate.class),
            eq("firstName:\"Data\" AND lastName:\"Loader\" AND email:\"dloader@bullhorn.com\""), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class));
        when(restApiMock.queryForList(eq(CorporateUser.class), eq("id=1"), any(), any()))
            .thenReturn(TestUtils.getList(CorporateUser.class, 1));
        when(restApiMock.queryForList(eq(Skill.class), eq("(id=1)"), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertNewCorpUsingExternalID() throws Exception {
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CLIENT_CORPORATION))
            .thenReturn(Collections.singletonList("externalID"));
        Row row = TestUtils.createRow("id,externalID", "1,CORPORATION_123");

        // Mock out all existing reference entities
        ClientCorporation clientCorporation = new ClientCorporation(1);
        clientCorporation.setExternalID("CORPORATION_123");

        ClientContact clientContact = new ClientContact(1);
        clientContact.setExternalID("defaultContactCORPORATION_123");

        when(restApiMock.searchForList(eq(ClientCorporation.class), eq("externalID:\"CORPORATION_123\""), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporation.class));
        when(restApiMock.queryForList(eq(ClientCorporation.class), eq("id=1"), any(), any()))
            .thenReturn(TestUtils.getList(clientCorporation));
        when(restApiMock.queryForList(eq(ClientContact.class), eq("clientCorporation.id=1 AND status='Archive'"),
            any(), any())).thenReturn(TestUtils.getList(ClientContact.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(restApiMock).updateEntity(eq(clientContact));
    }

    @Test
    public void testRunInsertSuccessForMultipleAssociations() throws Exception {
        // Associate with multiple primarySkills
        Row row = TestUtils.createRow(
            "externalID,firstName,lastName,email,primarySkills.id",
            "11,Data,Loader,dloader@bullhorn.com,1;2;3");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Collections.singletonList("externalID"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"11\""), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class));
        when(restApiMock.queryForList(eq(Skill.class), eq("(id=1 OR id=2 OR id=3)"), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2, 3));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        // Verify that only one association call got made for all of the associated primarySkills
        verify(restApiMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1),
            eq(CandidateAssociations.getInstance().primarySkills()), eq(Arrays.asList(1, 2, 3)));
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertSuccessWithNoExistField() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryID," +
                "owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,1,1,");
        when(restApiMock.queryForList(eq(CorporateUser.class), eq("id=1"), any(), any()))
            .thenReturn(TestUtils.getList(CorporateUser.class, 1));
        when(restApiMock.queryForList(eq(Skill.class), eq("(id=1)"), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertSuccessForNote() throws Exception {
        Row row = TestUtils.createRow(
            "candidates.externalID,clientContacts.externalID,leads.customText1,jobOrders.externalID,"
                + "opportunities.externalID,placements.customText1",
            "1;2,3,4,5,6,7");

        // Mock out all existing reference entities
        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        when(restApiMock.searchForList(eq(Candidate.class),
            eq("(externalID:\"1\" OR externalID:\"2\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(candidate1, candidate2));

        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        when(restApiMock.searchForList(eq(ClientContact.class),
            eq("(externalID:\"3\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(clientContact));

        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        when(restApiMock.searchForList(eq(Lead.class),
            eq("(customText1:\"4\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(lead));

        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        when(restApiMock.searchForList(eq(JobOrder.class),
            eq("(externalID:\"5\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(jobOrder));

        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        when(restApiMock.searchForList(eq(Opportunity.class),
            eq("(externalID:\"6\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(opportunity));

        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        when(restApiMock.searchForList(eq(Placement.class), eq("(customText1:\"7\")"), any(), any()))
            .thenReturn(TestUtils.getList(placement));

        // Create expected note object in insertEntity call
        Note expected = new Note();
        expected.setCandidates(new OneToMany<>(candidate1, candidate2));
        expected.setClientContacts(new OneToMany<>(clientContact));
        expected.setLeads(new OneToMany<>(lead));
        expected.setJobOrders(new OneToMany<>(jobOrder));
        expected.setOpportunities(new OneToMany<>(opportunity));
        expected.setPlacements(new OneToMany<>(placement));
        when(restApiMock.insertEntity(eq(expected))).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, times(1)).insertEntity(any());
        verify(restApiMock, never()).updateEntity(any());
        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertSuccessForDisabledCorporateUser() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,personReference.name,comments",
            "ext-1,Soft-Deleted,This note should find the disabled corporate user");

        // Mock out all existing reference entities
        Person corporateUser = TestUtils.createPerson(1001, "CorporateUser", true);
        corporateUser.setName("Soft-Deleted");
        Person candidate = TestUtils.createPerson(1002, "Candidate", true);
        candidate.setName("Soft-Deleted");
        when(restApiMock.queryForList(eq(Person.class),
            eq("name='Soft-Deleted'"), eq(Sets.newHashSet("isDeleted", "id")), any()))
            .thenReturn(TestUtils.getList(corporateUser, candidate));

        // Create expected note object in insertEntity call
        Note expected = new Note();
        expected.setExternalID("ext-1");
        expected.setPersonReference(corporateUser);
        expected.setComments("This note should find the disabled corporate user");
        when(restApiMock.insertEntity(eq(expected))).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, times(1)).insertEntity(any());
        verify(restApiMock, never()).updateEntity(any());
        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertSuccessForActiveCandidatePerson() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,personReference.name,comments",
            "ext-1,David S. Pumpkins,This note should find the active candidate");

        // Mock out all existing reference entities
        Person deletedCandidate = TestUtils.createPerson(1001, "Candidate", true);
        deletedCandidate.setName("David S. Pumpkins");
        Person activeCandidate = TestUtils.createPerson(1002, "Candidate", false);
        activeCandidate.setName("David S. Pumpkins");
        when(restApiMock.queryForList(eq(Person.class),
            eq("name='David S. Pumpkins'"), eq(Sets.newHashSet("isDeleted", "id")), any()))
            .thenReturn(TestUtils.getList(deletedCandidate, activeCandidate));

        // Create expected note object in insertEntity call
        Note expected = new Note();
        expected.setExternalID("ext-1");
        expected.setPersonReference(activeCandidate);
        expected.setComments("This note should find the active candidate");
        when(restApiMock.insertEntity(eq(expected))).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, times(1)).insertEntity(any());
        verify(restApiMock, never()).updateEntity(any());
        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertFailureForDeletedPersonRecords() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,personReference.name,comments",
            "ext-1,Deleted Candidate,This note should fail to find the soft-deleted candidate");

        // Mock out all existing reference entities
        Person person = TestUtils.createPerson(1001, "Deleted Candidate", true);
        person.setName("Deleted Candidate");
        when(restApiMock.queryForList(eq(Person.class),
            eq("name='Deleted Candidate'"), eq(Sets.newHashSet("isDeleted", "id")), any()))
            .thenReturn(TestUtils.getList(person));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new RestApiException(
            "Cannot find To-One Association: 'personReference.name' with value: 'Deleted Candidate'"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInsertSuccessForAppointmentAttendee() throws Exception {
        Row row = TestUtils.createRow(
            "appointment.subject,attendee.name,acceptanceStatus",
            "Haunted Elevator,David S. Pumpkins,72");

        // Mock out appointment reference entity
        Appointment appointment = new Appointment();
        appointment.setId(1001);
        appointment.setSubject("Haunted Elevator");
        when(restApiMock.queryForList(eq(Appointment.class),
            eq("subject='Haunted Elevator' AND isDeleted=false"), eq(Sets.newHashSet("id")), any()))
            .thenReturn(TestUtils.getList(appointment));

        // Mock out person reference entity
        Person person = TestUtils.createPerson(1001, "Candidate", false);
        person.setName("David S. Pumpkins");
        when(restApiMock.queryForList(eq(Person.class),
            eq("name='David S. Pumpkins'"), eq(Sets.newHashSet("isDeleted", "id")), any()))
            .thenReturn(TestUtils.getList(person));

        // Create expected appointmentAttendee object in insertEntity call
        AppointmentAttendee expected = new AppointmentAttendee();
        expected.setAppointment(appointment);
        expected.setAttendee(person);
        expected.setAcceptanceStatus(72);
        when(restApiMock.insertEntity(eq(expected))).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.APPOINTMENT_ATTENDEE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, times(1)).insertEntity(any());
        verify(restApiMock, never()).updateEntity(any());
        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInsertSuccessForClientCorporationCustomObjects() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.externalID,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.searchForList(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporation.class, 1));
        when(restApiMock.queryForList(eq(ClientCorporationCustomObjectInstance2.class),
            eq("text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporationCustomObjectInstance2.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Collections.singletonList("text1"));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.insert(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInsertSuccessForPersonCustomObjects() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("person.customText1,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.queryForList(eq(Person.class), eq("customText1='ext-1'"), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createPerson(1, "Candidate", false)));
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class),
            eq("text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(PersonCustomObjectInstance2.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getEntityExistFields(any()))
            .thenReturn(Collections.singletonList("text1"));

        LoadTask task = new LoadTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.insert(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInsertSuccessForPersonCustomObjectsWithParentExistField() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("person.customText1,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.queryForList(eq(Person.class), eq("customText1='ext-1'"), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createPerson(1, "ClientContact", false)));
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class),
            eq("person.customText1=customText1 AND text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(PersonCustomObjectInstance2.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getEntityExistFields(any()))
            .thenReturn(Arrays.asList("person.customText1", "text1"));

        LoadTask task = new LoadTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.insert(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInsertErrorForCandidateMissingRecords() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,primarySkills.id", "Data,Loader,1;2;3");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1,
            "com.bullhornsdk.data.exception.RestApiException: Error occurred: "
                + "primarySkills does not exist with id of the following values:\n\t3");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunInsertErrorForNoteMissingRecords() throws Exception {
        Row row = TestUtils.createRow("candidates.id", "1;2");
        when(restApiMock.searchForList(eq(Candidate.class),
            eq("(id:1 OR id:2) AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Error occurred: "
                + "candidates does not exist with id of the following values:\n\t2");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunUpdateSuccess() throws Exception {
        Row row = TestUtils.createRow(
            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryID," +
                "owner.id",
            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,1,1,");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Collections.singletonList("externalID"));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"11\""), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1));
        when(restApiMock.queryForList(eq(CorporateUser.class), eq("id=1"), any(), any()))
            .thenReturn(TestUtils.getList(CorporateUser.class, 1));
        when(restApiMock.queryForList(eq(Skill.class), eq("(id=1)"), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void testRunUpdateSuccessForNote() throws Exception {
        Row row = TestUtils.createRow(
            "id,candidates.externalID,clientContacts.externalID,leads.customText1,jobOrders.externalID,"
                + "opportunities.externalID,placements.customText1",
            "1,1;2,3,4,5,6,7");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.NOTE)).thenReturn(Collections.singletonList("id"));
        when(restApiMock.searchForList(eq(Note.class), eq("noteID:1"), any(), any())).thenReturn(TestUtils.getList(Note.class, 1));

        Candidate candidate1 = new Candidate(1001);
        candidate1.setExternalID("1");
        Candidate candidate2 = new Candidate(1002);
        candidate2.setExternalID("2");
        when(restApiMock.searchForList(eq(Candidate.class),
            eq("(externalID:\"1\" OR externalID:\"2\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(candidate1, candidate2));

        ClientContact clientContact = new ClientContact(1003);
        clientContact.setExternalID("3");
        when(restApiMock.searchForList(eq(ClientContact.class)
            , eq("(externalID:\"3\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(clientContact));

        Lead lead = new Lead(1004);
        lead.setCustomText1("4");
        when(restApiMock.searchForList(eq(Lead.class),
            eq("(customText1:\"4\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(lead));

        JobOrder jobOrder = new JobOrder(1005);
        jobOrder.setExternalID("5");
        when(restApiMock.searchForList(eq(JobOrder.class),
            eq("(externalID:\"5\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(jobOrder));

        Opportunity opportunity = new Opportunity(1006);
        opportunity.setExternalID("6");
        when(restApiMock.searchForList(eq(Opportunity.class),
            eq("(externalID:\"6\") AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(opportunity));

        Placement placement = new Placement(1007);
        placement.setCustomText1("7");
        when(restApiMock.searchForList(eq(Placement.class), eq("(customText1:\"7\")"), any(), any()))
            .thenReturn(TestUtils.getList(placement));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        // Create expected note object in insertEntity call
        Note expected = new Note(1);
        expected.setCandidates(new OneToMany<>(new Candidate(1001), new Candidate(1002)));
        expected.setClientContacts(new OneToMany<>(new ClientContact(1003)));
        expected.setLeads(new OneToMany<>(new Lead(1004)));
        expected.setJobOrders(new OneToMany<>(new JobOrder(1005)));
        expected.setOpportunities(new OneToMany<>(new Opportunity(1006)));
        expected.setPlacements(new OneToMany<>(new Placement(1007)));
        when(restApiMock.updateEntity(eq(expected))).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, never()).insertEntity(any());
        verify(restApiMock, times(1)).updateEntity(any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void testRunUpdateSuccessForClientCorporationCustomObjects() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.externalID,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.searchForList(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporation.class, 1));
        when(restApiMock.queryForList(eq(ClientCorporationCustomObjectInstance2.class),
            eq("text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporationCustomObjectInstance2.class, 1));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Collections.singletonList("text1"));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.update(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunUpdateSuccessForPersonCustomObjects() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("person.customText1,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.queryForList(eq(Person.class), eq("customText1='ext-1'"), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createPerson(1, "ClientContact", false)));
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class),
            eq("text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(PersonCustomObjectInstance2.class, 1));
        when(propertyFileUtilMock.getEntityExistFields(any()))
            .thenReturn(Collections.singletonList("text1"));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.update(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunUpdateSuccessForPersonCustomObjectsWithParentExistField() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("person.customText1,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.queryForList(eq(Person.class), eq("customText1='ext-1'"), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createPerson(1, "ClientContact", false)));
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class),
            eq("person.customText1='ext-1' AND text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(PersonCustomObjectInstance2.class, 1));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));
        when(propertyFileUtilMock.getEntityExistFields(any()))
            .thenReturn(Arrays.asList("person.customText1", "text1"));

        LoadTask task = new LoadTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.update(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunCannotFindParentForCandidateCustomObjects() throws IOException, InstantiationException,
        IllegalAccessException {
        Row row = TestUtils.createRow("person.customText1,text1,text2,date1",
            "ext-1,Test,Skip,2016-08-30");
        when(restApiMock.queryForList(eq(Person.class), eq("customText1='ext-1'"), any(), any()))
            .thenReturn(TestUtils.getList(Person.class));
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class),
            eq("person.customText1=ext-1 AND text1='Test'"), any(), any()))
            .thenReturn(TestUtils.getList(PersonCustomObjectInstance2.class));
        when(propertyFileUtilMock.getEntityExistFields(any()))
            .thenReturn(Collections.singletonList("text1"));

        LoadTask task = new LoadTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new RestApiException(
            "Cannot find To-One Association: 'person.customText1' with value: 'ext-1'"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInvalidField() throws Exception {
        Row row = TestUtils.createRow("bogus",
            "This should fail with meaningful error because the field bogus does not exist on Candidate.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock,
            printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunIncorrectCapitalizationField() throws Exception {
        Row row = TestUtils.createRow("ID,NaME", "1,Should Fix Incorrect Capitalization");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Collections.singletonList("id"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void testRunInvalidNoteField() throws Exception {
        Row row = TestUtils.createRow("clientCorporations.id", "1;2");

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock,
            printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'clientCorporations' does not exist on Note");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunInvalidAddressField() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,city", "Data,Loader,Failure");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Invalid address field format: 'city'. "
                + "Must use: 'address.city' to set an address field.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunValidAddressField() throws Exception {
        Row row = TestUtils.createRow("id,address.city", "1,Success");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Collections.singletonList("id"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.UPDATE, 1);
    }

    @Test
    public void testRunValidAddressFieldForCountryID() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,address.CountryID", "Data,Loader,2216"); // 2216 = Canada
        when(restApiMock.queryForList(eq(CorporateUser.class), eq("id=1"), any(), any()))
            .thenReturn(TestUtils.getList(CorporateUser.class, 1));
        when(restApiMock.queryForList(eq(Skill.class), eq("id=1"), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        // Verify the candidate's countryID
        ArgumentCaptor<Candidate> entityArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(restApiMock).insertEntity(entityArgumentCaptor.capture());
        Candidate actualCandidate = entityArgumentCaptor.getValue();
        Assert.assertEquals(Integer.valueOf(2216), actualCandidate.getAddress().getCountryID());

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInvalidAddressFieldForCountryID() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,address.countryID", "Data,Loader,BOGUS");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "java.lang.NumberFormatException: For input string: \"BOGUS\"");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunInvalidToOneAssociation() throws Exception {
        Row row = TestUtils.createRow("bogus.id",
            "This should fail with meaningful error because bogus does not exist.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunInvalidToOneAssociationField() throws Exception {
        Row row = TestUtils.createRow("owner.bogus",
            "This should fail with meaningful error because the field bogus does not exist on the owner to-one association.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'bogus' does not exist on CorporateUser");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunInvalidToManyAssociationField() throws Exception {
        Row row = TestUtils.createRow("candidates.bogus", "1;2");

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunIgnoreEmptyToManyAssociationField() throws Exception {
        String[] headerArray = new String[]{"externalID", "primarySkills.id"};
        String[] valueArray = new String[]{"11", ""};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, never()).getAllAssociationsList(any(), any(), any(), any(), any());
        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDoNotIgnoreEmptyToManyAssociationField() throws Exception {
        String[] headerArray = new String[]{"externalID", "primarySkills.id"};
        String[] valueArray = new String[]{"11", ""};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(restApiMock.getAllAssociationsList(eq(Candidate.class), any(),
            eq(CandidateAssociations.getInstance().primarySkills()), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2, 3));
        when(propertyFileUtilMock.getProcessEmptyAssociations()).thenReturn(Boolean.TRUE);

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, times(1)).disassociateWithEntity(eq(Candidate.class),
            eq(1), eq(CandidateAssociations.getInstance().primarySkills()), eq(Arrays.asList(1, 2, 3)));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunInvalidToOneAddressAssociationField() throws Exception {
        Row row = TestUtils.createRow("secondaryAddress.bogus",
            "This should fail with meaningful error because the field bogus does not exist on the address to-one association.");

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'bogus' does not exist on Address");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunMultipleExistingRecords() throws Exception {
        Row row = TestUtils.createRow("externalID,firstName,lastName", "11,Data,Loader");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Collections.singletonList("externalID"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"11\""), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1, 2));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Multiple Records Exist. "
                + "Found 2 Candidate records with the same ExistField criteria of: externalID=11");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunCatchException() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName", "Data,Loader");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.NullPointerException");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunConvertedAttachmentToDescriptionCandidate() throws Exception {
        Row row = TestUtils.createRow("externalID,firstName,lastName,email", "11,Data,Loader,dloader@bullhorn.com");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        when(propertyFileUtilMock.getConvertedAttachmentFilepath(EntityInfo.CANDIDATE, "11"))
            .thenReturn(TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html"));
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        String fileContents = FileUtils.readFileToString(new File(
            TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html")));

        task.run();

        verify(restApiMock).insertEntity(candidateArgumentCaptor.capture());
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        Assert.assertEquals(fileContents, actualCandidate.getDescription());
    }

    @Test
    public void testRunConvertedAttachmentToDescriptionCandidateTestFileNotPresent() throws Exception {
        Row row = TestUtils.createRow("externalID,firstName,lastName,email", "11,Data,Loader,dloader@bullhorn.com");
        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);

        task.run();

        verify(restApiMock).insertEntity(candidateArgumentCaptor.capture());
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        Assert.assertNull(actualCandidate.getDescription());
    }

    @Test
    public void testRunConvertedAttachmentToDescriptionClientCorporation() throws Exception {
        Row row = TestUtils.createRow("externalID,name", "11,DL Technologies");
        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        when(propertyFileUtilMock.getConvertedAttachmentFilepath(EntityInfo.CLIENT_CORPORATION, "11"))
            .thenReturn(TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html"));
        ArgumentCaptor<ClientCorporation> clientCorporationArgumentCaptor = ArgumentCaptor.forClass(ClientCorporation.class);
        String fileContents = FileUtils.readFileToString(new File(
            TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html")));

        task.run();

        verify(restApiMock).insertEntity(clientCorporationArgumentCaptor.capture());
        ClientCorporation actualClientCorporation = clientCorporationArgumentCaptor.getValue();
        Assert.assertEquals(fileContents, actualClientCorporation.getCompanyDescription());
    }

    @Test
    public void testRunConvertedAttachmentToDescriptionClientCorporationTestFileNotPresent() throws Exception {
        Row row = TestUtils.createRow("externalID,name", "11,DL Technologies");
        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        ArgumentCaptor<ClientCorporation> clientCorporationArgumentCaptor = ArgumentCaptor.forClass(ClientCorporation.class);

        task.run();

        verify(restApiMock).insertEntity(clientCorporationArgumentCaptor.capture());
        ClientCorporation actualClientCorporation = clientCorporationArgumentCaptor.getValue();
        Assert.assertNull(actualClientCorporation.getCompanyDescription());
    }

    @Test
    public void testRunConvertedAttachmentToDescriptionOpportunity() throws Exception {
        Row row = TestUtils.createRow("externalID,title", "11,New Opportunity");
        LoadTask task = new LoadTask(EntityInfo.OPPORTUNITY, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        when(propertyFileUtilMock.getConvertedAttachmentFilepath(EntityInfo.OPPORTUNITY, "11"))
            .thenReturn(TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html"));
        ArgumentCaptor<Opportunity> opportunityArgumentCaptor = ArgumentCaptor.forClass(Opportunity.class);
        String fileContents = FileUtils.readFileToString(new File(
            TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html")));

        task.run();

        verify(restApiMock).insertEntity(opportunityArgumentCaptor.capture());
        Opportunity actualOpportunity = opportunityArgumentCaptor.getValue();
        Assert.assertEquals(fileContents, actualOpportunity.getDescription());
    }

    @Test
    public void testRunAssociationAlreadyExists() throws Exception {
        Row row = TestUtils.createRow("externalID,primarySkills.id", "ext-1,1;2;3");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 100));
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any())).
            thenReturn(TestUtils.getList(Skill.class, 1, 2, 3));
        when(restApiMock.getAllAssociationsList(eq(Candidate.class), any(),
            eq(CandidateAssociations.getInstance().primarySkills()), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2, 3));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, never()).associateWithEntity(any(), any(), any(), any());
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());
    }

    @Test
    public void testRunAssociationMultipleAlreadyExist() throws Exception {
        Row row = TestUtils.createRow("externalID,primarySkills.id", "ext-1,1;2;3");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 100));
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any())).
            thenReturn(TestUtils.getList(Skill.class, 1, 2, 3));
        when(restApiMock.getAllAssociationsList(eq(Candidate.class), any(),
            eq(CandidateAssociations.getInstance().primarySkills()), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, times(1)).associateWithEntity(eq(Candidate.class),
            eq(100), eq(CandidateAssociations.getInstance().primarySkills()), eq(Collections.singletonList(3)));
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());
    }

    @Test
    public void testRunAssociateAndDisassociate() throws Exception {
        Row row = TestUtils.createRow("externalID,primarySkills.id", "ext-1,3;4");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 100));
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any())).
            thenReturn(TestUtils.getList(Skill.class, 3, 4));
        when(restApiMock.getAllAssociationsList(eq(Candidate.class), any(),
            eq(CandidateAssociations.getInstance().primarySkills()), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2, 3));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, times(1)).associateWithEntity(eq(Candidate.class),
            eq(100), eq(CandidateAssociations.getInstance().primarySkills()), eq(Collections.singletonList(4)));
        verify(restApiMock, times(1)).disassociateWithEntity(eq(Candidate.class),
            eq(100), eq(CandidateAssociations.getInstance().primarySkills()), eq(Arrays.asList(1, 2)));
    }

    @Test
    public void testRunDuplicateToOneAssociationsExist() throws Exception {
        Row row = TestUtils.createRow("externalID,category.name", "11,hackers");
        Category category1 = new Category();
        category1.setId(1001);
        category1.setName("hackers");
        Category category2 = new Category();
        category2.setId(1002);
        category2.setName("hackers");
        when(restApiMock.queryForList(eq(Category.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(category1, category2));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Found 2 duplicate To-One Associations: " +
                "'category.name' with value: 'hackers'");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunDuplicateToManyAssociationsExist() throws Exception {
        Row row = TestUtils.createRow("externalID,primarySkills.name", "11,hacking");
        RestApiException restApiException = new RestApiException("Some Duplicate Warning from REST");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createSkill(1001, "hacking"), TestUtils.createSkill(1002, "hacking")));
        when(restApiMock.associateWithEntity(eq(Candidate.class), eq(1),
            eq(CandidateAssociations.getInstance().primarySkills()), any())).thenThrow(restApiException);

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1,
            "com.bullhornsdk.data.exception.RestApiException: Found 2 duplicate To-Many Associations: " +
                "'primarySkills.name' with value:\n\thacking");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunRepeatedToManyAssociations() throws Exception {
        Row row = TestUtils.createRow("externalID,businessSectors.name", "12,BusinessSector 1;BusinessSector 1");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Collections.singletonList("externalID"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"12\""), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class));
        when(restApiMock.queryForList(eq(BusinessSector.class), eq("(name='BusinessSector 1')"), any(), any()))
            .thenReturn(TestUtils.getList(BusinessSector.class, 1));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        // Verify that only returning a single business sector when two identical ones were entered in the field is OK
        verify(restApiMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1),
            eq(CandidateAssociations.getInstance().businessSectors()), eq(Collections.singletonList(1)));
        verify(restApiMock, never()).disassociateWithEntity(any(), any(), any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunUpdateEntityException() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        when(restApiMock.searchForList(eq(ClientCorporation.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporation.class, 1));
        when(restApiMock.queryForList(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(ClientCorporationCustomObjectInstance2.class, 1));
        when(restApiMock.updateEntity(any())).thenThrow(new RestApiException("Random SDK-REST Exception"));
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Collections.singletonList("text1"));

        LoadTask task = new LoadTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row,
            csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new RestApiException("Random SDK-REST Exception"), 1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunAssociationException() throws Exception {
        Row row = TestUtils.createRow("externalID,primarySkills.id", "11,1");
        RestApiException restApiException = new RestApiException("Flagrant Error");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any())).thenReturn(TestUtils.getList(Skill.class, 1));
        when(restApiMock.associateWithEntity(eq(Candidate.class), eq(1),
            eq(CandidateAssociations.getInstance().primarySkills()), any())).thenThrow(restApiException);

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1,
            "com.bullhornsdk.data.exception.RestApiException: Flagrant Error");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunUpdateRowProcessedCounts() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName", "Data,Loader");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        AbstractTask.rowProcessedCount.set(110);

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(printUtilMock, times(1)).printAndLog("Processed: 111 records.");
    }

    @Test
    public void testRunLuceneSearchStatementAllFieldTypes() throws Exception {
        Row row = TestUtils.createRow("dayRate,isLockedOut,customInt1,customFloat1,customDate1",
            "123.45,true,12345,123.45,2017-08-01");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Arrays.asList("dayRate", "isLockedOut", "customInt1", "customFloat1", "customDate1"));
        when(restApiMock.searchForList(eq(Candidate.class), any(), any(), any())).thenReturn(TestUtils.getList(Candidate.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "dayRate:123.45 AND isLockedOut:true AND customInt1:12345 AND "
            + "customFloat1:123.45 AND customDate1:2017-08-01";
        verify(restApiMock, times(1)).searchForList(eq(Candidate.class), eq(expectedQuery),
            eq(Sets.newHashSet("id")), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunLuceneSearchStatementWildcardMatching() throws Exception {
        String[] headerArray = new String[]{"action", "candidates.companyName", "comments"};
        String[] valueArray = new String[]{"Email", "Boeing*", "Candidates generated from the companyName field"};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(true);
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 90));

        LoadTask task = new LoadTask(EntityInfo.NOTE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "(companyName: Boeing*) AND isDeleted:0";
        verify(restApiMock, times(1)).searchForList(eq(Candidate.class), eq(expectedQuery), any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 90, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunLuceneSearchStatementNullFieldDefaults() throws Exception {
        String[] headerArray = new String[]{"dayRate", "isLockedOut", "customInt1", "customFloat1", "customDate1"};
        String[] valueArray = new String[]{"", "", "", "", ""};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Arrays.asList("dayRate", "isLockedOut", "customInt1", "customFloat1", "customDate1"));
        when(restApiMock.searchForList(eq(Candidate.class), any(), any(), any())).thenReturn(TestUtils.getList(Candidate.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "dayRate: AND isLockedOut: AND customInt1: AND customFloat1: AND customDate1:";
        verify(restApiMock, times(1)).searchForList(eq(Candidate.class), eq(expectedQuery),
            eq(Sets.newHashSet("id")), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunLuceneSearchStatementInvalidValues() throws Exception {
        Row row = TestUtils.createRow("dayRate,isLockedOut,customInt1,customFloat1,customDate1",
            "bogus,bogus,bogus,bogus,bogus");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Arrays.asList("dayRate", "isLockedOut", "customInt1", "customFloat1", "customDate1"));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "java.text.ParseException: Unparseable number: \"bogus\"");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunLuceneSearchStatementUnsupportedType() throws Exception {
        Row row = TestUtils.createRow("migrateGUID", "1234");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
            .thenReturn(Collections.singletonList("migrateGUID"));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Failed to create lucene search string for: " +
                "'migrateGUID' with unsupported field type: class java.lang.Object");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunDatabaseQueryWhereStatementAllFieldTypes() throws Exception {
        Row row = TestUtils.createRow("salary1,isLastJob,customInt1,companyName,startDate",
            "123.45,true,12345,Acme Inc.,<1500144555510");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE_WORK_HISTORY))
            .thenReturn(Arrays.asList("salary1", "isLastJob", "customInt1", "companyName", "startDate"));
        when(restApiMock.queryForList(eq(CandidateWorkHistory.class), any(), any(), any())).thenReturn(TestUtils.getList
            (CandidateWorkHistory.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_WORK_HISTORY, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "salary1=123.45 AND isLastJob=true AND customInt1=12345 AND companyName='Acme Inc.'" +
            " AND startDate<1500144555510";
        verify(restApiMock, times(1)).queryForList(eq(CandidateWorkHistory.class), eq(expectedQuery),
            eq(Sets.newHashSet("id")), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDatabaseQueryWhereStatementNullFieldDefaults() throws Exception {
        String[] headerArray = new String[]{"salary1", "isLastJob", "customInt1", "companyName"};
        String[] valueArray = new String[]{"", "", "", ""};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE_WORK_HISTORY))
            .thenReturn(Arrays.asList("salary1", "isLastJob", "customInt1", "companyName"));
        when(restApiMock.queryForList(eq(CandidateWorkHistory.class), any(), any(), any())).thenReturn(TestUtils.getList
            (CandidateWorkHistory.class));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_WORK_HISTORY, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "salary1= AND isLastJob=false AND customInt1= AND companyName=''";
        verify(restApiMock, times(1)).queryForList(eq(CandidateWorkHistory.class), eq(expectedQuery),
            eq(Sets.newHashSet("id")), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDatabaseQueryWhereStatementToManyWildcardMatching() throws Exception {
        String[] headerArray = new String[]{"firstName", "lastName", "primarySkills.name"};
        String[] valueArray = new String[]{"Stephanie", "Scribbles", "Sales*;Market*;IT"};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(true);
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any())).thenReturn(TestUtils.getList
            (Skill.class, 1, 2, 3, 4, 5, 6));
        when(restApiMock.getAllAssociationsList(eq(Candidate.class), any(),
            eq(CandidateAssociations.getInstance().primarySkills()), any(), any()))
            .thenReturn(TestUtils.getList(Skill.class, 1, 2));
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 100));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "(name like 'Sales%' OR name like 'Market%' OR name='IT')";
        verify(restApiMock, times(1)).queryForList(eq(Skill.class), eq(expectedQuery),
            any(), any());
        verify(restApiMock, times(1)).associateWithEntity(eq(Candidate.class),
            eq(100), eq(CandidateAssociations.getInstance().primarySkills()), eq(Arrays.asList(3, 4, 5, 6)));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 100, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDatabaseQueryWhereStatementToOneWildcardMatching() throws Exception {
        String[] headerArray = new String[]{"firstName", "lastName", "category.name"};
        String[] valueArray = new String[]{"Stephanie", "Scribbles", "Sales*"};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(true);
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 100));
        when(restApiMock.queryForList(eq(Category.class), any(), any(), any())).thenReturn(TestUtils.getList
            (Category.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "name like 'Sales%'";
        verify(restApiMock, times(1)).queryForList(eq(Category.class), eq(expectedQuery),
            any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 100, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDatabaseQueryWhereStatementLimitWildcardMatching() throws Exception {
        String[] headerArray = new String[]{"firstName", "lastName", "owner.name"};
        String[] valueArray = new String[]{"Stephanie", "Scribbles", "Bob Smiley"};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(true);
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 80));
        when(restApiMock.queryForList(eq(CorporateUser.class), any(), any(), any())).thenReturn(TestUtils.getList
            (CorporateUser.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "name='Bob Smiley'";
        verify(restApiMock, times(1)).queryForList(eq(CorporateUser.class), eq(expectedQuery),
            any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 80, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDatabaseQueryWhereStatementLimitWildcardMatchingWhenEscaped() throws Exception {
        String[] headerArray = new String[]{"firstName", "lastName", "owner.name"};
        String[] valueArray = new String[]{"Stephanie", "Scribbles", "\\*Bob Smiley\\*"};
        Row row = TestUtils.createRow(headerArray, valueArray);
        when(propertyFileUtilMock.getWildcardMatching()).thenReturn(true);
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 80));
        when(restApiMock.queryForList(eq(CorporateUser.class), any(), any(), any())).thenReturn(TestUtils.getList
            (CorporateUser.class, 1));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        String expectedQuery = "name='*Bob Smiley*'";
        verify(restApiMock, times(1)).queryForList(eq(CorporateUser.class), eq(expectedQuery),
            any(), any());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 80, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
    }

    @Test
    public void testRunDatabaseQueryWhereStatementInvalidNumberValue() throws Exception {
        Row row = TestUtils.createRow("salary1,isLastJob,customInt1,companyName,startDate",
            "bogus,true,12345,Acme Inc.,<1500144555510");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE_WORK_HISTORY))
            .thenReturn(Arrays.asList("salary1", "isLastJob", "customInt1", "companyName", "startDate"));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_WORK_HISTORY, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "java.text.ParseException: Unparseable number: \"bogus\"");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunDatabaseQueryWhereStatementInvalidDateValue() throws Exception {
        Row row = TestUtils.createRow("salary1,isLastJob,customInt1,companyName,startDate",
            "123.45,true,12345,Acme Inc.,bogus");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE_WORK_HISTORY))
            .thenReturn(Arrays.asList("salary1", "isLastJob", "customInt1", "companyName", "startDate"));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_WORK_HISTORY, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "java.lang.IllegalArgumentException: Invalid format: \"bogus\"");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunDatabaseQueryWhereStatementUnsupportedType() throws Exception {
        Row row = TestUtils.createRow("migrateGUID", "1234");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE_WORK_HISTORY))
            .thenReturn(Collections.singletonList("migrateGUID"));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE_WORK_HISTORY, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Failed to create query where clause for: " +
                "'migrateGUID' with unsupported field type: class java.lang.Object");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureToWriteResultsFiles() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,email", "Data,Loader,data@example.com");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        IOException ioException = new IOException("Cannot write file");
        Mockito.doThrow(ioException).when(csvFileWriterMock).writeRow(any(), any());

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(3)).writeRow(any(), eq(expectedResult));
        verify(printUtilMock, times(3)).printAndLog(eq(ioException));
    }

    @Test
    public void testRunResultsFileEnabled() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,email", "Data,Loader,dloader@bullhorn.com");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getResultsFileEnabled()).thenReturn(true);

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(completeUtilMock, times(1)).rowComplete(eq(row), eq(expectedResult), eq(actionTotalsMock));
    }

    @Test
    public void testRunCache() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,email,primarySkills.name", "Data,Loader,dloader@bullhorn.com,Java");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getCaching()).thenReturn(true);
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createSkill(1001, "Java")));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(cacheMock, times(1)).getEntry(eq(EntityInfo.SKILL), any(), eq(Sets.newHashSet("id", "name")));
    }

    @Test
    public void testRunCacheEmptyResults() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,email,primarySkills.name", "Data,Loader,dloader@bullhorn.com,Java");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getCaching()).thenReturn(true);
        when(cacheMock.getEntry(any(), any(), any())).thenReturn(Collections.emptyList());

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(restApiMock, never()).queryForList(eq(Skill.class), any(), any(), any());
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1,
            "com.bullhornsdk.data.exception.RestApiException: Error occurred: "
                + "primarySkills does not exist with name of the following values:\n\tJava");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunCacheDisabled() throws Exception {
        Row row = TestUtils.createRow("firstName,lastName,email,primarySkills.name", "Data,Loader,dloader@bullhorn.com,Java");
        when(restApiMock.insertEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.INSERT, 1));
        when(propertyFileUtilMock.getCaching()).thenReturn(false);
        when(restApiMock.queryForList(eq(Skill.class), any(), any(), any()))
            .thenReturn(TestUtils.getList(TestUtils.createSkill(1001, "Java")));

        LoadTask task = new LoadTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.INSERT, 1);
        verify(cacheMock, never()).getEntry(any(), any(), any());
    }
}
