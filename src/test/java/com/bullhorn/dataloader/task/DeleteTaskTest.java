package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Appointment;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import com.bullhornsdk.data.model.response.list.CandidateListWrapper;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import com.bullhornsdk.data.model.response.list.StandardListWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteTaskTest {

    private PropertyFileUtil propertyFileUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private BullhornData bullhornDataMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;

    private DeleteTask task;

    @Before
    public void setUp() throws Exception {
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        printUtilMock = Mockito.mock(PrintUtil.class);

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("id", "1");
    }

    @Test
    public void run_Success_Candidate() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        when(bullhornDataMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(bullhornDataMock.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Appointment() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.APPOINTMENT, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        when(bullhornDataMock.query(eq(Appointment.class), eq("isDeleted=false AND id=1"), any(), any())).thenReturn(getListWrapper(Appointment.class));
        when(bullhornDataMock.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Placement() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.PLACEMENT, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        when(bullhornDataMock.search(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(getListWrapper(Placement.class));
        when(bullhornDataMock.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_AlreadySoftDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        when(bullhornDataMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(new CandidateListWrapper());
        when(bullhornDataMock.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Delete: Candidate record with ID: 1 does not exist or has already been soft-deleted.");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_NonDeletableEntityFailure() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.CLIENT_CORPORATION, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Delete: ClientCorporation records are not deletable.");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_IdColumnFailure() throws IOException {
        dataMap.clear();
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Row 1: Cannot Perform Delete: missing 'id' column.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_RestFailure() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);
        DeleteResponse response = new DeleteResponse();
        response.setChangeType(ChangeType.DELETE.toString());
        Message message = new Message();
        message.setPropertyName("FailureField");
        message.setDetailMessage("Because failed");
        response.setMessages(Arrays.asList(message));
        when(bullhornDataMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(bullhornDataMock.deleteEntity(any(), anyInt())).thenReturn(response);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Error occurred when making DELETE REST call:\n" +
            "\tError occurred on field FailureField due to the following: Because failed\n");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void getBooleanWhereStatement() throws IOException {
        String falseString = "false";
        String trueString = "true";
        String zeroString = "0";
        String oneString = "1";
        String twoString = "2";

        task = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornDataMock, printUtilMock, actionTotalsMock);

        Assert.assertEquals("false", task.getBooleanWhereStatement(falseString));
        Assert.assertEquals("true", task.getBooleanWhereStatement(trueString));
        Assert.assertEquals("false", task.getBooleanWhereStatement(zeroString));
        Assert.assertEquals("true", task.getBooleanWhereStatement(oneString));
        Assert.assertEquals("false", task.getBooleanWhereStatement(twoString));
    }

    public <B extends BullhornEntity> ListWrapper<B> getListWrapper(Class<B> entityClass) throws IllegalAccessException, InstantiationException {
        ListWrapper<B> listWrapper = new StandardListWrapper<B>();
        B entity = entityClass.newInstance();
        entity.setId(1);
        listWrapper.setData(Arrays.asList(entity));
        return listWrapper;
    }
}
