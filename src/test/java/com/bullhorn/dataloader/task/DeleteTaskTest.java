package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.standard.Appointment;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteTaskTest {

    private PropertyFileUtil propertyFileUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private RestApi restApiMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;
    private ArgumentCaptor<Result> resultArgumentCaptor;

    @Before
    public void setup() throws Exception {
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        restApiMock = mock(RestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        printUtilMock = mock(PrintUtil.class);

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void run_Success_Candidate() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Appointment() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.APPOINTMENT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.query(eq(Appointment.class), eq("isDeleted=false AND id=1"), any(), any())).thenReturn(TestUtils.getListWrapper(Appointment.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Note() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(Note.class), eq("isDeleted:false AND noteID:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Note.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Placement() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.PLACEMENT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Placement.class, 1));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_AlreadySoftDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(Candidate.class), eq("isDeleted:0 AND id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class));
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot Perform Delete: Candidate record with ID: 1 does not exist or has already been soft-deleted.");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_AlreadyHardDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.PLACEMENT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getListWrapper(Placement.class));
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot Perform Delete: Placement record with ID: 1 does not exist or has already been soft-deleted.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_NonDeletableEntityFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        DeleteTask task = new DeleteTask(EntityInfo.CLIENT_CORPORATION, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot Perform Delete: ClientCorporation records are not deletable.");

        task.run();

        verify(csvFileWriterMock).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_IdColumnFailure() throws IOException {
        Row row = new Row(1);
        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Cannot Perform Delete: missing 'id' column.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void getBooleanWhereStatement() throws IOException {
        Row row = TestUtils.createRow("id", "1");
        String falseString = "false";
        String trueString = "true";
        String zeroString = "0";
        String oneString = "1";
        String twoString = "2";

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);

        Assert.assertEquals("false", task.getBooleanWhereStatement(falseString));
        Assert.assertEquals("true", task.getBooleanWhereStatement(trueString));
        Assert.assertEquals("false", task.getBooleanWhereStatement(zeroString));
        Assert.assertEquals("true", task.getBooleanWhereStatement(oneString));
        Assert.assertEquals("false", task.getBooleanWhereStatement(twoString));
    }

    @Test
    public void testGetFieldEntityClassWithAssociation() throws IOException {
        Row row = TestUtils.createRow("id", "1");
        String candidateID = "candidate.id";

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Class<BullhornEntityInfo> bullhornEntityInfo = task.getFieldEntityClass(candidateID);

        Assert.assertEquals(bullhornEntityInfo.getSimpleName(), EntityInfo.CANDIDATE.getEntityName());
    }
}
