package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.csv.CsvFileWriter;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
import com.bullhornsdk.data.model.entity.association.standard.ClientCorporationAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: Remove all 'static org.mockito' imports
public class DeleteCustomObjectTaskTest {

    private ActionTotals actionTotalsMock;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private RestApi restApiMock;
    private CsvFileWriter csvFileWriterMock;
    private Map<String, String> dataMap;
    private Preloader preloaderMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = Mockito.mock(ActionTotals.class);
        restApiMock = Mockito.mock(RestApi.class);
        csvFileWriterMock = Mockito.mock(CsvFileWriter.class);
        preloaderMock = Mockito.mock(Preloader.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);

        // Capture arguments to the writeRow method - this is our output from the run
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);

        dataMap = new LinkedHashMap<>();
        dataMap.put("id", "1");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
    }

    @Test
    public void run_Success_ClientCorporation() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("clientCorporation.externalID", "ext-1");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, dataMap, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any())).thenReturn(TestUtils.getListWrapper(ClientCorporation.class, 100));
        when(restApiMock.disassociateWithEntity(eq(ClientCorporation.class), eq(100), eq(ClientCorporationAssociations.getInstance().customObject1s()), eq(Sets.newHashSet(1)))).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 100));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Success_Person() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("person.customText1", "ext-1");
        dataMap.put("person._subtype", "Candidate");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(1, EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, dataMap, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(Candidate.class), eq("customText1:\"ext-1\""), any(), any())).thenReturn(TestUtils.getListWrapper(Candidate.class, 100));
        when(restApiMock.disassociateWithEntity(eq(Candidate.class), eq(100), eq(CandidateAssociations.getInstance().customObject2s()), eq(Sets.newHashSet(1)))).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 100));
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Fail_MissingIdColumn() throws IOException {
        dataMap.remove("id");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, dataMap, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Row 1: Cannot Perform Delete: missing 'id' column.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Fail_NoAssociationField() throws IOException {
        DeleteCustomObjectTask task = new DeleteCustomObjectTask(1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, dataMap, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "java.io.IOException: No association entities found in csv for ClientCorporationCustomObjectInstance1. CustomObjectInstances require a parent entity in the csv.");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Fail_NoParentFound() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("clientCorporation.externalID", "ext-1");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, dataMap, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any())).thenReturn(TestUtils.getListWrapper());
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Cannot find To-One Association: 'clientCorporation.externalID' with value: 'ext-1'");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

    @Test
    public void run_Fail_CannotDisassociate() throws IOException, InstantiationException, IllegalAccessException {
        dataMap.put("clientCorporation.externalID", "ext-1");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, dataMap, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        when(restApiMock.search(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any())).thenReturn(TestUtils.getListWrapper(ClientCorporation.class, 100));
        when(restApiMock.disassociateWithEntity(any(), any(), any(), any())).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, null, "externalID", "Flagrant Error"));
        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Row 1: Error occurred when making UPDATE REST call:\n" +
            "\tError occurred on field externalID due to the following: Flagrant Error\n");

        task.run();

        verify(csvFileWriterMock).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(actualResult, new ReflectionEquals(expectedResult));
    }

}
