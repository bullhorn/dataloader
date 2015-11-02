package bullhorn.dataloader.service;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import bullhorn.dataloader.domain.ClientContact;
import bullhorn.dataloader.domain.ID;
import bullhorn.dataloader.domain.MasterData;
import bullhorn.dataloader.util.BullhornAPI;

public class ClientContactImportService implements Runnable, ConcurrentServiceInterface {
	
	private static Log log = LogFactory.getLog(ClientContactImportService.class);
	
	Object obj;
	MasterData masterData;
	BullhornAPI bhapi;
	
	public Integer clientContact() {
		try {
			
			ClientContact contact = (ClientContact) obj;
			
			// Check if record exists in BH and get postURL
			String[] postInfo = bhapi.getPostURL(contact);
			String type = postInfo[0];
			String postURL = postInfo[1];
			
			// Set username/password properties (which will not be defined in the CSV)
			if (type.equalsIgnoreCase("put")) {
				Random randomGenerator = new Random();
				contact.setUsername(contact.getLastName() + randomGenerator.nextInt(10000));
				contact.setPassword("testpw12345");
			}
			contact.setIsDeleted("false");
			
			// Instantiate new master data service as association functions are encapsulated in that service
			// Remember to pass it a MasterData object so that it uses the cached object
			MasterDataService mds = new MasterDataService();
			mds.setMasterData(masterData);
			mds.setBhapi(bhapi);
			
			// Determine owner. If owner isn't passed in, it uses session user 
			ID ownerID = new ID();
			// If an ID is passed in
			if (contact.getOwnerID() != null && contact.getOwnerID().length() > 0) {
				ownerID.setId(contact.getOwnerID());
				contact.setOwner(ownerID);
			// Else look up by name
			} else if (contact.getOwnerName() != null && contact.getOwnerName().length() > 0) {
				ownerID.setId(String.valueOf(mds.getKeyByValue(masterData.getInternalUsers(), contact.getOwnerName())));
				contact.setOwner(ownerID);
			}
			
			// Save
			JSONObject jsResp = bhapi.save(contact, postURL, type);
			
			// Get ID of the created/updated record
			int clientContactID = jsResp.getInt("changedEntityId");
			
			// Note: associations are expicitly excluded from serialization as they need to be handled separately
			if (contact.getCategories() != null && contact.getCategories().length() > 0) {
				mds.associateCategories(clientContactID, contact.getCategories(), "ClientContact");				
			}
			
			return clientContactID;
					

		} catch (Exception e) {
			log.error(e);
		}

		return null;
	
	}
	
	public void run() {
		clientContact();
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
