package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteCustomObjectTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.response.list.CountryListWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConcurrencyServiceTest {

    private CsvFileWriter csvFileWriterMock;
    private ExecutorService executorServiceMock;
    private PropertyFileUtil propertyFileUtilMock;
    private BullhornRestApi bullhornRestApiMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;

    @SuppressWarnings("rawtypes")
    @Before
    public void setup() throws IOException {
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        executorServiceMock = Mockito.mock(ExecutorService.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
    }

    @Test
    public void runLoadProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(LoadTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/Candidate.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");

        Map<String, Method> methodMap = EntityInfo.CANDIDATE.getSetterMethodMap();
        CountryListWrapper listWrapper = new CountryListWrapper();
        List<Country> countryList = new ArrayList<>();
        Country usa = new Country();
        usa.setId(1);
        usa.setName("USA");
        countryList.add(usa);
        listWrapper.setData(countryList);
        when(bullhornRestApiMock.queryForAllRecords(any(), any(), any(), any())).thenReturn(listWrapper);
        Map<String, Integer> countryNameToIdMap = new HashMap<>();
        countryNameToIdMap.put("USA", 1);

        final LoadTask expectedTask = new LoadTask(Command.LOAD, 1, EntityInfo.CANDIDATE, expectedDataMap, methodMap, countryNameToIdMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadProcess();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadTask actualTask = (LoadTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runCustomObjectLoadProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(LoadCustomObjectTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/ClientCorporationCustomObjectInstance1.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD,
            EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("clientCorporation.id", "1");
        expectedDataMap.put("text1", "test");

        Map<String, Method> methodMap = EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1.getSetterMethodMap();

        final LoadCustomObjectTask expectedTask = new LoadCustomObjectTask(Command.LOAD, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, expectedDataMap, methodMap, null, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadProcess();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadCustomObjectTask actualTask = (LoadCustomObjectTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runDeleteProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(DeleteTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/Candidate.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");

        final DeleteTask expectedTask = new DeleteTask(Command.DELETE, 1, EntityInfo.CANDIDATE, expectedDataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteProcess();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final DeleteTask actualTask = (DeleteTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runDeleteProcessTest_CustomObject() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(DeleteTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/ClientCorporationCustomObjectInstance1.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE,
            EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("clientCorporation.id", "1");
        expectedDataMap.put("text1", "test");

        final DeleteCustomObjectTask expectedTask = new DeleteCustomObjectTask(Command.DELETE, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, expectedDataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteProcess();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final DeleteCustomObjectTask actualTask = (DeleteCustomObjectTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void EntityAttachmentConcurrencyServiceTestLoadAttachments() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(LoadAttachmentTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/CandidateAttachments.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD_ATTACHMENTS,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "1");

        Map<String, Method> methodMap = new HashMap<>();
        for (Method method : Arrays.asList(FileMeta.class.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        final LoadAttachmentTask expectedTask = new LoadAttachmentTask(Command.LOAD_ATTACHMENTS, 1, EntityInfo.CANDIDATE, expectedDataMap, methodMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadAttachmentsProcess();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final LoadAttachmentTask actualTask = (LoadAttachmentTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runConvertAttachmentsProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(ConvertAttachmentTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/CandidateAttachments.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.CONVERT_ATTACHMENTS,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "1");

        final ConvertAttachmentTask expectedTask = new ConvertAttachmentTask(Command.CONVERT_ATTACHMENTS, 1, EntityInfo.CANDIDATE, expectedDataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runConvertAttachmentsProcess();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());

        @SuppressWarnings("unchecked")
        final ConvertAttachmentTask actualTask = (ConvertAttachmentTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void runDeleteAttachmentsProcessTest() throws IOException, InterruptedException {
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(DeleteAttachmentTask.class);
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/CandidateAttachments_success.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.DELETE_ATTACHMENTS,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);
        final Map<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");
        expectedDataMap.put("action", "INSERT");
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");
        expectedDataMap.put("parentEntityID", "1");
        final DeleteAttachmentTask expectedTask = new DeleteAttachmentTask(Command.DELETE_ATTACHMENTS, 1, EntityInfo.CANDIDATE, expectedDataMap, csvFileWriterMock, propertyFileUtilMock, bullhornRestApiMock, printUtilMock, actionTotalsMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteAttachmentsProcess();

        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        final DeleteAttachmentTask actualTask = (DeleteAttachmentTask) taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void testGetActionTotals() throws IOException {
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/Candidate.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        ActionTotals actionTotals = service.getActionTotals();

        Assert.assertEquals(actionTotals, actionTotalsMock);
    }

    @Test
    public void testGetBullhornData() throws IOException {
        CsvFileReader csvFileReader = new CsvFileReader("src/test/resources/Candidate.csv");
        final ConcurrencyService service = new ConcurrencyService(
            Command.LOAD,
            EntityInfo.CANDIDATE,
            csvFileReader,
            csvFileWriterMock,
            executorServiceMock,
            propertyFileUtilMock,
            bullhornRestApiMock,
            printUtilMock,
            actionTotalsMock);

        BullhornRestApi bullhornRestApi = service.getBullhornRestApi();

        Assert.assertEquals(bullhornRestApi, bullhornRestApiMock);
    }
}
