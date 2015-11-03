package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.BullhornAPI;

interface ConcurrentServiceInterface {

    void setObj(Object obj);

    void setMasterDataService(MasterDataService md);

    void setBhapi(BullhornAPI bhapi);

}