package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntityAttachmentConcurrencyServiceTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private ExecutorService executorService;
    private ArgumentCaptor<LoadAttachmentTask> taskCaptor;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;

    @Before
    public void setUp() throws IOException {
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        executorService = Mockito.mock(ExecutorService.class);
        printUtil = Mockito.mock(PrintUtil.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        csvReader = new CsvReader("src/test/resources/CandidateAttachments.csv");
        csvReader.readHeaders();

        taskCaptor  = ArgumentCaptor.forClass(LoadAttachmentTask.class);

    }

    @Test
    public void EntityAttachmentConcurrencyServiceTest() throws IOException, InterruptedException {
        //arrange
        EntityAttachmentConcurrencyService service = new EntityAttachmentConcurrencyService(
                Method.LOADATTACHMENTS,
                "Candidate",
                csvReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData,
                printUtil,
                actionTotals);
        LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");
        LoadAttachmentTask expectedTask = new LoadAttachmentTask(Method.LOADATTACHMENTS, "Candidate", expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        //act
        service.runLoadAttchmentProcess();

        //assert
        verify(executorService).execute(taskCaptor.capture());
        LoadAttachmentTask actualTask = taskCaptor.getValue();
        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

}
