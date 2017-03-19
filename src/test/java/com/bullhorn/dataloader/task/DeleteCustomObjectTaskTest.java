package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class DeleteCustomObjectTaskTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private Map<String, String> dataMap;
    private BullhornRestApi bullhornRestApi;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    private DeleteCustomObjectTask task;

    @Before
    public void setUp() throws Exception {
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornRestApi = Mockito.mock(BullhornRestApi.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);

        dataMap = new LinkedHashMap<>();
        dataMap.put("id", "1");
        dataMap.put("text1", "Test");
        dataMap.put("text2", "Skip");
    }

    @Test
    public void run_Success() throws IOException {
        dataMap.put("clientCorporation.id", "1");

        task = new DeleteCustomObjectTask(Command.DELETE, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        when(bullhornRestApi.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = Result.Delete(1);

        task.run();

        Mockito.verify(csvFileWriter, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_PersonSuccess() throws IOException {
        dataMap.put("person.id", "1");
        dataMap.put("person._subtype", "Candidate");

        task = new DeleteCustomObjectTask(Command.DELETE, 1, EntityInfo.PERSON_CUSTOM_OBJECT_INSTANCE_2, dataMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        when(bullhornRestApi.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = Result.Delete(1);

        task.run();

        Mockito.verify(csvFileWriter, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }

    @Test
    public void run_Fail_NoAssociationField() throws IOException {
        task = new DeleteCustomObjectTask(Command.DELETE, 1, EntityInfo.CLIENT_CORPORATION_CUSTOM_OBJECT_INSTANCE_2, dataMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        when(bullhornRestApi.deleteEntity(any(), anyInt())).thenReturn(new DeleteResponse());
        Result expectedResult = Result.Failure(new IOException("No association entities found in csv for ClientCorporationCustomObjectInstance2. CustomObjectInstances require a parent entity in the csv."),1);

        task.run();

        Mockito.verify(csvFileWriter, Mockito.times(1)).writeRow(any(), eq(expectedResult));
    }


}
