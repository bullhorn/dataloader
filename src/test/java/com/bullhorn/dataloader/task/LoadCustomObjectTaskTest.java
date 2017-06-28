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
import com.bullhornsdk.data.model.enums.ChangeType;
import com.bullhornsdk.data.model.enums.MetaParameter;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Optional.of(existField));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");

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
    @SuppressWarnings("unchecked")
    public void testRunInsertClientCorporationCustomObject() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        ClientCorporationCustomObjectInstance2 customObject = new ClientCorporationCustomObjectInstance2();
        customObject.setId(1);
        ClientCorporation clientCorporation = new ClientCorporation(1);
        ClientCorporation clientCorporationWithCustomObject = new ClientCorporation(1);
        OneToMany<ClientCorporationCustomObjectInstance2> oneToMany = new OneToMany<>();
        customObject.setText1("Test");
        oneToMany.setData(TestUtils.getList(customObject));
        clientCorporationWithCustomObject.setCustomObject2s(oneToMany);

        // First time through, the parent has no custom objects, the second time through where we grab the ID, it does
        // have our new custom object we just created.
        when(restApiMock.searchForList(eq(ClientCorporation.class), any(), any(), any())).thenReturn(
            TestUtils.getList(clientCorporation),
            TestUtils.getList(clientCorporationWithCustomObject));

        // Checking for existing object occurs three times. Return an empty list for the first two.
        when(restApiMock.queryForList(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(
            TestUtils.getList(ClientCorporationCustomObjectInstance2.class),
            TestUtils.getList(ClientCorporationCustomObjectInstance2.class),
            TestUtils.getList(customObject));

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Insert(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRunInsertClientContact() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("person.id,person._subtype,text1,text2,date1", "1,cLiEnT CoNtAcT,Test,Skip,2016-08-30");
        PersonCustomObjectInstance2 customObject = new PersonCustomObjectInstance2();
        customObject.setId(1);
        ClientContact clientContact = new ClientContact(1);
        ClientContact clientContactWithCustomObject = new ClientContact(1);
        OneToMany<PersonCustomObjectInstance2> oneToMany = new OneToMany<>();
        customObject.setText1("Test");
        oneToMany.setData(TestUtils.getList(customObject));
        clientContactWithCustomObject.setCustomObject2s(oneToMany);

        // First time through, the parent has no custom objects, the second time through where we grab the ID, it does
        // have our new custom object we just created.
        when(restApiMock.searchForList(eq(ClientContact.class), any(), any(), any())).thenReturn(
            TestUtils.getList(clientContact),
            TestUtils.getList(clientContactWithCustomObject));

        // Checking for existing object occurs three times. Return an empty list for the first two.
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class), any(), any(), any())).thenReturn(
            TestUtils.getList(PersonCustomObjectInstance2.class),
            TestUtils.getList(PersonCustomObjectInstance2.class),
            TestUtils.getList(customObject));

        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Insert(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRunInsertCandidate() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("person.id,person._subtype,text1,text2,date1", "1,Candidate,Test,Skip,2016-08-30");
        PersonCustomObjectInstance2 customObject = new PersonCustomObjectInstance2();
        customObject.setId(1);
        Candidate candidate = new Candidate(1);
        Candidate clientContactWithCustomObject = new Candidate(1);
        OneToMany<PersonCustomObjectInstance2> oneToMany = new OneToMany<>();
        customObject.setText1("Test");
        oneToMany.setData(TestUtils.getList(customObject));
        clientContactWithCustomObject.setCustomObject2s(oneToMany);

        // First time through, the parent has no custom objects, the second time through where we grab the ID, it does
        // have our new custom object we just created.
        when(restApiMock.searchForList(eq(Candidate.class), any(), any(), any())).thenReturn(
            TestUtils.getList(candidate),
            TestUtils.getList(clientContactWithCustomObject));

        // Checking for existing object occurs three times. Return an empty list for the first two.
        when(restApiMock.queryForList(eq(PersonCustomObjectInstance2.class), any(), any(), any())).thenReturn(
            TestUtils.getList(PersonCustomObjectInstance2.class),
            TestUtils.getList(PersonCustomObjectInstance2.class),
            TestUtils.getList(customObject));

        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Insert(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunMissingPersonSubType() throws IOException {
        Row row = TestUtils.createRow("person.id,text1,text2,date1", "1,Test,Skip,2016-08-30");

        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new Exception("The required field person._subType is missing. This field must be included to load PersonCustomObjectInstance2"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInvalidPersonSubType() throws IOException {
        Row row = TestUtils.createRow("person.id,person._subtype,text1,text2,date1", "1,Potato,Test,Skip,2016-08-30");

        task = new LoadCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new Exception("The person._subType field must be either Candidate or ClientContact"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunUpdate() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        when(restApiMock.searchForList(eq(ClientCorporation.class), any(), any(), any())).thenReturn(TestUtils.getList(ClientCorporation.class, 1));
        when(restApiMock.queryForList(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(TestUtils.getList(ClientCorporationCustomObjectInstance2.class, 1));
        when(restApiMock.updateEntity(any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 1));

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Update(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunNoRestReturn() throws IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new RestApiException("Cannot find To-One Association: 'clientCorporation.id' with value: '1'"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRunDuplicateError() throws Exception {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        when(restApiMock.queryForList(eq(ClientCorporationCustomObjectInstance2.class), any(), any(), any())).thenReturn(TestUtils.getList(ClientCorporationCustomObjectInstance2.class, 1, 1));
        when(restApiMock.searchForList(eq(ClientCorporation.class), any(), any(), any())).thenReturn(TestUtils.getList(ClientCorporation.class, 1));

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new RestApiException("Cannot Perform Update - Multiple Records Exist. Found 2 ClientCorporationCustomObjectInstance2 records with the same ExistField criteria of: {text1=Test, clientCorporation.id=1}"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunNoPermissionToInsertCustomObject() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        when(restApiMock.searchForList(eq(ClientCorporation.class), any(), any(), any())).thenReturn(TestUtils.getList(ClientCorporation.class, 1));

        String noPermissionException = "{\n" +
            "  \"errorMessage\" : \"error persisting an entity of type: Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\",\n" +
            "  \"errors\" : [ ],\n" +
            "  \"entityName\" : \"Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\"\n" +
            "}";
        when(restApiMock.updateEntity(any())).thenThrow(new RestApiException(noPermissionException));

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new RestApiException("ClientCorporation Custom Object 2 is not set up."));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunRandomSdkException() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        when(restApiMock.searchForList(eq(ClientCorporation.class), any(), any(), any())).thenReturn(TestUtils.getList(ClientCorporation.class, 1));
        when(restApiMock.updateEntity(any())).thenThrow(new RestApiException("bogus"));

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new RestApiException("bogus"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunBadField() throws InvocationTargetException, IllegalAccessException, IOException {
        Row row = TestUtils.createRow("clientCorporation.id,text1,text2,date1", "1,Test,Skip,2016-08-30");
        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.entity = new ClientCorporationCustomObjectInstance2();

        RestApiException actualException = new RestApiException();
        try {
            task.getParentEntity("candidate.id");
        } catch (RestApiException e) {
            actualException = e;
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }

        RestApiException expectedException = new RestApiException("To-One Association: 'candidate' does not exist on ClientCorporationCustomObjectInstance2");
        Assert.assertEquals(actualException.getMessage(), expectedException.getMessage());
    }

    @Test
    public void testRunParentLocatorMissing() throws IOException {
        Row row = TestUtils.createRow("text1,text2,date1", "Test,Skip,2016-08-30");

        task = new LoadCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.Failure(new IOException("Missing parent entity locator column, for example: 'candidate.id', 'candidate.externalID', or 'candidate.whatever' so that the custom object can be loaded to the correct parent entity."));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
