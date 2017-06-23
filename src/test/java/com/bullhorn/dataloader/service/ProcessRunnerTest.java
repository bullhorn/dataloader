package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteCustomObjectTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.ThreadPoolUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessRunnerTest {

    private RestApi restApiMock;
    private ExecutorService executorServiceMock;
    private Preloader preloaderMock;
    private PrintUtil printUtilMock;
    private ProcessRunner processRunner;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() throws InterruptedException {
        restApiMock = Mockito.mock(RestApi.class);
        RestSession restSessionMock = Mockito.mock(RestSession.class);
        executorServiceMock = Mockito.mock(ExecutorService.class);
        preloaderMock = Mockito.mock(Preloader.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        ThreadPoolUtil threadPoolUtilMock = Mockito.mock(ThreadPoolUtil.class);

        processRunner = new ProcessRunner(restSessionMock, preloaderMock, printUtilMock, propertyFileUtilMock, threadPoolUtilMock);

        Mockito.when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        Mockito.when(threadPoolUtilMock.getExecutorService()).thenReturn(executorServiceMock);
        Mockito.when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);
    }

    @Test
    public void runLoadProcessTest() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.runLoadProcess(EntityInfo.CANDIDATE, filePath);

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

        ActionTotals actualTotals = processRunner.runLoadProcess(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, filePath);

        Mockito.verify(preloaderMock, Mockito.never()).getCountryNameToIdMap();
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

        ActionTotals actualTotals = processRunner.runDeleteProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preloaderMock, Mockito.never()).getCountryNameToIdMap();
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

        ActionTotals actualTotals = processRunner.runDeleteProcess(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, filePath);

        Mockito.verify(preloaderMock, Mockito.never()).getCountryNameToIdMap();
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

        ActionTotals actualTotals = processRunner.runLoadAttachmentsProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preloaderMock, Mockito.never()).getCountryNameToIdMap();
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

        ActionTotals actualTotals = processRunner.runConvertAttachmentsProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preloaderMock, Mockito.never()).getCountryNameToIdMap();
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

        ActionTotals actualTotals = processRunner.runDeleteAttachmentsProcess(EntityInfo.CANDIDATE, filePath);

        Mockito.verify(preloaderMock, Mockito.never()).getCountryNameToIdMap();
        Mockito.verify(executorServiceMock, Mockito.times(1)).execute(Mockito.any());
        Mockito.verify(executorServiceMock, Mockito.times(1)).shutdown();
        Mockito.verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        Mockito.verify(printUtilMock, Mockito.times(1)).printActionTotals(Mockito.eq(Command.DELETE_ATTACHMENTS), Mockito.eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteAttachmentTask.class);
    }
}
