package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.util.BullhornAPI;

public abstract class AbstractEntityImportService implements Runnable, ConcurrentServiceInterface {
    protected final Log log = LogFactory.getLog(getClass());
    protected Object obj;
    protected MasterDataService masterDataService;
    protected BullhornAPI bhapi;

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public void setMasterDataService(MasterDataService masterData) {
        this.masterDataService = masterData;
    }

    public void setBhapi(BullhornAPI bhapi) {
        this.bhapi = bhapi;
    }
}
