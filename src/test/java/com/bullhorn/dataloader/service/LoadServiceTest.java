package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.meta.Entity;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

public class LoadServiceTest {

	private PrintUtil printUtilMock;
	private PropertyFileUtil propertyFileUtilMock;
	private ValidationUtil validationUtil;
	private InputStream inputStreamFake;
	private LoadService loadService;
	private ConcurrencyService concurrencyServiceMock;

	@Before
	public void setup() throws Exception {
		printUtilMock = Mockito.mock(PrintUtil.class);
		propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
		validationUtil = new ValidationUtil(printUtilMock);
		inputStreamFake = IOUtils.toInputStream("text to simulate user entry", "UTF-8");
		loadService = Mockito.spy(new LoadService(printUtilMock, propertyFileUtilMock, validationUtil, inputStreamFake));

		// mock out AbstractService Methods that call class outside of this test scope
		concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
		Mockito.doReturn(concurrencyServiceMock).when(loadService).createConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(concurrencyServiceMock).runLoadProcess();
	}
	
	@Test
	public void testRun_file() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		loadService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runLoadProcess();
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun_directoryOneFile() throws Exception {
		final String filePath = getFilePath("loadFromDirectory/ClientContact");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		loadService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runLoadProcess();
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun_directory_twoFilesSameEntity() throws Exception {
		final String filePath = getFilePath("loadFromDirectory/opportunity");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		loadService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(2)).runLoadProcess();
		Mockito.verify(printUtilMock, Mockito.times(7)).printAndLog(Mockito.anyString());
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   1. Opportunity records from Opportunity1.csv");
	}

	@Test
	public void testRun_directory_fourFiles() throws Exception {
		final String filePath = getFilePath("loadFromDirectory");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		loadService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(4)).runLoadProcess();
		Mockito.verify(printUtilMock, Mockito.times(13)).printAndLog(Mockito.anyString());
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("   1. ClientCorporation records from ClientCorporation_1.csv");
	}

	@Test(expected=IllegalStateException.class)
	public void testRun_invalidThrowsException() throws Exception {
		final String[] testArgs = {Command.LOAD.getMethodName()};
		loadService.run(testArgs);
	}

	@Test
	public void testIsValidArguments_File() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String filePath = getFilePath("Invalid_Candidate_File.csv");
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
		final String filePath = getFilePath("Certification.csv");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError("Certification", "read only");
	}

	@Test
	public void testIsValidArguments_Directory() throws Exception {
		final String filePath = getFilePath("loadFromDirectory");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_noCsvFiles() throws Exception {
		final String filePath = getFilePath("testResume");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_noLoadableCsvFiles() throws Exception {
		final String filePath = getFilePath("loadFromDirectory/businessSector");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testGetValidCsvFilesFromPath_file() throws Exception {
		final String filePath = getFilePath("loadFromDirectory/Candidate_Valid_File.csv");
		final File file = new File(filePath);
		final SortedMap<Entity, List<String>> expectedMap = new TreeMap<>(Entity.loadOrderComparator);
		expectedMap.put(Entity.CANDIDATE, Arrays.asList(file.getAbsolutePath()));

		final SortedMap<Entity, List<String>> actualMap = loadService.getValidCsvFiles(filePath, Entity.loadOrderComparator);

		Assert.assertEquals(actualMap.keySet(), expectedMap.keySet());
		Assert.assertEquals(actualMap.values().toArray()[0], expectedMap.values().toArray()[0]);
	}

	@Test
	public void testGetLoadableCsvFilesFromPath() throws Exception {
		final String filePath = getFilePath("loadFromDirectory");
		final SortedMap<Entity, List<String>> expectedMap = new TreeMap<>(Entity.loadOrderComparator);

		File candidateFile = new File(getFilePath("loadFromDirectory/Candidate_Valid_File.csv"));
		File candidateWorkHistoryFile = new File(getFilePath("loadFromDirectory/CandidateWorkHistory.csv"));
		File clientCorporationFile1 = new File(getFilePath("loadFromDirectory/ClientCorporation_1.csv"));
		File clientCorporationFile2 = new File(getFilePath("loadFromDirectory/ClientCorporation_2.csv"));

		expectedMap.put(Entity.CLIENT_CORPORATION, Arrays.asList(clientCorporationFile1.getAbsolutePath(), clientCorporationFile2.getAbsolutePath()));
		expectedMap.put(Entity.CANDIDATE, Arrays.asList(candidateFile.getAbsolutePath()));
		expectedMap.put(Entity.CANDIDATE_WORK_HISTORY, Arrays.asList(candidateWorkHistoryFile.getAbsolutePath()));

		final SortedMap<Entity, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath(filePath);

		Assert.assertEquals(actualMap.keySet(), expectedMap.keySet());
		Assert.assertEquals(actualMap.values().toArray()[0], expectedMap.values().toArray()[0]);
		Assert.assertEquals(actualMap.values().toArray()[1], expectedMap.values().toArray()[1]);
		Assert.assertEquals(actualMap.values().toArray()[2], expectedMap.values().toArray()[2]);

		Set<Map.Entry<Entity, List<String>>> sortedSet = actualMap.entrySet();
		Assert.assertEquals(3, sortedSet.size());
		Iterator<Map.Entry<Entity, List<String>>> iter = sortedSet.iterator();
		Assert.assertEquals(iter.next().getKey(), Entity.CLIENT_CORPORATION);
		Assert.assertEquals(iter.next().getKey(), Entity.CANDIDATE);
		Assert.assertEquals(iter.next().getKey(), Entity.CANDIDATE_WORK_HISTORY);
	}

	@Test
	public void testGetLoadableCsvFilesFromPath_badFile() throws Exception {
		final SortedMap<Entity, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath("bad_file.csv");
		Assert.assertTrue(actualMap.isEmpty());
	}

	@Test
	public void testGetLoadableCsvFilesFromPath_badDirectory() throws Exception {
		final SortedMap<Entity, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath("bad_directory/");
		Assert.assertTrue(actualMap.isEmpty());
	}

	@Test
	public void testGetLoadableCsvFilesFromPath_emptyDirectory() throws Exception {
		final SortedMap<Entity, List<String>> actualMap = loadService.getLoadableCsvFilesFromPath(getFilePath("testResume"));
		Assert.assertTrue(actualMap.isEmpty());
	}

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}
