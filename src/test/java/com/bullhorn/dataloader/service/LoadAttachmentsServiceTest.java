package com.bullhorn.dataloader.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class LoadAttachmentsServiceTest {

	private PrintUtil printUtil;
	private LoadAttachmentsService loadAttachmentsService;
	private EntityAttachmentConcurrencyService entityAttachmentConcurrencyServiceMock;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		loadAttachmentsService = Mockito.spy(new LoadAttachmentsService(printUtil));

		// mock out AbstractService Methods that call class outside of this test scope createEntityAttachmentConcurrencyService
		entityAttachmentConcurrencyServiceMock = Mockito.mock(EntityAttachmentConcurrencyService.class);
		Mockito.doReturn(entityAttachmentConcurrencyServiceMock).when(loadAttachmentsService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(entityAttachmentConcurrencyServiceMock).runLoadAttachmentProcess();

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentsService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentsService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentsService).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};

		loadAttachmentsService.run(testArgs);

		Mockito.verify(entityAttachmentConcurrencyServiceMock, Mockito.times(1)).runLoadAttachmentProcess();
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArguments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};

		final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		Mockito.verify(printUtil, Mockito.never()).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArgumentsMissingArgument() throws Exception {
		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName()};

		final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath, "tooMany"};

		final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		final String entity = "Candidates";
		final String filePath = "filePath";
		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};

		final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		final String entity = "Candidates";
		final String filePath = "";
		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		final boolean actualResult = loadAttachmentsService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
}
