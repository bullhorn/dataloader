package com.bullhorn.dataloader.service.executor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.google.common.cache.LoadingCache;

import junit.framework.TestCase;

public class JsonServiceTest {

    private class SetupJsonService {
        private BullhornAPI bhapi;
        private JsonService jsonService;
        private CsvFileWriter fileWriter;
        private BullhornApiAssociator bhapiAssociator;
        private PropertyFileUtil propertyFileUtil;

        public BullhornApiAssociator getBhapiAssociator() {
            return bhapiAssociator;
        }

        public BullhornAPI getBhapi() {
            return bhapi;
        }

        public JsonService getJsonService() {
            return jsonService;
        }

        public SetupJsonService invoke() throws ExecutionException, IOException {
            LoadingCache<EntityQuery, Result> loadingCache = Mockito.mock(LoadingCache.class);
            JsonRow jsonRow = new JsonRow();
            jsonRow.addDeferredAction(
                    new String[] {"categories", "id"},
                    new ArrayList<Integer>() {{ add(1); add(2); }}
            );

            bhapi = Mockito.mock(BullhornAPI.class);
            bhapiAssociator = Mockito.mock(BullhornApiAssociator.class);
            propertyFileUtil = Mockito.mock(PropertyFileUtil.class);
            fileWriter = Mockito.mock(CsvFileWriter.class);

            when(bhapi.getLabelByName("Candidate")).thenReturn(Optional.of("Candidate"));
            when(bhapi.getLabelByName("categories")).thenReturn(Optional.of("Category"));
            when(propertyFileUtil.getEntityExistFields("Candidate")).thenReturn(Optional.ofNullable(Arrays.asList(new String[] {"id", "name"})));
            when(loadingCache.get(any(EntityQuery.class))).thenReturn(
                    Result.Insert(1), Result.Update(2), Result.Insert(3)
            );

            // when
            jsonService = new JsonService("Candidate", bhapi, bhapiAssociator, jsonRow, loadingCache, fileWriter, propertyFileUtil);
            return this;
        }
    }

    @Test
    public void testToManyAssociations_multipleValues() throws ExecutionException, IOException {
        // setup
        SetupJsonService setupJsonService = new SetupJsonService().invoke();

        JsonService jsonService = setupJsonService.getJsonService();
        BullhornApiAssociator bhapiAssociator = setupJsonService.getBhapiAssociator();
        BullhornAPI bhapi = setupJsonService.getBhapi();
        MetaMap metaMap = new MetaMap(new SimpleDateFormat("mm/dd/yyyy"), "|");
        metaMap.setFieldNameToDataType("id", "Integer");

        when(bhapi.call(any(GetMethod.class))).thenReturn(new JSONObject());
        when(bhapi.getMetaDataTypes(any())).thenReturn(metaMap);

        // act
        jsonService.run();

        // assert
        ArgumentCaptor<EntityInstance> actualParent = ArgumentCaptor.forClass(EntityInstance.class);
        ArgumentCaptor<EntityInstance> actualAssociation = ArgumentCaptor.forClass(EntityInstance.class);

        verify(bhapiAssociator).associate(actualParent.capture(), actualAssociation.capture());

        EntityInstance expectedParent = new EntityInstance("1", "Candidate");
        EntityInstance expectedAssociation = new EntityInstance("2,3", "categories");

        TestCase.assertEquals(expectedParent, actualParent.getValue());
        TestCase.assertEquals(expectedAssociation, actualAssociation.getValue());
    }
}
