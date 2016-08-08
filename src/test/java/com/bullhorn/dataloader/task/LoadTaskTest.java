package com.bullhorn.dataloader.task;

import java.util.LinkedHashMap;

import org.junit.Before;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.api.BullhornData;

public class LoadTaskTest {


    private LinkedHashMap<String, String> dataMap;
    private CsvFileWriter csvFileWriter;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;
    private LoadTask task;

    @Before
    public void setUp() throws Exception {
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);
    }

}
