package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
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
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class LoadCustomObjectTaskTest {

    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private BullhornRestApi bullhornRestApiMock;
    private ActionTotals actionTotalsMock;

    private LoadCustomObjectTask task;

    private Map<String, String> dataMap;
    private Map<String, Method> methodMap;

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);

        List<String> existField = Arrays.asList(new String[]{"text1"});
        Mockito.doReturn(Optional.ofNullable(existField)).when(propertyFileUtilMock).getEntityExistFields(any());
        Mockito.doReturn(";").when(propertyFileUtilMock).getListDelimiter();

        methodMap = EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2.getSetterMethodMap();

        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
        dataMap.put("date1", "2016-08-30");

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
        when(bullhornRestApiMock.getMetaData(any(), eq(MetaParameter.BASIC), eq(null))).thenReturn(meta);

        String dateFormatString = "yyyy-mm-dd";
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern(dateFormatString));
    }

    @Test
    public void runTest_Insert() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);


        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Arrays.asList(new ClientCorporation(1)));
        ClientCorporationListWrapper listWrapper2 = new ClientCorporationListWrapper();
        ClientCorporation clientCorporation = new ClientCorporation(1);
        OneToMany<ClientCorporationCustomObjectInstance2> oneToMany = new OneToMany<>();
        clientCorporationCustomObjectInstance2.setText1("Test");
        clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        clientCorporation.setCustomObject2s(oneToMany);
        listWrapper2.setData(Arrays.asList(clientCorporation));
        when(bullhornRestApiMock.search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any())).thenReturn(listWrapper, listWrapper2);

        Mockito.doReturn(new CreateResponse()).when(bullhornRestApiMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_ClientContactInsert() throws IOException {
        //setup
        dataMap = new LinkedHashMap<>();
        dataMap.put("person.id", "1");
        dataMap.put("person._subtype", "cLiEnT CoNtAcT");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
        dataMap.put("date1", "2016-08-30");

        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Arrays.asList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(PersonCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientContactListWrapper listWrapper = new ClientContactListWrapper();
        listWrapper.setData(Arrays.asList(new ClientContact(1)));
        ClientContactListWrapper listWrapper2 = new ClientContactListWrapper();
        ClientContact clientContact = new ClientContact(1);
        OneToMany<PersonCustomObjectInstance2> oneToMany = new OneToMany<>();
        personCustomObjectInstance2.setText1("Test");
        personCustomObjectInstance2List = Arrays.asList(personCustomObjectInstance2);
        oneToMany.setData(personCustomObjectInstance2List);
        oneToMany.setTotal(personCustomObjectInstance2List.size());
        clientContact.setCustomObject2s(oneToMany);
        listWrapper2.setData(Arrays.asList(clientContact));
        when(bullhornRestApiMock.search(eq(ClientContact.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any())).thenReturn(listWrapper, listWrapper2);

        Mockito.doReturn(new CreateResponse()).when(bullhornRestApiMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_CandidateInsert() throws IOException {
        //setup
        dataMap = new LinkedHashMap<>();
        dataMap.put("person.id", "1");
        dataMap.put("person._subtype", "Candidate");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
        dataMap.put("date1", "2016-08-30");

        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Arrays.asList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(PersonCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);


        CandidateListWrapper listWrapper = new CandidateListWrapper();
        listWrapper.setData(Arrays.asList(new Candidate(1)));
        CandidateListWrapper listWrapper2 = new CandidateListWrapper();
        Candidate candidate = new Candidate(1);
        OneToMany<PersonCustomObjectInstance2> oneToMany = new OneToMany<>();
        personCustomObjectInstance2.setText1("Test");
        personCustomObjectInstance2List = Arrays.asList(personCustomObjectInstance2);
        oneToMany.setData(personCustomObjectInstance2List);
        oneToMany.setTotal(personCustomObjectInstance2List.size());
        candidate.setCustomObject2s(oneToMany);
        listWrapper2.setData(Arrays.asList(candidate));
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any())).thenReturn(listWrapper, listWrapper2);

        Mockito.doReturn(new CreateResponse()).when(bullhornRestApiMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_PersonSubTypeNotValid() throws IOException {
        //setup
        dataMap = new LinkedHashMap<>();
        dataMap.put("person.id", "1");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
        dataMap.put("date1", "2016-08-30");

        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new Exception("Row 1: The required field person._subType is missing. This field must be included to load PersonCustomObjectInstance2"));

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Arrays.asList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(PersonCustomObjectInstance2.class), eq("text1='Test' AND person.id=1"), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper2);

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_PersonSubTypeNotIncluded() throws IOException {
        //setup
        dataMap = new LinkedHashMap<>();
        dataMap.put("person.id", "1");
        dataMap.put("person._subtype", "Potato");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
        dataMap.put("date1", "2016-08-30");

        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new Exception("Row 1: The person._subType field must be either Candidate or ClientContact"));

        PersonCustomObjectInstance2ListWrapper customObjectListWrapper = new PersonCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        PersonCustomObjectInstance2ListWrapper customObjectListWrapper2 = new PersonCustomObjectInstance2ListWrapper();
        PersonCustomObjectInstance2 personCustomObjectInstance2 = new PersonCustomObjectInstance2();
        personCustomObjectInstance2.setId(1);
        List<PersonCustomObjectInstance2> personCustomObjectInstance2List = Arrays.asList(personCustomObjectInstance2);
        customObjectListWrapper2.setData(personCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(PersonCustomObjectInstance2.class), eq("text1='Test' AND person.id=1"), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper2);

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_Update() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Update(1);

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), eq("text1='Test' AND clientCorporation.id=1"), any(), any())).thenReturn(customObjectListWrapper);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        ClientCorporation clientCorporation = new ClientCorporation(1);
        OneToMany<ClientCorporationCustomObjectInstance2> oneToMany = new OneToMany<>();
        ClientCorporationCustomObjectInstance2 otherClientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        otherClientCorporationCustomObjectInstance2.setId(2);
        clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2, otherClientCorporationCustomObjectInstance2);
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        clientCorporation.setCustomObject2s(oneToMany);
        listWrapper.setData(Arrays.asList(clientCorporation));
        Mockito.doReturn(listWrapper).when(bullhornRestApiMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any());

        Mockito.doReturn(new CreateResponse()).when(bullhornRestApiMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_Failure() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new NullPointerException());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void getCustomObjectIdTest_Pass() throws Exception {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.entity = new ClientCorporationCustomObjectInstance2();
        ((ClientCorporationCustomObjectInstance2) task.entity).setText1("test");
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        clientCorporationCustomObjectInstance2.setText1("test");
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        OneToMany oneToMany = new OneToMany();
        oneToMany.setData(clientCorporationCustomObjectInstance2List);
        oneToMany.setTotal(clientCorporationCustomObjectInstance2List.size());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);

        when(bullhornRestApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper);

        task.parentField = "clientCorporation.id";

        //test
        task.getCustomObjectId();

        //verify
        Assert.assertTrue(1 == task.entityID);
    }

    @Test
    public void getCustomObjectIdTest_ThrowDupe() throws Exception {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        task.entity = new ClientCorporationCustomObjectInstance2();
        RestApiException expectedException = new RestApiException("Row 1: Found duplicate.");

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

        when(bullhornRestApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper);

        task.parentField = "clientCorporation.id";

        //test
        RestApiException actualException = null;
        try {
            task.getCustomObjectId();
        } catch (RestApiException e) {
            actualException = e;
        }

        //verify
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void noPermissionToInsertCustomObjectTest() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        String cleanedExceptionMessage = "ClientCorporation Custom Object 2 is not set up.";
        Result expectedResult = Result.Failure(new RestApiException(cleanedExceptionMessage));

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Arrays.asList(new ClientCorporation(1)));
        Mockito.doReturn(listWrapper).when(bullhornRestApiMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")), any());

        String noPermissionException = "{\n" +
            "  \"errorMessage\" : \"error persisting an entity of type: Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\",\n" +
            "  \"errors\" : [ ],\n" +
            "  \"entityName\" : \"Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\"\n" +
            "}";
        Mockito.doThrow(new RestApiException(noPermissionException)).when(bullhornRestApiMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void checkIfCouldUpdateCustomObjectTest_ThrowRandom() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        String cleanedExceptionMessage = "bogus";
        Result expectedResult = Result.Failure(new RestApiException(cleanedExceptionMessage));

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornRestApiMock.query(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Arrays.asList(new ClientCorporation(1)));
        Mockito.doReturn(listWrapper).when(bullhornRestApiMock).search(eq(ClientCorporation.class), any(), eq(Sets.newHashSet("id", "customObject2s(*)")), any());

        String noPermissionException = "bogus";
        Mockito.doThrow(new RestApiException(noPermissionException)).when(bullhornRestApiMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void getParentEntityTest_Exception() throws InvocationTargetException, IllegalAccessException {
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        RestApiException expectedException = new RestApiException("Row 1: To-One Association: 'candidate' does not exist on ClientCorporationCustomObjectInstance2");
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
        task = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Failure(new IOException("Missing parent entity locator column, for example: 'candidate.id', 'candidate.externalID', or 'candidate.whatever' so that the custom object can be loaded to the correct parent entity."));
        dataMap.remove("clientCorporation.id");

        task.run();

        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }
}
