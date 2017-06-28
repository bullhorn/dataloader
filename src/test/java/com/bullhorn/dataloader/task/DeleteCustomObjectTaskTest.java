package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteCustomObjectTaskTest {

    private ActionTotals actionTotalsMock;
    private RestApi restApiMock;
    private CsvFileWriter csvFileWriterMock;
    private Preloader preloaderMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;

    @Before
    public void setup() throws Exception {
        actionTotalsMock = mock(ActionTotals.class);
        restApiMock = mock(RestApi.class);
        csvFileWriterMock = mock(CsvFileWriter.class);
        preloaderMock = mock(Preloader.class);
        printUtilMock = mock(PrintUtil.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
    }

    @Test
    public void run_Success_ClientCorporation() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id,text1,text2,clientCorporation.externalID", "1,Test,Skip,ext-1");
        when(restApiMock.searchForList(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any())).thenReturn(TestUtils.getList(ClientCorporation.class, 100));
        when(restApiMock.disassociateWithEntity(eq(ClientCorporation.class), eq(100), eq(ClientCorporationAssociations.getInstance().customObject1s()), eq(Sets.newHashSet(1)))).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 100));

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Success_Person() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id,text1,text2,person.customText1,person._subtype", "1,Test,Skip,ext-1,Candidate");
        when(restApiMock.searchForList(eq(Candidate.class), eq("customText1:\"ext-1\""), any(), any())).thenReturn(TestUtils.getList(Candidate.class, 100));
        when(restApiMock.disassociateWithEntity(eq(Candidate.class), eq(100), eq(CandidateAssociations.getInstance().customObject2s()), eq(Sets.newHashSet(1)))).thenReturn(TestUtils.getResponse(ChangeType.UPDATE, 100));

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.DELETE, 1, "");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Fail_MissingIdColumn() throws IOException {
        Row row = TestUtils.createRow("text1,text2", "Test,Skip");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1, "java.lang.IllegalArgumentException: Cannot Perform Delete: missing 'id' column.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Fail_NoAssociationField() throws IOException {
        Row row = TestUtils.createRow("id,text1,text2", "1,Test,Skip");

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "java.io.IOException: No association entities found in csv for ClientCorporationCustomObjectInstance1. CustomObjectInstances require a parent entity in the csv.");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Fail_NoParentFound() throws IOException, InstantiationException, IllegalAccessException {
        Row row = TestUtils.createRow("id,text1,text2,clientCorporation.externalID", "1,Test,Skip,ext-1");

        when(restApiMock.searchForList(eq(ClientCorporation.class), eq("externalID:\"ext-1\""), any(), any())).thenReturn(TestUtils.getList());

        DeleteCustomObjectTask task = new DeleteCustomObjectTask(EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_1, row, preloaderMock, csvFileWriterMock, propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock);
        task.run();

        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, 1, "com.bullhornsdk.data.exception.RestApiException: Cannot find To-One Association: 'clientCorporation.externalID' with value: 'ext-1'");
        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
    }
}
