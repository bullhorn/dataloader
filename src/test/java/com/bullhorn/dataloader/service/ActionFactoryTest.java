package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.InputStream;

import static org.mockito.Mockito.mock;

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
        ValidationUtil validationUtilMock = mock(ValidationUtil.class);

        actionFactory = new ActionFactory(printUtilMock, propertyFileUtilMock, validationUtilMock, completeUtilMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);
    }

    @Test
    public void getActionHelp() throws Exception {
        Class expectedResult = HelpService.class;
        Action actualResult = actionFactory.getAction(Command.HELP);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionTemplate() throws Exception {
        Class expectedResult = TemplateService.class;
        Action actualResult = actionFactory.getAction(Command.TEMPLATE);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionConvertAttachments() throws Exception {
        Class expectedResult = ConvertAttachmentsService.class;
        Action actualResult = actionFactory.getAction(Command.CONVERT_ATTACHMENTS);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionLoad() throws Exception {
        Class expectedResult = LoadService.class;
        Action actualResult = actionFactory.getAction(Command.LOAD);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionExport() throws Exception {
        Class expectedResult = ExportService.class;
        Action actualResult = actionFactory.getAction(Command.EXPORT);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionDelete() throws Exception {
        Class expectedResult = DeleteService.class;
        Action actualResult = actionFactory.getAction(Command.DELETE);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionLoadAttachments() throws Exception {
        Class expectedResult = LoadAttachmentsService.class;
        Action actualResult = actionFactory.getAction(Command.LOAD_ATTACHMENTS);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }

    @Test
    public void getActionDeleteAttachments() throws Exception {
        Class expectedResult = DeleteAttachmentsService.class;
        Action actualResult = actionFactory.getAction(Command.DELETE_ATTACHMENTS);
        Assert.assertThat(actualResult.getClass(), new ReflectionEquals(expectedResult));
    }
}
