package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.csv.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhornsdk.data.exception.RestApiException;
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

    private ActionTotals actionTotalsMock;
    private ConnectionUtil connectionUtilMock;
    private HttpClient httpClientMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;

    private ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor;

    private CompleteUtil completeUtil;

    @Before
    public void setup() throws IOException {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        BullhornRestApi bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        httpClientMock = Mockito.mock(HttpClient.class);
        httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        timerMock = Mockito.mock(Timer.class);

        Mockito.when(propertyFileUtilMock.getNumThreads()).thenReturn(9);
        Mockito.when(timerMock.getDurationMillis()).thenReturn(999L);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(1);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(2);
        Mockito.when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(3);
        Mockito.when(actionTotalsMock.getAllActionsTotal()).thenReturn(6);
        Mockito.when(connectionUtilMock.getSession()).thenReturn(bullhornRestApiMock);
        Mockito.when(bullhornRestApiMock.getRestUrl()).thenReturn("http://bullhorn-rest-api/");
        Mockito.when(bullhornRestApiMock.getBhRestToken()).thenReturn("12345678-1234-1234-1234-1234567890AB");
        Mockito.when(httpClientMock.executeMethod(any())).thenReturn(0);

        completeUtil = new CompleteUtil(connectionUtilMock, httpClientMock, propertyFileUtilMock, printUtilMock);
    }

    @Test
    public void testComplete() throws IOException {
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

        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, timerMock);

        Mockito.verify(httpClientMock).executeMethod(httpMethodArgumentCaptor.capture());
        final HttpMethod httpMethod = httpMethodArgumentCaptor.getValue();
        PostMethod postMethod = (PostMethod) httpMethod;
        Assert.assertEquals(expectedURL, postMethod.getURI().toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        postMethod.getRequestEntity().writeRequest(outputStream);
        String actualPayload = outputStream.toString();
        Assert.assertEquals(expectedPayload, actualPayload);
    }

    @Test
    public void testComplete_error() throws IOException {
        RestApiException restApiException = new RestApiException("ERROR TEXT");
        Mockito.when(httpClientMock.executeMethod(any())).thenThrow(restApiException);

        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, timerMock);

        Mockito.verify(printUtilMock).printAndLog(restApiException);
    }
}
