package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.Address;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class ClientCorporationImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(ClientCorporationImportService.class);
	
	Object obj;
	MasterData masterData;
	String BhRestToken;
	
	public Integer clientCorporation() {
		
		try {
			
			BullhornAPI bhapi = new BullhornAPI();
			bhapi.setBhRestToken(BhRestToken);
			ClientCorporation corp = (ClientCorporation) obj;
			
			// Check if record exists in BH
			JSONObject qryJSON = bhapi.doesRecordExist("ClientCorporation", "id", corp.getClientCorporationID());
			
			// Assemble URL
			String type = "put";
			String postURL = "https://bhnext.bullhornstaffing.com/core/entity/ClientCorporation";
			if (corp.getClientCorporationID() != null && corp.getClientCorporationID().length() > 0) {
				if (qryJSON.getJSONObject("data").getInt("id") > 0) {
					postURL = postURL + "/" +  qryJSON.getJSONObject("data").getInt("id");
					type = "post";
				}
			}
			
			postURL = postURL + "?BhRestToken=" + BhRestToken;
			
			// Populate address fields
			if (corp.getAddress() == null) {
				Address address = new Address();
				address.setAddress1(corp.getAddress1());
				address.setAddress2(corp.getAddress2());
				address.setCity(corp.getCity());
				address.setCountryID(corp.getCountry());
				address.setState(corp.getState());
				address.setZip(corp.getZip());
				corp.setAddress(address);
			}
			
			JSONObject jsResp = bhapi.save(corp, postURL, type);
			
			// Get ID of the created/updated record
			int clientCorporationID = jsResp.getInt("changedEntityId");
			
			return clientCorporationID;
			
		} catch (Exception e) {
			log.error(e);
		}
		
		return null;
	}
	
	public void run() {
		clientCorporation();
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

	public String getBhRestToken() {
		return BhRestToken;
	}

	public void setBhRestToken(String bhRestToken) {
		BhRestToken = bhRestToken;
	}

}
