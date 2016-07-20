package com.bullhorn.dataloader.service.executor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;

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
        //arrange
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{" + StringConsts.CHANGED_ENTITY_ID + ": 99}"));
        Result expectedResult = Result.Delete(99);

        //act
        DeleteTask deleteTask = new DeleteTask("Candidate", bhApi, jsonRow, csvFileWriter, propertyFileUtil);
        deleteTask.run();

        //assert
        verify(csvFileWriter).writeRow(jsonRowArgumentCaptor.capture(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void softDeleteFailureTest() throws ExecutionException, IOException {
        //arrange
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{errorMessage: REST ERROR}"));
        Result expectedResult = Result.Failure("REST ERROR");

        //act
        DeleteTask deleteTask = new DeleteTask("Candidate", bhApi, jsonRow, csvFileWriter, propertyFileUtil);
        deleteTask.run();

        //assert
        verify(csvFileWriter).writeRow(jsonRowArgumentCaptor.capture(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void hardDeleteSuccessTest() throws ExecutionException, IOException {
        //arrange
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{" + StringConsts.CHANGED_ENTITY_ID + ": 99}"));
        Result expectedResult = Result.Delete(99);

        //act
        DeleteTask deleteTask = new DeleteTask("Placement", bhApi, jsonRow, csvFileWriter, propertyFileUtil);
        deleteTask.run();

        //assert
        verify(csvFileWriter).writeRow(jsonRowArgumentCaptor.capture(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void hardDeleteFailureTest() throws ExecutionException, IOException {
        //arrange
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        when(bhApi.call(any(PostMethod.class))).thenReturn(new JSONObject("{errorMessage: REST ERROR}"));
        Result expectedResult = Result.Failure("REST ERROR");

        //act
        DeleteTask deleteTask = new DeleteTask("Placement", bhApi, jsonRow, csvFileWriter, propertyFileUtil);
        deleteTask.run();

        //assert
        verify(csvFileWriter).writeRow(jsonRowArgumentCaptor.capture(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void readOnlyFailureTest() throws ExecutionException, IOException {
        //arrange
        JsonRow jsonRow = new JsonRow();
        jsonRow.addImmediateAction(new String[] {"id"}, 99);
        Result expectedResult = Result.Failure("ERROR: Cannot delete BusinessSector because it is not deletable in REST.");

        //act
        DeleteTask deleteTask = new DeleteTask("BusinessSector", bhApi, jsonRow, csvFileWriter, propertyFileUtil);
        deleteTask.run();

        //assert
        verify(csvFileWriter).writeRow(jsonRowArgumentCaptor.capture(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
