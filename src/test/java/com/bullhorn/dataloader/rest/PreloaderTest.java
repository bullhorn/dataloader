package com.bullhorn.dataloader.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.model.entity.core.standard.Country;

public class PreloaderTest {

    private RestApi restApiMock;
    private PrintUtil printUtilMock;
    private Preloader preloader;

    @Before
    public void setup() throws IOException {
        RestSession restSessionMock = mock(RestSession.class);
        restApiMock = mock(RestApi.class);
        printUtilMock = mock(PrintUtil.class);
        preloader = new Preloader(restSessionMock, printUtilMock);

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.queryForList(eq(Country.class), any(), any(), any()))
            .thenReturn(TestUtils.createCountryList("United States,Canada", "1,2"));
    }

    @Test
    public void testConvertRowSuccess() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryName",
            "St. Louis,MO,United States");
        Row expectedRow = TestUtils.createRow("address.city,address.state,address.countryID",
            "St. Louis,MO,1");

        Row convertedRow = preloader.convertRow(row);

        for (int i = 0; i < expectedRow.getCells().size(); ++i) {
            Assert.assertTrue(new ReflectionEquals(expectedRow.getCells().get(i)).matches(convertedRow.getCells().get(i)));
        }
        verify(restApiMock, times(1)).queryForList(any(), any(), any(), any());
    }

    @Test
    public void testConvertRowCaseInsensitive() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryName",
            "St. Louis,MO,UNITED STATES");
        Row expectedRow = TestUtils.createRow("address.city,address.state,address.countryID",
            "St. Louis,MO,1");

        Row convertedRow = preloader.convertRow(row);

        for (int i = 0; i < expectedRow.getCells().size(); ++i) {
            Assert.assertTrue(new ReflectionEquals(expectedRow.getCells().get(i)).matches(convertedRow.getCells().get(i)));
        }
        verify(restApiMock, times(1)).queryForList(any(), any(), any(), any());
    }

    @Test
    public void testConvertRowInvalid() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryName",
            "St. Louis,MO,Nowhere");
        Row expectedRow = TestUtils.createRow("address.city,address.state,address.countryID",
            "St. Louis,MO,Nowhere");

        Row convertedRow = preloader.convertRow(row);

        for (int i = 0; i < expectedRow.getCells().size(); ++i) {
            Assert.assertTrue(new ReflectionEquals(expectedRow.getCells().get(i)).matches(convertedRow.getCells().get(i)));
        }
        verify(restApiMock, times(1)).queryForList(any(), any(), any(), any());
    }

    @Test
    public void testCreateRowNoOp() throws IOException {
        Row row = TestUtils.createRow("address.city,address.state,address.countryID", "St. Louis, MO, 1");

        Row convertedRow = preloader.convertRow(row);

        Assert.assertTrue(new ReflectionEquals(convertedRow).matches(row));
        verify(restApiMock, never()).queryForList(any(), any(), any(), any());
    }

    @Test
    public void testConvertRowAddingNameCell() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName,address.city,address.state,address.countryName",
            "John,Schmidt,St. Louis,MO,United States");
        Row expectedRow = TestUtils.createRow("firstName,lastName,address.city,address.state,address.countryID,name",
            "John,Schmidt,St. Louis,MO,1,John Schmidt");

        Row convertedRow = preloader.convertRow(row);

        for (int i = 0; i < expectedRow.getCells().size(); ++i) {
            Assert.assertTrue(new ReflectionEquals(expectedRow.getCells().get(i)).matches(convertedRow.getCells().get(i)));
        }
        verify(restApiMock, times(1)).queryForList(any(), any(), any(), any());
        verify(printUtilMock, times(1)).printAndLog(
            "Added name field as '<firstName> <lastName>' since both firstName and lastName were provided but name was not.");
    }

    @Test
    public void testConvertRowNotAddingNameCell() throws IOException {
        Row row = TestUtils.createRow("firstName", "John");

        Row convertedRow = preloader.convertRow(row);

        Assert.assertTrue(new ReflectionEquals(convertedRow).matches(row));
        verify(restApiMock, never()).queryForList(any(), any(), any(), any());
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testConvertRowNotAddingNameCell2() throws IOException {
        Row row = TestUtils.createRow("firstName,lastName,name", "John,Schmidt,John Jacob Jingleheimer Schmidt");

        Row convertedRow = preloader.convertRow(row);

        Assert.assertTrue(new ReflectionEquals(convertedRow).matches(row));
        verify(restApiMock, never()).queryForList(any(), any(), any(), any());
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testConvertRowNotAddingNameCell3() throws IOException {
        Row row = TestUtils.createRow("firstName,middleName", "John,Jacob");

        Row convertedRow = preloader.convertRow(row);

        Assert.assertTrue(new ReflectionEquals(convertedRow).matches(row));
        verify(restApiMock, never()).queryForList(any(), any(), any(), any());
        verify(printUtilMock, never()).printAndLog(anyString());
    }
}
