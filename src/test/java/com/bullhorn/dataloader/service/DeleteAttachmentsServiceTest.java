package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

public class DeleteAttachmentsServiceTest {

    private ActionTotals actionTotalsMock;
    private BullhornRestApi bullhornRestApiMock;
    private CompleteUtil completeUtilMock;
    private ConcurrencyService concurrencyServiceMock;
    private DeleteAttachmentsService deleteAttachmentsService;
    private PrintUtil printUtilMock;

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        PropertyFileUtil propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        ConnectionUtil connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        InputStream inputStreamMock = Mockito.mock(InputStream.class);
        Timer timerMock = Mockito.mock(Timer.class);

        deleteAttachmentsService = Mockito.spy(new DeleteAttachmentsService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock));

        concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
        Mockito.doReturn(concurrencyServiceMock).when(deleteAttachmentsService).createConcurrencyService(Mockito.any(), Mockito.any(), Mockito.anyString());
        Mockito.doReturn(actionTotalsMock).when(concurrencyServiceMock).getActionTotals();
        Mockito.doReturn(999L).when(timerMock).getDurationMillis();
        Mockito.doReturn(bullhornRestApiMock).when(concurrencyServiceMock).getBullhornRestApi();
        Mockito.doNothing().when(concurrencyServiceMock).runDeleteAttachmentsProcess();
    }

    @Test
    public void testRun() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

        deleteAttachmentsService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runDeleteAttachmentsProcess();
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.DELETE_ATTACHMENTS, filePath, EntityInfo.CANDIDATE, actionTotalsMock, 999L, bullhornRestApiMock);
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
