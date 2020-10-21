package com.bullhorn.dataloader.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
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
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.response.file.standard.StandardFileApiResponse;

public class DeleteAttachmentTaskTest {

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
    }

    @Test
    public void testDeleteAttachmentSuccess() throws Exception {
        Row row = TestUtils.createRow("id,Candidate.externalID,relativeFilePath,isResume,parentEntityID", "1,1," +
            "testResume/TestResume.doc,0,1");
        StandardFileApiResponse fileApiResponse = new StandardFileApiResponse();
        fileApiResponse.setFileId(0);
        when(restApiMock.deleteFile(anyObject(), anyInt(), anyInt())).thenReturn(fileApiResponse);

        DeleteAttachmentTask task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.delete(0);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testDeleteAttachmentFailure() throws IOException {
        Row row = TestUtils.createRow("id,Candidate.externalID,relativeFilePath,isResume,parentEntityID", "1,1," +
             "testResume/TestResume.doc,0,1");
        when(restApiMock.deleteFile(any(), anyInt(), anyInt())).thenThrow(new RestApiException("Test"));

        DeleteAttachmentTask task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new RestApiException("Test"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testDeleteAttachmentMissingID() throws IOException {
        Row row = TestUtils.createRow("Candidate.externalID,relativeFilePath,isResume,parentEntityID", "1," +
             "testResume/TestResume.doc,0,1");

        DeleteAttachmentTask task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new IOException("Missing the 'id' column required for deleteAttachments"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testDeleteAttachmentMissingParentEntityID() throws Exception {
        Row row = TestUtils.createRow("id,Candidate.externalID,relativeFilePath,isResume", "1,1," +
             "testResume/TestResume.doc,0");

        DeleteAttachmentTask task = new DeleteAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new IOException("Missing the 'parentEntityID' column required for deleteAttachments"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
