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
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

public class DeleteServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private DeleteService deleteService;
    private PrintUtil printUtilMock;
    private ProcessRunnerUtil processRunnerUtilMock;
    private Timer timerMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        ConnectionUtil connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        InputStream inputStreamFake = IOUtils.toInputStream("yes", "UTF-8");
        printUtilMock = Mockito.mock(PrintUtil.class);
        processRunnerUtilMock = Mockito.mock(ProcessRunnerUtil.class);
        PropertyFileUtil propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        timerMock = Mockito.mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        deleteService = new DeleteService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, processRunnerUtilMock, inputStreamFake, timerMock);

        Mockito.doReturn(actionTotalsMock).when(processRunnerUtilMock).runDeleteProcess(Mockito.any(), Mockito.any());
    }

    @Test
    public void testRun_file() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runDeleteProcess(EntityInfo.CANDIDATE, filePath);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.DELETE, filePath, EntityInfo.CANDIDATE, actionTotalsMock, timerMock);
    }

    @Test
    public void testRun_directoryOneFile() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        final String expectedFileName = filePath + "/ClientContact.csv";
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runDeleteProcess(EntityInfo.CLIENT_CONTACT, expectedFileName);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testRun_directoryFourFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String expectedCandidateFileName = filePath + "/Candidate_Valid_File.csv";
        final String expectedCandidateWorkHistoryFileName = filePath + "/CandidateWorkHistory.csv";
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runDeleteProcess(EntityInfo.CANDIDATE, expectedCandidateFileName);
        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runDeleteProcess(EntityInfo.CANDIDATE_WORK_HISTORY, expectedCandidateWorkHistoryFileName);
        Mockito.verify(printUtilMock, Mockito.times(7)).printAndLog(Mockito.anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void testRun_invalidThrowsException() throws Exception {
        final String[] testArgs = {Command.DELETE.getMethodName()};
        deleteService.run(testArgs);
    }

    @Test
    public void testIsValidArguments() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_BadEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_MissingArgument() throws Exception {
        final String[] testArgs = {Command.DELETE.getMethodName()};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_TooManyArgments() throws Exception {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_InvalidFile() throws Exception {
        final String filePath = "filePath";
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_EmptyFile() throws Exception {
        final String filePath = "";
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_ReadOnlyEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_NonDeletableEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("ClientCorporation.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_Directory() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_noCsvFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("testResume");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_noDeletableCsvFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/businessSector");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }
}
