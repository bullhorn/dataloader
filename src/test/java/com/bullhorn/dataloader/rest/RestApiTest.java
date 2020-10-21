package com.bullhorn.dataloader.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmissionHistory;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.AbstractCrudResponse;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Sets;

public class RestApiTest {
    private StandardBullhornData bullhornDataMock;
    private RestApiExtension restApiExtensionMock;
    private PropertyFileUtil propertyFileUtilMock;
    private PrintUtil printUtilMock;
    private RestApi restApi;

    @Before
    public void setup() {
        bullhornDataMock = mock(StandardBullhornData.class);
        restApiExtensionMock = mock(RestApiExtension.class);
        propertyFileUtilMock = mock(PropertyFileUtil.class);
        printUtilMock = mock(PrintUtil.class);
        restApi = new RestApi(bullhornDataMock, restApiExtensionMock, propertyFileUtilMock, printUtilMock);

        when(bullhornDataMock.associateWithEntity(any(), any(), any(), any())).thenReturn(
            new AbstractCrudResponse(), new AbstractCrudResponse(), new AbstractCrudResponse(), new AbstractCrudResponse());
        when(bullhornDataMock.disassociateWithEntity(any(), any(), any(), any())).thenReturn(
            new AbstractCrudResponse(), new AbstractCrudResponse(), new AbstractCrudResponse(), new AbstractCrudResponse());
    }

    @Test
    public void testGetters() {
        restApi.getBhRestToken();
        verify(bullhornDataMock, times(1)).getBhRestToken();

        restApi.getRestUrl();
        verify(bullhornDataMock, times(1)).getRestUrl();
    }

    @Test
    public void testGetMetaData() {
        restApi.getMetaData(Candidate.class, MetaParameter.BASIC, null);
        verify(bullhornDataMock, times(1)).getMetaData(eq(Candidate.class), eq(MetaParameter.BASIC), eq(null));
    }

    @Test
    public void testSearchForListNoExternalID() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.search(eq(Candidate.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(Candidate.class, 0, 10, IntStream.rangeClosed(1, 10).toArray()));
        restApi.searchForList(Candidate.class, "name:\"Data Loader\"", Sets.newHashSet("id"), ParamFactory.searchParams());
        verify(restApiExtensionMock, never()).getByExternalId(any(), any(), any(), any());
        verify(bullhornDataMock, times(1)).search(eq(Candidate.class), eq("name:\"Data Loader\""), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(Candidate Search): name:\"Data Loader\", fields: [id]"));
    }

    @Test
    public void testSearchForListExternalIdUnsupportedEntity() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.search(eq(Lead.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(Lead.class, 0, 10, IntStream.rangeClosed(1, 10).toArray()));
        when(bullhornDataMock.search(eq(Opportunity.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(Opportunity.class, 0, 10, IntStream.rangeClosed(1, 10).toArray()));
        when(bullhornDataMock.search(eq(JobOrder.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(JobOrder.class, 0, 10, IntStream.rangeClosed(1, 10).toArray()));

        restApi.searchForList(Lead.class, "externalID:\"ext 1\"", null, ParamFactory.searchParams());
        restApi.searchForList(Opportunity.class, "externalID:\"ext 1\"", null, ParamFactory.searchParams());
        restApi.searchForList(JobOrder.class, "externalID:\"ext 1\"", null, ParamFactory.searchParams());

        verify(bullhornDataMock, times(1)).search(eq(Lead.class), eq("externalID:\"ext 1\""), eq(Sets.newHashSet("id")), any());
        verify(bullhornDataMock, times(1)).search(eq(Opportunity.class), eq("externalID:\"ext 1\""), eq(Sets.newHashSet("id")), any());
        verify(bullhornDataMock, times(1)).search(eq(JobOrder.class), eq("externalID:\"ext 1\""), eq(Sets.newHashSet("id")), any());
        verify(restApiExtensionMock, never()).getByExternalId(any(), any(), any(), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(Lead Search): externalID:\"ext 1\", fields: [id]"));
    }

    @Test
    public void testSearchForListExternalIdSuccess() throws InstantiationException, IllegalAccessException {
        SearchResult<Candidate> searchResult = new SearchResult<>();
        searchResult.setList(TestUtils.getList(Candidate.class, 1));
        when(restApiExtensionMock.getByExternalId(eq(restApi), eq(Candidate.class), eq("ext 1"), eq(Sets.newHashSet("id", "name")))).thenReturn(searchResult);

        restApi.searchForList(Candidate.class, "externalID:\"ext 1\"", Sets.newHashSet("id", "name"), ParamFactory.searchParams());

        verify(restApiExtensionMock, times(1)).getByExternalId(eq(restApi), eq(Candidate.class), eq("ext 1"), eq(Sets.newHashSet("id", "name")));
        verify(bullhornDataMock, never()).searchForList(any(), any(), any(), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(Candidate Search): externalID:\"ext 1\", fields: [id, name]"));
    }

    @Test
    public void testSearchForListExternalIdFailure() throws InstantiationException, IllegalAccessException {
        SearchResult<Candidate> searchResult = new SearchResult<>();
        searchResult.setSuccess(false);
        when(restApiExtensionMock.getByExternalId(eq(restApi), eq(Candidate.class), eq("ext 1"), any()))
            .thenReturn(searchResult);
        when(bullhornDataMock.search(eq(Candidate.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(Candidate.class, 0, 10, IntStream.rangeClosed(1, 10).toArray()));

        restApi.searchForList(Candidate.class, "externalID:\"ext 1\"", Sets.newHashSet("lastName", "firstName", "email"), ParamFactory.searchParams());

        verify(restApiExtensionMock, times(1)).getByExternalId(eq(restApi), eq(Candidate.class), eq("ext 1"), eq(Sets.newHashSet("lastName", "firstName", "email", "id")));
        verify(bullhornDataMock, times(1)).search(eq(Candidate.class), eq("externalID:\"ext 1\""), eq(Sets.newHashSet("lastName", "firstName", "email", "id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(Candidate Search): externalID:\"ext 1\", fields: [email, firstName, id, lastName]"));
    }

    @Test
    public void testSearchForListMultipleCalls() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.search(eq(ClientContact.class), any(), any(), any())).thenReturn(
            TestUtils.getListWrapper(ClientContact.class, 0, 600, IntStream.rangeClosed(1, 500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 500, 600, IntStream.rangeClosed(501, 600).toArray()));
        List<ClientContact> list = restApi.searchForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.searchParams());
        verify(bullhornDataMock, times(2)).search(eq(ClientContact.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(ClientContact Search): name='Data Loader', fields: [id]"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(500 - 600)"));
        Assert.assertEquals(600, list.size());
    }

    @Test
    public void testSearchForListMultipleCallsPartialReturn() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.search(eq(ClientContact.class), any(), any(), any())).thenReturn(
            TestUtils.getListWrapper(ClientContact.class, 0, 600, IntStream.rangeClosed(1, 200).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 200, 600, IntStream.rangeClosed(201, 400).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 400, 600, IntStream.rangeClosed(401, 600).toArray()));
        List<ClientContact> list = restApi.searchForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.searchParams());
        verify(bullhornDataMock, times(3)).search(eq(ClientContact.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(ClientContact Search): name='Data Loader', fields: [id]"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(200 - 600)"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(400 - 600)"));
        Assert.assertEquals(600, list.size());
    }

    @Test
    public void testSearchForListMultipleCallsEmptyReturn() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.search(eq(ClientContact.class), any(), any(), any())).thenReturn(
            TestUtils.getListWrapper(ClientContact.class, 0, 600, IntStream.rangeClosed(1, 200).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 200, 600, IntStream.rangeClosed(201, 400).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 400, 600));
        List<ClientContact> list = restApi.searchForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.searchParams());
        verify(bullhornDataMock, times(3)).search(eq(ClientContact.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(ClientContact Search): name='Data Loader', fields: [id]"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(200 - 600)"));
        Assert.assertEquals(400, list.size());
    }

    @Test
    public void testQueryForList() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.query(eq(ClientContact.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(ClientContact.class, 0, 10, IntStream.rangeClosed(1, 10).toArray()));
        restApi.queryForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).query(eq(ClientContact.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(ClientContact Query): name='Data Loader', fields: [id]"));
    }

    @Test
    public void testQueryForListNullStartValue() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.query(eq(JobSubmissionHistory.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(JobSubmissionHistory.class, null, 1, IntStream.rangeClosed(1, 10).toArray()));
        restApi.queryForList(JobSubmissionHistory.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).query(eq(JobSubmissionHistory.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(JobSubmissionHistory Query): name='Data Loader', fields: [id]"));
    }

    @Test
    public void testQueryForListNullTotalValue() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.query(eq(JobSubmissionHistory.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(JobSubmissionHistory.class, 0, null, IntStream.rangeClosed(1, 10).toArray()));
        restApi.queryForList(JobSubmissionHistory.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).query(eq(JobSubmissionHistory.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(JobSubmissionHistory Query): name='Data Loader', fields: [id]"));
    }

    @Test
    public void testQueryForListNullStartAndTotalValue() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.query(eq(JobSubmissionHistory.class), any(), any(), any())).
            thenReturn(TestUtils.getListWrapper(JobSubmissionHistory.class, null, null, IntStream.rangeClosed(1, 10).toArray()));
        restApi.queryForList(JobSubmissionHistory.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).query(eq(JobSubmissionHistory.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(JobSubmissionHistory Query): name='Data Loader', fields: [id]"));
    }

    @Test
    public void testQueryForListMultipleCalls() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.query(eq(ClientContact.class), any(), any(), any())).thenReturn(
            TestUtils.getListWrapper(ClientContact.class, 0, 600, IntStream.rangeClosed(1, 500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 500, 600, IntStream.rangeClosed(501, 600).toArray()));
        List<ClientContact> list = restApi.queryForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(2)).query(eq(ClientContact.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(ClientContact Query): name='Data Loader', fields: [id]"));
        Assert.assertEquals(600, list.size());
    }

    /**
     * A test to ensure that the Data Loader stops making new requests to get more data if there are more than 20,000 matching records.
     * In this test, there are 25,000 records (50 calls) that can be made, but only 20,000 records (40 calls) should be made.
     */
    @Test
    public void testQueryForListMaximumReturnSize() throws InstantiationException, IllegalAccessException {
        when(bullhornDataMock.query(eq(ClientContact.class), any(), any(), any())).thenReturn(
            TestUtils.getListWrapper(ClientContact.class, 0, 25000, IntStream.rangeClosed(1, 500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 500, 25000, IntStream.rangeClosed(501, 1000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 1000, 25000, IntStream.rangeClosed(1001, 1500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 1500, 25000, IntStream.rangeClosed(1501, 2000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 2000, 25000, IntStream.rangeClosed(2001, 2500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 2500, 25000, IntStream.rangeClosed(2501, 3000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 3000, 25000, IntStream.rangeClosed(3001, 3500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 3500, 25000, IntStream.rangeClosed(3501, 4000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 4000, 25000, IntStream.rangeClosed(4001, 4500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 4500, 25000, IntStream.rangeClosed(4501, 5000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 5000, 25000, IntStream.rangeClosed(5001, 5500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 5500, 25000, IntStream.rangeClosed(5501, 6000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 6000, 25000, IntStream.rangeClosed(6001, 6500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 6500, 25000, IntStream.rangeClosed(6501, 7000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 7000, 25000, IntStream.rangeClosed(7001, 7500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 7500, 25000, IntStream.rangeClosed(7501, 8000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 8000, 25000, IntStream.rangeClosed(8001, 8500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 8500, 25000, IntStream.rangeClosed(8501, 9000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 9000, 25000, IntStream.rangeClosed(9001, 9500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 9500, 25000, IntStream.rangeClosed(9501, 10000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 10000, 25000, IntStream.rangeClosed(10001, 10500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 10500, 25000, IntStream.rangeClosed(10501, 11000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 11000, 25000, IntStream.rangeClosed(11001, 11500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 11500, 25000, IntStream.rangeClosed(11501, 12000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 12000, 25000, IntStream.rangeClosed(12001, 12500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 12500, 25000, IntStream.rangeClosed(12501, 13000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 13000, 25000, IntStream.rangeClosed(13001, 13500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 13500, 25000, IntStream.rangeClosed(13501, 14000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 14000, 25000, IntStream.rangeClosed(14001, 14500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 14500, 25000, IntStream.rangeClosed(14501, 15000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 15000, 25000, IntStream.rangeClosed(15001, 15500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 15500, 25000, IntStream.rangeClosed(15501, 16000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 16000, 25000, IntStream.rangeClosed(16001, 16500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 16500, 25000, IntStream.rangeClosed(16501, 17000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 17000, 25000, IntStream.rangeClosed(17001, 17500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 17500, 25000, IntStream.rangeClosed(17501, 18000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 18000, 25000, IntStream.rangeClosed(18001, 18500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 18500, 25000, IntStream.rangeClosed(18501, 19000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 19000, 25000, IntStream.rangeClosed(19001, 19500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 19500, 25000, IntStream.rangeClosed(19501, 20000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 20000, 25000, IntStream.rangeClosed(20001, 20000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 20500, 25000, IntStream.rangeClosed(20501, 21000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 21000, 25000, IntStream.rangeClosed(21001, 21500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 21500, 25000, IntStream.rangeClosed(21501, 22000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 22000, 25000, IntStream.rangeClosed(22001, 22500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 22500, 25000, IntStream.rangeClosed(22501, 23000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 23000, 25000, IntStream.rangeClosed(23001, 23500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 23500, 25000, IntStream.rangeClosed(23501, 24000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 24000, 25000, IntStream.rangeClosed(24001, 24500).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 24500, 25000, IntStream.rangeClosed(24501, 25000).toArray()),
            TestUtils.getListWrapper(ClientContact.class, 25000, 25000, IntStream.rangeClosed(25001, 25500).toArray()));

        List<ClientContact> list = restApi.queryForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.queryParams());

        verify(bullhornDataMock, times(40)).query(eq(ClientContact.class), eq("name='Data Loader'"), eq(Sets.newHashSet("id")), any());
        verify(printUtilMock, times(1)).log(any(), eq("Find(ClientContact Query): name='Data Loader', fields: [id]"));
        verify(printUtilMock, never()).log(any(), eq("--> Follow On Find(0 - 500)"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(500 - 1000)"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(1000 - 1500)"));
        verify(printUtilMock, times(1)).log(any(), eq("--> Follow On Find(19500 - 20000)"));
        verify(printUtilMock, never()).log(any(), eq("--> Follow On Find(20000 - 20500)"));
        verify(printUtilMock, never()).log(any(), eq("--> Follow On Find(20500 - 21000)"));
        Assert.assertEquals(20000, list.size());
    }

    @Test
    public void testInsertEntity() {
        Candidate candidate = new Candidate();

        CrudResponse crudResponse = restApi.insertEntity(candidate);

        verify(bullhornDataMock, times(1)).insertEntity(eq(candidate));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(eq(crudResponse));
        verify(printUtilMock, times(1)).log(any(), eq("Insert(Candidate)"));
    }

    @Test
    public void testUpdateEntity() {
        Candidate candidate = new Candidate(123);

        CrudResponse crudResponse = restApi.updateEntity(candidate);

        verify(bullhornDataMock, times(1)).updateEntity(eq(candidate));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(eq(crudResponse));
        verify(printUtilMock, times(1)).log(any(), eq("Update(Candidate): #123"));
    }

    @Test
    public void testDeleteEntity() {
        CrudResponse crudResponse = restApi.deleteEntity(Candidate.class, 1);

        verify(bullhornDataMock, times(1)).deleteEntity(eq(Candidate.class), eq(1));
        verify(restApiExtensionMock, times(1)).postDelete(eq(restApi), eq(crudResponse));
        verify(restApiExtensionMock, times(2)).checkForRestSdkErrorMessages(eq(crudResponse));
    }

    @Test
    public void testGetAllAssociationsList() {
        Set<Integer> entityIDs = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<String> fields = new HashSet<>(Arrays.asList("id", "name"));

        restApi.getAllAssociationsList(Candidate.class, entityIDs, CandidateAssociations.getInstance().primarySkills(),
            fields, ParamFactory.associationParams());

        verify(bullhornDataMock, times(1)).getAllAssociations(eq(Candidate.class),
            eq(entityIDs), eq(CandidateAssociations.getInstance().primarySkills()), eq(fields), any());
        verify(printUtilMock, times(1)).log(any(), eq("FindAssociations(Candidate): #[1, 2, 3] - primarySkills, fields: [id, name]"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAssociateWithEntity() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> associationIds = Arrays.asList(1, 2, 3);
        Set<Integer> associationSet = new HashSet(associationIds);

        List<CrudResponse> crudResponses = restApi.associateWithEntity(Candidate.class, 1, categoriesAssociationField, associationIds);

        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(associationSet));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAssociateWithEntity501() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> idList = new ArrayList();
        for (int i = 1; i < 502; ++i) {
            idList.add(i);
        }
        Set<Integer> firstCall = new HashSet(idList.subList(0, 500));
        Set<Integer> secondCall = new HashSet(idList.subList(500, 501));

        List<CrudResponse> crudResponses = restApi.associateWithEntity(Candidate.class, 1, categoriesAssociationField, idList);

        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(firstCall));
        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(secondCall));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAssociateWithEntity1000() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> idList = new ArrayList();
        for (int i = 1; i < 1001; ++i) {
            idList.add(i);
        }
        Set<Integer> firstCall = new HashSet(idList.subList(0, 500));
        Set<Integer> secondCall = new HashSet(idList.subList(500, 1000));

        List<CrudResponse> crudResponses = restApi.associateWithEntity(Candidate.class, 1, categoriesAssociationField, idList);

        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(firstCall));
        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(secondCall));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAssociateWithEntity1001() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> idList = new ArrayList();
        for (int i = 1; i < 1002; ++i) {
            idList.add(i);
        }
        Set<Integer> firstCall = new HashSet(idList.subList(0, 500));
        Set<Integer> secondCall = new HashSet(idList.subList(500, 1000));
        Set<Integer> thirdCall = new HashSet(idList.subList(1000, 1001));

        List<CrudResponse> crudResponses = restApi.associateWithEntity(Candidate.class, 1, categoriesAssociationField, idList);

        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(firstCall));
        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(secondCall));
        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(thirdCall));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(1));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDisassociateWithEntity() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> associationIds = Arrays.asList(1, 2, 3);
        Set<Integer> associationSet = new HashSet(associationIds);

        List<CrudResponse> crudResponses = restApi.disassociateWithEntity(Candidate.class, 1, categoriesAssociationField, associationIds);

        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(associationSet));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDisassociateWithEntity501() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> idList = new ArrayList();
        for (int i = 1; i < 502; ++i) {
            idList.add(i);
        }
        Set<Integer> firstCall = new HashSet(idList.subList(0, 500));
        Set<Integer> secondCall = new HashSet(idList.subList(500, 501));

        List<CrudResponse> crudResponses = restApi.disassociateWithEntity(Candidate.class, 1, categoriesAssociationField, idList);

        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(firstCall));
        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(secondCall));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDisassociateWithEntity1000() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> idList = new ArrayList();
        for (int i = 1; i < 1001; ++i) {
            idList.add(i);
        }
        Set<Integer> firstCall = new HashSet(idList.subList(0, 500));
        Set<Integer> secondCall = new HashSet(idList.subList(500, 1000));

        List<CrudResponse> crudResponses = restApi.disassociateWithEntity(Candidate.class, 1, categoriesAssociationField, idList);

        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(firstCall));
        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(secondCall));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDisassociateWithEntity1001() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        List<Integer> idList = new ArrayList();
        for (int i = 1; i < 1002; ++i) {
            idList.add(i);
        }
        Set<Integer> firstCall = new HashSet(idList.subList(0, 500));
        Set<Integer> secondCall = new HashSet(idList.subList(500, 1000));
        Set<Integer> thirdCall = new HashSet(idList.subList(1000, 1001));

        List<CrudResponse> crudResponses = restApi.disassociateWithEntity(Candidate.class, 1, categoriesAssociationField, idList);

        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(firstCall));
        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(secondCall));
        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(thirdCall));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(0));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(1));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(crudResponses.get(2));
    }

    @Test
    public void testGetFileMetaData() {
        restApi.getFileMetaData(Candidate.class, 1);
        verify(bullhornDataMock, times(1)).getFileMetaData(eq(Candidate.class), eq(1));
    }

    @Test
    public void testGetFileContent() {
        restApi.getFileContent(Candidate.class, 1, 1);
        verify(bullhornDataMock, times(1)).getFileContent(eq(Candidate.class), eq(1), eq(1));
    }

    @Test
    public void testAddFile() {
        FileMeta fileMeta = new StandardFileMeta();
        restApi.addFile(Candidate.class, 1, fileMeta);
        verify(bullhornDataMock, times(1)).addFile(eq(Candidate.class), eq(1), eq(fileMeta));
    }

    @Test
    public void testUpdateFile() {
        FileMeta fileMeta = new StandardFileMeta();
        restApi.updateFile(Candidate.class, 1, fileMeta);
        verify(bullhornDataMock, times(1)).updateFile(eq(Candidate.class), eq(1), eq(fileMeta));
    }

    @Test
    public void testDeleteFile() {
        restApi.deleteFile(Candidate.class, 1, 1);
        verify(bullhornDataMock, times(1)).deleteFile(eq(Candidate.class), eq(1), eq(1));
    }

    @Test
    public void testPerformGetRequest() {
        Map<String, String> map = new HashMap<>();
        restApi.performGetRequest("url", Candidate.class, map);
        verify(bullhornDataMock, times(1)).performGetRequest(eq("url"), eq(Candidate.class), eq(map));
    }
}
