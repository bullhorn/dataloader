package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhornsdk.data.api.BullhornData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
    private ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor;

    @Before
    public void setup() throws IOException {
        httpClientMock = Mockito.mock(HttpClient.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);
        httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        completeUtil = new CompleteUtil(httpClientMock, propertyFileUtilMock, printUtilMock);

        Mockito.when(httpClientMock.executeMethod(any())).thenReturn(0);
        Mockito.when(propertyFileUtilMock.getNumThreads()).thenReturn(9);
        Mockito.when(bullhornDataMock.getRestUrl()).thenReturn("http://bullhorn-rest-api/");
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(1);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(2);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(3);
        Mockito.when(actionTotalsMock.getAllActionsTotal()).thenReturn(6);
    }

    @Test
    public void completeLoadTest() throws IOException {
        long durationMsec = 999;
        String expectedURI = "http://bullhorn-rest-api/dataloader/complete" +
            "?command=LOAD" +
            "&entity=Candidate" +
            "&file=Candidate.csv" +
            "&totalRecords=6" +
            "&successRecords=3" +
            "&failureRecords=3" +
            "&durationMSec=999" +
            "&numThreads=9";

        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, durationMsec, bullhornDataMock);

        Mockito.verify(httpClientMock).executeMethod(httpMethodArgumentCaptor.capture());
        final HttpMethod httpMethod = httpMethodArgumentCaptor.getValue();
        Assert.assertEquals(expectedURI, httpMethod.getURI().toString());
    }
}
