package com.bullhorn.dataloader.task;

import com.beust.jcommander.internal.Lists;
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
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.standard.StandardFileContent;
import com.bullhornsdk.data.model.response.file.standard.StandardFileWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadAttachmentTaskTest {

    private PrintUtil printUtilMock;
    private CsvFileWriter csvFileWriter;
    private RestApi restApi;
    private ActionTotals actionTotals;
    private PropertyFileUtil propertyFileUtilMock_CandidateID;
    private PropertyFileUtil propertyFileUtilMock_CandidateExternalID;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LoadAttachmentTask task;

    private String relativeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");

    @Before
    public void setup() throws Exception {
        printUtilMock = mock(PrintUtil.class);
        csvFileWriter = mock(CsvFileWriter.class);
        restApi = mock(RestApi.class);
        actionTotals = mock(ActionTotals.class);
        propertyFileUtilMock_CandidateID = mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = mock(PropertyFileUtil.class);

        List<String> idExistField = Collections.singletonList("id");
        doReturn(Optional.of(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Candidate");

        List<String> externalIdExistField = Collections.singletonList("externalID");
        doReturn(Optional.of(externalIdExistField)).when(propertyFileUtilMock_CandidateExternalID).getEntityExistFields("Candidate");

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadAttachmentSuccessTest() throws Exception {
        Row row = TestUtils.createRow("candidate.id,relativeFilePath,isResume", "1001," + relativeFilePath + ",0");
        Result expectedResult = Result.Insert(0);
        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_CandidateID, restApi, printUtilMock, actionTotals);
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.searchForList(eq(Candidate.class), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task.run();
        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());

        Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadAttachmentNoRelativeFilePathTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,isResume", "2016Ext,1");
        Result expectedResult = Result.Failure(new IOException("Missing the 'relativeFilePath' column required for loadAttachments"));
        when(restApi.searchForList(eq(Candidate.class), eq("externalID:\"2016Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_CandidateExternalID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadAttachmentFailureTest() throws Exception {
        Row row = TestUtils.createRow("candidate.id,relativeFilePath,isResume", "1001," + relativeFilePath + ",0");
        Result expectedResult = Result.Failure(new RestApiException("Test"));
        when(restApi.searchForList(eq(Candidate.class), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenThrow(new RestApiException("Test"));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_CandidateID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void existPropertyConfiguredCorrectlyTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        Result expectedResult = Result.Insert(0);
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_CandidateExternalID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void existPropertyMissingTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Properties file is missing the 'candidateExistField' property required to lookup the parent entity.");
        PropertyFileUtil propertyFileUtilMock_Empty = mock(PropertyFileUtil.class);
        doReturn(Optional.empty()).when(propertyFileUtilMock_Empty).getEntityExistFields("Candidate");
        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_Empty, restApi, printUtilMock, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(anyObject(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void existPropertyConfiguredIncorrectlyTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: 'candidateExistField': 'bogus' does not exist on Candidate");
        PropertyFileUtil propertyFileUtilMock_Incorrect = mock(PropertyFileUtil.class);
        List<String> existFields = Collections.singletonList("bogus");
        doReturn(Optional.of(existFields)).when(propertyFileUtilMock_Incorrect).getEntityExistFields("Candidate");
        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_Incorrect, restApi, printUtilMock, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(anyObject(), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updateAttachmentSuccessTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        Result expectedResult = Result.Update(0);
        FileMeta file1 = new StandardFileMeta();
        file1.setName("original name");
        file1.setId(222);
        file1.setExternalID("extFileId1");
        List<FileMeta> fileList = Lists.newArrayList();
        fileList.add(file1);
        StandardFileContent mockedFileContent = new StandardFileContent();
        mockedFileContent.setFileContent("thisisafilecontent");
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApi.updateFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);
        when(restApi.getFileMetaData(anyObject(), anyInt())).thenReturn(fileList);
        when(restApi.getFileContent(anyObject(), anyInt(), anyInt())).thenReturn(mockedFileContent);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriter, propertyFileUtilMock_CandidateExternalID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(row), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }
}
