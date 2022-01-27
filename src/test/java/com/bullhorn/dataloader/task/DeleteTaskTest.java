package com.bullhorn.dataloader.task;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.standard.Appointment;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Placement;

public class DeleteTaskTest {

    private PropertyFileUtil propertyFileUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private RestApi restApiMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;
    private Cache cacheMock;
    private CompleteUtil completeUtilMock;

    @Before
    public void setup() {
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        restApiMock = mock(RestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        printUtilMock = mock(PrintUtil.class);
        cacheMock = mock(Cache.class);
        completeUtilMock = mock(CompleteUtil.class);

        when(cacheMock.getEntry(any(), any(), any())).thenReturn(null);
    }

    @Test
    public void testRunSuccessCandidate() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1 AND isDeleted:0"), any(), any())).thenReturn
            (TestUtils.getList(Candidate.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.delete(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunSuccessAppointment() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.queryForList(eq(Appointment.class), eq("id=1 AND isDeleted=false"), any(), any()))
            .thenReturn(TestUtils.getList(Appointment.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.APPOINTMENT, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.delete(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunSuccessNote() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Note.class), eq("noteID:1 AND isDeleted:false"), any(), any()))
            .thenReturn(TestUtils.getList(Note.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.NOTE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock,
            printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.delete(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunSuccessPlacement() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getList
            (Placement.class, 1));

        DeleteTask task = new DeleteTask(EntityInfo.PLACEMENT, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.delete(1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunAlreadySoftDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1 AND isDeleted:0"), any(), any())).thenReturn
            (TestUtils.getList(Candidate.class));

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new DataLoaderException(ErrorInfo.INTERNAL_SERVER_ERROR,
            "Cannot Perform Delete: Candidate record with ID: 1 does not exist or has already been soft-deleted."), 1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunAlreadyHardDeletedFailure() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id", "1");
        when(restApiMock.searchForList(eq(Placement.class), eq("id:1"), any(), any())).thenReturn(TestUtils.getList
            (Placement.class));

        DeleteTask task = new DeleteTask(EntityInfo.PLACEMENT, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new DataLoaderException(ErrorInfo.INTERNAL_SERVER_ERROR,
            "Cannot Perform Delete: Placement record with ID: 1 does not exist or has already been soft-deleted."), 1);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunNonDeletableEntityFailure() throws IOException {
        Row row = TestUtils.createRow("id", "1");

        DeleteTask task = new DeleteTask(EntityInfo.CLIENT_CORPORATION, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new DataLoaderException(ErrorInfo.INTERNAL_SERVER_ERROR,
            "Cannot Perform Delete: ClientCorporation records are not deletable."));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunIdColumnFailure() throws IOException {
        Row row = new Row("/going/to/fail.csv", 1);

        DeleteTask task = new DeleteTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new DataLoaderException(ErrorInfo.INVALID_SETTING,
            "Cannot Perform Delete: missing 'id' column."));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
