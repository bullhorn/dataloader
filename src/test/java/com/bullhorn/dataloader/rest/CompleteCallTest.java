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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompleteCallTest {

    private ActionTotals actionTotalsMock;
    private HttpClient httpClientMock;
    private PrintUtil printUtilMock;
    private Timer timerMock;

    private ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor;

    private CompleteCall completeCall;

    @Before
    public void setup() throws IOException {
        actionTotalsMock = mock(ActionTotals.class);
        RestApi restApiMock = mock(RestApi.class);
        RestSession restSessionMock = mock(RestSession.class);
        httpClientMock = mock(HttpClient.class);
        httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        printUtilMock = mock(PrintUtil.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        timerMock = mock(Timer.class);

        when(propertyFileUtilMock.getNumThreads()).thenReturn(9);
        when(timerMock.getDurationMillis()).thenReturn(999L);
        when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(1);
        when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(2);
        when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(3);
        when(actionTotalsMock.getAllActionsTotal()).thenReturn(6);
        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.getRestUrl()).thenReturn("http://bullhorn-rest-api/");
        when(restApiMock.getBhRestToken()).thenReturn("12345678-1234-1234-1234-1234567890AB");
        when(httpClientMock.executeMethod(any())).thenReturn(0);

        completeCall = new CompleteCall(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock);
    }

    @Test
    public void testComplete() throws IOException {
        String expectedURL = "http://bullhorn-rest-api/services/dataLoader/complete?BhRestToken=12345678-1234-1234-1234-1234567890AB";
        String expectedPayload = "{"
            + "\"totalRecords\":6,"
            + "\"file\":\"Candidate.csv\","
            + "\"failureRecords\":3,"
            + "\"durationMsec\":999,"
            + "\"successRecords\":3,"
            + "\"numThreads\":9,"
            + "\"command\":\"LOAD\","
            + "\"entity\":\"Candidate\""
            + "}";

        completeCall.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, timerMock);

        verify(httpClientMock).executeMethod(httpMethodArgumentCaptor.capture());
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
        when(httpClientMock.executeMethod(any())).thenThrow(restApiException);

        completeCall.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock, timerMock);

        verify(printUtilMock).printAndLog(restApiException);
    }
}
