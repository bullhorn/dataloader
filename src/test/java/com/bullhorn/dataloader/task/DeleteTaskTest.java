package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteTaskTest {

    private PropertyFileUtil propertyFileUtil;
    private BullhornAPI bhApi;
    private CsvFileWriter csvFileWriter;
    private ArgumentCaptor<JsonRow> jsonRowArgumentCaptor;
    private ArgumentCaptor<Result> resultArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        bhApi = Mockito.mock(BullhornAPI.class);
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);

        // Mock out the bhApi class methods we need to call
        when(bhApi.serialize(any())).thenReturn("{isDeleted: true}");

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        jsonRowArgumentCaptor = ArgumentCaptor.forClass(JsonRow.class);
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void softDeleteSuccessTest() throws ExecutionException, IOException {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{" + StringConsts.CHANGED_ENTITY_ID + ": 99}"));

        final Result expectedResult = Result.Delete(99);
        final DeleteTask deleteTask = new DeleteTask("Candidate", bhApi, jsonRow, csvFileWriter, propertyFileUtil);

        deleteTask.run();
        verify(csvFileWriter).writeRow(eq(null), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void softDeleteFailureTest() throws ExecutionException, IOException {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{errorMessage: REST ERROR}"));

        final Result expectedResult = Result.Failure("REST ERROR");
        final DeleteTask deleteTask = new DeleteTask("Candidate", bhApi, jsonRow, csvFileWriter, propertyFileUtil);

        deleteTask.run();
        verify(csvFileWriter).writeRow(eq(null), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void hardDeleteSuccessTest() throws ExecutionException, IOException {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{" + StringConsts.CHANGED_ENTITY_ID + ": 99}"));

        final Result expectedResult = Result.Delete(99);
        final DeleteTask deleteTask = new DeleteTask("Placement", bhApi, jsonRow, csvFileWriter, propertyFileUtil);

        deleteTask.run();
        verify(csvFileWriter).writeRow(eq(null), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void hardDeleteFailureTest() throws ExecutionException, IOException {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{errorMessage: REST ERROR}"));

        final Result expectedResult = Result.Failure("REST ERROR");
        final DeleteTask deleteTask = new DeleteTask("Placement", bhApi, jsonRow, csvFileWriter, propertyFileUtil);

        deleteTask.run();
        verify(csvFileWriter).writeRow(eq(null), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void readOnlyFailureTest() throws ExecutionException, IOException {
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);

        final Result expectedResult = Result.Failure("ERROR: Cannot delete BusinessSector because it is not deletable in REST.");
        final DeleteTask deleteTask = new DeleteTask("BusinessSector", bhApi, jsonRow, csvFileWriter, propertyFileUtil);

        deleteTask.run();
        verify(csvFileWriter).writeRow(eq(null), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void missingIdFailureTest() throws ExecutionException, IOException {
        JsonRow jsonRow = new JsonRow();
        jsonRow.setRowNumber(1);
        jsonRow.addImmediateAction(new String[] {"name"}, "John Smith");

        final Result expectedResult = Result.Failure("ERROR: Cannot delete row: 1.  CSV row is missing the \"id\" column.");
        final DeleteTask deleteTask = new DeleteTask("Candidate", bhApi, jsonRow, csvFileWriter, propertyFileUtil);

        deleteTask.run();
        verify(csvFileWriter).writeRow(eq(null), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertEquals(expectedResult, actualResult);
    }
}