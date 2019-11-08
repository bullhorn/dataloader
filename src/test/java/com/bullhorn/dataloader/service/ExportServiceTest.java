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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExportServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private RestSession restSessionMock;
    private InputStream inputStreamFake;
    private ExportService exportService;
    private PrintUtil printUtilMock;
    private ProcessRunner processRunnerMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = mock(ActionTotals.class);
        completeUtilMock = mock(CompleteUtil.class);
        restSessionMock = mock(RestSession.class);
        inputStreamFake = IOUtils.toInputStream("Yes!", "UTF-8");
        printUtilMock = mock(PrintUtil.class);
        processRunnerMock = mock(ProcessRunner.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        timerMock = mock(Timer.class);

        exportService = new ExportService(printUtilMock, propertyFileUtilMock, completeUtilMock,
            restSessionMock, processRunnerMock, inputStreamFake, timerMock);

        when(processRunnerMock.run(any(), any(), any())).thenReturn(actionTotalsMock);
    }

    @Test
    public void testRunFile() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        exportService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.EXPORT, EntityInfo.CANDIDATE, filePath);
        verify(printUtilMock, times(2)).printAndLog(anyString());
        verify(completeUtilMock, times(1)).complete(Command.EXPORT, filePath, EntityInfo.CANDIDATE, actionTotalsMock);
    }

    @Test
    public void testRunDirectoryWithOneFile() throws Exception {
        final String directoryPath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        File file = new File(directoryPath, "ClientContact.csv");
        final String filePath = file.getPath();
        final String[] testArgs = {Command.EXPORT.getMethodName(), directoryPath};

        exportService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.EXPORT, EntityInfo.CLIENT_CONTACT, filePath);
        verify(printUtilMock, times(2)).printAndLog(anyString());
        verify(completeUtilMock, times(1)).complete(Command.EXPORT, filePath, EntityInfo.CLIENT_CONTACT, actionTotalsMock);
    }

    @Test
    public void testRunDirectoryWithFourFilesSameEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/opportunity");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        exportService.run(testArgs);

        verify(processRunnerMock, times(4)).run(eq(Command.EXPORT), eq(EntityInfo.OPPORTUNITY), any());
        verify(printUtilMock, times(13)).printAndLog(anyString());
        verify(printUtilMock, times(1)).printAndLog("   1. Opportunity records from Opportunity1.csv");
        verify(printUtilMock, times(1)).printAndLog("   2. Opportunity records from Opportunity2.csv");
        verify(printUtilMock, times(1)).printAndLog("   3. Opportunity records from OpportunityA.csv");
        verify(printUtilMock, times(1)).printAndLog("   4. Opportunity records from OpportunityB.csv");
    }

    @Test
    public void testRunDirectoryFourFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        exportService.run(testArgs);

        verify(processRunnerMock, times(4)).run(eq(Command.EXPORT), any(), any());
        verify(printUtilMock, times(13)).printAndLog(anyString());
        verify(printUtilMock, times(1)).printAndLog("   1. ClientCorporation records from ClientCorporation_1.csv");
        verify(printUtilMock, times(1)).printAndLog("   2. ClientCorporation records from ClientCorporation_2.csv");
        verify(printUtilMock, times(1)).printAndLog("   3. Candidate records from Candidate_Valid_File.csv");
        verify(printUtilMock, times(1)).printAndLog("   4. CandidateWorkHistory records from CandidateWorkHistory.csv");
    }

    @Test
    public void testRunDirectoryFourFilesContinueNo() throws Exception {
        inputStreamFake = IOUtils.toInputStream("No", "UTF-8");
        exportService = new ExportService(printUtilMock, propertyFileUtilMock, completeUtilMock, restSessionMock, processRunnerMock, inputStreamFake, timerMock);

        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        exportService.run(testArgs);

        verify(processRunnerMock, never()).run(any(), any(), any());
        verify(printUtilMock, times(5)).printAndLog(anyString());
    }

    @Test(expected = Exception.class)
    public void testRunMissingArgumentException() throws Exception {
        final String[] testArgs = {Command.EXPORT.getMethodName()};
        exportService.run(testArgs);
    }

    @Test
    public void testIsValidArgumentsFile() {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() {
        final String[] testArgs = {Command.EXPORT.getMethodName()};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArguments() {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsInvalidFile() {
        final String filePath = "filePath";
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyFile() {
        final String filePath = "";
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsReadOnlyEntity() {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsDirectory() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsNoCsvFiles() {
        final String filePath = TestUtils.getResourceFilePath("testResume");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBusinessSectors() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/businessSector");
        final String[] testArgs = {Command.EXPORT.getMethodName(), filePath};

        final boolean actualResult = exportService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
    }
}
