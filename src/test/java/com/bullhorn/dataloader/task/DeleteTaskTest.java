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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteTaskTest {

    private PropertyFileUtil propertyFileUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private RestApi restApiMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;

    @Before
    public void setup() throws Exception {
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        restApiMock = mock(RestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        printUtilMock = mock(PrintUtil.class);
    }

    @Test
    public void run_Success_Candidate() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1 AND isDeleted:0"), any(), any())).thenReturn(TestUtils.getList(Candidate.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Success_Appointment() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.queryForList(eq(Appointment.class), eq("id=1 AND isDeleted=false"), any(), any())).thenReturn(TestUtils.getList(Appointment.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.APPOINTMENT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Success_Note() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Note.class), eq("noteID:1 AND isDeleted:false"), any(), any())).thenReturn(TestUtils.getList(Note.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Success_Placement() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getList(Placement.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.PLACEMENT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_AlreadySoftDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1 AND isDeleted:0"), any(), any())).thenReturn(TestUtils.getList(Candidate.class));

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot Perform Delete: Candidate record with ID: 1 does not exist or has already been soft-deleted.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_AlreadyHardDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getList(Placement.class));

        DeleteTask task = new DeleteTask(EntityInfo.PLACEMENT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot Perform Delete: Placement record with ID: 1 does not exist or has already been soft-deleted.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_NonDeletableEntityFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");

        DeleteTask task = new DeleteTask(EntityInfo.CLIENT_CORPORATION, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot Perform Delete: ClientCorporation records are not deletable.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_IdColumnFailure() throws IOException {
        Row row = new Row(1);

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Cannot Perform Delete: missing 'id' column.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
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
}
