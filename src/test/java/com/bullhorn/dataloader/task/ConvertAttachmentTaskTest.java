package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.csv.CsvFileWriter;
import com.bullhorn.dataloader.csv.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConvertAttachmentTaskTest {

    private Map<String, String> dataMap;
    private Map<String, String> dataMap2;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private CsvFileWriter csvFileWriter;
    private BullhornRestApi bullhornRestApi;
    private PrintUtil printUtil;
    private ActionTotals actionTotalsMock;
    private ConvertAttachmentTask task;
    private PropertyFileUtil candidateExternalIdProperties;

    @Before
    public void setup() throws Exception {
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornRestApi = Mockito.mock(BullhornRestApi.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);
        candidateExternalIdProperties = Mockito.mock(PropertyFileUtil.class);

        dataMap = new LinkedHashMap<>();
        dataMap.put("id", "1");
        dataMap.put("relativeFilePath", TestUtils.getResourceFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume", "1");

        dataMap2 = new LinkedHashMap<>();
        dataMap2.put("candidate.externalID", "2016Ext");
        dataMap2.put("isResume", "1");

        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void convertAttachmentToHtmlTest() throws Exception {
        task = new ConvertAttachmentTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornRestApi, printUtil, actionTotalsMock);

        String result = task.convertAttachmentToHtml();

        Assert.assertNotNull(result);
    }

    @Test
    public void run_Success() throws IOException {
        Result expectedResult = Result.Convert();
        task = Mockito.spy(new ConvertAttachmentTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornRestApi, printUtil, actionTotalsMock));
        doNothing().when(task).writeHtmlToFile(anyString());

        task.run();
        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.CONVERT, 1);
    }

    @Test
    public void run_Success_Skip() throws IOException {
        dataMap = new LinkedHashMap<>();
        dataMap.put("id", "1");
        dataMap.put("relativeFilePath", TestUtils.getResourceFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume", "0");

        Result expectedResult = Result.Skip();
        task = Mockito.spy(new ConvertAttachmentTask(1, EntityInfo.CANDIDATE, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornRestApi, printUtil, actionTotalsMock));
        doNothing().when(task).writeHtmlToFile(anyString());

        task.run();
        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.SKIP, 1);
    }

    @Test
    public void getConvertedAttachmentPathTest() {
        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("clientContact.externalID", "1");
        dataMap.put("relativeFilePath", TestUtils.getResourceFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume", "0");
        String expectedResult = "convertedAttachments/ClientContact/1.html";

        task = new ConvertAttachmentTask(1, EntityInfo.CLIENT_CONTACT, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornRestApi, printUtil, actionTotalsMock);

        String actualResult = task.getConvertedAttachmentPath();

        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    @Test
    public void convertAttachmentNoRelativeFilePathTest() throws Exception {
        final Result expectedResult = Result.Failure(new IOException("Row 1: Missing the 'relativeFilePath' column required for convertAttachments"));
        when(bullhornRestApi.search(anyObject(), eq("externalID:\"2016Ext\""), anySet(), anyObject())).thenReturn(TestUtils.getListWrapper(Candidate.class, 1001));

        task = new ConvertAttachmentTask(1, EntityInfo.CANDIDATE, dataMap2, csvFileWriter, candidateExternalIdProperties, bullhornRestApi, printUtil, actionTotalsMock);
        task.run();

        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }
}
