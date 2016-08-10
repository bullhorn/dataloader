package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

public class ConvertAttachmentTaskTest {

    private LinkedHashMap<String, String> dataMap;
    private ExecutorService executorService;
    private Map<String, Method> methodMap;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private Map<String, Integer> countryNameToIdMap;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;
    private ConvertAttachmentTask task;
    private PropertyFileUtil candidateIdProperties;
    private PropertyFileUtil candidateExternalIdProperties;

    @Before
    public void setUp() throws Exception {
        executorService = Mockito.mock(ExecutorService.class);
        csvReader = Mockito.mock(CsvReader.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);

        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("id","1");
        dataMap.put("relativeFilePath",getFilePath("testResume/TestResume.doc"));
        dataMap.put("isResume","0");

        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void convertAttachmentToHtmlTest() throws Exception {
        task = Mockito.spy(new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, Candidate.class, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals));

        String result = task.convertAttachmentToHtml();

        Assert.assertNotNull(result);
    }

    @Test
    public void run_Success() throws IOException {
        Result expectedResult = Result.Convert();
        task = Mockito.spy(new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, Candidate.class, dataMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals));
        doNothing().when(task).writeHtmlToFile(anyString());


        task.run();
        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());

        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }

}
