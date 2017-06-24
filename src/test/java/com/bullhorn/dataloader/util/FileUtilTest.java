package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.EntityInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;

public class FileUtilTest {

    private PrintUtil printUtilMock;
    private ValidationUtil validationUtil;

    @Before
    public void setup() throws Exception {
        printUtilMock = mock(PrintUtil.class);
        validationUtil = new ValidationUtil(printUtilMock);
    }

    @Test
    public void testConstructor() throws IOException {
        FileUtil fileUtil = new FileUtil();
        Assert.assertNotNull(fileUtil);
    }

    @Test
    public void testGetValidCsvFilesFromPath_file() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/Candidate_Valid_File.csv");
        final File file = new File(filePath);
        final SortedMap<EntityInfo, List<String>> expectedMap = new TreeMap<>(EntityInfo.loadOrderComparator);
        expectedMap.put(EntityInfo.CANDIDATE, Arrays.asList(file.getAbsolutePath()));

        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getValidCsvFiles(filePath, validationUtil, EntityInfo.loadOrderComparator);

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

        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath(filePath, validationUtil);

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
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath("bad_file.csv", validationUtil);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_badDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath("bad_directory/", validationUtil);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_emptyDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath(TestUtils.getResourceFilePath("testResume"), validationUtil);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath() throws Exception {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath(filePath, validationUtil);

        Set<Map.Entry<EntityInfo, List<String>>> sortedSet = actualMap.entrySet();
        Assert.assertEquals(2, sortedSet.size());
        Iterator<Map.Entry<EntityInfo, List<String>>> iter = sortedSet.iterator();
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE);
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_badFile() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath("bad_file.csv", validationUtil);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_badDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath("bad_directory/", validationUtil);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_emptyDirectory() throws Exception {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath(TestUtils.getResourceFilePath("testResume"), validationUtil);
        Assert.assertTrue(actualMap.isEmpty());
    }
}
