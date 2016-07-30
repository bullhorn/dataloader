package com.bullhorn.dataloader.service;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.executor.EntityAttachmentsConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class DeleteAttachmentsServiceTest {

	private PrintUtil printUtil;
	private DeleteAttachmentsService deleteAttachmentsService;
	private EntityAttachmentsConcurrencyService entityAttachmentsConcurrencyServiceMock;
	private BullhornAPI bullhornAPIMock;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		deleteAttachmentsService = Mockito.spy(new DeleteAttachmentsService(printUtil));
		bullhornAPIMock = Mockito.mock(BullhornAPI.class);

		// mock out AbstractService Methods that call class outside of this test scope
		entityAttachmentsConcurrencyServiceMock = Mockito.mock(EntityAttachmentsConcurrencyService.class);
		Mockito.doReturn(entityAttachmentsConcurrencyServiceMock).when(deleteAttachmentsService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(entityAttachmentsConcurrencyServiceMock).runDeleteAttachmentsProcess();

		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(bullhornAPIMock).when(deleteAttachmentsService).createSession();

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

		deleteAttachmentsService.run(testArgs);

		Mockito.verify(entityAttachmentsConcurrencyServiceMock, Mockito.times(1)).runDeleteAttachmentsProcess();
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createSession();

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtil, Mockito.never()).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String filePath = getFilePath("Invalid_Candidate_File.csv");
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createSession();

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_MissingArgument() throws Exception {
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName()};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createSession();

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_TooManyArgments() throws Exception {
		final String filePath = "Candidate.csv";
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath, "tooMany"};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createSession();

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_InvalidFile() throws Exception {
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createSession();

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_EmptyFile() throws Exception {
		final String filePath = "";
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteAttachmentsService).createSession();

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}