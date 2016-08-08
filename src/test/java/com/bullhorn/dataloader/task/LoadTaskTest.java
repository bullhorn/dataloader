package com.bullhorn.dataloader.task;

import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.csvreader.CsvReader;

public class LoadTaskTest {

    private PropertyFileUtil propertyFileUtil;
    private LinkedHashMap<String, String> dataMap;
    private ExecutorService executorService;
    private Map<String, Method> methodMap;
    private Map<String, Integer> countryNameToIdMap;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;
    private LoadTask task;

    @Before
    public void setUp() throws Exception {
        executorService = Mockito.mock(ExecutorService.class);
        csvReader = Mockito.mock(CsvReader.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);
        final ConcurrencyService service = new ConcurrencyService(Command.LOAD_ATTACHMENTS, "Candidate", csvReader, csvFileWriter, executorService, propertyFileUtil, bullhornData, printUtil, actionTotals);
        methodMap = service.createMethodMap(Candidate.class);
        countryNameToIdMap = new LinkedHashMap<String, Integer>();
        countryNameToIdMap.put("United States", 1);
        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("externalID", "11");
        dataMap.put("firstName", "Load");
        dataMap.put("lastName", "Test");
        final String PropertiesFile = getFilePath("loadAttachmentTaskTest/LoadAttachmentTaskTest_CandidateExternalID.properties");
        propertyFileUtil = new PropertyFileUtil(PropertiesFile);
    }

    @Test
    public void insertAttachmentToDescriptionTest() throws Exception {
        Candidate candidate = new Candidate();
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals));
        task.entity = candidate;
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");

        task.insertAttachmentToDescription();

        Assert.assertNotNull(candidate.getDescription());
    }

    @Test
    public void getAttachmentFilePathTest() {
        String entityName = "Candidate";
        String externalID = "123";
        String expected = "convertedAttachments/Candidate/123.html";
        task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);

        String actual = task.getAttachmentFilePath(entityName, externalID);

        Assert.assertEquals(actual, expected);
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }

}
