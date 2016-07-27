package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
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

import static org.mockito.Mockito.verify;

public class EntityAttachmentConcurrencyServiceTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private LinkedHashMap<String, String> dataMap;
    private BullhornData bullhornData;
    private ExecutorService executorService;
    private ArgumentCaptor<LoadAttachmentTask> taskCaptor;

    @Before
    public void setUp() throws IOException {
        propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        executorService = Mockito.mock(ExecutorService.class);
        csvReader = new CsvReader("src/test/resources/CandidateAttachments.csv");
        csvReader.readHeaders();

        taskCaptor  = ArgumentCaptor.forClass(LoadAttachmentTask.class);

    }

    @Test
    public void EntityAttachmentConcurrencyServiceTest() throws IOException {
        //arrange
        EntityAttachmentConcurrencyService service = new EntityAttachmentConcurrencyService(
                "Candidate",
                csvReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData
        );
        LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");
        LoadAttachmentTask expectedTask = new LoadAttachmentTask("Candidate", expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData);

        //act
        service.runLoadAttchmentProcess();

        //assert
        verify(executorService).execute(taskCaptor.capture());
        LoadAttachmentTask actualTask = taskCaptor.getValue();
        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

}
