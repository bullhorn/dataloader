package com.bullhorn.dataloader.service.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

public class BullhornAPITest {

    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = Mockito.mock(Properties.class);
    }

    @Test
    public void testExistsFields() throws Exception {
        //arrange
        String testExistField = "candidateExistField";
        String testFilter = "testFilter";
        when(properties.getProperty("dateFormat")).thenReturn("MM/dd/yyyy");
        when(properties.stringPropertyNames()).thenReturn(Sets.newSet(testExistField));
        when(properties.getProperty(testExistField)).thenReturn(testFilter);

        //act
        BullhornAPI bullhornAPI = new BullhornAPI(properties);

        //assert
        String testFilterActual = bullhornAPI.getEntityExistsFieldsProperty("Candidate");
        assertEquals(testFilterActual, testFilter);
    }
}