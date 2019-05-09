//package com.bullhorn.dataloader.task;
//
//import com.bullhorn.dataloader.TestUtils;
//import com.bullhorn.dataloader.data.ActionTotals;
//import com.bullhorn.dataloader.data.CsvFileWriter;
//import com.bullhorn.dataloader.data.Result;
//import com.bullhorn.dataloader.data.Row;
//import com.bullhorn.dataloader.enums.EntityInfo;
//import com.bullhorn.dataloader.rest.Cache;
//import com.bullhorn.dataloader.rest.CompleteUtil;
//import com.bullhorn.dataloader.rest.RestApi;
//import com.bullhorn.dataloader.util.PrintUtil;
//import com.bullhorn.dataloader.util.PropertyFileUtil;
//import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
//import com.bullhornsdk.data.model.entity.core.standard.Candidate;
//import com.bullhornsdk.data.model.entity.embedded.Address;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class ExportTaskTest {
//
//    private ActionTotals actionTotalsMock;
//    private RestApi restApiMock;
//    private CsvFileWriter csvFileWriterMock;
//    private PrintUtil printUtilMock;
//    private PropertyFileUtil propertyFileUtilMock;
//    private Cache cacheMock;
//    private CompleteUtil completeUtilMock;
//
//    @Before
//    public void setup() {
//        actionTotalsMock = mock(ActionTotals.class);
//        restApiMock = mock(RestApi.class);
//        csvFileWriterMock = mock(CsvFileWriter.class);
//        printUtilMock = mock(PrintUtil.class);
//        propertyFileUtilMock = mock(PropertyFileUtil.class);
//        cacheMock = mock(Cache.class);
//        completeUtilMock = mock(CompleteUtil.class);
//
//        when(propertyFileUtilMock.getListDelimiter()).thenReturn(";");
//        when(cacheMock.getEntry(any(), any(), any())).thenReturn(null);
//
//    }
//
//    @Test
//    public void testRunSuccessCandidate() throws Exception {
//        Row row = TestUtils.createRow(
//            "externalID,customDate1,firstName,lastName,email,primarySkills.name,address.address1,address.countryID,owner.id",
//            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,hacking skills;ninja skills,test,1,1,");
//        ArgumentCaptor<Row> rowArgumentCaptor = ArgumentCaptor.forClass(Row.class);
//        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
//            .thenReturn(Collections.singletonList("externalID"));
//        Candidate fakeCandidate = new Candidate(1);
//        fakeCandidate.setExternalID("11");
//        fakeCandidate.setFirstName("Sir");
//        fakeCandidate.setLastName("Lancelot");
//        fakeCandidate.setEmail("lancelot@spam.egg");
//        Address fakeAddress = new Address();
//        fakeAddress.setCountryID(12345);
//        fakeCandidate.setAddress(fakeAddress);
//        fakeCandidate.setPrimarySkills(TestUtils.getOneToMany(3, TestUtils.createSkill(1001, "bo staff skills"),
//            TestUtils.createSkill(1002, "hacking skills"), TestUtils.createSkill(1003, null)));
//        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"11\""), any(), any()))
//            .thenReturn(TestUtils.getList(fakeCandidate));
//
//        ExportTask task = new ExportTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
//            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
//        task.run();
//
//        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.EXPORT, 1, "");
//        verify(csvFileWriterMock, times(1)).writeRow(rowArgumentCaptor.capture(), eq(expectedResult));
//        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.EXPORT, 1);
//        Row actualRow = rowArgumentCaptor.getValue();
//        Assert.assertEquals("path/to/fake/file.csv", actualRow.getFilePath());
//        Assert.assertEquals(new Integer(1), actualRow.getNumber());
//        Assert.assertEquals(row.getNames(), actualRow.getNames());
//        Assert.assertEquals(Arrays.asList("11", "", "Sir", "Lancelot", "lancelot@spam.egg", "bo staff skills;hacking skills", "", "12345", ""), actualRow.getValues());
//    }
//
//    @Test
//    public void testRunSuccessCandidateMoreThanFiveAssociations() throws Exception {
//        Row row = TestUtils.createRow("externalID,primarySkills.name", "ext-1001,New Skill 1;New Skill 2;New Skill 3");
//        ArgumentCaptor<Row> rowArgumentCaptor = ArgumentCaptor.forClass(Row.class);
//        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
//            .thenReturn(Collections.singletonList("externalID"));
//        Candidate fakeCandidate = new Candidate(1);
//        fakeCandidate.setExternalID("ext-1001");
//        fakeCandidate.setPrimarySkills(TestUtils.getOneToMany(7,
//            TestUtils.createSkill(1001, "skill_1"),
//            TestUtils.createSkill(1002, "skill_2"),
//            TestUtils.createSkill(1003, "skill_3"),
//            TestUtils.createSkill(1004, "skill_4"),
//            TestUtils.createSkill(1005, "skill_5")));
//        when(restApiMock.searchForList(eq(Candidate.class), eq("externalID:\"ext-1001\""), any(), any()))
//            .thenReturn(TestUtils.getList(fakeCandidate));
//        when(restApiMock.getAllAssociationsList(eq(Candidate.class), any(),
//            eq(CandidateAssociations.getInstance().primarySkills()), any(), any()))
//            .thenReturn(TestUtils.getList(
//                TestUtils.createSkill(1001, "skill_1"),
//                TestUtils.createSkill(1002, "skill_2"),
//                TestUtils.createSkill(1003, "skill_3"),
//                TestUtils.createSkill(1004, "skill_4"),
//                TestUtils.createSkill(1005, "skill_5"),
//                TestUtils.createSkill(1006, "skill_6"),
//                TestUtils.createSkill(1007, "skill_7")));
//
//        ExportTask task = new ExportTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
//            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
//        task.run();
//
//        Result expectedResult = new Result(Result.Status.SUCCESS, Result.Action.EXPORT, 1, "");
//        verify(csvFileWriterMock, times(1)).writeRow(rowArgumentCaptor.capture(), eq(expectedResult));
//        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.EXPORT, 1);
//        Row actualRow = rowArgumentCaptor.getValue();
//        Assert.assertEquals("path/to/fake/file.csv", actualRow.getFilePath());
//        Assert.assertEquals(new Integer(1), actualRow.getNumber());
//        Assert.assertEquals(row.getNames(), actualRow.getNames());
//        Assert.assertEquals(Arrays.asList("ext-1001", "skill_1;skill_2;skill_3;skill_4;skill_5;skill_6;skill_7"), actualRow.getValues());
//    }
//
//    @Test
//    public void testRunFailureNoExistField() throws Exception {
//        Row row = TestUtils.createRow(
//            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryID,owner.id",
//            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,1,1,");
//
//        ExportTask task = new ExportTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
//            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
//        task.run();
//
//        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
//            "com.bullhornsdk.data.exception.RestApiException: "
//                + "Cannot perform export because exist field is not specified for entity: Candidate");
//        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
//        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
//    }
//
//    @Test
//    public void testRunFailureRecordNotFound() throws Exception {
//        Row row = TestUtils.createRow(
//            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryID,owner.id",
//            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,1,1,");
//        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
//            .thenReturn(Arrays.asList("firstName", "lastName", "email"));
//        when(restApiMock.searchForList(eq(Candidate.class),
//            eq("firstName:\"Data\" AND lastName:\"Loader\" AND email:\"dloader@bullhorn.com\""), any(), any()))
//            .thenReturn(TestUtils.getList(Candidate.class));
//
//        ExportTask task = new ExportTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
//            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
//        task.run();
//
//        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
//            "com.bullhornsdk.data.exception.RestApiException: No Matching Candidate Records Exist with ExistField criteria of: "
//                + "firstName=Data AND lastName=Loader AND email=dloader@bullhorn.com");
//        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
//        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
//    }
//
//    @Test
//    public void testRunFailureTooManyRecordsFound() throws Exception {
//        Row row = TestUtils.createRow(
//            "externalID,customDate1,firstName,lastName,email,primarySkills.id,address.address1,address.countryID,owner.id",
//            "11,2016-08-30,Data,Loader,dloader@bullhorn.com,1,test,1,1,");
//        when(propertyFileUtilMock.getEntityExistFields(EntityInfo.CANDIDATE))
//            .thenReturn(Arrays.asList("firstName", "lastName", "email"));
//        when(restApiMock.searchForList(eq(Candidate.class),
//            eq("firstName:\"Data\" AND lastName:\"Loader\" AND email:\"dloader@bullhorn.com\""), any(), any()))
//            .thenReturn(TestUtils.getList(Candidate.class, 101, 102));
//
//        ExportTask task = new ExportTask(EntityInfo.CANDIDATE, row, csvFileWriterMock,
//            propertyFileUtilMock, restApiMock, printUtilMock, actionTotalsMock, cacheMock, completeUtilMock);
//        task.run();
//
//        Result expectedResult = new Result(Result.Status.FAILURE, Result.Action.FAILURE, -1,
//            "com.bullhornsdk.data.exception.RestApiException: Multiple Records Exist. "
//                + "Found 2 Candidate records with the same ExistField criteria of: "
//                + "firstName=Data AND lastName=Loader AND email=dloader@bullhorn.com");
//        verify(csvFileWriterMock, times(1)).writeRow(any(), eq(expectedResult));
//        TestUtils.verifyActionTotals(actionTotalsMock, Result.Action.FAILURE, 1);
//    }
//}
