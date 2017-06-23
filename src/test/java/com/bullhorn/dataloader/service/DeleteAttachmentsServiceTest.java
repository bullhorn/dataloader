package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.ProcessRunnerUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

public class DeleteAttachmentsServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private DeleteAttachmentsService deleteAttachmentsService;
    private PrintUtil printUtilMock;
    private ProcessRunnerUtil processRunnerUtilMock;
    private Timer timerMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        ConnectionUtil connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        InputStream inputStreamMock = Mockito.mock(InputStream.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        processRunnerUtilMock = Mockito.mock(ProcessRunnerUtil.class);
        PropertyFileUtil propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        timerMock = Mockito.mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        deleteAttachmentsService = new DeleteAttachmentsService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, processRunnerUtilMock, inputStreamMock, timerMock);

        Mockito.doReturn(actionTotalsMock).when(processRunnerUtilMock).runDeleteAttachmentsProcess(Mockito.any(), Mockito.any());
    }

    @Test
    public void testRun() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        deleteAttachmentsService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runDeleteAttachmentsProcess(EntityInfo.CANDIDATE, filePath);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.DELETE_ATTACHMENTS, filePath, EntityInfo.CANDIDATE, actionTotalsMock, timerMock);
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
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_BadEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_MissingArgument() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName()};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_TooManyArgments() throws Exception {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_InvalidFile() throws Exception {
        final String filePath = "filePath";
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_EmptyFile() throws Exception {
        final String filePath = "";
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_NonAttachmentEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }
}
