package com.bullhorn.dataloader.service;

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
	private BullhornAPI bullhornAPIMock;
	private EntityConcurrencyService entityConcurrencyServiceMock;

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
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};

		deleteService.run(testArgs);

		Mockito.verify(entityConcurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};

		final boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		Mockito.verify(printUtil, Mockito.never()).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_MissingArgument() throws Exception {
		final String[] testArgs = {Command.DELETE.getMethodName()};

		final boolean actualResult = deleteService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_TooManyArgments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath, "tooMany"};

		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String entity = "Candidates";
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};

		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments_EmptyFile() throws Exception {
		final String entity = "Candidates";
		final String filePath = "";
		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};

		final boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
}
