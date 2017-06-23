package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.response.list.CountryListWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreloaderUtilTest {

    private BullhornRestApi bullhornRestApiMock;
    private PreloaderUtil preloaderUtil;

    @Before
    public void setup() throws IOException, InterruptedException {
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        ConnectionUtil connectionUtilMock = Mockito.mock(ConnectionUtil.class);

        preloaderUtil = new PreloaderUtil(connectionUtilMock);

        Mockito.when(connectionUtilMock.getSession()).thenReturn(bullhornRestApiMock);
    }

    @Test
    public void getCountryNameToIdMapTest() throws IOException, InterruptedException {
        CountryListWrapper countryListWrapper = new CountryListWrapper();
        List<Country> countryList = new ArrayList<>();
        Country usa = new Country();
        usa.setId(1);
        usa.setName("USA");
        countryList.add(usa);
        countryListWrapper.setData(countryList);

        Mockito.when(bullhornRestApiMock.queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(countryListWrapper);

        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("USA", 1);

        Map<String, Integer> actualMap = preloaderUtil.getCountryNameToIdMap();

        Assert.assertThat(actualMap, new ReflectionEquals(expectedMap));
    }
}
