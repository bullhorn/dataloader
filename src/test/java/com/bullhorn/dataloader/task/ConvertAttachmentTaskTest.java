package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.response.list.CandidateListWrapper;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConvertAttachmentTaskTest {

    private Map<String, String> dataMap;
    private Map<String, String> dataMap2;
    private ExecutorService executorService;
    private Map<String, Method> methodMap;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private Map<String, Integer> countryNameToIdMap;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotalMock;
    private ConvertAttachmentTask task;
    private PropertyFileUtil candidateIdProperties;
    private PropertyFileUtil candidateExternalIdProperties;

    @Before
    public void setUp() throws Exception {
        executorService = Mockito.mock(ExecutorService.class);
        csvReader = Mockito.mock(CsvReader.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotalMock = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);
        candidateExternalIdProperties = Mockito.mock(PropertyFileUtil.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("id", "1");
        dataMap.put("relativeFilePath", TestUtils.getResourceFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume", "1");

        dataMap2 = new LinkedHashMap<String, String>();
        dataMap2.put("candidate.externalID", "2016Ext");
        dataMap2.put("isResume", "1");

        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void convertAttachmentToHtmlTest() throws Exception {
        task = Mockito.spy(new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotalMock));

        String result = task.convertAttachmentToHtml();

        Assert.assertNotNull(result);
    }

    @Test
    public void run_Success() throws IOException {
        Result expectedResult = Result.Convert();
        task = Mockito.spy(new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotalMock));
        doNothing().when(task).writeHtmlToFile(anyString());

        task.run();
        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));

        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalMock, Mockito.times(1)).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void run_Success_Skip() throws IOException {
        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("id", "1");
        dataMap.put("relativeFilePath", TestUtils.getResourceFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume", "0");

        Result expectedResult = Result.Skip();
        task = Mockito.spy(new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotalMock));
        doNothing().when(task).writeHtmlToFile(anyString());

        task.run();
        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));

        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.INSERT);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.UPDATE);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.CONVERT);
        Mockito.verify(actionTotalMock, Mockito.times(1)).incrementActionTotal(Result.Action.SKIP);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.DELETE);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.FAILURE);
        Mockito.verify(actionTotalMock, never()).incrementActionTotal(Result.Action.NOT_SET);
    }

    @Test
    public void getConvertedAttachmentPathTest() {
        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("clientContact.externalID", "1");
        dataMap.put("relativeFilePath", TestUtils.getResourceFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume", "0");
        String expectedResult = "convertedAttachments/ClientContact/1.html";

        task = Mockito.spy(new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CLIENT_CONTACT, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotalMock));
        task.init();

        String actualResult = task.getConvertedAttachmentPath();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void convertAttachmentNoRelativeFilePathTest() throws IOException {
        final Result expectedResult = Result.Failure(new IOException("Row 1: Missing the 'relativeFilePath' column required for convertAttachments"));

        final List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(1001));

        final ListWrapper<Candidate> listWrapper = new CandidateListWrapper();
        listWrapper.setData(candidates);

        when(bullhornData.search(anyObject(), eq("externalID:\"2016Ext\""), anySet(), anyObject())).thenReturn(listWrapper);

        task = new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CANDIDATE, dataMap2, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotalMock);

        task.init();
        task.run();

        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
