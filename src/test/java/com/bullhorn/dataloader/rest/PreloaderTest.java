package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.response.list.CountryListWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreloaderTest {

    private RestApi restApiMock;
    private Preloader preloader;

    @Before
    public void setup() throws IOException, InterruptedException {
        restApiMock = mock(RestApi.class);
        RestSession restSessionMock = mock(RestSession.class);

        List<Country> countryList = new ArrayList<>();
        Country usa = new Country();
        usa.setId(1);
        usa.setName("USA");
        countryList.add(usa);

        preloader = new Preloader(restSessionMock);

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.queryForAllRecordsList(eq(Country.class), any(), any(), any())).thenReturn(countryList);
    }

    @Test
    public void preloadCandidateWorkHistory() throws IOException, InterruptedException {
        preloader.preload(EntityInfo.CANDIDATE_WORK_HISTORY);
        verify(restApiMock, never()).queryForAllRecordsList(any(), any(), any(), any());
    }

    @Test
    public void preloadCandidate() throws IOException, InterruptedException {
        preloader.preload(EntityInfo.CANDIDATE);
        verify(restApiMock, times(1)).queryForAllRecordsList(any(), any(), any(), any());
    }

    @Test
    public void getCountryNameToIdMapTest() throws IOException, InterruptedException {
        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("USA", 1);

        Map<String, Integer> actualMap = preloader.getCountryNameToIdMap();

        Assert.assertThat(actualMap, new ReflectionEquals(expectedMap));
    }
}
