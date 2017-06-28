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
import com.bullhornsdk.data.model.entity.core.customobject.ClientCorporationCustomObjectInstance2;
import com.bullhornsdk.data.model.entity.core.customobject.PersonCustomObjectInstance2;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.model.response.crud.CreateResponse;
import com.bullhornsdk.data.model.response.list.CandidateListWrapper;
import com.bullhornsdk.data.model.response.list.ClientContactListWrapper;
import com.bullhornsdk.data.model.response.list.ClientCorporationListWrapper;
import com.bullhornsdk.data.model.response.list.customobject.ClientCorporationCustomObjectInstance2ListWrapper;
import com.bullhornsdk.data.model.response.list.customobject.PersonCustomObjectInstance2ListWrapper;
import com.google.common.collect.Sets;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadCustomObjectTaskTest {

    private ActionTotals actionTotalsMock;
    private RestApi restApiMock;
    private CsvFileWriter csvFileWriterMock;
    private Preloader preloaderMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;

    private LoadCustomObjectTask task;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        actionTotalsMock = mock(ActionTotals.class);
        restApiMock = mock(RestApi.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        preloaderMock = mock(Preloader.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);

        List<String> existField = Collections.singletonList("text1");
        doReturn(Optional.of(existField)).when(propertyFileUtilMock).getEntityExistFields(any());
        doReturn(";").when(propertyFileUtilMock).getListDelimiter();

        StandardMetaData meta = new StandardMetaData();
        List<Field> fields = new ArrayList<>();
        Field field = new Field();
        field.setName("id");
        fields.add(field);
        Field field2 = new Field();
        field2.setName("text1");
        fields.add(field2);
        Field field3 = new Field();
        field3.setName("date1");
        fields.add(field3);
        meta.setFields(fields);
        when(restApiMock.getMetaData(any(), eq(MetaParameter.BASIC), eq(null))).thenReturn(meta);

        String dateFormatString = "yyyy-mm-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
    }

    @Test
    public void runTest_Insert() throws IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Collections.singletonList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(restApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Collections.singletonList(new ClientCorporation(1)));
        ClientCorporationListWrapper listWrapper2 = new ClientCorporationListWrapper();
        ClientCorporation clientCorporation = new ClientCorporation(1);
        OneToMany<ClientCorporationCustomObjectInstance2> oneToMany = new OneToMany<>();
        clientCorporationCustomObjectInstance2.setText1("Test");
        clientCorporationCustomObjectInstance2List = Collections.singletonList(clientCorporationCustomObjectInstance2);
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        clientCorporation.setCustomObject2s(oneToMany);
        listWrapper2.setData(Collections.singletonList(clientCorporation));
        when(restApiMock.search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any())).thenReturn(listWrapper, listWrapper2);

        doReturn(new CreateResponse()).when(restApiMock).updateEntity(any());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_ClientContactInsert() throws IOException {
        Row row = TestUtils.createRow("person.id,person._subtype,text1,text2,date1", "1,cLiEnT CoNtAcT,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Collections.singletonList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(restApiMock.query(eq(PersonCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientContactListWrapper listWrapper = new ClientContactListWrapper();
        listWrapper.setData(Collections.singletonList(new ClientContact(1)));
        ClientContactListWrapper listWrapper2 = new ClientContactListWrapper();
        ClientContact clientContact = new ClientContact(1);
        OneToMany<PersonCustomObjectInstance2> oneToMany = new OneToMany<>();
        personCustomObjectInstance2.setText1("Test");
        personCustomObjectInstance2List = Collections.singletonList(personCustomObjectInstance2);
        oneToMany.setData(personCustomObjectInstance2List);
        oneToMany.setTotal(personCustomObjectInstance2List.size());
        clientContact.setCustomObject2s(oneToMany);
        listWrapper2.setData(Collections.singletonList(clientContact));
        when(restApiMock.search(eq(ClientContact.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any())).thenReturn(listWrapper, listWrapper2);

        doReturn(new CreateResponse()).when(restApiMock).updateEntity(any());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_CandidateInsert() throws IOException {
        Row row = TestUtils.createRow("person.id,person._subtype,text1,text2,date1", "1,Candidate,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Collections.singletonList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(restApiMock.query(eq(PersonCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);


        CandidateListWrapper listWrapper = new CandidateListWrapper();
        listWrapper.setData(Collections.singletonList(new Candidate(1)));
        CandidateListWrapper listWrapper2 = new CandidateListWrapper();
        Candidate candidate = new Candidate(1);
        OneToMany<PersonCustomObjectInstance2> oneToMany = new OneToMany<>();
        personCustomObjectInstance2.setText1("Test");
        personCustomObjectInstance2List = Collections.singletonList(personCustomObjectInstance2);
        oneToMany.setData(personCustomObjectInstance2List);
        oneToMany.setTotal(personCustomObjectInstance2List.size());
        candidate.setCustomObject2s(oneToMany);
        listWrapper2.setData(Collections.singletonList(candidate));
        when(restApiMock.search(eq(Candidate.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any())).thenReturn(listWrapper, listWrapper2);

        doReturn(new CreateResponse()).when(restApiMock).updateEntity(any());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_PersonSubTypeNotIncluded() throws IOException {
        Row row = TestUtils.createRow("person.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new Exception("The required field person._subType is missing. This field must be included to load PersonCustomObjectInstance2"));

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Collections.singletonList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(restApiMock.query(eq(PersonCustomObjectInstance2.class), eq("text1='Test' AND person.id=1"), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper2);

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_PersonSubTypeNotValid() throws IOException {
        Row row = TestUtils.createRow("person.id,person._subtype,text1,text2,date1", "1,Potato,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new Exception("The person._subType field must be either Candidate or ClientContact"));

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Collections.singletonList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(restApiMock.query(eq(PersonCustomObjectInstance2.class), eq("text1='Test' AND person.id=1"), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper2);

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_Update() throws IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Update(1);

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Collections.singletonList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);
        when(restApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), eq("text1='Test' AND clientCorporation.id=1"), any(), any())).thenReturn(customObjectListWrapper);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        ClientCorporation clientCorporation = new ClientCorporation(1);
        OneToMany<ClientCorporationCustomObjectInstance2> oneToMany = new OneToMany<>();
        ClientCorporationCustomObjectInstance2 otherClientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        otherClientCorporationCustomObjectInstance2.setId(2);
        clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2, otherClientCorporationCustomObjectInstance2);
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        clientCorporation.setCustomObject2s(oneToMany);
        listWrapper.setData(Collections.singletonList(clientCorporation));
        doReturn(listWrapper).when(restApiMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any());

        doReturn(new CreateResponse()).when(restApiMock).updateEntity(any());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_Failure() throws IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new NullPointerException());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCustomObjectIdTest_Pass() throws Exception {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.entity = new ClientCorporationCustomObjectInstance2();
        ((ClientCorporationCustomObjectInstance2) task.entity).setText1("test");
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        clientCorporationCustomObjectInstance2.setText1("test");
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Collections.singletonList(clientCorporationCustomObjectInstance2);
        OneToMany oneToMany = new OneToMany();
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);

        when(restApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper);

        task.parentField = "clientCorporation.id";

        task.getCustomObjectId();

        Assert.assertTrue(1 == task.entityID);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCustomObjectIdTest_ThrowDuplicateWarning() throws Exception {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.entity = new ClientCorporationCustomObjectInstance2();
        RestApiException expectedException = new RestApiException("Found duplicate.");

        ((ClientCorporationCustomObjectInstance2) task.entity).setText1("test");
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        clientCorporationCustomObjectInstance2.setText1("test");
        ClientCorporationCustomObjectInstance2 dupeClientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        dupeClientCorporationCustomObjectInstance2.setId(2);
        dupeClientCorporationCustomObjectInstance2.setText1("test");
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2, dupeClientCorporationCustomObjectInstance2);
        OneToMany oneToMany = new OneToMany();
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);

        when(restApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper);

        task.parentField = "clientCorporation.id";

        RestApiException actualException = null;
        try {
            task.getCustomObjectId();
        } catch (RestApiException e) {
            actualException = e;
        }

        assert actualException != null;
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void noPermissionToInsertCustomObjectTest() throws IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String cleanedExceptionMessage = "ClientCorporation Custom Object 2 is not set up.";
        Result expectedResult = Result.Failure(new RestApiException(cleanedExceptionMessage));

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Collections.singletonList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(restApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Collections.singletonList(new ClientCorporation(1)));
        doReturn(listWrapper).when(restApiMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any());

        String noPermissionException = "{\n" +
            "  \"errorMessage\" : \"error persisting an entity of type: Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\",\n" +
            "  \"errors\" : [ ],\n" +
            "  \"entityName\" : \"Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\"\n" +
            "}";
        doThrow(new RestApiException(noPermissionException)).when(restApiMock).updateEntity(any());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void checkIfCouldUpdateCustomObjectTest_ThrowRandom() throws IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String cleanedExceptionMessage = "bogus";
        Result expectedResult = Result.Failure(new RestApiException(cleanedExceptionMessage));

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Collections.singletonList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(restApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Collections.singletonList(new ClientCorporation(1)));
        doReturn(listWrapper).when(restApiMock).search(eq(ClientCorporation.class), any(), eq(Sets.newHashSet("id", "customObject2s(*)")), any());

        String noPermissionException = "bogus";
        doThrow(new RestApiException(noPermissionException)).when(restApiMock).updateEntity(any());

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void getParentEntityTest_Exception() throws InvocationTargetException, IllegalAccessException, IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        RestApiException expectedException = new RestApiException("To-One Association: 'candidate' does not exist on ClientCorporationCustomObjectInstance2");
        task.entity = new ClientCorporationCustomObjectInstance2();

        RestApiException actualException = new RestApiException();
        try {
            task.getParentEntity("candidate.id");
        } catch (RestApiException e) {
            actualException = e;
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }

        Assert.assertEquals(actualException.getMessage(), expectedException.getMessage());
    }

    @Test
    public void parentEntityIsNotInCsvTest() throws IOException {
        Row row = TestUtils.createRow("text1,text2,date1", "Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new IOException("Missing parent entity locator column, for example: 'candidate.id', 'candidate.externalID', or 'candidate.whatever' so that the custom object can be loaded to the correct parent entity."));

        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
