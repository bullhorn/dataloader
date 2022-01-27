package com.bullhorn.dataloader.rest;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhornsdk.data.exception.RestApiException;

public class CompleteUtilTest {

    private ActionTotals actionTotalsMock;
    private HttpClient httpClientMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private RestSession restSessionMock;
    private Timer timerMock;

    private ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor;

    private CompleteUtil completeUtil;

    @Before
    public void setup() throws IOException {
        actionTotalsMock = mock(ActionTotals.class);
        httpClientMock = mock(HttpClient.class);
        httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        RestApi restApiMock = mock(RestApi.class);
        restSessionMock = mock(RestSession.class);
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

        completeUtil = new CompleteUtil(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock, timerMock);
        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock);

        verify(httpClientMock).executeMethod(httpMethodArgumentCaptor.capture());
        HttpMethod httpMethod = httpMethodArgumentCaptor.getValue();
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

        completeUtil = new CompleteUtil(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock, timerMock);
        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock);

        verify(printUtilMock).log(restApiException);
    }

    @Test
    public void testResultsFileSuccess() throws IOException {
        String resultsFilePath = TestUtils.getResourceFilePath("results.json");
        File resultsFile = new File(resultsFilePath);

        try {
            Row row = TestUtils.createRow("firstName,lastName", "Data,Loader");
            Result result = Result.insert(1);
            when(actionTotalsMock.getAllActionsTotal()).thenReturn(1);
            when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(1);
            when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(0);
            when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(0);
            when(propertyFileUtilMock.getResultsFileEnabled()).thenReturn(true);
            when(propertyFileUtilMock.getResultsFilePath()).thenReturn(resultsFilePath);
            when(propertyFileUtilMock.getResultsFileWriteIntervalMsec()).thenReturn(10000);

            String fileContents = FileUtils.readFileToString(resultsFile);
            Assert.assertTrue(fileContents.startsWith("{}"));

            completeUtil = new CompleteUtil(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock, timerMock);
            completeUtil.rowComplete(row, result, actionTotalsMock);
            completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock);

            String updatedFileContents = FileUtils.readFileToString(resultsFile);
            JSONObject jsonObject = new JSONObject(updatedFileContents);
            Assert.assertEquals(jsonObject.getInt("processed"), 1);
            Assert.assertEquals(jsonObject.getInt("inserted"), 1);
            Assert.assertEquals(jsonObject.getInt("updated"), 0);
            Assert.assertEquals(jsonObject.getInt("deleted"), 0);
            Assert.assertEquals(jsonObject.getInt("failed"), 0);
            Assert.assertEquals(jsonObject.getInt("durationMsec"), 999);
            Assert.assertFalse(jsonObject.has("errors"));
        } finally {
            // Reset resource file
            FileUtils.writeStringToFile(resultsFile, "{}");
        }
    }

    @Test
    public void testResultsFileFailureWithoutID() throws IOException {
        String resultsFilePath = TestUtils.getResourceFilePath("results.json");
        File resultsFile = new File(resultsFilePath);

        try {
            Row row = TestUtils.createRow("bogus", "1;2");
            Result result = Result.failure(new DataLoaderException(ErrorInfo.INTERNAL_SERVER_ERROR,
                "'bogus' does not exist on Candidate"));
            when(actionTotalsMock.getAllActionsTotal()).thenReturn(1);
            when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(0);
            when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(0);
            when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(1);
            when(propertyFileUtilMock.getResultsFileEnabled()).thenReturn(true);
            when(propertyFileUtilMock.getResultsFilePath()).thenReturn(resultsFilePath);
            when(propertyFileUtilMock.getResultsFileWriteIntervalMsec()).thenReturn(10000);

            String fileContents = FileUtils.readFileToString(resultsFile);
            Assert.assertTrue(fileContents.startsWith("{}"));

            completeUtil = new CompleteUtil(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock, timerMock);
            completeUtil.rowComplete(row, result, actionTotalsMock);
            completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock);

            String updatedFileContents = FileUtils.readFileToString(resultsFile);
            JSONObject jsonObject = new JSONObject(updatedFileContents);
            Assert.assertEquals(jsonObject.getInt("processed"), 1);
            Assert.assertEquals(jsonObject.getInt("inserted"), 0);
            Assert.assertEquals(jsonObject.getInt("updated"), 0);
            Assert.assertEquals(jsonObject.getInt("deleted"), 0);
            Assert.assertEquals(jsonObject.getInt("failed"), 1);
            Assert.assertEquals(jsonObject.getInt("durationMsec"), 999);
            Assert.assertTrue(jsonObject.has("errors"));
            JSONObject firstError = jsonObject.getJSONArray("errors").getJSONObject(0);
            Assert.assertEquals(firstError.getInt("row"), 1);
            Assert.assertFalse(firstError.has("id"));
            Assert.assertEquals(firstError.getString("message"), "'bogus' does not exist on Candidate");
        } finally {
            // Reset resource file
            FileUtils.writeStringToFile(resultsFile, "{}");
        }
    }

    @Test
    public void testResultsFileFailureWithID() throws IOException {
        String resultsFilePath = TestUtils.getResourceFilePath("results.json");
        File resultsFile = new File(resultsFilePath);

        try {
            Row row = TestUtils.createRow("bogus", "1;2");
            Result result = Result.failure(new DataLoaderException(ErrorInfo.INTERNAL_SERVER_ERROR,
                "'bogus' does not exist on Candidate"), 101);
            when(actionTotalsMock.getAllActionsTotal()).thenReturn(1);
            when(actionTotalsMock.getActionTotal(Result.Action.INSERT)).thenReturn(0);
            when(actionTotalsMock.getActionTotal(Result.Action.UPDATE)).thenReturn(0);
            when(actionTotalsMock.getActionTotal(Result.Action.FAILURE)).thenReturn(1);
            when(propertyFileUtilMock.getResultsFileEnabled()).thenReturn(true);
            when(propertyFileUtilMock.getResultsFilePath()).thenReturn(resultsFilePath);
            when(propertyFileUtilMock.getResultsFileWriteIntervalMsec()).thenReturn(10000);

            String fileContents = FileUtils.readFileToString(resultsFile);
            Assert.assertTrue(fileContents.startsWith("{}"));

            completeUtil = new CompleteUtil(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock, timerMock);
            completeUtil.rowComplete(row, result, actionTotalsMock);
            completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock);

            String updatedFileContents = FileUtils.readFileToString(resultsFile);
            JSONObject jsonObject = new JSONObject(updatedFileContents);
            Assert.assertEquals(jsonObject.getInt("processed"), 1);
            Assert.assertEquals(jsonObject.getInt("inserted"), 0);
            Assert.assertEquals(jsonObject.getInt("updated"), 0);
            Assert.assertEquals(jsonObject.getInt("deleted"), 0);
            Assert.assertEquals(jsonObject.getInt("failed"), 1);
            Assert.assertEquals(jsonObject.getInt("durationMsec"), 999);
            Assert.assertTrue(jsonObject.has("errors"));
            JSONObject firstError = jsonObject.getJSONArray("errors").getJSONObject(0);
            Assert.assertEquals(firstError.getInt("row"), 1);
            Assert.assertEquals(firstError.getInt("id"), 101);
            Assert.assertEquals(firstError.getString("message"), "'bogus' does not exist on Candidate");
        } finally {
            // Reset resource file
            FileUtils.writeStringToFile(resultsFile, "{}");
        }
    }

    @Test
    public void testResultsFileCannotWriteFile() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName", "Data,Loader");
        when(propertyFileUtilMock.getResultsFileEnabled()).thenReturn(true);
        when(propertyFileUtilMock.getResultsFilePath()).thenReturn("");
        when(propertyFileUtilMock.getResultsFileWriteIntervalMsec()).thenReturn(10000);

        completeUtil = new CompleteUtil(restSessionMock, httpClientMock, propertyFileUtilMock, printUtilMock, timerMock);
        completeUtil.rowComplete(row, Result.insert(1), actionTotalsMock);
        completeUtil.complete(Command.LOAD, "Candidate.csv", EntityInfo.CANDIDATE, actionTotalsMock);

        verify(printUtilMock, times(1)).printAndLog(contains("Error writing results file: java.io.FileNotFoundException:"));
    }
}
