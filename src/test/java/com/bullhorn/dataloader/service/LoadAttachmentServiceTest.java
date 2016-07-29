package com.bullhorn.dataloader.service;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class LoadAttachmentServiceTest {

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final EntityAttachmentConcurrencyService entityAttachmentConcurrencyServiceMock = Mockito.mock(EntityAttachmentConcurrencyService.class);
		
		Mockito.doNothing().when(entityAttachmentConcurrencyServiceMock).runLoadAttachmentProcess();

		final LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// mock out AbstractService Methods that call class outside of this test scope createEntityAttachmentConcurrencyService
		Mockito.doReturn(entityAttachmentConcurrencyServiceMock).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.eq(Command.LOAD_ATTACHMENTS), Mockito.eq(entity), Mockito.eq(filePath));
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		loadAttachmentService.run(testArgs);

		Mockito.verify(entityAttachmentConcurrencyServiceMock, Mockito.times(1)).runLoadAttachmentProcess();
		Mockito.verify(loadAttachmentService, Mockito.times(2)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		final boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.never()).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsMissingArgment() throws Exception {
		final LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName()};
		final boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath, "tooMany"};
		final boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		final String entity = "Candidates";
		final String filePath = "filePath";
		final LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		final boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		final String entity = "Candidates";
		final String filePath = "";
		final LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		final boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testUsage() throws Exception {
		final PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();

		final DeleteService deleteService = Mockito.spy(new DeleteService());

		final Field prinUtilField = AbstractService.class.getDeclaredField("printUtil");
		prinUtilField.setAccessible(true);
		prinUtilField.set(deleteService, printUtilMock);

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		deleteService.printUsage();
		
		Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
		
	}

}
