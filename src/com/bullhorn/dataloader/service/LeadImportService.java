package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.ID;
import com.bullhorn.dataloader.domain.Lead;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class LeadImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(LeadImportService.class);
	
	Object obj;
	MasterData masterData;
	BullhornAPI bhapi;
	
	public Integer lead() {
		try {
			
			Lead lead = (Lead) obj;
			
			// Check if record exists in BH and get postURL
			String[] postInfo = bhapi.getPostURL(lead);
			String type = postInfo[0];
			String postURL = postInfo[1];
			
			MasterDataService mds = new MasterDataService();
			mds.setMasterData(masterData);
			mds.setBhapi(bhapi);
			
			// Determine owner. If owner isn't passed in, it uses session user 
			ID ownerID = new ID();
			// If an ID is passed in
			if (lead.getOwnerID() != null && lead.getOwnerID().length() > 0) {
				ownerID.setId(lead.getOwnerID());
				lead.setOwner(ownerID);
			// Else look up by name
			} else if (lead.getOwnerName() != null && lead.getOwnerName().length() > 0) {
				ownerID.setId(String.valueOf(mds.getKeyByValue(masterData.getInternalUsers(), lead.getOwnerName())));
				lead.setOwner(ownerID);
			}
			
			// Save
			JSONObject jsResp = bhapi.save(lead, postURL, type);
			
			// Get ID of the created/updated record
			int leadID = jsResp.getInt("changedEntityId");
			
			// Note: associations are expicitly excluded from serialization as they need to be handled separately
			if (lead.getCategories() != null && lead.getCategories().length() > 0) {
				mds.associateCategories(leadID, lead.getCategories(), "Lead");				
			}
			
			return leadID;
					

		} catch (Exception e) {
			log.error(e);
		}

		return null;
	
	}
	
	public void run() {
		lead();
	}
	
	public Object getObj() {
		return obj;
	}
	
	public void setObj(Object obj) {
		this.obj = obj;
	}

	public MasterData getMasterData() {
		return masterData;
	}

	public void setMasterData(MasterData masterData) {
		this.masterData = masterData;
	}

	public BullhornAPI getBhapi() {
		return bhapi;
	}

	public void setBhapi(BullhornAPI bhapi) {
		this.bhapi = bhapi;
	}

}
