package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.api.BullhornData;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.Matchers.any;

public class LoadServiceTest {

    private ActionTotals actionTotalsMock;
    private BullhornData bullhornDataMock;
    private CompleteUtil completeUtilMock;
    private ConcurrencyService concurrencyServiceMock;
    private InputStream inputStreamFake;
    private LoadService loadService;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;
    private ValidationUtil validationUtil;

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        validationUtil = new ValidationUtil(printUtilMock);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);
        inputStreamFake = IOUtils.toInputStream("Yes!", "UTF-8");
        timerMock = Mockito.mock(Timer.class);

        loadService = Mockito.spy(new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamFake, timerMock));

        concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
        Mockito.doReturn(concurrencyServiceMock).when(loadService).createConcurrencyService(any(), any(), Mockito.anyString());
        Mockito.doReturn(actionTotalsMock).when(concurrencyServiceMock).getActionTotals();
        Mockito.doReturn(999L).when(timerMock).getDurationMillis();
        Mockito.doReturn(bullhornDataMock).when(concurrencyServiceMock).getBullhornData();
        Mockito.doNothing().when(concurrencyServiceMock).runLoadProcess();
        Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
    }

    @Test
    public void testRun_file() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("Candidate_Valid_File.csv");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runLoadProcess();
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        Mockito.verify(completeUtilMock, Mockito.times(1)).complete(Command.LOAD, filePath, EntityInfo.CANDIDATE, actionTotalsMock, 999L, bullhornDataMock);
    }

    @Test
    public void testRun_directoryOneFile() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/ClientContact");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runLoadProcess();
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testRun_directory_fourFilesSameEntity() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/opportunity");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.times(4)).runLoadProcess();
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

        Mockito.verify(concurrencyServiceMock, Mockito.times(4)).runLoadProcess();
        Mockito.verify(printUtilMock, Mockito.times(13)).printAndLog(Mockito.anyString());
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   1. ClientCorporation records from ClientCorporation_1.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   2. ClientCorporation records from ClientCorporation_2.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   3. Candidate records from Candidate_Valid_File.csv");
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   4. CandidateWorkHistory records from CandidateWorkHistory.csv");
    }

    @Test
    public void testRun_directory_fourFilesContinueNo() throws Exception {
        inputStreamFake = IOUtils.toInputStream("No", "UTF-8");
        loadService = Mockito.spy(new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamFake, timerMock));
        concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
        Mockito.doReturn(concurrencyServiceMock).when(loadService).createConcurrencyService(any(), any(), Mockito.anyString());
        Mockito.doNothing().when(concurrencyServiceMock).runLoadProcess();

        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

        loadService.run(testArgs);

        Mockito.verify(concurrencyServiceMock, Mockito.never()).runLoadProcess();
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
        Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError("BusinessSector", "read only");
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

    @Test
    public void testGetValidCsvFilesFromPath_file() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/Candidate_Valid_File.csv");
        final File file = new File(filePath);
        final SortedMap<EntityInfo, List<String>> expectedMap = new TreeMap<>(EntityInfo.loadOrderComparator);
        expectedMap.put(EntityInfo.CANDIDATE, Arrays.asList(file.getAbsolutePath()));

        final SortedMap<EntityInfo, List<String>> actualMap = loadService.getValidCsvFiles(filePath, EntityInfo.loadOrderComparator);

        Assert.assertEquals(actualMap.keySet(), expectedMap.keySet());
        Assert.assertEquals(actualMap.values().toArray()[0], expectedMap.values().toArray()[0]);
    }

    @Test
    public void testGetLoadableCsvFilesFromPath() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final SortedMap<EntityInfo, List<String>> expectedMap = new TreeMap<>(EntityInfo.loadOrderComparator);

        File candidateFile = new File(TestUtils.getResourceFilePath("loadFromDirectory/Candidate_Valid_File.csv"));
        File candidateWorkHistoryFile = new File(TestUtils.getResourceFilePath("loadFromDirectory/CandidateWorkHistory.csv"));
        File clientCorporationFile1 = new File(TestUtils.getResourceFilePath("loadFromDirectory/ClientCorporation_1.csv"));
        File clientCorporationFile2 = new File(TestUtils.getResourceFilePath("loadFromDirectory/ClientCorporation_2.csv"));

        expectedMap.put(EntityInfo.CLIENT_CORPORATION, Arrays.asList(clientCorporationFile1.getAbsolutePath(), clientCorporationFile2.getAbsolutePath()));
        expectedMap.put(EntityInfo.CANDIDATE, Arrays.asList(candidateFile.getAbsolutePath()));
        expectedMap.put(EntityInfo.CANDIDATE_WORK_HISTORY, Arrays.asList(candidateWorkHistoryFile.getAbsolutePath()));

        final SortedMap<EntityInfo, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath(filePath);

        Assert.assertEquals(actualMap.keySet(), expectedMap.keySet());
        Assert.assertEquals(actualMap.values().toArray()[0], expectedMap.values().toArray()[0]);
        Assert.assertEquals(actualMap.values().toArray()[1], expectedMap.values().toArray()[1]);
        Assert.assertEquals(actualMap.values().toArray()[2], expectedMap.values().toArray()[2]);

        Set<Map.Entry<EntityInfo, List<String>>> sortedSet = actualMap.entrySet();
        Assert.assertEquals(3, sortedSet.size());
        Iterator<Map.Entry<EntityInfo, List<String>>> iter = sortedSet.iterator();
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CLIENT_CORPORATION);
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE);
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE_WORK_HISTORY);
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_badFile() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath("bad_file.csv");
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_badDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath("bad_directory/");
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_emptyDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath(TestUtils.getResourceFilePath("testResume"));
        Assert.assertTrue(actualMap.isEmpty());
    }
}
