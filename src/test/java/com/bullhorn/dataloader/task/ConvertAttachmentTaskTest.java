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
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConvertAttachmentTaskTest {

    private ArgumentCaptor<Result> resultArgumentCaptor;
    private CsvFileWriter csvFileWriterMock;
    private RestApi restApiMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;
    private ConvertAttachmentTask task;
    private PropertyFileUtil propertyFileUtilMock;
    private String resumeFilePath;

    @Before
    public void setup() throws Exception {
        csvFileWriterMock = mock(CsvFileWriter.class);
        restApiMock = mock(RestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        resumeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void convertAttachmentToHtmlTest() throws Exception {
        Row row = TestUtils.createRow("id,relativeFilePath,isResume", "1," + resumeFilePath + ",1");
        task = new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        String result = task.convertAttachmentToHtml();
        Assert.assertNotNull(result);
    }

    @Test
    public void run_Success() throws IOException {
        Row row = TestUtils.createRow("id,relativeFilePath,isResume", "1," + resumeFilePath + ",1");
        Result expectedResult = Result.Convert();
        task = spy(new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock));
        doNothing().when(task).writeHtmlToFile(anyString());

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.CONVERT, 1);
    }

    @Test
    public void run_Success_Skip() throws IOException {
        Row row = TestUtils.createRow("id,relativeFilePath,isResume", "1," + resumeFilePath + ",0");
        Result expectedResult = Result.Skip();
        task = spy(new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock));
        doNothing().when(task).writeHtmlToFile(anyString());

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.SKIP, 1);
    }

    @Test
    public void getConvertedAttachmentPathTest() throws IOException {
        Row row = TestUtils.createRow("clientContact.externalID,relativeFilePath,isResume", "1," + resumeFilePath + ",0");
        String expectedResult = "convertedAttachments/ClientContact/1.html";

        task = new ConvertAttachmentTask(EntityInfo.CLIENT_CONTACT, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);

        String actualResult = task.getConvertedAttachmentPath();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void convertAttachmentNoRelativeFilePathTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,isResume", "2016Ext,1");
        final Result expectedResult = Result.Failure(new IOException("Missing the 'relativeFilePath' column required for convertAttachments"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2016Ext\""), any(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        task = new ConvertAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
