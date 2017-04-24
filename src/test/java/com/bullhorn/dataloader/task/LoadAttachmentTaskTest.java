package com.bullhorn.dataloader.task;

import com.beust.jcommander.internal.Lists;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
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
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadAttachmentTaskTest {

    private PrintUtil printUtilMock;
    private CsvFileWriter csvFileWriter;
    private BullhornRestApi bullhornRestApi;
    private ActionTotals actionTotals;
    private PropertyFileUtil propertyFileUtilMock_CandidateID;
    private PropertyFileUtil propertyFileUtilMock_CandidateExternalID;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private Map<String, String> dataMap;
    private Map<String, String> dataMap2;
    private Map<String, String> dataMap3;
    private LoadAttachmentTask task;

    private Map<String, Method> methodMap = new HashMap();

    private String relativeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornRestApi = Mockito.mock(BullhornRestApi.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        propertyFileUtilMock_CandidateID = Mockito.mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = Mockito.mock(PropertyFileUtil.class);

        for (Method method : Arrays.asList(FileMeta.class.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        List<String> idExistField = Arrays.asList(new String[]{"id"});
        Mockito.doReturn(Optional.ofNullable(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Candidate");

        List<String> externalIdExistField = Arrays.asList(new String[]{"externalID"});
        Mockito.doReturn(Optional.ofNullable(externalIdExistField)).when(propertyFileUtilMock_CandidateExternalID).getEntityExistFields("Candidate");

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("candidate.id", "1001");
        dataMap.put("relativeFilePath", relativeFilePath);
        dataMap.put("isResume", "0");

        dataMap2 = new LinkedHashMap<String, String>();
        dataMap2.put("candidate.externalID", "2011Ext");
        dataMap2.put("relativeFilePath", relativeFilePath);
        dataMap2.put("isResume", "1");
        dataMap2.put("externalID", "extFileId1");
        dataMap2.put("name", "new filename");

        dataMap3 = new LinkedHashMap<String, String>();
        dataMap3.put("candidate.externalID", "2016Ext");
        dataMap3.put("isResume", "1");
    }

    @Test
    public void loadAttachmentSuccessTest() throws Exception {
        final String[] expectedValues = {"1001", relativeFilePath, "0", "1001"};
        final Result expectedResult = Result.Insert(0);
        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap, methodMap, csvFileWriter, propertyFileUtilMock_CandidateID, bullhornRestApi, printUtilMock, actionTotals);
        final FileContent mockedFileContent = Mockito.mock(FileContent.class);
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(bullhornRestApi.search(anyObject(), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(bullhornRestApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void loadAttachmentNoRelativeFilePathTest() throws Exception {
        final String[] expectedValues = {"2016Ext", "1", "1001"};
        final Result expectedResult = Result.Failure(new IOException("Row 1: Missing the 'relativeFilePath' column required for loadAttachments"));
        when(bullhornRestApi.search(anyObject(), eq("externalID:\"2016Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));

        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap3, methodMap, csvFileWriter, propertyFileUtilMock_CandidateExternalID, bullhornRestApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void loadAttachmentFailureTest() throws Exception {
        final String[] expectedValues = {"1001", relativeFilePath, "0", "1001"};
        final Result expectedResult = Result.Failure(new RestApiException("Test"));
        final FileContent mockedFileContent = Mockito.mock(FileContent.class);
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(bullhornRestApi.search(anyObject(), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(bullhornRestApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenThrow(new RestApiException("Test"));

        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap, methodMap, csvFileWriter, propertyFileUtilMock_CandidateID, bullhornRestApi, printUtilMock, actionTotals);
        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyConfiguredCorrectlyTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = Result.Insert(0);
        final FileContent mockedFileContent = Mockito.mock(FileContent.class);
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(bullhornRestApi.search(anyObject(), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(bullhornRestApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_CandidateExternalID, bullhornRestApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyMissingTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Row 1: Properties file is missing the 'candidateExistField' property required to lookup the parent entity.");
        PropertyFileUtil propertyFileUtilMock_Empty = Mockito.mock(PropertyFileUtil.class);
        Mockito.doReturn(Optional.ofNullable(null)).when(propertyFileUtilMock_Empty).getEntityExistFields("Candidate");
        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_Empty, bullhornRestApi, printUtilMock, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(anyObject(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyConfiguredIncorrectlyTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: 'candidateExistField': 'bogus' does not exist on Candidate");
        PropertyFileUtil propertyFileUtilMock_Incorrect = Mockito.mock(PropertyFileUtil.class);
        List<String> existFields = Arrays.asList(new String[]{"bogus"});
        Mockito.doReturn(Optional.ofNullable(existFields)).when(propertyFileUtilMock_Incorrect).getEntityExistFields("Candidate");
        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_Incorrect, bullhornRestApi, printUtilMock, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(anyObject(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void updateAttachmentSuccessTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = Result.Update(0);
        FileMeta file1 = new StandardFileMeta();
        file1.setName("original name");
        file1.setId(222);
        file1.setExternalID("extFileId1");
        List<FileMeta> fileList = Lists.newArrayList();
        fileList.add(file1);
        StandardFileContent mockedFileContent = new StandardFileContent();
        mockedFileContent.setFileContent("thisisafilecontent");
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(bullhornRestApi.search(anyObject(), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(bullhornRestApi.updateFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);
        when(bullhornRestApi.getFileMetaData(anyObject(), anyInt())).thenReturn(fileList);
        when(bullhornRestApi.getFileContent(anyObject(), anyInt(), anyInt())).thenReturn(mockedFileContent);

        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_CandidateExternalID, bullhornRestApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
