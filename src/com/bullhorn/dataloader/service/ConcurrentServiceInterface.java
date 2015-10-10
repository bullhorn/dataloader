package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public interface ConcurrentServiceInterface {
	
	public void setObj(Object obj);
	public void setMasterData(MasterData md);
	public void setBhapi(BullhornAPI bhapi);
	
}