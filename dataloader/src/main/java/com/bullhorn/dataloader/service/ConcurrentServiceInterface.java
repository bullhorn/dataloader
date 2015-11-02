package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public interface ConcurrentServiceInterface {

    void setObj(Object obj);

    void setMasterData(MasterData md);

    void setBhapi(BullhornAPI bhapi);

}