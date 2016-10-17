package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class CompleteUtilTest {

    private PropertyFileUtil propertyFileUtilMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;
    private CompleteUtil completeUtil;

    @Before
    public void setup() {
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        completeUtil = new CompleteUtil(propertyFileUtilMock, printUtilMock);

        Mockito.when(propertyFileUtilMock.getNumThreads()).thenReturn(10);
    }

    @Test
    public void completeTest() throws IOException {
        long durationMsec = 1000;

        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, durationMsec);

        // TODO: Test the injection of a REST Endpoint
    }
}
