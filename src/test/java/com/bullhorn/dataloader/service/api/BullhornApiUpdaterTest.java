package com.bullhorn.dataloader.service.api;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.query.EntityQuery;

import junit.framework.TestCase;

public class BullhornApiUpdaterTest {
    @Test
    public void testMerge_NoIdReturnedFromRest_IdWasInCsv() throws IOException {
        // arrange
        BullhornAPI bhapi = getBullhornAPI();

        JSONObject jsonObject = new JSONObject();
        EntityQuery entityQuery = new EntityQuery("ClientContact", jsonObject);
        entityQuery.addInt("id", "123");

        // act
        BullhornApiUpdater bullhornApiUpdater = new BullhornApiUpdater(bhapi);
        Optional<Integer> optional = bullhornApiUpdater.merge(entityQuery);

        // assert
        TestCase.assertFalse(optional.isPresent());
    }

    private BullhornAPI getBullhornAPI() throws IOException {
        BullhornAPI bhapi = Mockito.mock(BullhornAPI.class);
        when(bhapi.get(any())).thenReturn(new JSONObject("{\"count\":0}"));
        when(bhapi.getLabelByName(any())).thenReturn(Optional.of("CliCon"));
        when(bhapi.serialize(any())).thenReturn("");
        return bhapi;
    }
}
