package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LoadServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private RestSession restSessionMock;
    private InputStream inputStreamFake;
    private LoadService loadService;
    private PrintUtil printUtilMock;
    private ProcessRunner processRunnerMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;
    private ValidationUtil validationUtil;

    @Before
    public void setup() throws IOException, InterruptedException {
        actionTotalsMock = mock(ActionTotals.class);
        completeUtilMock = mock(CompleteUtil.class);
        restSessionMock = mock(RestSession.class);
        inputStreamFake = IOUtils.toInputStream("Yes!", "UTF-8");
        printUtilMock = mock(PrintUtil.class);
        processRunnerMock = mock(ProcessRunner.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        timerMock = mock(Timer.class);
        validationUtil = new ValidationUtil(printUtilMock);

        loadService = new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, restSessionMock, processRunnerMock, inputStreamFake, timerMock);

        doReturn(actionTotalsMock).when(processRunnerMock).run(any(), any(), any());
    }

    @Test
    public void testRunFile() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.LOAD, EntityInfo.CANDIDATE, filePath);
        verify(printUtilMock, times(2)).printAndLog(anyString());
        verify(completeUtilMock, times(1)).complete(Command.LOAD, filePath, EntityInfo.CANDIDATE, actionTotalsMock);
    }

    @Test
    public void testRunDirectoryOneFile() throws IOException, InterruptedException {
        final String directoryPath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        File file = new File(directoryPath, "ClientContact.csv");
        final String filePath = file.getPath();
        final String[] testArgs = {Command.LOAD.getMethodName(), directoryPath};

        loadService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.LOAD, EntityInfo.CLIENT_CONTACT, filePath);
        verify(printUtilMock, times(2)).printAndLog(anyString());
        verify(completeUtilMock, times(1)).complete(Command.LOAD, filePath, EntityInfo.CLIENT_CONTACT, actionTotalsMock);
    }

    @Test
    public void testRunDirectoryOneFileWithWait() throws IOException, InterruptedException {
        final String directoryPath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        File file = new File(directoryPath, "ClientContact.csv");
        final String filePath = file.getPath();
        final String[] testArgs = {Command.LOAD.getMethodName(), directoryPath};
        doReturn(2).when(propertyFileUtilMock).getWaitSecondsBetweenFilesInDirectory();

        loadService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.LOAD, EntityInfo.CLIENT_CONTACT, filePath);
        verify(printUtilMock, times(3)).printAndLog(anyString());
        verify(printUtilMock, times(1)).printAndLog("...Waiting 2 seconds for indexers to catch up...");
        verify(completeUtilMock, times(1)).complete(Command.LOAD, filePath, EntityInfo.CLIENT_CONTACT, actionTotalsMock);
    }

    @Test
    public void testRunDirectoryFourFilesSameEntity() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/opportunity");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        verify(processRunnerMock, times(4)).run(eq(Command.LOAD), eq(EntityInfo.OPPORTUNITY), any());
        verify(printUtilMock, times(13)).printAndLog(anyString());
        verify(printUtilMock, times(1)).printAndLog("   1. Opportunity records from Opportunity1.csv");
        verify(printUtilMock, times(1)).printAndLog("   2. Opportunity records from Opportunity2.csv");
        verify(printUtilMock, times(1)).printAndLog("   3. Opportunity records from OpportunityA.csv");
        verify(printUtilMock, times(1)).printAndLog("   4. Opportunity records from OpportunityB.csv");
    }

    @Test
    public void testRunDirectoryFourFiles() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        verify(processRunnerMock, times(4)).run(eq(Command.LOAD), any(), any());
        verify(printUtilMock, times(13)).printAndLog(anyString());
        verify(printUtilMock, times(1)).printAndLog("   1. ClientCorporation records from ClientCorporation_1.csv");
        verify(printUtilMock, times(1)).printAndLog("   2. ClientCorporation records from ClientCorporation_2.csv");
        verify(printUtilMock, times(1)).printAndLog("   3. Candidate records from Candidate_Valid_File.csv");
        verify(printUtilMock, times(1)).printAndLog("   4. CandidateWorkHistory records from CandidateWorkHistory.csv");
    }

    @Test
    public void testRunDirectoryFourFilesContinueNo() throws IOException, InterruptedException {
        inputStreamFake = IOUtils.toInputStream("No", "UTF-8");
        loadService = new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, restSessionMock, processRunnerMock, inputStreamFake, timerMock);

        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        verify(processRunnerMock, never()).run(any(), any(), any());
        verify(printUtilMock, times(5)).printAndLog(anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void testRunInvalidThrowsException() throws IOException, InterruptedException {
        final String[] testArgs = {Command.LOAD.getMethodName()};
        loadService.run(testArgs);
    }

    @Test
    public void testIsValidArgumentsFile() {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() {
        final String[] testArgs = {Command.LOAD.getMethodName()};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArguments() {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsInvalidFile() {
        final String filePath = "filePath";
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyFile() {
        final String filePath = "";
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsReadOnlyEntity() {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsDirectory() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsNoCsvFiles() {
        final String filePath = TestUtils.getResourceFilePath("testResume");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsNoLoadableCsvFiles() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/businessSector");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        final boolean actualResult = loadService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}
