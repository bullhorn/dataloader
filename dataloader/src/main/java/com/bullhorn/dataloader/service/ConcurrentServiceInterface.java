package bullhorn.dataloader.service;

import bullhorn.dataloader.domain.MasterData;
import bullhorn.dataloader.util.BullhornAPI;

public interface ConcurrentServiceInterface {
	
	public void setObj(Object obj);
	public void setMasterData(MasterData md);
	public void setBhapi(BullhornAPI bhapi);
	
}