package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
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
    private BullhornAPI bhApi;
    private CsvFileWriter csvFileWriter;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private BullhornData bullhornData;

    private LoadAttachmentTask task;

    @Before
    public void setUp() throws Exception {
        bhApi = Mockito.mock(BullhornAPI.class);
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);

        // Mock out the bhApi class methods we need to call
        when(bhApi.serialize(any())).thenReturn("{isDeleted: true}");

        // Capture arguments to the writeRow method - this is our output from the deleteTask run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("externalID","1");
        dataMap.put("relativeFilePath","testResume/Amy Cherwin Resume.doc");
        dataMap.put("isResume","0");
    }

    @Test
    public void loadAttachmentSuccessTest() throws Exception {
        //arrange
        String[] expectedValues = {"1", "testResume/Test Resume.doc", "0"};
        Result expectedResult = new Result(Result.Status.SUCCESS, null, 0, "");
        task = new LoadAttachmentTask("Candidate", dataMap, csvFileWriter, propertyFileUtil, bullhornData);
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
        Result expectedResult = new Result(Result.Status.FAILURE, null, null, "Test");
        task = new LoadAttachmentTask("Candidate", dataMap, csvFileWriter, propertyFileUtil, bullhornData);
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
