package com.bullhorn.dataloader.service;

import static org.mockito.Mockito.mock;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;

public class ActionFactoryTest {

    private ActionFactory actionFactory;

    @Before
    public void setup() {
        CompleteUtil completeUtilMock = mock(CompleteUtil.class);
        InputStream inputStreamMock = mock(InputStream.class);
        PrintUtil printUtilMock = mock(PrintUtil.class);
        ProcessRunner processRunnerMock = mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        RestSession restSessionMock = mock(RestSession.class);
        Timer timerMock = mock(Timer.class);

        actionFactory = new ActionFactory(printUtilMock, propertyFileUtilMock, completeUtilMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);
    }

    @Test
    public void testGetActionConvertAttachments() {
        Class expectedResult = ConvertAttachmentsService.class;
        Action actualResult = actionFactory.getAction(Command.CONVERT_ATTACHMENTS);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionDelete() {
        Class expectedResult = DeleteService.class;
        Action actualResult = actionFactory.getAction(Command.DELETE);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionDeleteAttachments() {
        Class expectedResult = DeleteAttachmentsService.class;
        Action actualResult = actionFactory.getAction(Command.DELETE_ATTACHMENTS);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionExport() {
        Class expectedResult = ExportService.class;
        Action actualResult = actionFactory.getAction(Command.EXPORT);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionHelp() {
        Class expectedResult = HelpService.class;
        Action actualResult = actionFactory.getAction(Command.HELP);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionLoad() {
        Class expectedResult = LoadService.class;
        Action actualResult = actionFactory.getAction(Command.LOAD);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionLoadAttachments() {
        Class expectedResult = LoadAttachmentsService.class;
        Action actualResult = actionFactory.getAction(Command.LOAD_ATTACHMENTS);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionLogin() {
        Class expectedResult = LoginService.class;
        Action actualResult = actionFactory.getAction(Command.LOGIN);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionMeta() {
        Class expectedResult = MetaService.class;
        Action actualResult = actionFactory.getAction(Command.META);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }

    @Test
    public void testGetActionTemplate() {
        Class expectedResult = TemplateService.class;
        Action actualResult = actionFactory.getAction(Command.TEMPLATE);
        Assert.assertEquals(actualResult.getClass(), expectedResult);
    }
}
