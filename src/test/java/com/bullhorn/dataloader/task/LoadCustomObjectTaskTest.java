package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.customobject.ClientCorporationCustomObjectInstance2;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.bullhornsdk.data.model.response.crud.CreateResponse;
import com.bullhornsdk.data.model.response.list.ClientCorporationListWrapper;
import com.bullhornsdk.data.model.response.list.customobject.ClientCorporationCustomObjectInstance2ListWrapper;
import com.csvreader.CsvReader;
import com.google.common.collect.Sets;
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
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class LoadCustomObjectTaskTest {

    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private ValidationUtil validationUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private BullhornData bullhornDataMock;
    private ActionTotals actionTotalsMock;
    private CsvReader csvReaderMock;
    private ExecutorService executorServiceMock;

    private LoadCustomObjectTask task;

    private LinkedHashMap<String, String> dataMap;
    private Map<String, Method> methodMap;

    @Before
    public void setUp() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        validationUtilMock = Mockito.mock(ValidationUtil.class);
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        csvReaderMock = Mockito.mock(CsvReader.class);
        executorServiceMock = Mockito.mock(ExecutorService.class);

        List<String> existField = Arrays.asList(new String[]{"text1"});
        Mockito.doReturn(Optional.ofNullable(existField )).when(propertyFileUtilMock).getEntityExistFields(any());
        Mockito.doReturn(";").when(propertyFileUtilMock).getListDelimiter();

        ConcurrencyService concurrencyService = new ConcurrencyService(Command.LOAD, "ClientCorporationCustomObjectInstance2", csvReaderMock, csvFileWriterMock, executorServiceMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        methodMap = concurrencyService.createMethodMap(ClientCorporationCustomObjectInstance2.class);

        dataMap = new LinkedHashMap<>();
        dataMap.put("clientCorporation.id", "1");
        dataMap.put("text1", "Test");

    }

    @Test
    public void runTest_Insert() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, ClientCorporationCustomObjectInstance2.class, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Insert(1);

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornDataMock.query(eq(ClientCorporationCustomObjectInstance2.class), eq("text1='Test'"), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Arrays.asList(new ClientCorporation(1)));
        Mockito.doReturn(listWrapper).when(bullhornDataMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")),any());

        Mockito.doReturn(new CreateResponse()).when(bullhornDataMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_Update() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, ClientCorporationCustomObjectInstance2.class, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        Result expectedResult = Result.Update(1);

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornDataMock.query(eq(ClientCorporationCustomObjectInstance2.class), eq("text1='Test'"), any(), any())).thenReturn(customObjectListWrapper);

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
        Mockito.doReturn(listWrapper).when(bullhornDataMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")),any());

        Mockito.doReturn(new CreateResponse()).when(bullhornDataMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void runTest_Failure() throws IOException {
        //setup
        task = Mockito.spy(new LoadCustomObjectTask(Command.LOAD, 1, ClientCorporationCustomObjectInstance2.class, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock));
        Result expectedResult = Result.Failure(new NullPointerException());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void getCustomObjectIdTest() throws Exception {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, ClientCorporationCustomObjectInstance2.class, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornDataMock.query(eq(ClientCorporationCustomObjectInstance2.class), eq("text1='Test'"), any(), any())).thenReturn(customObjectListWrapper);

        //test
        task.getCustomObjectId();

        //verify
        Assert.assertTrue(1 == task.entityID);
    }

    @Test
    public void noPermissionToInsertCustomObjectTest() throws IOException {
        //setup
        task = new LoadCustomObjectTask(Command.LOAD, 1, ClientCorporationCustomObjectInstance2.class, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        String cleanedExceptionMessage = "ClientCorporation Custom Object field customObject2s is not set up.";
        Result expectedResult = Result.Failure(new RestApiException(cleanedExceptionMessage));

        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper = new ClientCorporationCustomObjectInstance2ListWrapper();
        customObjectListWrapper.setData(new ArrayList<>());
        ClientCorporationCustomObjectInstance2ListWrapper customObjectListWrapper2 = new ClientCorporationCustomObjectInstance2ListWrapper();
        ClientCorporationCustomObjectInstance2 clientCorporationCustomObjectInstance2 = new ClientCorporationCustomObjectInstance2();
        clientCorporationCustomObjectInstance2.setId(1);
        List<ClientCorporationCustomObjectInstance2> clientCorporationCustomObjectInstance2List = Arrays.asList(clientCorporationCustomObjectInstance2);
        customObjectListWrapper2.setData(clientCorporationCustomObjectInstance2List);
        when(bullhornDataMock.query(eq(ClientCorporationCustomObjectInstance2.class), eq("text1='Test'"), any(), any())).thenReturn(customObjectListWrapper, customObjectListWrapper2);

        ClientCorporationListWrapper listWrapper = new ClientCorporationListWrapper();
        listWrapper.setData(Arrays.asList(new ClientCorporation(1)));
        Mockito.doReturn(listWrapper).when(bullhornDataMock).search(eq(ClientCorporation.class), eq("id:1"), eq(Sets.newHashSet("id", "customObject2s(*)")),any());

        String noPermissionException = "{\n" +
            "  \"errorMessage\" : \"error persisting an entity of type: Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\",\n" +
            "  \"errors\" : [ ],\n" +
            "  \"entityName\" : \"Update Failed: You do not have permission for ClientCorporation Custom Object field customObject2s.\"\n" +
            "}";
        Mockito.doThrow(new RestApiException(noPermissionException)).when(bullhornDataMock).updateEntity(any());

        //test
        task.run();

        //verify
        Mockito.verify(csvFileWriterMock, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void getParentEntityTest_Exception() throws InvocationTargetException, IllegalAccessException {
        task = new LoadCustomObjectTask(Command.LOAD, 1, ClientCorporationCustomObjectInstance2.class, dataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        RestApiException expectedException = new RestApiException("Row 1: To-One Association: 'candidate' does not exist on ClientCorporationCustomObjectInstance2");
        task.entity = new ClientCorporationCustomObjectInstance2();

        RestApiException actualException = new RestApiException();
        try {
            task.getParentEntity("candidate.id");
        } catch (RestApiException e){
            actualException = e;
        }

        Assert.assertEquals(actualException.getMessage(), expectedException.getMessage());
    }


}
