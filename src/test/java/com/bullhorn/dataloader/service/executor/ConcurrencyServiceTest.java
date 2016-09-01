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

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.customobject.ClientCorporationCustomObjectInstance1;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.response.list.CountryListWrapper;
import com.csvreader.CsvReader;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;

public class ConcurrencyServiceTest <T extends AbstractTask>  {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private ExecutorService executorService;
    @SuppressWarnings("rawtypes")
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
    }

    @Test
    public void runLoadProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(LoadTask.class);

        csvReader = new CsvReader("src/test/resources/Candidate.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD,
            EntityInfo.CANDIDATE,
            csvReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornData,
            printUtil,
            actionTotals);

        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");

        Map<String, Method> methodMap = service.createMethodMap(Candidate.class);
        CountryListWrapper listWrapper = new CountryListWrapper();
        List<Country> countryList = new ArrayList<>();
        Country usa = new Country();
        usa.setId(1);
        usa.setName("USA");
        countryList.add(usa);
        listWrapper.setData(countryList);
        when(bullhornData.queryForAllRecords(any(), any(), any(), any())).thenReturn(listWrapper);
        Map<String, Integer> countryNameToIdMap = new HashMap<>();
        countryNameToIdMap.put("USA", 1);

        final LoadTask expectedTask = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, expectedDataMap, methodMap, countryNameToIdMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadProcess();
        verify(executorService).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadTask actualTask = (LoadTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runCustomObjectLoadProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(LoadCustomObjectTask.class);

        csvReader = new CsvReader("src/test/resources/ClientCorporationCustomObjectInstance1.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD,
            EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1,
            csvReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornData,
            printUtil,
            actionTotals);

        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("clientCorporation.id", "1");
        expectedDataMap.put("text1", "test");

        Map<String, Method> methodMap = service.createMethodMap(ClientCorporationCustomObjectInstance1.class);

        final LoadCustomObjectTask expectedTask = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, expectedDataMap, methodMap, null, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadProcess();
        verify(executorService).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadCustomObjectTask actualTask = (LoadCustomObjectTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runDeleteProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(DeleteTask.class);

        csvReader = new CsvReader("src/test/resources/Candidate.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE,
            EntityInfo.CANDIDATE,
            csvReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornData,
            printUtil,
            actionTotals);

        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");

        final DeleteTask expectedTask = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteProcess();
        verify(executorService).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final DeleteTask actualTask = (DeleteTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void getCSVMapTestWithInvalidHeaderCount() throws IOException, InterruptedException {
        boolean errorThrown = false;
        csvReader = new CsvReader("src/test/resources/ClientCorporation_Invalid.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE_ATTACHMENTS,
            EntityInfo.CLIENT_CORPORATION,
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
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(LoadAttachmentTask.class);

        csvReader = new CsvReader("src/test/resources/CandidateAttachments.csv");
        csvReader.readHeaders();

        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD_ATTACHMENTS,
            EntityInfo.CANDIDATE,
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
        expectedDataMap.put("isResume", "1");

        Map<String, Method> methodMap = new HashMap();
        for (Method method : Arrays.asList(FileMeta.class.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        final LoadAttachmentTask expectedTask = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, expectedDataMap, methodMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadAttachmentsProcess();
        verify(executorService).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadAttachmentTask actualTask = (LoadAttachmentTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runConvertAttachmentsProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(ConvertAttachmentTask.class);

        csvReader = new CsvReader("src/test/resources/CandidateAttachments.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.CONVERT_ATTACHMENTS,
            EntityInfo.CANDIDATE,
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
        expectedDataMap.put("isResume", "1");

        final ConvertAttachmentTask expectedTask = new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CANDIDATE, expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runConvertAttachmentsProcess();
        verify(executorService).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final ConvertAttachmentTask actualTask = (ConvertAttachmentTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runDeleteAttachmentsProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(DeleteAttachmentTask.class);

        csvReader = new CsvReader("src/test/resources/CandidateAttachments_success.csv");
        csvReader.readHeaders();
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE_ATTACHMENTS,
            EntityInfo.CANDIDATE,
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
        final DeleteAttachmentTask expectedTask = new DeleteAttachmentTask(Command.DELETE_ATTACHMENTS, 1, EntityInfo.CANDIDATE, expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteAttachmentsProcess();

        verify(executorService).execute((Runnable) taskCaptor.capture());
        final DeleteAttachmentTask actualTask = (DeleteAttachmentTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }
}
