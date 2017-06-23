package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.csv.CsvFileWriter;
import com.bullhorn.dataloader.csv.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.standard.Appointment;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.enums.ChangeType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteTaskTest {

    private PropertyFileUtil propertyFileUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private BullhornRestApi bullhornRestApiMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private Map<String, String> dataMap;

    @Before
    public void setup() throws Exception {
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
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
        DeleteTask task = new DeleteTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Appointment() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        DeleteTask task = new DeleteTask(1, EntityInfo.APPOINTMENT, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.query(eq(Appointment.class), eq("isDeleted=false AND id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Appointment.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Note() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        DeleteTask task = new DeleteTask(1, EntityInfo.NOTE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.search(eq(Note.class), eq("isDeleted:false AND noteID:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Note.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Placement() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        DeleteTask task = new DeleteTask(1, EntityInfo.PLACEMENT, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.search(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Placement.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_AlreadySoftDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        DeleteTask task = new DeleteTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Delete: Candidate record with ID: 1 does not exist or has already been soft-deleted.");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_AlreadyHardDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        DeleteTask task = new DeleteTask(1, EntityInfo.PLACEMENT, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.search(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Placement.class));
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Delete: Placement record with ID: 1 does not exist or has already been soft-deleted.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_NonDeletableEntityFailure() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        DeleteTask task = new DeleteTask(1, EntityInfo.CLIENT_CORPORATION, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot Perform Delete: ClientCorporation records are not deletable.");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_IdColumnFailure() throws IOException {
        dataMap.clear();
        DeleteTask task = new DeleteTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Row 1: Cannot Perform Delete: missing 'id' column.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_RestFailure() throws IOException, InstantiationException, IllegalAccessException {
        final String[] expectedValues = {"1"};
        DeleteTask task = new DeleteTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(bullhornRestApiMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        when(bullhornRestApiMock.deleteEntity(any(), anyInt())).thenReturn(TestUtils.getResponse(ChangeType.DELETE, null, "FailureField", "Because failed"));
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

        DeleteTask task = new DeleteTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);

        Assert.assertEquals("false", task.getBooleanWhereStatement(falseString));
        Assert.assertEquals("true", task.getBooleanWhereStatement(trueString));
        Assert.assertEquals("false", task.getBooleanWhereStatement(zeroString));
        Assert.assertEquals("true", task.getBooleanWhereStatement(oneString));
        Assert.assertEquals("false", task.getBooleanWhereStatement(twoString));
    }

    @Test
    public void testGetFieldEntityClassWithAssociation() {
        final String candidateID = "candidate.id";

        DeleteTask task = new DeleteTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        Class<BullhornEntityInfo> bullhornEntityInfo = task.getFieldEntityClass(candidateID);

        Assert.assertEquals(bullhornEntityInfo.getSimpleName(), EntityInfo.CANDIDATE.getEntityName());
    }
}
