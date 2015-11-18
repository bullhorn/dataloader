package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.api.MasterDataService;
import com.bullhorn.dataloader.service.api.BullhornAPI;

interface ConcurrentServiceInterface {

    void setObj(Object obj);

    void setMasterDataService(MasterDataService md);

    void setBhapi(BullhornAPI bhapi);

}