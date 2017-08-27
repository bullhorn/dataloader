package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.Row;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.io.IOException;

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
    public void setup() throws IOException {
        RestSession restSessionMock = mock(RestSession.class);
        restApiMock = mock(RestApi.class);
        preloader = new Preloader(restSessionMock);

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.queryForAllRecordsList(eq(Country.class), any(), any(), any()))
            .thenReturn(TestUtils.createCountryList("United States,Canada", "1,2"));
    }

    @Test
    public void convertRowSuccess() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryName",
            "St. Louis,MO,United States");
        Row expectedRow = TestUtils.createRow("address.city,address.state,address.countryID",
            "St. Louis,MO,1");

        Row convertedRow = preloader.convertRow(row);

        for (int i = 0; i < expectedRow.getCells().size(); ++i) {
            Assert.assertThat(convertedRow.getCells().get(i), new ReflectionEquals(expectedRow.getCells().get(i)));
        }
        verify(restApiMock, times(1)).queryForAllRecordsList(any(), any(), any(), any());
    }

    @Test
    public void convertRowInvalid() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryName",
            "St. Louis,MO,Nowhere");
        Row expectedRow = TestUtils.createRow("address.city,address.state,address.countryID",
            "St. Louis,MO,Nowhere");

        Row convertedRow = preloader.convertRow(row);

        for (int i = 0; i < expectedRow.getCells().size(); ++i) {
            Assert.assertThat(convertedRow.getCells().get(i), new ReflectionEquals(expectedRow.getCells().get(i)));
        }
        verify(restApiMock, times(1)).queryForAllRecordsList(any(), any(), any(), any());
    }

    @Test
    public void createRowNoOp() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryID", "St. Louis, MO, 1");

        Row convertedRow = preloader.convertRow(row);

        Assert.assertThat(convertedRow, new ReflectionEquals(row));
        verify(restApiMock, never()).queryForAllRecordsList(any(), any(), any(), any());
    }
}
