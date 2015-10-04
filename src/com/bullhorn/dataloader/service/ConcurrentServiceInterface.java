package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.domain.MasterData;

public interface ConcurrentServiceInterface {
	
	public void setObj(Object obj);
	public void setMasterData(MasterData md);
	public void setBhRestToken(String BhRestToken);

}