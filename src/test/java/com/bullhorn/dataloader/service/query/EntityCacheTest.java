package com.bullhorn.dataloader.service.query;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.api.BullhornApiUpdater;
import com.bullhorn.dataloader.service.csv.Result;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class EntityCacheTest {

    private LoadingCache<EntityQuery, Result> associationCache;
    private BullhornApiUpdater bullhornApiUpdater;

    @Before
    public void setUp() throws Exception {
        bullhornApiUpdater = Mockito.mock(BullhornApiUpdater.class);

        this.associationCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build(new EntityCache(bullhornApiUpdater));
    }

    @Test
    public void testLoad_NoRecordsReturned() throws Exception {
        final EntityQuery entityQuery = new EntityQuery("Candidate", null);
        final JSONObject jsonObject = new JSONObject("{count:0, data:[]}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject);
        when(bullhornApiUpdater.merge(any())).thenReturn(Result.Insert(42));

        final Result actualResult = associationCache.get(entityQuery);

        Assert.assertEquals(Result.Insert(42), actualResult);
    }

    @Test
    public void testLoad_OneRecordNoFilterFields() throws Exception {
        final EntityQuery entityQuery = new EntityQuery("Candidate", null);
        final JSONObject jsonObject = new JSONObject("{count:1, data:[{id: 1}]}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject);
        when(bullhornApiUpdater.merge(any())).thenReturn(Result.Insert(42));

        final Result actualResult = associationCache.get(entityQuery);

        Assert.assertEquals(Result.Insert(42), actualResult);
    }

    @Test
    public void testLoad_OneRecordWithFilterField() throws Exception {
        final EntityQuery entityQuery = new EntityQuery("Candidate", null);
        entityQuery.addInt("int1", "1");
        final JSONObject jsonObject = new JSONObject("{count:1, data:[{id: 99}]}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject);

        final Result actualResult = associationCache.get(entityQuery);

        Assert.assertEquals(Result.Update(99), actualResult);
    }

    @Test
    public void testLoad_MultipleRecordsReturned() throws Exception {
        final EntityQuery entityQuery = new EntityQuery("Candidate", null);
        entityQuery.addInt("int1", "1");
        final JSONObject jsonObject = new JSONObject("{count:2, data:[{id:1},{id:2}]}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject);

        final Result actualResult = associationCache.get(entityQuery);

        final String expected = "ERROR: Association returned more than 1 result: EntityQuery{entity='Candidate', filterFields={int1=1}, nestedJson=null, id=Optional.empty}";
        Assert.assertEquals(Result.Failure(expected), actualResult);
    }

    @Test
    public void testLoad_CountMissing() throws Exception {
        final EntityQuery entityQuery = new EntityQuery("Candidate", null);
        entityQuery.addInt("int1", "1");
        final JSONObject jsonObject = new JSONObject("{}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject);

        final Result actualResult = associationCache.get(entityQuery);

        final String expected = "ERROR: JSON response is missing \"count\" field. Received: {}";
        Assert.assertEquals(Result.Failure(expected), actualResult);
    }

    @Test
    public void testLoad_DuplicateInsert() throws Exception {
        when(bullhornApiUpdater.getCall(any())).thenReturn(new JSONObject("{count:1, data:[]}"));
        when(bullhornApiUpdater.merge(any())).thenReturn(Result.Insert(42));
        Assert.assertEquals(0, associationCache.size());

        final EntityQuery entityQuery1 = new EntityQuery("Candidate", null);
        Result actualResult1 = associationCache.get(entityQuery1);
        Assert.assertEquals(Result.Insert(42), actualResult1);
        Assert.assertEquals(1, associationCache.size());

        final EntityQuery entityQuery2 = new EntityQuery("Candidate", null);
        Result actualResult2 = associationCache.get(entityQuery2);
        Assert.assertEquals(Result.Insert(42), actualResult2);
        Assert.assertEquals(1, associationCache.size());

        final EntityQuery entityQuery3 = new EntityQuery("Candidate", "{}");
        Result actualResult3 = associationCache.get(entityQuery3);
        Assert.assertEquals(Result.Insert(42), actualResult3);
        Assert.assertEquals(2, associationCache.size());
    }

    @Test
    public void testLoad_DuplicateUpdate() throws Exception {
        final JSONObject jsonObject1 = new JSONObject("{count:1, data:[{id:123}]}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject1);
        Assert.assertEquals(0, associationCache.size());

        final EntityQuery entityQuery1 = new EntityQuery("Candidate", null);
        entityQuery1.addInt("int1", "1");
        final Result actualResult1 = associationCache.get(entityQuery1);
        Assert.assertEquals(Result.Update(123), actualResult1);
        Assert.assertEquals(1, associationCache.size());

        final EntityQuery entityQuery2 = new EntityQuery("Candidate", null);
        entityQuery2.addInt("int1", "1");
        final Result actualResult2 = associationCache.get(entityQuery2);
        Assert.assertEquals(Result.Update(123), actualResult2);
        Assert.assertEquals(1, associationCache.size());

        final JSONObject jsonObject2 = new JSONObject("{count:1, data:[{id:456}]}");
        when(bullhornApiUpdater.getCall(any())).thenReturn(jsonObject2);
        final EntityQuery entityQuery3 = new EntityQuery("Candidate", null);
        entityQuery3.addInt("int1", "2");
        final Result actualResult3 = associationCache.get(entityQuery3);
        Assert.assertEquals(Result.Update(456), actualResult3);
        Assert.assertEquals(2, associationCache.size());
    }
}
