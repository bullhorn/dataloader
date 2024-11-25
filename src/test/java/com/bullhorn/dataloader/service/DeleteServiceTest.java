package com.bullhorn.dataloader.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
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

public class DeleteServiceTest {

    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtilMock;
    private DeleteService deleteService;
    private PrintUtil printUtilMock;
    private ProcessRunner processRunnerMock;

    @Before
    public void setup() throws IOException, InterruptedException {
        actionTotalsMock = mock(ActionTotals.class);
        completeUtilMock = mock(CompleteUtil.class);
        RestSession restSessionMock = mock(RestSession.class);
        InputStream inputStreamFake = IOUtils.toInputStream("yes", StandardCharsets.UTF_8);
        printUtilMock = mock(PrintUtil.class);
        processRunnerMock = mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        Timer timerMock = mock(Timer.class);

        deleteService = new DeleteService(printUtilMock, propertyFileUtilMock, completeUtilMock, restSessionMock, processRunnerMock, inputStreamFake, timerMock);

        doReturn(actionTotalsMock).when(processRunnerMock).run(any(), any(), any());
    }

    @Test
    public void testRunFile() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.DELETE, EntityInfo.CANDIDATE, filePath);
        verify(printUtilMock, times(2)).printAndLog(anyString());
        verify(completeUtilMock, times(1)).complete(Command.DELETE, filePath, EntityInfo.CANDIDATE, actionTotalsMock);
    }

    @Test
    public void testRunDirectoryOneFile() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        File file = new File(filePath, "ClientContact.csv");
        final String expectedFileName = file.getPath();
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.DELETE, EntityInfo.CLIENT_CONTACT, expectedFileName);
        verify(printUtilMock, times(2)).printAndLog(anyString());
    }

    @Test
    public void testRunDirectoryFourFiles() throws IOException, InterruptedException {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        File validFile = new File(filePath, "Candidate_Valid_File.csv");
        File workHistoryFile = new File(filePath, "CandidateWorkHistory.csv");
        final String expectedCandidateFileName = validFile.getPath();
        final String expectedCandidateWorkHistoryFileName = workHistoryFile.getPath();
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        verify(processRunnerMock, times(1)).run(Command.DELETE, EntityInfo.CANDIDATE, expectedCandidateFileName);
        verify(processRunnerMock, times(1)).run(Command.DELETE, EntityInfo.CANDIDATE_WORK_HISTORY, expectedCandidateWorkHistoryFileName);
        verify(printUtilMock, times(7)).printAndLog(anyString());
    }

    @Test(expected = Exception.class)
    public void testRunMissingArgumentException() throws IOException, InterruptedException {
        final String[] testArgs = {Command.DELETE.getMethodName()};
        deleteService.run(testArgs);
    }

    @Test
    public void testIsValidArguments() {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() {
        final String filePath = TestUtils.getResourceFilePath("Invalid_Candidate_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() {
        final String[] testArgs = {Command.DELETE.getMethodName()};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArguments() {
        final String filePath = "Candidate.csv";
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath, "tooMany"};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test(expected = DataLoaderException.class)
    public void testIsValidArgumentsInvalidFile() {
        deleteService.isValidArguments(new String[]{Command.DELETE.getMethodName(), "filePath"});
    }

    @Test(expected = DataLoaderException.class)
    public void testIsValidArgumentsEmptyFile() {
        deleteService.isValidArguments(new String[]{Command.DELETE.getMethodName(), ""});
    }

    @Test
    public void testIsValidArgumentsReadOnlyEntity() {
        final String filePath = TestUtils.getResourceFilePath("BusinessSector.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsNonDeletableEntity() {
        final String filePath = TestUtils.getResourceFilePath("ClientCorporation.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsDirectory() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsNoCsvFiles() {
        final String filePath = TestUtils.getResourceFilePath("testResume");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsNoDeletableCsvFiles() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/businessSector");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}
