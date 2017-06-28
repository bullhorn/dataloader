package com.bullhorn.dataloader.rest;

import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RestApiTest {
    private BullhornData bullhornDataMock;
    private RestApiExtension restApiExtensionMock;
    private RestApi restApi;

    @Before
    public void setup() {
        bullhornDataMock = mock(BullhornData.class);
        restApiExtensionMock = mock(RestApiExtension.class);
        restApi = new RestApi(bullhornDataMock, restApiExtensionMock);
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
    public void testSearch() {
        restApi.search(Candidate.class, "name:\"Data Loader\"", null, ParamFactory.searchParams());
        verify(bullhornDataMock, times(1)).search(eq(Candidate.class), eq("name:\"Data Loader\""), eq(null), any());
    }

    @Test
    public void testQuery() {
        restApi.query(ClientContact.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).query(eq(ClientContact.class), eq("name='Data Loader'"), eq(null), any());
    }

    @Test
    public void testQueryForList() {
        restApi.queryForList(ClientContact.class, "name='Data Loader'", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).queryForList(eq(ClientContact.class), eq("name='Data Loader'"), eq(null), any());
    }

    @Test
    public void testQueryForAllRecords() {
        restApi.queryForAllRecords(Country.class, "id IS NOT nulls", null, ParamFactory.queryParams());
        verify(bullhornDataMock, times(1)).queryForAllRecords(eq(Country.class), eq("id IS NOT nulls"), eq(null), any());
    }

    @Test
    public void testInsertEntity() {
        Candidate candidate = new Candidate();

        CrudResponse crudResponse = restApi.insertEntity(candidate);

        verify(bullhornDataMock, times(1)).insertEntity(eq(candidate));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(eq(crudResponse));
    }

    @Test
    public void testUpdateEntity() {
        Candidate candidate = new Candidate();

        CrudResponse crudResponse = restApi.updateEntity(candidate);

        verify(bullhornDataMock, times(1)).updateEntity(eq(candidate));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(eq(crudResponse));
    }

    @Test
    public void testDeleteEntity() {
        CrudResponse crudResponse = restApi.deleteEntity(Candidate.class, 1);

        verify(bullhornDataMock, times(1)).deleteEntity(eq(Candidate.class), eq(1));
        verify(restApiExtensionMock, times(1)).postDelete(eq(restApi), eq(crudResponse));
        verify(restApiExtensionMock, times(2)).checkForRestSdkErrorMessages(eq(crudResponse));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAssociateWithEntity() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        Set<Integer> associationIds = new HashSet<>(Arrays.asList(1, 2, 3));

        CrudResponse crudResponse = restApi.associateWithEntity(Candidate.class, 1, categoriesAssociationField, associationIds);

        verify(bullhornDataMock, times(1)).associateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(associationIds));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(eq(crudResponse));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDisassociateWithEntity() {
        AssociationField categoriesAssociationField = AssociationFactory.candidateAssociations().getAssociation("categories");
        Set<Integer> associationIds = new HashSet<>(Arrays.asList(1, 2, 3));

        CrudResponse crudResponse = restApi.disassociateWithEntity(Candidate.class, 1, categoriesAssociationField, associationIds);

        verify(bullhornDataMock, times(1)).disassociateWithEntity(eq(Candidate.class), eq(1), eq(categoriesAssociationField), eq(associationIds));
        verify(restApiExtensionMock, times(1)).checkForRestSdkErrorMessages(eq(crudResponse));
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
}
