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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class DeleteServiceTest {

    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private ValidationUtil validationUtil;
    private CompleteUtil completeUtilMock;
    private ConnectionUtil connectionUtilMock;
    private InputStream inputStreamFake;
    private Timer timerMock;
    private ConcurrencyService concurrencyServiceMock;
    private BullhornRestApi bullhornRestApiMock;
    private ActionTotals actionTotalsMock;
    private DeleteService deleteService;

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        validationUtil = new ValidationUtil(printUtilMock);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        inputStreamFake = IOUtils.toInputStream("yes", "UTF-8");
        timerMock = Mockito.mock(Timer.class);
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);

        deleteService = Mockito.spy(new DeleteService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamFake, timerMock));

        // TODO: Stop mocking the ConcurrencyService and replace with ConnectionService mocking
        concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
        Mockito.doReturn(concurrencyServiceMock).when(deleteService).createConcurrencyService(Mockito.any(), Mockito.any(), Mockito.anyString());
        Mockito.doReturn(actionTotalsMock).when(concurrencyServiceMock).getActionTotals();
        Mockito.doReturn(999L).when(timerMock).getDurationMillis();
        Mockito.doReturn(bullhornRestApiMock).when(concurrencyServiceMock).getBullhornRestApi();
        Mockito.doNothing().when(concurrencyServiceMock).runLoadProcess();
        Mockito.doNothing().when(concurrencyServiceMock).runDeleteProcess();
        Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
    }

    @Test
    public void testRun_file() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.DELETE, filePath, EntityInfo.CANDIDATE, actionTotalsMock, 999L, bullhornRestApiMock);
    }

    @Test
    public void testRun_directoryOneFile() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testRun_directoryFourFiles() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        deleteService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(2)).runDeleteProcess();
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
        Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError("BusinessSector", "not deletable");
    }

    @Test
    public void testIsValidArguments_NonDeletableEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("ClientCorporation.csv");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError("ClientCorporation", "not deletable");
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

    @Test
    public void testGetDeletableCsvFilesFromPath() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final SortedMap<EntityInfo, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath(filePath);

        Set<Map.Entry<EntityInfo, List<String>>> sortedSet = actualMap.entrySet();
        Assert.assertEquals(2, sortedSet.size());
        Iterator<Map.Entry<EntityInfo, List<String>>> iter = sortedSet.iterator();
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE);
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_badFile() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath("bad_file.csv");
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_badDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath("bad_directory/");
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_emptyDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath(TestUtils.getResourceFilePath("testResume"));
        Assert.assertTrue(actualMap.isEmpty());
    }
}
