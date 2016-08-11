package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

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

public class DeleteServiceTest {

	private PrintUtil printUtilMock;
	private PropertyFileUtil propertyFileUtilMock;
	private ValidationUtil validationUtil;
    private InputStream inputStreamFake;
	private ConcurrencyService concurrencyServiceMock;
	private DeleteService deleteService;

	@Before
	public void setup() throws Exception {
		printUtilMock = Mockito.mock(PrintUtil.class);
		propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
		validationUtil = new ValidationUtil(printUtilMock);
        inputStreamFake = IOUtils.toInputStream("text to simulate user entry", "UTF-8");
		deleteService = Mockito.spy(new DeleteService(printUtilMock, propertyFileUtilMock, validationUtil, inputStreamFake));

		// mock out AbstractService Methods that call class outside of this test scope
		concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
		Mockito.doReturn(concurrencyServiceMock).when(deleteService).createConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(concurrencyServiceMock).runDeleteProcess();

		// track this call
		Mockito.doNothing().when(printUtilMock).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun_file() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		deleteService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun_directoryOneFile() throws Exception {
		final String filePath = getFilePath("loadFromDirectory/ClientContact");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		deleteService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun_directoryFourFiles() throws Exception {
		final String filePath = getFilePath("loadFromDirectory");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		deleteService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(2)).runDeleteProcess();
		Mockito.verify(printUtilMock, Mockito.times(7)).printAndLog(Mockito.anyString());
	}

	@Test(expected=IllegalStateException.class)
	public void testRun_invalidThrowsException() throws Exception {
		final String[] testArgs = {Command.DELETE.getMethodName()};
		deleteService.run(testArgs);
	}

	@Test
	public void testIsValidArguments() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String filePath = getFilePath("Invalid_Candidate_File.csv");
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
		final String filePath = getFilePath("Certification.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError("Certification", "not deletable");
	}

	@Test
	public void testIsValidArguments_NonDeletableEntity() throws Exception {
		final String filePath = getFilePath("ClientCorporation.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printEntityError("ClientCorporation", "not deletable");
	}

    @Test
    public void testIsValidArguments_Directory() throws Exception {
        final String filePath = getFilePath("loadFromDirectory");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_noCsvFiles() throws Exception {
        final String filePath = getFilePath("testResume");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArguments_noDeletableCsvFiles() throws Exception {
        final String filePath = getFilePath("loadFromDirectory/businessSector");
        final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

        final boolean actualResult = deleteService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}

	@Test
	public void testGetDeletableCsvFilesFromPath() throws Exception {
		final String filePath = getFilePath("loadFromDirectory");
		final SortedMap<Entity, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath(filePath);

		Set<Map.Entry<Entity, List<String>>> sortedSet = actualMap.entrySet();
		Assert.assertEquals(2, sortedSet.size());
		Iterator<Map.Entry<Entity, List<String>>> iter = sortedSet.iterator();
		Assert.assertEquals(iter.next().getKey(), Entity.CANDIDATE_WORK_HISTORY);
		Assert.assertEquals(iter.next().getKey(), Entity.CANDIDATE);
	}

	@Test
	public void testGetDeletableCsvFilesFromPath_badFile() throws Exception {
		final SortedMap<Entity, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath("bad_file.csv");
		Assert.assertTrue(actualMap.isEmpty());
	}

	@Test
	public void testGetDeletableCsvFilesFromPath_badDirectory() throws Exception {
		final SortedMap<Entity, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath("bad_directory/");
		Assert.assertTrue(actualMap.isEmpty());
	}

	@Test
	public void testGetDeletableCsvFilesFromPath_emptyDirectory() throws Exception {
		final SortedMap<Entity, List<String>> actualMap = deleteService.getDeletableCsvFilesFromPath(getFilePath("testResume"));
		Assert.assertTrue(actualMap.isEmpty());
	}
}