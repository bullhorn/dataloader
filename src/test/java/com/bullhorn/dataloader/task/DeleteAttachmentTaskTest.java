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
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.response.file.standard.StandardFileApiResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteAttachmentTaskTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private RestApi restApi;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    private DeleteAttachmentTask task;

    @Before
    public void setup() throws Exception {
        propertyFileUtil = mock(PropertyFileUtil.class);
        csvFileWriter = mock(CsvFileWriter.class);
        restApi = mock(RestApi.class);
        actionTotals = mock(ActionTotals.class);
        printUtil = mock(PrintUtil.class);

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void testDeleteAttachmentSuccess() throws Exception {
        Row row = TestUtils.createRow("id,Candidate.externalID,relativeFilePath,isResume,parentEntityID", "1,1,testResume/TestResume.doc,0,1");
        final Result expectedResult = Result.Delete(0);
        task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
        final StandardFileApiResponse fileApiResponse = new StandardFileApiResponse();
        fileApiResponse.setFileId(0);
        when(restApi.deleteFile(anyObject(), anyInt(), anyInt())).thenReturn(fileApiResponse);

        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void testDeleteAttachmentFailure() throws ExecutionException, IOException {
        Row row = TestUtils.createRow("id,Candidate.externalID,relativeFilePath,isResume,parentEntityID", "1,1,testResume/TestResume.doc,0,1");
        final Result expectedResult = Result.Failure(new RestApiException("Test"));
        task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
        when(restApi.deleteFile(any(), anyInt(), anyInt())).thenThrow(new RestApiException("Test"));

        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void testDeleteAttachmentMissingID() throws ExecutionException, IOException {
        Row row = TestUtils.createRow("Candidate.externalID,relativeFilePath,isResume,parentEntityID", "1,testResume/TestResume.doc,0,1");
        final Result expectedResult = Result.Failure(new IOException("Missing the 'id' column required for deleteAttachments"));
        task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void testDeleteAttachmentMissingParentEntityID() throws ExecutionException, IOException {
        Row row = TestUtils.createRow("id,Candidate.externalID,relativeFilePath,isResume", "1,1,testResume/TestResume.doc,0");
        final Result expectedResult = Result.Failure(new IOException("Missing the 'parentEntityID' column required for deleteAttachments"));
        task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
