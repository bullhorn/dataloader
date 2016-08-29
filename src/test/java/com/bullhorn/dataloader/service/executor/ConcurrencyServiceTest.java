package com.bullhorn.dataloader.service.executor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.file.FileMeta;
import com.csvreader.CsvReader;

public class ConcurrencyServiceTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private ExecutorService executorService;
    @SuppressWarnings("rawtypes")
    private ArgumentCaptor<LoadAttachmentTask> taskCaptor;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    @Before
    public void setUp() throws IOException {
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        executorService = Mockito.mock(ExecutorService.class);
        printUtil = Mockito.mock(PrintUtil.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        csvReader = new CsvReader("src/test/resources/CandidateAttachments.csv");
        csvReader.readHeaders();
        taskCaptor = ArgumentCaptor.forClass(LoadAttachmentTask.class);
    }

    @Test
    public void getCSVMapTestWithInvalidHeaderCount() throws IOException, InterruptedException {
        boolean errorThrown = false;
        csvReader = new CsvReader("src/test/resources/ClientCorporation_Invalid.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE_ATTACHMENTS,
            "ClientCorporation",
            csvReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornData,
            printUtil,
            actionTotals);
        try {
            service.getCsvDataMap();
        }
        catch (IOException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);

    }

    @Test
    public void EntityAttachmentConcurrencyServiceTestLoadAttachments() throws IOException, InterruptedException {
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD_ATTACHMENTS,
            "Candidate",
            csvReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornData,
            printUtil,
            actionTotals);

        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");

        Map<String, Method> methodMap = new HashMap();
        for (Method method : Arrays.asList(FileMeta.class.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        final LoadAttachmentTask<Candidate> expectedTask = new LoadAttachmentTask<Candidate>(Command.LOAD_ATTACHMENTS, 1, Candidate.class, expectedDataMap, methodMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadAttachmentsProcess();
        verify(executorService).execute(taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadAttachmentTask<Candidate> actualTask = (LoadAttachmentTask<Candidate>) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void EntityAttachmentConcurrencyServiceTestDeleteAttachments() throws IOException, InterruptedException {
        final ArgumentCaptor<DeleteAttachmentTask> deleteAttachmentTaskArgumentCaptor = ArgumentCaptor.forClass(DeleteAttachmentTask.class);
        csvReader = new CsvReader("src/test/resources/CandidateAttachments_success.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE_ATTACHMENTS,
            "Candidate",
            csvReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornData,
            printUtil,
            actionTotals);
        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");
        expectedDataMap.put("action", "INSERT");
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");
        expectedDataMap.put("parentEntityID", "1");
        final DeleteAttachmentTask expectedTask = new DeleteAttachmentTask(Command.DELETE_ATTACHMENTS, 1, Candidate.class, expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteAttachmentsProcess();

        verify(executorService).execute(deleteAttachmentTaskArgumentCaptor.capture());
        final DeleteAttachmentTask actualTask = deleteAttachmentTaskArgumentCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }
}
