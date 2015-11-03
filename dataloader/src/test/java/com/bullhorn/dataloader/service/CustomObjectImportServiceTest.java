package com.bullhorn.dataloader.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.bullhorn.dataloader.domain.CustomObject;
import com.bullhorn.dataloader.service.util.ImportServiceUtils;
import com.bullhorn.dataloader.util.BullhornAPI;

public class CustomObjectImportServiceTest {

    @Test
    public void testCustomObject() throws Exception {
        // setup
        ImportServiceUtils importServiceUtils = new ImportServiceUtils();
        mockCustomObject(importServiceUtils);
        CustomObjectImportService customObjectImportService = new CustomObjectImportService();
        importServiceUtils.injectFakeDependencies(customObjectImportService);
        customObjectImportService.setObj(getCustomObject());

        // execution
        customObjectImportService.customObject();

        // verification
        verifyCustomObject(importServiceUtils);
    }

    private void mockCustomObject(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        when(bullhornAPI.getPostURL(any(CustomObject.class)))
                .thenReturn(new String[]{"put", "url"});
        when(bullhornAPI.serialize(any(CustomObject.class)))
                .thenReturn("{}");

    }

    private void verifyCustomObject(ImportServiceUtils importServiceUtils) throws Exception {
        BullhornAPI bullhornAPI = importServiceUtils.getBhAPI();
        verify(bullhornAPI).save("{\"nulls\":[{}]}", "nullentity/null/null?BhRestToken=null", "post");
    }

    public Object getCustomObject() {
        return new CustomObject();
    }
}