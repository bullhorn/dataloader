package com.bullhorn.dataloader.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.PropertyValidation;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Skill;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.response.crud.CreateResponse;
import com.bullhornsdk.data.model.response.crud.UpdateResponse;
import com.bullhornsdk.data.model.response.list.CandidateListWrapper;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import com.bullhornsdk.data.model.response.list.StandardListWrapper;
import com.csvreader.CsvReader;

public class LoadTaskTest {

    private LinkedHashMap<String, String> dataMap;
    private ExecutorService executorService;
    private Map<String, Method> methodMap;
    private ArgumentCaptor<Result> resultArgumentCaptor;
    private Map<String, Integer> countryNameToIdMap;
    private CsvFileWriter csvFileWriter;
    private CsvReader csvReader;
    private BullhornData bullhornData;
    private PrintUtil printUtil;
    private ActionTotals actionTotals;
    private LoadTask task;
    private PropertyFileUtil candidateIdProperties;
    private PropertyFileUtil candidateExternalIdProperties;
    private PropertyValidation propertyValidation;
    private ConcurrencyService service;

    @Before
    public void setUp() throws Exception {
        executorService = Mockito.mock(ExecutorService.class);
        csvReader = Mockito.mock(CsvReader.class);
        csvFileWriter = Mockito.mock(CsvFileWriter.class);
        bullhornData = Mockito.mock(BullhornData.class);
        actionTotals = Mockito.mock(ActionTotals.class);
        printUtil = Mockito.mock(PrintUtil.class);
        propertyValidation = new PropertyValidation();

        // Load in properties files for testing candidateExistField=id and candidateExistField=ExternalID
        final String CandidateIdPropertiesFile = getFilePath("loadTaskTest/LoadTaskTest_CandidateID.properties");
        candidateIdProperties = new PropertyFileUtil(CandidateIdPropertiesFile, propertyValidation, printUtil);

        final String CandidateExternalIdPropertiesFile = getFilePath("loadTaskTest/LoadTaskTest_CandidateExternalID.properties");
        candidateExternalIdProperties = new PropertyFileUtil(CandidateExternalIdPropertiesFile, propertyValidation, printUtil);

        service = new ConcurrencyService(Command.LOAD_ATTACHMENTS, "Candidate", csvReader, csvFileWriter, executorService, candidateExternalIdProperties, bullhornData, printUtil, actionTotals);
        methodMap = service.createMethodMap(Candidate.class);
        countryNameToIdMap = new LinkedHashMap<String, Integer>();
        countryNameToIdMap.put("United States", 1);
        dataMap = new LinkedHashMap<String, String>();
        dataMap.put("externalID", "11");
        dataMap.put("firstName", "Load");
        dataMap.put("lastName", "Test");
        dataMap.put("primarySkills.id", "1");
        dataMap.put("address.address1", "test");
        dataMap.put("address.countryName", "USA");
        dataMap.put("owner.id", "1");
        resultArgumentCaptor = ArgumentCaptor.forClass(Result.class);
    }

    @Test
    public void insertAttachmentToDescriptionCandidateTest() throws Exception {
        Candidate candidate = new Candidate();
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals));
        task.entity = candidate;
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");

        task.insertAttachmentToDescription();

        Assert.assertNotNull(candidate.getDescription());
    }

    @Test
    public void insertAttachmentToDescriptionClientCorporationTest() throws Exception {
        ClientCorporation corporation = new ClientCorporation();
        methodMap = service.createMethodMap(ClientCorporation.class);
        task = Mockito.spy(new LoadTask(Command.LOAD, 1, ClientCorporation.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals));
        task.entity = corporation;
        when(task.getAttachmentFilePath("ClientCorporation", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");

        task.insertAttachmentToDescription();

        //client corp uses companyDescription field instead of description
        Assert.assertNotNull(corporation.getCompanyDescription());
    }

    @Test
    public void getAttachmentFilePathTest() {
        String entityName = "Candidate";
        String externalID = "123";
        String expected = "convertedAttachments/Candidate/123.html";
        task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals);

        String actual = task.getAttachmentFilePath(entityName, externalID);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithDescriptionMethodInMapTest() {
        String expected = "description";
        task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithDescriptionSubstringMethodInMapTest() {
        //client corp uses companyDescription field instead of description
        String expected = "companydescription";
        methodMap = service.createMethodMap(ClientCorporation.class);
        task = new LoadTask(Command.LOAD, 1, ClientCorporation.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getDescriptionMethodWithNoDescriptionMethodInMapTest() {
        //placement does not have a description field
        String expected = "";
        methodMap = service.createMethodMap(Placement.class);
        task = new LoadTask(Command.LOAD, 1, ClientCorporation.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals);

        String actual = task.getDescriptionMethod();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void run_InsertSuccess() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.INSERT, 1, "");
        task = Mockito.spy(task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals));
        when(bullhornData.search(any(), eq("externalID:\"11\""), any(), any())).thenReturn(new CandidateListWrapper());
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornData.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornData.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        CreateResponse response = new CreateResponse();
        response.setChangedEntityId(1);
        when(bullhornData.insertEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
}

    @Test
    public void run_UpdateSuccess() throws IOException, InstantiationException, IllegalAccessException {
        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.UPDATE, 1, "");
        task = Mockito.spy(task = new LoadTask(Command.LOAD, 1, Candidate.class, dataMap, methodMap, countryNameToIdMap, csvFileWriter, candidateExternalIdProperties, bullhornData, printUtil, actionTotals));
        when(bullhornData.search(eq(Candidate.class), eq("externalID:\"11\""), any(), any())).thenReturn(getListWrapper(Candidate.class));
        when(task.getAttachmentFilePath("Candidate", "11")).thenReturn("src/test/resources/convertedAttachments/Candidate/11.html");
        when(bullhornData.query(eq(CorporateUser.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(CorporateUser.class));
        when(bullhornData.query(eq(Skill.class), eq("id=1"), any(), any())).thenReturn(getListWrapper(Skill.class));
        UpdateResponse response = new UpdateResponse();
        response.setChangedEntityId(1);
        when(bullhornData.updateEntity(any())).thenReturn(response);

        task.run();

        verify(csvFileWriter).writeRow(any(), resultArgumentCaptor.capture());
        final Result actualResult = resultArgumentCaptor.getValue();
        Assert.assertThat(expectedResult, new ReflectionEquals(actualResult));
    }

    private <B extends BullhornEntity> ListWrapper<B> getListWrapper(Class<B> entityClass) throws IllegalAccessException, InstantiationException {
        ListWrapper<B> listWrapper = new StandardListWrapper<B>();
        B entity = entityClass.newInstance();
        entity.setId(1);
        listWrapper.setData(Arrays.asList(entity));
        return listWrapper;
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }

}
