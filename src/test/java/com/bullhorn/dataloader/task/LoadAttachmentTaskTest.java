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
import org.junit.Before;
import org.junit.Test;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadAttachmentTaskTest {

    private PrintUtil printUtilMock;
    private CsvFileWriter csvFileWriterMock;
    private RestApi restApiMock;
    private ActionTotals actionTotalsMock;
    private PropertyFileUtil propertyFileUtilMock_CandidateID;
    private PropertyFileUtil propertyFileUtilMock_CandidateExternalID;
    private LoadAttachmentTask task;

    private String relativeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");

    @Before
    public void setup() throws Exception {
        printUtilMock = mock(PrintUtil.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        restApiMock = mock(RestApi.class);
        actionTotalsMock = mock(ActionTotals.class);
        propertyFileUtilMock_CandidateID = mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = mock(PropertyFileUtil.class);

        when(propertyFileUtilMock_CandidateID.getEntityExistFields("Candidate")).thenReturn(Optional.of(Collections.singletonList("id")));
        when(propertyFileUtilMock_CandidateExternalID.getEntityExistFields("Candidate")).thenReturn(Optional.of(Collections.singletonList("externalID")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadAttachmentSuccessTest() throws Exception {
        Row row = TestUtils.createRow("candidate.id,relativeFilePath,isResume", "1001," + relativeFilePath + ",0");
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_CandidateID, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.insert(0);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadAttachmentNoRelativeFilePathTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,isResume", "2016Ext,1");
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2016Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.failure(new IOException("Missing the 'relativeFilePath' column required for loadAttachments"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadAttachmentFailureTest() throws Exception {
        Row row = TestUtils.createRow("candidate.id,relativeFilePath,isResume", "1001," + relativeFilePath + ",0");
        when(restApiMock.searchForList(eq(Candidate.class), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenThrow(new RestApiException("Test"));

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_CandidateID, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.failure(new RestApiException("Test"));
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void existPropertyConfiguredCorrectlyTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        FileContent mockedFileContent = mock(FileContent.class);
        FileMeta mockedFileMeta = mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.insert(0);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void existPropertyMissingTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        PropertyFileUtil propertyFileUtilMock_Empty = mock(PropertyFileUtil.class);
        doReturn(Optional.empty()).when(propertyFileUtilMock_Empty).getEntityExistFields("Candidate");

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_Empty, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Properties file is missing the 'candidateExistField' property required to lookup the parent entity.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void existPropertyConfiguredIncorrectlyTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
        PropertyFileUtil propertyFileUtilMock_Incorrect = mock(PropertyFileUtil.class);
        List<String> existFields = Collections.singletonList("bogus");
        doReturn(Optional.of(existFields)).when(propertyFileUtilMock_Incorrect).getEntityExistFields("Candidate");

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_Incorrect, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: 'candidateExistField': 'bogus' does not exist on Candidate");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updateAttachmentSuccessTest() throws Exception {
        Row row = TestUtils.createRow("candidate.externalID,relativeFilePath,isResume,externalID,name", "2011Ext," + relativeFilePath + ",1,extFileId1,new filename");
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
        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getList(Candidate.class, 1001));
        when(restApiMock.updateFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);
        when(restApiMock.getFileMetaData(anyObject(), anyInt())).thenReturn(fileList);
        when(restApiMock.getFileContent(anyObject(), anyInt(), anyInt())).thenReturn(mockedFileContent);

        task = new LoadAttachmentTask(EntityInfo.CANDIDATE, row, csvFileWriterMock, propertyFileUtilMock_CandidateExternalID, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = Result.update(0);
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
