package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteCall;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeleteAttachmentsServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteCall completeCallMock;
    private DeleteAttachmentsService deleteAttachmentsService;
    private PrintUtil printUtilMock;
    private ProcessRunner processRunnerMock;
    private Timer timerMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = mock(ActionTotals.class);
        completeCallMock = mock(CompleteCall.class);
        RestSession restSessionMock = mock(RestSession.class);
        InputStream inputStreamMock = mock(InputStream.class);
        printUtilMock = mock(PrintUtil.class);
        processRunnerMock = mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        timerMock = mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        deleteAttachmentsService = new DeleteAttachmentsService(printUtilMock, propertyFileUtilMock, validationUtil, completeCallMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);

        doReturn(actionTotalsMock).when(processRunnerMock).runDeleteAttachmentsProcess(any(), any());
    }

    @Test
    public void testRun() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        deleteAttachmentsService.run(testArgs);

        verify(processRunnerMock, times(1)).runDeleteAttachmentsProcess(EntityInfo.CANDIDATE, filePath);
        verify(printUtilMock, times(2)).printAndLog(anyString());
        verify(completeCallMock, times(1)).complete(Command.DELETE_ATTACHMENTS, filePath, EntityInfo.CANDIDATE, actionTotalsMock, timerMock);
    }

    @Test(expected = IllegalStateException.class)
    public void testRun_missingArgument_ThrowsException() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName()};
        deleteAttachmentsService.run(testArgs);
    }

    @Test(expected = IllegalStateException.class)
    public void testIsValidArguments_badEntityFile() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};
        deleteAttachmentsService.run(testArgs);
    }

    @Test
    public void testIsValidArguments() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArguments_BadEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArguments_MissingArgument() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName()};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArguments_TooManyArgments() throws Exception {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArguments_InvalidFile() throws Exception {
        final String filePath = "filePath";
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArguments_EmptyFile() throws Exception {
        final String filePath = "";
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArguments_NonAttachmentEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}
