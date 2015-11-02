package com.bullhorn.dataloader.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bullhorn.dataloader.domain.Address;
import com.bullhorn.dataloader.domain.ClientContact;
import com.bullhorn.dataloader.domain.ClientCorporation;
import com.bullhorn.dataloader.domain.ID;
import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.domain.Opportunity;
import com.bullhorn.dataloader.util.BullhornAPI;

public class OpportunityImportService implements Runnable, ConcurrentServiceInterface {

    private static Log log = LogFactory.getLog(OpportunityImportService.class);

    Object obj;
    MasterData masterData;
    BullhornAPI bhapi;

    public void run() {
        try {

            Opportunity opportunity = (Opportunity) obj;

            // Check if record exists in BH and get postURL
            String[] postInfo = bhapi.getPostURL(opportunity);
            String type = postInfo[0];
            String postURL = postInfo[1];

            // If we don't have an opportunity, setup customer/contact
            // Else, allow customer and contact to be updated by ID only
            if (opportunity.getOpportunityID() == null || opportunity.getOpportunityID().length() <= 0) {

                // Create client by name if name is filled out and ID isn't
                if ((opportunity.clientCorporationID == null || opportunity.clientCorporationID.length() <= 0)
                        && (opportunity.clientCorporationName != null && opportunity.clientCorporationName.length() > 0)) {

                    ClientCorporation corp = new ClientCorporation();
                    Address address = new Address();
                    corp.setAddress(address);
                    corp.setAnnualRevenue("0");
                    corp.setFeeArrangement("0");
                    corp.setName(opportunity.getClientCorporationName());
                    corp.setNumEmployees("0");
                    corp.setNumOffices("0");
                    corp.setStatus("New");

                    ClientCorporationImportService corpImpSvc = new ClientCorporationImportService();
                    corpImpSvc.setBhapi(bhapi);
                    corpImpSvc.setObj(corp);

                    // set clientCorporation to the resulting ID
                    ID clientCorpID = new ID();
                    clientCorpID.setId(String.valueOf(corpImpSvc.clientCorporation()));
                    opportunity.setClientCorporation(clientCorpID);
                } else {
                    // if clientCorporationID is passed in, set clientCorporation to that ID
                    ID clientCorpID = new ID();
                    clientCorpID.setId(opportunity.getClientCorporationID());
                    opportunity.setClientCorporation(clientCorpID);
                }

                // Create contact by name if name is filled out and ID isn't
                // Client Corporation must not be null
                if ((opportunity.clientContactID == null || opportunity.clientContactID.length() <= 0)
                        && (opportunity.clientContactName != null && opportunity.clientContactName.length() > 0)
                        && (opportunity.getClientCorporation() != null && opportunity.getClientCorporation().getId().length() > 0)) {

                    ClientContact contact = new ClientContact();
                    contact.setFirstName(opportunity.clientContactName.split(" ")[0]);
                    contact.setLastName(opportunity.clientContactName.split(" ")[1]);
                    contact.setEmail("");
                    contact.setComments("");
                    contact.setStatus("New Lead");
                    contact.setName(opportunity.clientContactName);

                    ID clientCorporationID = new ID();
                    clientCorporationID.setId(opportunity.getClientCorporation().getId());
                    contact.setClientCorporation(clientCorporationID);

                    ClientContactImportService contactImpSvc = new ClientContactImportService();
                    contactImpSvc.setBhapi(bhapi);
                    contactImpSvc.setObj(contact);

                    // set clientContact to the resulting ID
                    ID clientContactID = new ID();
                    clientContactID.setId(String.valueOf(contactImpSvc.clientContact()));
                    opportunity.setClientContact(clientContactID);
                } else {
                    // if clientContactID is passed in, set clientContact to that ID
                    ID clientContactID = new ID();
                    clientContactID.setId(opportunity.getClientContactID());
                    opportunity.setClientContact(clientContactID);
                }
            } else {
                if (opportunity.getClientContactID() != null && opportunity.getClientContactID().length() > 0) {
                    ID clientContactID = new ID();
                    clientContactID.setId(opportunity.getClientContactID());
                    opportunity.setClientContact(clientContactID);
                }
                if (opportunity.getClientCorporationID() != null && opportunity.getClientCorporationID().length() > 0) {
                    ID clientCorpID = new ID();
                    clientCorpID.setId(opportunity.getClientCorporationID());
                    opportunity.setClientCorporation(clientCorpID);
                }
            }

            // Populate address fields
            if (opportunity.getAddress() == null) {
                Address address = new Address();
                address.setAddress1(opportunity.getAddress1());
                address.setAddress2(opportunity.getAddress2());
                address.setCity(opportunity.getCity());
                address.setCountryID(opportunity.getCountry());
                address.setState(opportunity.getState());
                address.setZip(opportunity.getZip());
                opportunity.setAddress(address);
            }

            MasterDataService mds = new MasterDataService();
            mds.setMasterData(masterData);
            mds.setBhapi(bhapi);

            // Determine owner. If owner isn't passed in, it uses session user
            ID ownerID = new ID();
            // If an ID is passed in
            if (opportunity.getOwnerID() != null && opportunity.getOwnerID().length() > 0) {
                ownerID.setId(opportunity.getOwnerID());
                opportunity.setOwner(ownerID);
                // Else look up by name
            } else if (opportunity.getOwnerName() != null && opportunity.getOwnerName().length() > 0) {
                ownerID.setId(String.valueOf(mds.getKeyByValue(masterData.getInternalUsers(), opportunity.getOwnerName())));
                opportunity.setOwner(ownerID);
            }

            // Primary category and business sector
            if (opportunity.getPrimaryCategory() != null && opportunity.getPrimaryCategory().length() > 0) {
                ID catID = new ID();
                catID.setId(String.valueOf(mds.getKeyByValue(masterData.getCategories(), opportunity.getPrimaryCategory())));
                opportunity.setCategory(catID);
            }
            if (opportunity.getPrimaryBusinessSector() != null && opportunity.getPrimaryBusinessSector().length() > 0) {
                ID busID = new ID();
                busID.setId(String.valueOf(mds.getKeyByValue(masterData.getCategories(), opportunity.getPrimaryBusinessSector())));
                opportunity.setBusinessSector(busID);
            }

            // Save
            JSONObject jsResp = bhapi.save(opportunity, postURL, type);

            // Get ID of the created/updated record
            int opportunityID = jsResp.getInt("changedEntityId");

            // Note: associations are expicitly excluded from serialization as they need to be handled separately
            // Assocations in the spreadsheet will replace existing associations
            if (opportunity.getCategories() != null && opportunity.getCategories().length() > 0) {
                mds.associateCategories(opportunityID, opportunity.getCategories(), "Opportunity");
            }
            if (opportunity.getSkills() != null && opportunity.getSkills().length() > 0) {
                mds.associateSkills(opportunityID, opportunity.getSkills(), "Opportunity");
            }
            if (opportunity.getBusinessSectors() != null && opportunity.getBusinessSectors().length() > 0) {
                mds.associateBusinessSectors(opportunityID, opportunity.getBusinessSectors(), "Opportunity");
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

    public BullhornAPI getBhapi() {
        return bhapi;
    }

    public void setBhapi(BullhornAPI bhapi) {
        this.bhapi = bhapi;
    }

}
