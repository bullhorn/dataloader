package com.bullhorn.dataloader.service;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class DeleteServiceTest {

	private PrintUtil printUtil;
	private DeleteService deleteService;
	private EntityConcurrencyService entityConcurrencyServiceMock;
    private BullhornAPI bullhornAPIMock;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		deleteService = Mockito.spy(new DeleteService(printUtil));
		bullhornAPIMock = Mockito.mock(BullhornAPI.class);

		// mock out AbstractService Methods that call class outside of this test scope
		entityConcurrencyServiceMock = Mockito.mock(EntityConcurrencyService.class);
		Mockito.doReturn(entityConcurrencyServiceMock).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(entityConcurrencyServiceMock).runDeleteProcess();

		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(bullhornAPIMock).when(deleteService).createSession();

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};

		deleteService.run(testArgs);

		Mockito.verify(entityConcurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtil, Mockito.never()).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String filePath = getFilePath("Invalid_Candidate_File.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_MissingArgument() throws Exception {
		final String[] testArgs = {Command.DELETE.getMethodName()};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_TooManyArgments() throws Exception {
		final String filePath = "Candidate.csv";
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath, "tooMany"};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_InvalidFile() throws Exception {
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_EmptyFile() throws Exception {
		final String filePath = "";
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_ReadOnlyEntity() throws Exception {
		final String filePath = getFilePath("Certification.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printEntityError("Certification", "not deletable");
	}

	@Test
	public void testIsValidArguments_NonDeletableEntity() throws Exception {
		final String filePath = getFilePath("ClientCorporation.csv");
		final String[] testArgs = {Command.DELETE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printEntityError("ClientCorporation", "not deletable");
	}

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}