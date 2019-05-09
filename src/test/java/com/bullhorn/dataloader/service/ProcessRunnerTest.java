package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.ExportTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.ThreadPoolUtil;
import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessRunnerTest {

    private ExecutorService executorServiceMock;
    private PropertyFileUtil propertyFileUtilMock;
    private PrintUtil printUtilMock;
    private Cache cacheMock;
    private ProcessRunner processRunner;
    private String idExistFieldWarning = "WARNING: The 'id' column is not being used for "
        + "duplicate checking. The id value will be ignored.";

    @Before
    public void setup() throws InterruptedException {
        RestApi restApiMock = mock(RestApi.class);
        RestSession restSessionMock = mock(RestSession.class);
        executorServiceMock = mock(ExecutorService.class);
        Preloader preloaderMock = mock(Preloader.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        printUtilMock = mock(PrintUtil.class);
        cacheMock = mock(Cache.class);
        CompleteUtil completeUtilMock = mock(CompleteUtil.class);
        ThreadPoolUtil threadPoolUtilMock = mock(ThreadPoolUtil.class);

        processRunner = new ProcessRunner(restSessionMock, preloaderMock, printUtilMock, propertyFileUtilMock,
            threadPoolUtilMock, cacheMock, completeUtilMock);

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(threadPoolUtilMock.getExecutorService()).thenReturn(executorServiceMock);
        when(executorServiceMock.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);
        when(preloaderMock.convertRow(any())).then(returnsFirstArg());
        when(propertyFileUtilMock.getDateParser()).thenReturn(DateTimeFormat.forPattern("yyyy-mm-dd"));
        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList());
    }

    @Test
    public void testRunConvertAttachments() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("CandidateAttachments.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.CONVERT_ATTACHMENTS, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.CONVERT_ATTACHMENTS), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), ConvertAttachmentTask.class);
    }

    @Test
    public void testRunDelete() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.DELETE, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.DELETE), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteTask.class);
    }

    @Test
    public void testRunDeleteCustomObject() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("ClientCorporationCustomObjectInstance1.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.DELETE, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.DELETE), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteTask.class);
    }

    @Test
    public void testRunDeleteAttachments() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("CandidateAttachments_success.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.DELETE_ATTACHMENTS, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.DELETE_ATTACHMENTS), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), DeleteAttachmentTask.class);
    }

    @Test
    public void testRunExport() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.EXPORT, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.EXPORT), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), ExportTask.class);
    }

    @Test
    public void testRunLoad() throws IOException, InterruptedException {
        when(propertyFileUtilMock.getEntityExistFields(any())).thenReturn(Lists.newArrayList(StringConsts.ID));
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.LOAD, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.LOAD), eq(actualTotals));
        verify(printUtilMock, never()).printAndLog(eq(idExistFieldWarning));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadTask.class);
    }

    @Test
    public void testRunLoadIdColumnWarning() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.LOAD, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.LOAD), eq(actualTotals));
        verify(printUtilMock, times(1)).printAndLog(eq(idExistFieldWarning));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadTask.class);
    }

    @Test
    public void testRunLoadCustomObject() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("ClientCorporationCustomObjectInstance1.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.LOAD, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.LOAD), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadTask.class);
    }

    @Test
    public void testRunLoadAttachments() throws IOException, InterruptedException {
        String filePath = TestUtils.getResourceFilePath("CandidateAttachments.csv");
        ArgumentCaptor taskCaptor = ArgumentCaptor.forClass(AbstractTask.class);

        ActionTotals actualTotals = processRunner.run(Command.LOAD_ATTACHMENTS, EntityInfo.CANDIDATE, filePath);

        verify(executorServiceMock, times(1)).execute(any());
        verify(executorServiceMock, times(1)).shutdown();
        verify(executorServiceMock).execute((Runnable) taskCaptor.capture());
        verify(printUtilMock, times(1)).printActionTotals(eq(Command.LOAD_ATTACHMENTS), eq(actualTotals));
        AbstractTask actualTask = (AbstractTask) taskCaptor.getValue();
        Assert.assertEquals(actualTask.getClass(), LoadAttachmentTask.class);
    }
}
