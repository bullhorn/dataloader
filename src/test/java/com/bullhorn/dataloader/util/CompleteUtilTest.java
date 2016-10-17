package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhornsdk.data.api.BullhornData;
import org.apache.commons.httpclient.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Matchers.any;

public class CompleteUtilTest {

    private PropertyFileUtil propertyFileUtilMock;
    private PrintUtil printUtilMock;
    private ActionTotals actionTotalsMock;
    private BullhornData bullhornDataMock;
    private CompleteUtil completeUtil;
    private HttpClient httpClientMock;

    @Before
    public void setup() throws IOException {
        httpClientMock = Mockito.mock(HttpClient.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);
        completeUtil = new CompleteUtil(httpClientMock, propertyFileUtilMock, printUtilMock);

        Mockito.when(httpClientMock.executeMethod(any())).thenReturn(0);
        Mockito.when(propertyFileUtilMock.getNumThreads()).thenReturn(10);
        Mockito.when(bullhornDataMock.getRestUrl()).thenReturn("");
    }

    @Test
    public void completeLoadTest() throws IOException {
        long durationMsec = 1000;

        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, durationMsec, bullhornDataMock);

        Mockito.verify(httpClientMock, Mockito.times(1)).executeMethod(any());
    }

    // TODO: Delete, Load/Convert/Delete Attachments
}
