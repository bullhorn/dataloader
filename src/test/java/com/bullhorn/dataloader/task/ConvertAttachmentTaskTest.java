package com.bullhorn.dataloader.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
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

public class ConvertAttachmentTaskTest {

    private ActionTotals actionTotalsMock;
    private Cache cacheMock;
    private CompleteUtil completeUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private RestApi restApiMock;

    @Before
    public void setup() {
        actionTotalsMock = mock(ActionTotals.class);
        cacheMock = mock(Cache.class);
        completeUtilMock = mock(CompleteUtil.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        restApiMock = mock(RestApi.class);
    }

    @Test
    public void testRunConvertSuccess() throws IOException {
        String relativeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume", "11," + relativeFilePath + ",1");
        when(propertyFileUtilMock.getConvertedAttachmentFilepath(EntityInfo.CANDIDATE, "11"))
            .thenReturn(TestUtils.getResourceFilePath("convertedAttachments/Candidate/11.html"));

        ConvertAttachmentTask task = new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(Result.convert()));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.CONVERT, 1);
    }

    @Test
    public void testRunSkipSuccess() throws IOException {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume", "12,not/a/file.doc,0");

        ConvertAttachmentTask task = new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(Result.skip()));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.SKIP, 1);
    }

    @Test
    public void testRunFailureNoRelativeFilePath() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,isResume", "13,1");

        ConvertAttachmentTask task = new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new IOException("Missing the 'relativeFilePath' column required for attachments"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }

    @Test
    public void testRunFailureBadFilePath() throws IOException {
        String relativeFilePath = "testResume/TestResume.doc";
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume", "14," + relativeFilePath + ",1");
        when(propertyFileUtilMock.getConvertedAttachmentFilepath(EntityInfo.CANDIDATE, "14")).thenReturn(relativeFilePath);

        ConvertAttachmentTask task = new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new FileNotFoundException("path/to/fake/testResume/TestResume.doc (No such file or directory)"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
    }
}
