package com.bullhorn.dataloader.service.api;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.query.EntityQuery;

public class BullhornApiUpdaterTest {

    @Test
    public void testMerge_NoIdReturnedFromRest_IdWasInCsv() throws IOException {
        final BullhornAPI bhapi = getBullhornAPI();
        final JSONObject jsonObject = new JSONObject();
        final EntityQuery entityQuery = new EntityQuery("ClientContact", jsonObject);
        entityQuery.addInt("id", "123");

        final BullhornApiUpdater bullhornApiUpdater = new BullhornApiUpdater(bhapi);
        final Result result = bullhornApiUpdater.merge(entityQuery);

        Assert.assertFalse(result.isSuccess());
    }

    private BullhornAPI getBullhornAPI() throws IOException {
        final BullhornAPI bhapi = Mockito.mock(BullhornAPI.class);
        when(bhapi.call(any())).thenReturn(new JSONObject("{\"count\":0}"));
        when(bhapi.getLabelByName(any())).thenReturn(Optional.of("CliCon"));
        when(bhapi.serialize(any())).thenReturn("");
        return bhapi;
    }
}
