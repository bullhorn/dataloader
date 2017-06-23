package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
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

public class PreloadUtilTest {

    private BullhornRestApi bullhornRestApiMock;
    private PreloadUtil preloadUtil;

    @Before
    public void setup() throws IOException, InterruptedException {
        bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        ConnectionUtil connectionUtilMock = Mockito.mock(ConnectionUtil.class);

        CountryListWrapper countryListWrapper = new CountryListWrapper();
        List<Country> countryList = new ArrayList<>();
        Country usa = new Country();
        usa.setId(1);
        usa.setName("USA");
        countryList.add(usa);
        countryListWrapper.setData(countryList);

        preloadUtil = new PreloadUtil(connectionUtilMock);

        Mockito.when(connectionUtilMock.getSession()).thenReturn(bullhornRestApiMock);
        Mockito.when(bullhornRestApiMock.queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(countryListWrapper);
    }

    @Test
    public void preloadCandidateWorkHistory() throws IOException, InterruptedException {
        preloadUtil.preload(EntityInfo.CANDIDATE_WORK_HISTORY);
        Mockito.verify(bullhornRestApiMock, Mockito.never()).queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void preloadCandidate() throws IOException, InterruptedException {
        preloadUtil.preload(EntityInfo.CANDIDATE);
        Mockito.verify(bullhornRestApiMock, Mockito.times(1)).queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void getCountryNameToIdMapTest() throws IOException, InterruptedException {
        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("USA", 1);

        Map<String, Integer> actualMap = preloadUtil.getCountryNameToIdMap();

        Assert.assertThat(actualMap, new ReflectionEquals(expectedMap));
    }
}
