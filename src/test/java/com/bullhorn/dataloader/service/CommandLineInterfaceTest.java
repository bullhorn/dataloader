package com.bullhorn.dataloader.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.util.PrintUtil;

public class CommandLineInterfaceTest {

    private CommandLineInterface commandLineInterface;
    private ActionBuilder actionBuilderMock;
    private PrintUtil printUtilMock;
    private LoadService loadServiceMock;
    private DeleteService deleteServiceMock;
    private LoadAttachmentsService loadAttachmentsServiceMock;
    private DeleteAttachmentsService deleteAttachmentsServiceMock;
    private TemplateService templateServiceMock;

    @Before
    public void setUp() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        actionBuilderMock = Mockito.mock(ActionBuilder.class);
        commandLineInterface = new CommandLineInterface(printUtilMock, actionBuilderMock);

        loadServiceMock = Mockito.mock(LoadService.class);
        deleteServiceMock = Mockito.mock(DeleteService.class);
        loadAttachmentsServiceMock = Mockito.mock(LoadAttachmentsService.class);
        deleteAttachmentsServiceMock = Mockito.mock(DeleteAttachmentsService.class);
        templateServiceMock = Mockito.mock(TemplateService.class);

        Mockito.doReturn(loadServiceMock).when(actionBuilderMock).getAction(Command.LOAD);
        Mockito.doReturn(deleteServiceMock).when(actionBuilderMock).getAction(Command.DELETE);
        Mockito.doReturn(loadAttachmentsServiceMock).when(actionBuilderMock).getAction(Command.LOAD_ATTACHMENTS);
        Mockito.doReturn(deleteAttachmentsServiceMock).when(actionBuilderMock).getAction(Command.DELETE_ATTACHMENTS);
        Mockito.doReturn(templateServiceMock).when(actionBuilderMock).getAction(Command.TEMPLATE);

        // track this call
        Mockito.doNothing().when(printUtilMock).printAndLog(Mockito.anyString());
        Mockito.doNothing().when(printUtilMock).printUsage();
    }

    @Test
    public void loadTest() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
        Mockito.when(loadServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(loadServiceMock, Mockito.times(1)).run(testArgs);

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void loadInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
        Mockito.when(loadServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void deleteTest() throws Exception {
        final String[] testArgs = {Command.DELETE.getMethodName(), "Candidate", "Candidate.csv"};
        Mockito.when(deleteServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(deleteServiceMock, Mockito.times(1)).run(testArgs);

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void deleteInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.DELETE.getMethodName()};
        Mockito.when(deleteServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void loadAttachmentTest() throws Exception {
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), "Candidate", "Attachements.csv"};
        Mockito.when(loadAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(loadAttachmentsServiceMock, Mockito.times(1)).run(testArgs);

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void loadAttachmentInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), "Candidate"};
        Mockito.when(loadAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void deleteAttachmentsTest() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), "Candidate", "Attachements.csv"};
        Mockito.when(deleteAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.times(1)).run(testArgs);

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void deleteAttachmentsInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), "Candidate", "Attachements.csv"};
        Mockito.when(deleteAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());
    }

    @Test
    public void templateTest() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};
        Mockito.when(templateServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(templateServiceMock, Mockito.times(1)).run(testArgs);
    }

    @Test
    public void templateInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidates"};
        Mockito.when(templateServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void invalidCommandTest() throws Exception {
        final String[] testArgs = {"bad", "Candidate"};

        commandLineInterface.start(testArgs);

        Mockito.verify(deleteServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(deleteAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(loadAttachmentsServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(templateServiceMock, Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(templateServiceMock, Mockito.never()).run(Mockito.any());

        Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

}
