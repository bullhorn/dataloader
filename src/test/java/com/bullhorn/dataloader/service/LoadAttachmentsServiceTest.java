package com.bullhorn.dataloader.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;

public class LoadAttachmentsServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private LoadAttachmentsService loadAttachmentsService;
    private PrintUtil printUtilMock;
    private ProcessRunner processRunnerMock;

    @Before
    public void setup() throws IOException, InterruptedException {
        actionTotalsMock = mock(ActionTotals.class);
        completeUtilMock = mock(CompleteUtil.class);
        RestSession restSessionMock = mock(RestSession.class);
        InputStream inputStreamMock = mock(InputStream.class);
        printUtilMock = mock(PrintUtil.class);
        processRunnerMock = mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        Timer timerMock = mock(Timer.class);

        loadAttachmentsService = new LoadAttachmentsService(printUtilMock, propertyFileUtilMock, completeUtilMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);

        doReturn(actionTotalsMock).when(processRunnerMock).run(any(), any(), any());
    }

    @Test
    public void testRun() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath};

        loadAttachmentsService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.LOAD_ATTACHMENTS, EntityInfo.CANDIDATE, filePath);
        verify(completeUtilMock, times(1)).complete(Command.LOAD_ATTACHMENTS, filePath, EntityInfo.CANDIDATE, actionTotalsMock);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test(expected = Exception.class)
    public void testRunMissingArgumentException() throws IOException, InterruptedException {
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName()};
        loadAttachmentsService.run(testArgs);
    }

    @Test(expected = Exception.class)
    public void testRunInvalidEntityException() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath};
        loadAttachmentsService.run(testArgs);
    }

    @Test
    public void testIsValidArgumentsSuccess() {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() {
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName()};

        final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArguments() {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test(expected = DataLoaderException.class)
    public void testIsValidArgumentsInvalidFile() {
        loadAttachmentsService.isValidArguments(new String[]{Command.LOAD_ATTACHMENTS.getMethodName(), "filePath"});
    }

    @Test(expected = DataLoaderException.class)
    public void testIsValidArgumentsEmptyFile() {
        loadAttachmentsService.isValidArguments(new String[]{Command.LOAD_ATTACHMENTS.getMethodName(), ""});
    }

    @Test
    public void testIsValidArgumentsInvalidParentEntity() {
        final String filePath = TestUtils.getResourceFilePath("AppointmentAttachments.csv");
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}
