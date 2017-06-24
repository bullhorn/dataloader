package com.bullhorn.dataloader.task;

import com.beust.jcommander.internal.Lists;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
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
    private Map<String, String> dataMap;
    private Map<String, String> dataMap2;
    private Map<String, String> dataMap3;
    private LoadAttachmentTask task;

    private Map<String, Method> methodMap = new HashMap<>();

    private String relativeFilePath = TestUtils.getResourceFilePath("testResume/TestResume.doc");

    @Before
    public void setup() throws Exception {
        printUtilMock = mock(PrintUtil.class);
        csvFileWriter = mock(CsvFileWriter.class);
        restApi = mock(RestApi.class);
        actionTotals = mock(ActionTotals.class);
        propertyFileUtilMock_CandidateID = mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = mock(PropertyFileUtil.class);

        for (Method method : Arrays.asList(FileMeta.class.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        List<String> idExistField = Arrays.asList(new String[]{"id"});
        doReturn(Optional.ofNullable(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Candidate");

        List<String> externalIdExistField = Arrays.asList(new String[]{"externalID"});
        doReturn(Optional.ofNullable(externalIdExistField)).when(propertyFileUtilMock_CandidateExternalID).getEntityExistFields("Candidate");

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
        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap, methodMap, csvFileWriter, propertyFileUtilMock_CandidateID, restApi, printUtilMock, actionTotals);
        final FileContent mockedFileContent = mock(FileContent.class);
        final FileMeta mockedFileMeta = mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.search(anyObject(), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(restApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void loadAttachmentNoRelativeFilePathTest() throws Exception {
        final String[] expectedValues = {"2016Ext", "1", "1001"};
        final Result expectedResult = Result.Failure(new IOException("Row 1: Missing the 'relativeFilePath' column required for loadAttachments"));
        when(restApi.search(anyObject(), eq("externalID:\"2016Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));

        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap3, methodMap, csvFileWriter, propertyFileUtilMock_CandidateExternalID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void loadAttachmentFailureTest() throws Exception {
        final String[] expectedValues = {"1001", relativeFilePath, "0", "1001"};
        final Result expectedResult = Result.Failure(new RestApiException("Test"));
        final FileContent mockedFileContent = mock(FileContent.class);
        final FileMeta mockedFileMeta = mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.search(anyObject(), eq("id:1001"), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(restApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenThrow(new RestApiException("Test"));

        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap, methodMap, csvFileWriter, propertyFileUtilMock_CandidateID, restApi, printUtilMock, actionTotals);
        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyConfiguredCorrectlyTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = Result.Insert(0);
        final FileContent mockedFileContent = mock(FileContent.class);
        final FileMeta mockedFileMeta = mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.search(anyObject(), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(restApi.addFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);

        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_CandidateExternalID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyMissingTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Row 1: Properties file is missing the 'candidateExistField' property required to lookup the parent entity.");
        PropertyFileUtil propertyFileUtilMock_Empty = mock(PropertyFileUtil.class);
        doReturn(Optional.ofNullable(null)).when(propertyFileUtilMock_Empty).getEntityExistFields("Candidate");
        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_Empty, restApi, printUtilMock, actionTotals);

        task.run();

        verify(csvFileWriter).writeRow(anyObject(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyConfiguredIncorrectlyTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "extFileId1", "new filename", "1001"};
        final Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "com.bullhornsdk.data.exception.RestApiException: Row 1: 'candidateExistField': 'bogus' does not exist on Candidate");
        PropertyFileUtil propertyFileUtilMock_Incorrect = mock(PropertyFileUtil.class);
        List<String> existFields = Arrays.asList(new String[]{"bogus"});
        doReturn(Optional.ofNullable(existFields)).when(propertyFileUtilMock_Incorrect).getEntityExistFields("Candidate");
        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_Incorrect, restApi, printUtilMock, actionTotals);

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
        final FileMeta mockedFileMeta = mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(restApi.search(anyObject(), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));
        when(restApi.updateFile(anyObject(), anyInt(), any(FileMeta.class))).thenReturn(fileWrapper);
        when(restApi.getFileMetaData(anyObject(), anyInt())).thenReturn(fileList);
        when(restApi.getFileContent(anyObject(), anyInt(), anyInt())).thenReturn(mockedFileContent);

        task = new LoadAttachmentTask(1, EntityInfo.CANDIDATE, dataMap2, methodMap, csvFileWriter, propertyFileUtilMock_CandidateExternalID, restApi, printUtilMock, actionTotals);
        task.run();

        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
