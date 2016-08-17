package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import com.bullhornsdk.data.model.response.crud.Message;
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

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    private DeleteTask task;

    @Before
    public void setUp() throws Exception {
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("id","1");
    }

    @Test
    public void run_Success() throws IOException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, Candidate.class, dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(bullhornData.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void run_NullFailure() throws IOException {
        dataMap = new LinkedHashMap<String, String>();

        task = new DeleteTask(Command.DELETE, 1, Candidate.class, dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.NOT_SET, -1, "java.lang.NumberFormatException: null");

        task.run();

        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void run_RestFailure() throws IOException {
        final String[] expectedValues = {"1"};
        task = new DeleteTask(Command.DELETE, 1, Candidate.class, dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        DeleteResponse response = new DeleteResponse();
        response.setChangeType(ChangeType.DELETE.toString());
        Message message = new Message();
        message.setPropertyName("FailureField");
        message.setDetailMessage("Because failed");
        response.setMessages(Arrays.asList(message));
        when(bullhornData.deleteEntity(any(), anyInt())).thenReturn(response);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.NOT_SET, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Error occurred when making DELETE REST call:\n" +
                "\tError occurred on field FailureField due to the following: Because failed\n");

        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }


}
