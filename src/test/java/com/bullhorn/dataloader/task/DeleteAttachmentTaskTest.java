package com.bullhorn.dataloader.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.response.file.standard.StandardFileApiResponse;

public class DeleteAttachmentTaskTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    private DeleteAttachmentTask task;

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
        dataMap.put("id", "1");
        dataMap.put("externalID","1");
        dataMap.put("relativeFilePath","testResume/Test Resume.doc");
        dataMap.put("isResume","0");
        dataMap.put("parentEntityID", "1");
    }

    @Test
    public void deleteAttachmentSuccessTest() throws Exception {
        //arrange
        final String[] expectedValues = {"1", "1", "testResume/Test Resume.doc", "0", "1"};
        final Result expectedResult = Result.Delete(0);
        task = new DeleteAttachmentTask(Method.DELETEATTACHMENTS, "Candidate", dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        final StandardFileApiResponse fileApiResponse = new StandardFileApiResponse();
        fileApiResponse.setFileId(0);
        when(bullhornData.deleteFile(anyObject(), anyInt(), anyInt())).thenReturn(fileApiResponse);

        //act
        task.run();

        //assert
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void deleteAttachmentFailureTest() throws ExecutionException, IOException {
        //arrange
        final String[] expectedValues = {"1", "1", "testResume/Test Resume.doc", "0", "1"};
        final Result expectedResult = Result.Failure(new RestApiException("Test").toString());
        task = new DeleteAttachmentTask(Method.DELETEATTACHMENTS, "Candidate", dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(bullhornData.deleteFile(any(), anyInt(), anyInt())).thenThrow(new RestApiException("Test"));

        //act
        task.run();

        //assert
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

}
