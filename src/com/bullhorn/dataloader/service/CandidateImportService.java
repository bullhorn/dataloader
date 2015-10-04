package com.bullhorn.dataloader.service;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.Candidate;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;

public class CandidateImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(CandidateImportService.class);
	
	Object obj;
	MasterData masterData;
	String BhRestToken;
	
	public void run() {
		try {
			
			BullhornAPI bhapi = new BullhornAPI();
			bhapi.setBhRestToken(BhRestToken);
			Candidate candidate = (Candidate) obj;
			
			// Check if record exists in BH
			JSONObject qryJSON = bhapi.doesRecordExist("Candidate", "email", candidate.getEmail());
			
			// Assemble URL
			String type = "put";
			String postURL = "https://bhnext.bullhornstaffing.com/core/entity/Candidate";
			if (qryJSON.getInt("count") > 0) {
				postURL = postURL + "/" +  qryJSON.getJSONArray("data").getJSONObject(0).getInt("id");
				type = "post";
			}
			
			postURL = postURL + "?BhRestToken=" + BhRestToken;
			
			// Set username/password properties (which will not be defined in the CSV)
			if (qryJSON.getInt("count") <= 0) {
				Random randomGenerator = new Random();
				candidate.setUsername(candidate.getLastName() + randomGenerator.nextInt(1000));
				candidate.setPassword("testpw12345");
			}
			candidate.setIsDeleted("false");
			candidate.setIsEditable("true");
			
			//Save
			JSONObject jsResp = bhapi.mapAndSave(candidate, postURL, type);
			
			// Get ID of the created/updated record
			int candidateID = jsResp.getInt("changedEntityId");
			
			// If it's an insert and you need to add associations, do it now
			// Instantiate new master data service as association functions are encapsulated in that service
			// Remember to pass it a MasterData object so that it uses the cached object
			MasterDataService mds = new MasterDataService();
			mds.setMasterData(masterData);
			mds.setBhRestToken(BhRestToken);
			
			// Note: associations are expicitly excluded from serialization as they need to be handled separately
			if (candidate.getCategories() != null && candidate.getCategories().length() > 0) {
				mds.associateCategories(candidateID, candidate.getCategories(), "Candidate");				
			}
					

		} catch (Exception e) {
			log.error(e);
		}
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
