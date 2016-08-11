package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.file.FileContent;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileWrapper;
import com.bullhornsdk.data.model.response.list.CandidateListWrapper;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadAttachmentTaskTest {

    private PrintUtil printUtilMock;
    private CsvFileWriter csvFileWriter;
    private BullhornData bullhornData;
    private ActionTotals actionTotals;
    private PropertyFileUtil propertyFileUtilMock_CandidateID;
    private PropertyFileUtil propertyFileUtilMock_CandidateExternalID;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private LinkedHashMap<String, String> dataMap2;
    private LoadAttachmentTask task;

    private String relativeFilePath = getFilePath("testResume/TestResume.doc");

    @Before
    public void setUp() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        propertyFileUtilMock_CandidateID = Mockito.mock(PropertyFileUtil.class);
        propertyFileUtilMock_CandidateExternalID = Mockito.mock(PropertyFileUtil.class);

        List<String> idExistField = Arrays.asList(new String [] {"id"});
        Mockito.doReturn(Optional.ofNullable(idExistField)).when(propertyFileUtilMock_CandidateID).getEntityExistFields("Candidate");

        List<String> externalIdExistField = Arrays.asList(new String [] {"externalID"});
        Mockito.doReturn(Optional.ofNullable(externalIdExistField)).when(propertyFileUtilMock_CandidateExternalID).getEntityExistFields("Candidate");

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("id","1001");
        dataMap.put("relativeFilePath",relativeFilePath);
        dataMap.put("isResume","0");

        dataMap2 = new LinkedHashMap<String, String>();
        dataMap2.put("externalID","2011Ext");
        dataMap2.put("relativeFilePath",relativeFilePath);
        dataMap2.put("isResume","1");
    }

    @Test
    public void loadAttachmentSuccessTest() throws Exception {
        final String[] expectedValues = {"1001", relativeFilePath, "0", "1001"};
        final Result expectedResult = Result.Insert(0);
        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, Candidate.class, dataMap, csvFileWriter, propertyFileUtilMock_CandidateID, bullhornData, printUtilMock, actionTotals);

        final List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1001));

        final ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);

        final FileContent mockedFileContent = Mockito.mock(FileContent.class);
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);

        //when(bullhornData.search(anyObject(), anyString(), anySet(), anyObject())).thenReturn(listWrapper);
        when(bullhornData.search(anyObject(), eq("id:1001"), anySet(), anyObject())).thenReturn(listWrapper);
        when(bullhornData.addFile(anyObject(), anyInt(), any(File.class), anyString(), anyObject(), anyBoolean())).thenReturn(fileWrapper);

        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void loadAttachmentFailureTest() throws ExecutionException, IOException {
        final String[] expectedValues = {"1001", relativeFilePath, "0", "1001"};
        final Result expectedResult = Result.Failure(new RestApiException("Test"));
        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, Candidate.class, dataMap, csvFileWriter, propertyFileUtilMock_CandidateID, bullhornData, printUtilMock, actionTotals);

        final List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1001));

        final ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);

        final FileContent mockedFileContent = Mockito.mock(FileContent.class);
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);

        when(bullhornData.search(anyObject(), eq("id:1001"), anySet(), anyObject())).thenReturn(listWrapper);
        when(bullhornData.addFile(anyObject(), anyInt(), any(File.class), anyString(), anyObject(), anyBoolean())).thenThrow(new RestApiException("Test"));

        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void existPropertyConfiguredCorrectlyTest() throws Exception {
        final String[] expectedValues = {"2011Ext", relativeFilePath, "1", "1001"};
        final Result expectedResult = Result.Insert(0);

        task = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, Candidate.class, dataMap2, csvFileWriter, propertyFileUtilMock_CandidateExternalID, bullhornData, printUtilMock, actionTotals);

        final List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1001));

        final ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);

        final FileContent mockedFileContent = Mockito.mock(FileContent.class);
        final FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        final StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);

        when(bullhornData.search(anyObject(), eq("externalID:\"2011Ext\""), anySet(), anyObject())).thenReturn(listWrapper);
        when(bullhornData.addFile(anyObject(), anyInt(), any(File.class), anyString(), anyObject(), anyBoolean())).thenReturn(fileWrapper);

        task.run();
        verify(csvFileWriter).writeRow(eq(expectedValues), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}
