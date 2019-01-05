package com.bullhorn.dataloader;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.service.Action;
import com.bullhorn.dataloader.service.ActionFactory;
import com.bullhorn.dataloader.service.DeleteAttachmentsService;
import com.bullhorn.dataloader.service.DeleteService;
import com.bullhorn.dataloader.service.HelpService;
import com.bullhorn.dataloader.service.LoadAttachmentsService;
import com.bullhorn.dataloader.service.LoadService;
import com.bullhorn.dataloader.service.TemplateService;
import com.bullhorn.dataloader.util.PrintUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandLineInterfaceTest {

    private CommandLineInterface commandLineInterface;
    private PrintUtil printUtilMock;
    private HelpService helpServiceMock;
    private LoadService loadServiceMock;
    private DeleteService deleteServiceMock;
    private LoadAttachmentsService loadAttachmentsServiceMock;
    private DeleteAttachmentsService deleteAttachmentsServiceMock;
    private TemplateService templateServiceMock;
    private List<Action> actions;

    @Before
    public void setup() throws Exception {
        printUtilMock = mock(PrintUtil.class);
        ActionFactory actionFactoryMock = mock(ActionFactory.class);
        commandLineInterface = new CommandLineInterface(printUtilMock, actionFactoryMock);

        helpServiceMock = mock(HelpService.class);
        loadServiceMock = mock(LoadService.class);
        deleteServiceMock = mock(DeleteService.class);
        loadAttachmentsServiceMock = mock(LoadAttachmentsService.class);
        deleteAttachmentsServiceMock = mock(DeleteAttachmentsService.class);
        templateServiceMock = mock(TemplateService.class);

        doReturn(helpServiceMock).when(actionFactoryMock).getAction(Command.HELP);
        doReturn(loadServiceMock).when(actionFactoryMock).getAction(Command.LOAD);
        doReturn(deleteServiceMock).when(actionFactoryMock).getAction(Command.DELETE);
        doReturn(loadAttachmentsServiceMock).when(actionFactoryMock).getAction(Command.LOAD_ATTACHMENTS);
        doReturn(deleteAttachmentsServiceMock).when(actionFactoryMock).getAction(Command.DELETE_ATTACHMENTS);
        doReturn(templateServiceMock).when(actionFactoryMock).getAction(Command.TEMPLATE);

        actions = new ArrayList<>();
        actions.add(helpServiceMock);
        actions.add(loadServiceMock);
        actions.add(deleteServiceMock);
        actions.add(loadAttachmentsServiceMock);
        actions.add(deleteAttachmentsServiceMock);
        actions.add(templateServiceMock);
    }

    @Test
    public void helpTest() throws Exception {
        final String[] testArgs = {Command.HELP.getMethodName(), "Candidate.csv"};
        when(helpServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        verifyActionRun(helpServiceMock, testArgs);
    }

    @Test
    public void helpInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.HELP.getMethodName(), "Candidate.csv"};
        when(helpServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        verifyActionFailed(helpServiceMock, testArgs);
    }

    @Test
    public void loadTest() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
        when(loadServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        verifyActionRun(loadServiceMock, testArgs);
    }

    @Test
    public void loadInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
        when(loadServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        verifyActionFailed(loadServiceMock, testArgs);
    }

    @Test
    public void deleteTest() throws Exception {
        final String[] testArgs = {Command.DELETE.getMethodName(), "Candidate", "Candidate.csv"};
        when(deleteServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        verifyActionRun(deleteServiceMock, testArgs);
    }

    @Test
    public void deleteInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.DELETE.getMethodName()};
        when(deleteServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        verifyActionFailed(deleteServiceMock, testArgs);
    }

    @Test
    public void loadAttachmentTest() throws Exception {
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), "Candidate", "Attachments.csv"};
        when(loadAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        verifyActionRun(loadAttachmentsServiceMock, testArgs);
    }

    @Test
    public void loadAttachmentInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), "Candidate"};
        when(loadAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        verifyActionFailed(loadAttachmentsServiceMock, testArgs);
    }

    @Test
    public void deleteAttachmentsTest() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), "Candidate", "Attachments.csv"};
        when(deleteAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        verifyActionRun(deleteAttachmentsServiceMock, testArgs);
    }

    @Test
    public void deleteAttachmentsInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), "Candidate", "Attachments.csv"};
        when(deleteAttachmentsServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        verifyActionFailed(deleteAttachmentsServiceMock, testArgs);
    }

    @Test
    public void templateTest() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};
        when(templateServiceMock.isValidArguments(testArgs)).thenReturn(true);

        commandLineInterface.start(testArgs);

        verifyActionRun(templateServiceMock, testArgs);
    }

    @Test
    public void templateInvalidArgsTest() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidates"};
        when(templateServiceMock.isValidArguments(testArgs)).thenReturn(false);

        commandLineInterface.start(testArgs);

        verifyActionFailed(templateServiceMock, testArgs);
    }

    @Test
    public void invalidCommandTest() throws Exception {
        final String[] testArgs = {"bad", "Candidate"};

        commandLineInterface.start(testArgs);

        for (Action action : actions) {
            verify(action, never()).isValidArguments(any());
            verify(action, never()).run(any());
        }

        verify(printUtilMock, times(1)).printUsage();
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void emptyCommandTest() throws Exception {
        final String[] testArgs = {};

        commandLineInterface.start(testArgs);

        for (Action action : actions) {
            verify(action, never()).isValidArguments(any());
            verify(action, never()).run(any());
        }

        verify(printUtilMock, times(1)).printUsage();
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void catchExceptionTest() throws Exception {
        final String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
        when(loadServiceMock.isValidArguments(testArgs)).thenReturn(true);
        InterruptedException interruptedException = new InterruptedException("ERROR TEXT");
        doThrow(interruptedException).when(loadServiceMock).run(testArgs);

        commandLineInterface.start(testArgs);

        verify(printUtilMock, times(1)).printAndLog(interruptedException);
    }

    /**
     * Helper method which ensures that only the given action is run, and run successfully
     *
     * @param actionRun The action that should have run
     * @param args      The arguments that should be provided to the action
     */
    private void verifyActionRun(Action actionRun, String[] args) throws Exception {
        for (Action action : actions) {
            if (action.equals(actionRun)) {
                verify(action, times(1)).isValidArguments(args);
                verify(action, times(1)).run(args);
            } else {
                verify(action, never()).isValidArguments(any());
                verify(action, never()).run(any());
            }
        }

        verify(printUtilMock, never()).printUsage();
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    /**
     * Helper method which ensures that only the given action is run, and run unsuccessfully
     *
     * @param actionFailed The action that should have fails
     * @param args         The arguments that should be provided to the action
     */
    private void verifyActionFailed(Action actionFailed, String[] args) throws Exception {
        for (Action action : actions) {
            if (action.equals(actionFailed)) {
                verify(action, times(1)).isValidArguments(args);
            } else {
                verify(action, never()).isValidArguments(any());
            }
            verify(action, never()).run(any());
        }

        verify(printUtilMock, times(1)).printUsage();
        verify(printUtilMock, never()).printAndLog(anyString());
    }
}
