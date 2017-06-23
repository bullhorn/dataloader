package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.enums.EntityInfo;
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

public class PreloaderTest {

    private RestApi restApiMock;
    private Preloader preloader;

    @Before
    public void setup() throws IOException, InterruptedException {
        restApiMock = Mockito.mock(RestApi.class);
        RestSession restSessionMock = Mockito.mock(RestSession.class);

        CountryListWrapper countryListWrapper = new CountryListWrapper();
        List<Country> countryList = new ArrayList<>();
        Country usa = new Country();
        usa.setId(1);
        usa.setName("USA");
        countryList.add(usa);
        countryListWrapper.setData(countryList);

        preloader = new Preloader(restSessionMock);

        Mockito.when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        Mockito.when(restApiMock.queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(countryListWrapper);
    }

    @Test
    public void preloadCandidateWorkHistory() throws IOException, InterruptedException {
        preloader.preload(EntityInfo.CANDIDATE_WORK_HISTORY);
        Mockito.verify(restApiMock, Mockito.never()).queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void preloadCandidate() throws IOException, InterruptedException {
        preloader.preload(EntityInfo.CANDIDATE);
        Mockito.verify(restApiMock, Mockito.times(1)).queryForAllRecords(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void getCountryNameToIdMapTest() throws IOException, InterruptedException {
        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("USA", 1);

        Map<String, Integer> actualMap = preloader.getCountryNameToIdMap();

        Assert.assertThat(actualMap, new ReflectionEquals(expectedMap));
    }
}
