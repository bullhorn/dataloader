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

public class LoadServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private ConnectionUtil connectionUtilMock;
    private InputStream inputStreamFake;
    private LoadService loadService;
    private PrintUtil printUtilMock;
    private ProcessRunnerUtil processRunnerUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;
    private ValidationUtil validationUtil;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        inputStreamFake = IOUtils.toInputStream("Yes!", "UTF-8");
        printUtilMock = Mockito.mock(PrintUtil.class);
        processRunnerUtilMock = Mockito.mock(ProcessRunnerUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        timerMock = Mockito.mock(Timer.class);
        validationUtil = new ValidationUtil(printUtilMock);

        loadService = new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, processRunnerUtilMock, inputStreamFake, timerMock);

        Mockito.doReturn(actionTotalsMock).when(processRunnerUtilMock).runLoadProcess(Mockito.any(), Mockito.any());
    }

    @Test
    public void testRun_file() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runLoadProcess(EntityInfo.CANDIDATE, filePath);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.LOAD, filePath, EntityInfo.CANDIDATE, actionTotalsMock, timerMock);
    }

    @Test
    public void testRun_directoryOneFile() throws Exception {
        final String directoryPath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        final String filePath = directoryPath + "/ClientContact.csv";
        final String[] testArgs = {Command.LOAD.getMethodName(), directoryPath};

        loadService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runLoadProcess(EntityInfo.CLIENT_CONTACT, directoryPath);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.LOAD, filePath, EntityInfo.CLIENT_CONTACT, actionTotalsMock, timerMock);
    }

    @Test
    public void testRun_directory_oneFile_withWait() throws Exception {
        final String directoryPath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        final String filePath = directoryPath + "/ClientContact.csv";
        final String[] testArgs = {Command.LOAD.getMethodName(), directoryPath};
        Mockito.doReturn(1).when(propertyFileUtilMock).getWaitTimeMsecBetweenFilesInDirectory();

        loadService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(1)).runLoadProcess(EntityInfo.CLIENT_CONTACT, directoryPath);
        Mockito.verify(printUtilMock, Mockito.times(3)).printAndLog(Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("...Waiting 0 seconds for indexers to catch up...");
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.LOAD, filePath, EntityInfo.CLIENT_CONTACT, actionTotalsMock, timerMock);
    }

    @Test
    public void testRun_directory_fourFilesSameEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/opportunity");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(4)).runLoadProcess(EntityInfo.OPPORTUNITY, filePath);
        Mockito.verify(printUtilMock, Mockito.times(13)).printAndLog(Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   1. Opportunity records from Opportunity1.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   2. Opportunity records from Opportunity2.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   3. Opportunity records from OpportunityA.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   4. Opportunity records from OpportunityB.csv");
    }

    @Test
    public void testRun_directory_fourFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.times(4)).runLoadProcess(Mockito.any(), Mockito.any());
        Mockito.verify(printUtilMock, Mockito.times(13)).printAndLog(Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   1. ClientCorporation records from ClientCorporation_1.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   2. ClientCorporation records from ClientCorporation_2.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   3. Candidate records from Candidate_Valid_File.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   4. CandidateWorkHistory records from CandidateWorkHistory.csv");
    }

    @Test
    public void testRun_directory_fourFilesContinueNo() throws Exception {
        inputStreamFake = IOUtils.toInputStream("No", "UTF-8");
        loadService = new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, processRunnerUtilMock, inputStreamFake, timerMock);

        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(processRunnerUtilMock, Mockito.never()).runLoadProcess(Mockito.any(), Mockito.any());
        Mockito.verify(printUtilMock, Mockito.times(5)).printAndLog(Mockito.anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void testRun_invalidThrowsException() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName()};
        loadService.run(testArgs);
    }

    @Test
    public void testIsValidArguments_File() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_BadEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_MissingArgument() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName()};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_TooManyArgments() throws Exception {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_InvalidFile() throws Exception {
        final String filePath = "filePath";
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_EmptyFile() throws Exception {
        final String filePath = "";
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_ReadOnlyEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_Directory() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_noCsvFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("testResume");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_noLoadableCsvFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/businessSector");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }
}
