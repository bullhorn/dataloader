package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteCustomObjectTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessRunnerUtilTest {

    private BullhornRestApi bullhornRestApiMock;
    private ExecutorService executorServiceMock;
    private PreLoaderUtil preLoaderUtilMock;
    private PrintUtil printUtilMock;
    private ProcessRunnerUtil processRunnerUtil;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() throws InterruptedException {
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        ConnectionUtil connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        executorServiceMock = Mockito.mock(ExecutorService.class);
        preLoaderUtilMock = Mockito.mock(PreLoaderUtil.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        ThreadPoolUtil threadPoolUtilMock = Mockito.mock(ThreadPoolUtil.class);

        processRunnerUtil = new ProcessRunnerUtil(connectionUtilMock, preLoaderUtilMock, printUtilMock, propertyFileUtilMock, threadPoolUtilMock);

        Mockito.when(connectionUtilMock.getSession()).thenReturn(bullhornRestApiMock);
        Mockito.when(threadPoolUtilMock.getExecutorService()).thenReturn(executorServiceMock);
        Mockito.when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);
    }

    @Test
    public void runLoadProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runLoadProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.times(1)).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.LOAD), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadTask.class);
    }

    @Test
    public void runCustomObjectLoadProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("ClientCorporationCustomObjectInstance1.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runLoadProcess(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.LOAD), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadCustomObjectTask.class);
    }

    @Test
    public void runDeleteProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runDeleteProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.DELETE), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteTask.class);
    }

    @Test
    public void runDeleteProcessTest_CustomObject() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("ClientCorporationCustomObjectInstance1.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runDeleteProcess(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.DELETE), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteCustomObjectTask.class);
    }

    @Test
    public void runLoadAttachmentsProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("CandidateAttachments.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runLoadAttachmentsProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.LOAD_ATTACHMENTS), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadAttachmentTask.class);
    }

    @Test
    public void runConvertAttachmentsProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("CandidateAttachments.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runConvertAttachmentsProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.CONVERT_ATTACHMENTS), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), ConvertAttachmentTask.class);
    }

    @Test
    public void runDeleteAttachmentsProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("CandidateAttachments_success.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunnerUtil.runDeleteAttachmentsProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preLoaderUtilMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.DELETE_ATTACHMENTS), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteAttachmentTask.class);
    }
}
