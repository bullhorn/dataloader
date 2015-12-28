package com.bullhorn.dataloader.service.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

public class BullhornAPITest {

    private Properties properties;
    private Set seenFlag;

    @Before
    public void setUp() throws Exception {
        this.properties = getMockProperties();
        this.seenFlag = Mockito.mock(Set.class);
    }

    static Properties getMockProperties() {
        Properties properties = Mockito.mock(Properties.class);
        when(properties.getProperty("dateFormat")).thenReturn("MM/dd/yyyy");
        when(properties.getProperty("cacheSize")).thenReturn("10");
        when(properties.getProperty("numThreads")).thenReturn("15");
        when(properties.getProperty("frontLoadedEntities")).thenReturn("a,b");
        when(properties.getProperty("listDelimiter")).thenReturn(";;");
        when(properties.getProperty("pageSize")).thenReturn("10");
        return properties;
    }

    @Test
    public void testExistsFields() throws Exception {
        //arrange
        String testExistField = "candidateExistField";
        String testFilter = "testFilter";
        when(properties.stringPropertyNames()).thenReturn(Sets.newSet(testExistField));
        when(properties.getProperty(testExistField)).thenReturn(testFilter);


        //act
        BullhornAPI bullhornAPI = new BullhornAPI(properties, seenFlag);

        //assert
        String testFilterActual = bullhornAPI.getEntityExistsFieldsProperty("Candidate").get();
        assertEquals(testFilterActual, testFilter);
    }
}