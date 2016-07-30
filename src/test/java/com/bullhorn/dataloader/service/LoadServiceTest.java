package com.bullhorn.dataloader.service;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class LoadServiceTest {

	private PrintUtil printUtil;
	private LoadService loadService;
	private EntityConcurrencyService entityConcurrencyServiceMock;
	private BullhornAPI bullhornAPIMock;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		loadService = Mockito.spy(new LoadService(printUtil));
		bullhornAPIMock = Mockito.mock(BullhornAPI.class);

		// mock out AbstractService Methods that call class outside of this test scope
		entityConcurrencyServiceMock = Mockito.mock(EntityConcurrencyService.class);
		Mockito.doReturn(entityConcurrencyServiceMock).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(entityConcurrencyServiceMock).runLoadProcess();

		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(bullhornAPIMock).when(loadService).createSession();

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testRun() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};

		loadService.run(testArgs);

		Mockito.verify(entityConcurrencyServiceMock, Mockito.times(1)).runLoadProcess();
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtil, Mockito.never()).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String filePath = getFilePath("Invalid_Candidate_File.csv");
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_MissingArgument() throws Exception {
		final String[] testArgs = {Command.LOAD.getMethodName()};
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_TooManyArgments() throws Exception {
		final String filePath = "Candidate.csv";
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath, "tooMany"};
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();

		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_InvalidFile() throws Exception {
		final String filePath = "filePath";
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();

		final boolean actualResult = loadService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_EmptyFile() throws Exception {
		final String filePath = "";
		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();

		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}
