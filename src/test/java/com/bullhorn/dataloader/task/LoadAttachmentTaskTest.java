package com.bullhorn.dataloader.task;

import com.beust.jcommander.internal.Lists;
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
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.standard.StandardFileContent;
import com.bullhornsdk.data.model.response.file.standard.StandardFileWrapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadAttachmentTaskTest {

    private PrintUtil printUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private RestApi restApiMock;
    private ActionTotals actionTotalsMock;
    private PropertyFileUtil propertyFileUtilMock;
    private LoadAttachmentTask task;
    private Cache cacheMock;
    private CompleteUtil completeUtilMock;

    private final String relativeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");

    @Before
    public void setup() {
        printUtilMock = mock(PrintUtil.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        restApiMock = mock(RestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        cacheMock = mock(Cache.class);
        completeUtilMock = mock(CompleteUtil.class);

        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Collections.singletonList("externalID"));
        when(cacheMock.getEntry(any(), any(), any())).thenReturn(null);
    }

    @Test
    public void testRunInsertSuccessIdLookup() throws Exception {
        Row row = TestUtils.createRow("candidate.id,relativeFilePath,isResume", "1001," + relativeFilePath + ",0");
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Collections.singletonList("id"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1001 AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(any(), any(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.insert(0, 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunInsertSuccessExternalIdLookup() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\" AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(any(), any(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.insert(0, 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunUpdateSuccessFileContent() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext," + relativeFilePath + ",1,extFileId1,Updated Filename");
        FileMeta file1 = new StandardFileMeta();
        file1.setName("original name");
        file1.setId(222);
        file1.setExternalID("extFileId1");
        List<FileMeta> fileList = Lists.newArrayList(file1);
        StandardFileContent mockedFileContent = new StandardFileContent();
        mockedFileContent.setFileContent("this is the file content");
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\" AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.updateFile(any(), any(), any(FileMeta.class))).thenReturn(fileWrapper);
        when(restApiMock.getFileMetaData(any(), any())).thenReturn(fileList);
        when(restApiMock.getFileContent(any(), any(), any())).thenReturn(mockedFileContent);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.update(0, 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunUpdateSuccessName() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,externalID,name",
            "2011Ext,extFileId1,Updated Filename");
        FileMeta file1 = new StandardFileMeta();
        file1.setName("original name");
        file1.setId(222);
        file1.setExternalID("extFileId1");
        List<FileMeta> fileList = Lists.newArrayList(file1);
        StandardFileContent mockedFileContent = new StandardFileContent();
        mockedFileContent.setFileContent("this is the file content");
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\" AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.updateFile(any(), any(), any(FileMeta.class))).thenReturn(fileWrapper);
        when(restApiMock.getFileMetaData(any(), any())).thenReturn(fileList);
        when(restApiMock.getFileContent(any(), any(), any())).thenReturn(mockedFileContent);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.update(0, 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureParentEntityNotFound() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Parent Entity not found.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureMultipleParentEntitiesFound() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\" AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001, 1002));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: Multiple Records Exist. "
                + "Found 2 Candidate records with the same ExistField criteria of: externalID=2011Ext AND isDeleted=0");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureMissingExistField() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Lists.newArrayList());

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "java.lang.IllegalArgumentException: Properties file is missing the 'candidateExistField' "
                + "property required to lookup the parent entity.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureFieldDoesNotExist() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Collections.singletonList("bogus"));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
            "com.bullhornsdk.data.exception.RestApiException: 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureNoRelativeFilePath() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,isResume", "2016Ext,1");
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2016Ext\" AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(
            new IOException("Missing the 'relativeFilePath' column required for attachments"), 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureCannotReadFileFromDisk() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name",
            "2011Ext,bogus/bogus.txt,1,extFileId1,new filename");
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\" AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(any(), any(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(
            new RestApiException("Cannot read file from disk: bogus/bogus.txt"), 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void testRunFailureRestError() throws Exception {
        Row row = TestUtils.createRow("candidate.id,relativeFilePath,isResume", "1001," + relativeFilePath + ",0");
        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE)).thenReturn(Collections.singletonList("id"));
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1001 AND isDeleted:0"), any(), any()))
            .thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(any(), any(), any(FileMeta.class))).thenThrow(new RestApiException("Test"));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock,
            restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
        task.run();

        Result expectedResult = Result.failure(new RestApiException("Test"), 1001);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
