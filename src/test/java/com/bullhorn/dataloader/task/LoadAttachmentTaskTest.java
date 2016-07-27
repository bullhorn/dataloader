package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.FileMeta;
import com.bullhornsdk.data.model.response.file.standard.StandardFileWrapper;
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
import java.util.LinkedHashMap;
import java.util.List;
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

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    private LoadAttachmentTask task;

    @Before
    public void setUp() throws Exception {
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("externalID","1");
        dataMap.put("relativeFilePath","testResume/Test Resume.doc");
        dataMap.put("isResume","0");
    }

    @Test
    public void loadAttachmentSuccessTest() throws Exception {
        //arrange
        String[] expectedValues = {"1", "testResume/Test Resume.doc", "0"};
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 0, "");
        task = new LoadAttachmentTask(Method.LOADATTACHMENTS, "Candidate", dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1));
        ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);
        FileContent mockedFileContent = Mockito.mock(FileContent.class);
        FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(bullhornData.search(anyObject(), anyString(), anySet(), anyObject())).thenReturn(listWrapper);
        when(bullhornData.addFile(anyObject(), anyInt(), any(File.class), anyString(), anyObject(), anyBoolean())).thenReturn(fileWrapper);

        //act
        task.run();

        //assert
        verify(csvFileWriter).writeAttachmentRow(eq(expectedValues), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void loadAttachmentFailureTest() throws ExecutionException, IOException {
        //arrange
        String[] expectedValues = {"1", "testResume/Test Resume.doc", "0"};
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.NOT_SET, -1, "Test");
        task = new LoadAttachmentTask(Method.LOADATTACHMENTS, "Candidate", dataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1));
        ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);
        FileContent mockedFileContent = Mockito.mock(FileContent.class);
        FileMeta mockedFileMeta = Mockito.mock(FileMeta.class);
        StandardFileWrapper fileWrapper = new StandardFileWrapper(mockedFileContent, mockedFileMeta);
        when(bullhornData.search(anyObject(), anyString(), anySet(), anyObject())).thenReturn(listWrapper);
        when(bullhornData.addFile(anyObject(), anyInt(), any(File.class), anyString(), anyObject(), anyBoolean())).thenThrow(new RestApiException("Test"));

        //act
        task.run();

        //assert
        verify(csvFileWriter).writeAttachmentRow(eq(expectedValues), resultArgumentCaptor.capture());
        Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

}
