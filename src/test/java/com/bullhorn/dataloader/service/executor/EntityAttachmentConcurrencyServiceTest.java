package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.csvreader.CsvReader;

public class EntityAttachmentConcurrencyServiceTest {

    private PropertyFileUtil propertyFileUtil;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private ExecutorService executorService;
    @SuppressWarnings("rawtypes")
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
        taskCaptor = ArgumentCaptor.forClass(LoadAttachmentTask.class);
    }

    @Test
    public void EntityAttachmentConcurrencyServiceTestLoadAttachments() throws IOException, InterruptedException {
        final EntityAttachmentConcurrencyService service = new EntityAttachmentConcurrencyService(
                Command.LOADATTACHMENTS,
                "Candidate",
                csvReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData,
                printUtil,
                actionTotals);

        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");

        final LoadAttachmentTask<Candidate> expectedTask = new LoadAttachmentTask<Candidate>(Command.LOAD_ATTACHMENTS, "Candidate", expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runLoadAttachmentProcess();
        verify(executorService).execute(taskCaptor.capture());

        @SuppressWarnings("unchecked")
		final LoadAttachmentTask<Candidate> actualTask = (LoadAttachmentTask<Candidate>)taskCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }

    @Test
    public void EntityAttachmentConcurrencyServiceTestDeleteAttachments() throws IOException, InterruptedException {
        final ArgumentCaptor<DeleteAttachmentTask> deleteAttachmentTaskArgumentCaptor = ArgumentCaptor.forClass(DeleteAttachmentTask.class);
        csvReader = new CsvReader("src/test/resources/CandidateAttachments_success.csv");
        csvReader.readHeaders();
        final EntityAttachmentConcurrencyService service = new EntityAttachmentConcurrencyService(
                Method.DELETEATTACHMENTS,
                "Candidate",
                csvReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData,
                printUtil,
                actionTotals);
        final LinkedHashMap<String, String> expectedDataMap = new LinkedHashMap<>();
        expectedDataMap.put("id", "1");
        expectedDataMap.put("action", "INSERT");
        expectedDataMap.put("externalID", "1");
        expectedDataMap.put("relativeFilePath", "src/test/resources/testResume/Test Resume.doc");
        expectedDataMap.put("isResume", "0");
        expectedDataMap.put("parentEntityID", "1");
        final DeleteAttachmentTask expectedTask = new DeleteAttachmentTask(Method.DELETEATTACHMENTS, "Candidate", expectedDataMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

        service.runDeleteAttachmentProcess();

        verify(executorService).execute(deleteAttachmentTaskArgumentCaptor.capture());
        final DeleteAttachmentTask actualTask = deleteAttachmentTaskArgumentCaptor.getValue();

        Assert.assertThat(expectedTask, new ReflectionEquals(actualTask));
    }
}
