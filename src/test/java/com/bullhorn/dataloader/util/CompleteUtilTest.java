package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhornsdk.data.api.BullhornData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
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
        Mockito.when(bullhornDataMock.getBhRestToken()).thenReturn("12345678-1234-1234-1234-1234567890AB");
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(1);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(2);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(3);
        Mockito.when(actionTotalsMock.getAllActionsTotal()).thenReturn(6);
    }

    @Test
    public void completeTest() throws IOException {
        long durationMsec = 999;
        String expectedURL = "http://bullhorn-rest-api/services/dataLoader/complete?BhRestToken=12345678-1234-1234-1234-1234567890AB";
        String expectedPayload = "{" +
            "\"totalRecords\":6," +
            "\"file\":\"Candidate.csv\"," +
            "\"failureRecords\":3," +
            "\"durationMsec\":999," +
            "\"successRecords\":3," +
            "\"numThreads\":9," +
            "\"command\":\"LOAD\"," +
            "\"entity\":\"Candidate\"" +
        "}";

        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, durationMsec, bullhornDataMock);

        Mockito.verify(httpClientMock).executeMethod(httpMethodArgumentCaptor.capture());
        final HttpMethod httpMethod = httpMethodArgumentCaptor.getValue();
        PostMethod postMethod = (PostMethod) httpMethod;
        Assert.assertEquals(expectedURL, postMethod.getURI().toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        postMethod.getRequestEntity().writeRequest(outputStream);
        String actualPayload = outputStream.toString();
        Assert.assertEquals(expectedPayload, actualPayload);
    }
}
