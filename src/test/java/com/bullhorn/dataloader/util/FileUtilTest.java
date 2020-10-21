package com.bullhorn.dataloader.util;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.EntityInfo;

public class FileUtilTest {

    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() {
        propertyFileUtilMock = mock(PropertyFileUtil.class);
    }

    @Test
    public void testConstructor() {
        FileUtil fileUtil = new FileUtil();
        Assert.assertNotNull(fileUtil);
    }

    @Test
    public void testGetValidCsvFilesFromPath_file() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory/Candidate_Valid_File.csv");
        final File file = new File(filePath);
        final SortedMap<EntityInfo, List<String>> expectedMap = new TreeMap<>(EntityInfo.loadOrderComparator);
        expectedMap.put(EntityInfo.CANDIDATE, Collections.singletonList(file.getAbsolutePath()));

        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getValidCsvFiles(filePath, propertyFileUtilMock, EntityInfo.loadOrderComparator);

        Assert.assertEquals(actualMap.keySet(), expectedMap.keySet());
        Assert.assertEquals(actualMap.values().toArray()[0], expectedMap.values().toArray()[0]);
    }

    @Test
    public void testGetLoadableCsvFilesFromPath() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final SortedMap<EntityInfo, List<String>> expectedMap = new TreeMap<>(EntityInfo.loadOrderComparator);

        File candidateFile = new File(TestUtils.getResourceFilePath("loadFromDirectory/Candidate_Valid_File.csv"));
        File candidateWorkHistoryFile = new File(TestUtils.getResourceFilePath("loadFromDirectory/CandidateWorkHistory.csv"));
        File clientCorporationFile1 = new File(TestUtils.getResourceFilePath("loadFromDirectory/ClientCorporation_1.csv"));
        File clientCorporationFile2 = new File(TestUtils.getResourceFilePath("loadFromDirectory/ClientCorporation_2.csv"));

        expectedMap.put(EntityInfo.CLIENT_CORPORATION, Arrays.asList(clientCorporationFile1.getAbsolutePath(), clientCorporationFile2.getAbsolutePath()));
        expectedMap.put(EntityInfo.CANDIDATE, Collections.singletonList(candidateFile.getAbsolutePath()));
        expectedMap.put(EntityInfo.CANDIDATE_WORK_HISTORY, Collections.singletonList(candidateWorkHistoryFile.getAbsolutePath()));

        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath(filePath, propertyFileUtilMock);

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
    public void testGetLoadableCsvFilesFromPath_badFile() {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath("bad_file.csv", propertyFileUtilMock);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_badDirectory() {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath("bad_directory/", propertyFileUtilMock);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetLoadableCsvFilesFromPath_emptyDirectory() {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getLoadableCsvFilesFromPath(TestUtils.getResourceFilePath("testResume"), propertyFileUtilMock);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath() {
        final String filePath = TestUtils.getResourceFilePath("loadFromDirectory");
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath(filePath, propertyFileUtilMock);

        Set<Map.Entry<EntityInfo, List<String>>> sortedSet = actualMap.entrySet();
        Assert.assertEquals(2, sortedSet.size());
        Iterator<Map.Entry<EntityInfo, List<String>>> iter = sortedSet.iterator();
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(iter.next().getKey(), EntityInfo.CANDIDATE);
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_badFile() {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath("bad_file.csv", propertyFileUtilMock);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_badDirectory() {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath("bad_directory/", propertyFileUtilMock);
        Assert.assertTrue(actualMap.isEmpty());
    }

    @Test
    public void testGetDeletableCsvFilesFromPath_emptyDirectory() {
        final SortedMap<EntityInfo, List<String>> actualMap = FileUtil.getDeletableCsvFilesFromPath(TestUtils.getResourceFilePath("testResume"), propertyFileUtilMock);
        Assert.assertTrue(actualMap.isEmpty());
    }
}
