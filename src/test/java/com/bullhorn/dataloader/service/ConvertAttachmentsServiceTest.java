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
import org.mockito.Mockito;

import java.io.InputStream;

public class ConvertAttachmentsServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteCall completeCallMock;
    private ProcessRunner processRunnerMock;
    private ConvertAttachmentsService convertAttachmentsService;
    private PrintUtil printUtilMock;
    private Timer timerMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        completeCallMock = Mockito.mock(CompleteCall.class);
        RestSession restSessionMock = Mockito.mock(RestSession.class);
        InputStream inputStreamMock = Mockito.mock(InputStream.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        processRunnerMock = Mockito.mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        timerMock = Mockito.mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        convertAttachmentsService = new ConvertAttachmentsService(printUtilMock, propertyFileUtilMock, validationUtil, completeCallMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);

        Mockito.doReturn(actionTotalsMock).when(processRunnerMock).runConvertAttachmentsProcess(Mockito.any(), Mockito.any());
    }

    @Test
    public void testRun() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath};

        convertAttachmentsService.run(testArgs);

        Mockito.verify(processRunnerMock, Mockito.times(1)).runConvertAttachmentsProcess(EntityInfo.CANDIDATE, filePath);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeCallMock, Mockito.times(1)).complete(Command.CONVERT_ATTACHMENTS, filePath, EntityInfo.CANDIDATE, actionTotalsMock, timerMock);
    }

    @Test(expected = IllegalStateException.class)
    public void testRun_missingArgument_ThrowsException() throws Exception {
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName()};
        convertAttachmentsService.run(testArgs);
    }

    @Test(expected = IllegalStateException.class)
    public void testIsValidArguments_badEntityFile() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath};
        convertAttachmentsService.run(testArgs);
    }

    @Test
    public void testIsValidArguments() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = convertAttachmentsService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_BadEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = convertAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_MissingArgument() throws Exception {
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName()};

        final boolean actualResult = convertAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_TooManyArgments() throws Exception {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = convertAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_InvalidFile() throws Exception {
        final String filePath = "filePath";
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = convertAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_EmptyFile() throws Exception {
        final String filePath = "";
        final String[] testArgs = {Command.CONVERT_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = convertAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }
}
