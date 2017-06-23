package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
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

public class CompleteCallTest {

    private ActionTotals actionTotalsMock;
    private RestSession restSessionMock;
    private HttpClient httpClientMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;

    private ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor;

    private CompleteCall completeCall;

    @Before
    public void setup() throws IOException {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        RestApi restApiMock = Mockito.mock(RestApi.class);
        restSessionMock = Mockito.mock(RestSession.class);
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
        Mockito.when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        Mockito.when(restApiMock.getRestUrl()).thenReturn("http://bullhorn-rest-api/");
        Mockito.when(restApiMock.getBhRestToken()).thenReturn("12345678-1234-1234-1234-1234567890AB");
        Mockito.when(httpClientMock.executeMethod(any())).thenReturn(0);

        completeCall = new CompleteCall(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock);
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

        completeCall.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, timerMock);

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

        completeCall.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, timerMock);

        Mockito.verify(printUtilMock).printAndLog(restApiException);
    }
}
