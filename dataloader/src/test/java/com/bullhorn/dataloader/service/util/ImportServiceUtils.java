package com.bullhorn.dataloader.service.util;

import static org.mockito.Mockito.mock;

import com.bullhorn.dataloader.service.AbstractEntityImportService;
import com.bullhorn.dataloader.service.MasterDataService;
import com.bullhorn.dataloader.util.BullhornAPI;

public class ImportServiceUtils {

    private final MasterDataService masterDataService;
    private final BullhornAPI bullhornAPI;

    public ImportServiceUtils() {
        masterDataService = mock(MasterDataService.class);
        bullhornAPI = mock(BullhornAPI.class);
    }

    public void injectFakeDependencies(AbstractEntityImportService abstractEntityImportService) throws Exception {
        abstractEntityImportService.setBhapi(getBhAPI());
        abstractEntityImportService.setMasterDataService(getMasterDataService());
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    public BullhornAPI getBhAPI() throws Exception {
        return bullhornAPI;
    }

}
