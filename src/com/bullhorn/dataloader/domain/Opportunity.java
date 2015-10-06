package com.bullhorn.dataloader.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.bullhorn.dataloader.domain.TranslatedType;

@JsonIgnoreProperties({ "clientContactID", 
						"clientCorporationID", 
						"opportunityID", 
						"clientContactName", 
						"clientCorporationName",
						"address1",
						"address2",
						"city",
						"state",
						"zip",
						"country",
						"categories",
						"businessSectors",
						"primaryCategory",
						"primaryBusinessSector"})

public class Opportunity {
	
	@TranslatedType(isID = true)
	public String opportunityID;
	public String title;
	public ID clientContact; //used for REST
	public ID clientCorporation; // used for REST
	public String clientCorporationID; // can pass in ID
	public String clientContactID; // can pass in ID
	public String clientContactName; // can pass in name
	public String clientCorporationName; // can pass in name
	@TranslatedType(isDate = true)
	public long estimatedStartDate = -1;
	public String isDeleted;
	public String isEditable;
	public String type;
	public Address address;
	public String address1;
	public String address2;
	public String city;
	public String state;
	public String zip;
	public String country;
	public String status;
	public String committed;
	public String correlatedCustomFloat1;
	public String correlatedCustomFloat2;
	public String correlatedCustomFloat3;
	public String correlatedCustomInt1;
	public String correlatedCustomInt2;
	public String correlatedCustomInt3;
	public String correlatedCustomText1;
	public String correlatedCustomText10;
	public String correlatedCustomText2;
	public String correlatedCustomText3;
	public String correlatedCustomText4;
	public String correlatedCustomText5;
	public String correlatedCustomText6;
	public String correlatedCustomText7;
	public String correlatedCustomText8;
	public String correlatedCustomText9;
	public String correlatedCustomTextBlock1;
	public String correlatedCustomTextBlock2;
	public String correlatedCustomTextBlock3;
	public String customFloat1;
	public String customFloat2;
	public String customFloat3;
	public String customInt1;
	public String customInt2;
	public String customInt3;
	public String customText1;
	public String customText10;
	public String customText11;
	public String customText12;
	public String customText13;
	public String customText14;
	public String customText15;
	public String customText16;
	public String customText17;
	public String customText18;
	public String customText19;
	public String customText2;
	public String customText20;
	public String customText3;
	public String customText4;
	public String customText5;
	public String customText6;
	public String customText7;
	public String customText8;
	public String customText9;
	public String customTextBlock1;
	public String customTextBlock2;
	public String customTextBlock3;
	public String customTextBlock4;
	public String customTextBlock5;
	@TranslatedType(isDate = true)
	public long customDate1 = -1;
	@TranslatedType(isDate = true)
	public long customDate2 = -1;
	@TranslatedType(isDate = true)
	public long customDate3 = -1;
	public String dealValue;
	public String description;
	public String estimatedDuration;
	public String externalID;
	public String opportunityMarkUp;
	public ID businessSector; // used in REST to set primary business sector
	public String primaryBusinessSector; // Pass in primary business sector name
	public String businessSectors; // comma separated list of to-many business sectors
	public ID category; // used in REST to set primary category
	public String primaryCategory; // Pass in primary category name
	public String categories; // comma separated list of to-many categories
	public String reasonClosed;
	public String reportTo;
	public String source;
	public String specialty_categoryID;
	public String weightedDealValue;
	public String winProbabilityPercent;
		
	
	public String getOpportunityID() {
		return opportunityID;
	}
	public void setOpportunityID(String opportunityID) {
		this.opportunityID = opportunityID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ID getClientContact() {
		return clientContact;
	}
	public void setClientContact(ID clientContact) {
		this.clientContact = clientContact;
	}
	public ID getClientCorporation() {
		return clientCorporation;
	}
	public void setClientCorporation(ID clientCorporation) {
		this.clientCorporation = clientCorporation;
	}
	public long getEstimatedStartDate() {
		return estimatedStartDate;
	}
	public void setEstimatedStartDate(long estimatedStartDate) {
		this.estimatedStartDate = estimatedStartDate;
	}
	public String getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getIsEditable() {
		return isEditable;
	}
	public void setIsEditable(String isEditable) {
		this.isEditable = isEditable;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClientCorporationID() {
		return clientCorporationID;
	}
	public void setClientCorporationID(String clientCorporationID) {
		this.clientCorporationID = clientCorporationID;
	}
	public String getClientContactID() {
		return clientContactID;
	}
	public void setClientContactID(String clientContactID) {
		this.clientContactID = clientContactID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getClientContactName() {
		return clientContactName;
	}
	public void setClientContactName(String clientContactName) {
		this.clientContactName = clientContactName;
	}
	public String getClientCorporationName() {
		return clientCorporationName;
	}
	public void setClientCorporationName(String clientCorporationName) {
		this.clientCorporationName = clientCorporationName;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public String getCommitted() {
		return committed;
	}
	public void setCommitted(String committed) {
		this.committed = committed;
	}
	public String getCorrelatedCustomFloat1() {
		return correlatedCustomFloat1;
	}
	public void setCorrelatedCustomFloat1(String correlatedCustomFloat1) {
		this.correlatedCustomFloat1 = correlatedCustomFloat1;
	}
	public String getCorrelatedCustomFloat2() {
		return correlatedCustomFloat2;
	}
	public void setCorrelatedCustomFloat2(String correlatedCustomFloat2) {
		this.correlatedCustomFloat2 = correlatedCustomFloat2;
	}
	public String getCorrelatedCustomFloat3() {
		return correlatedCustomFloat3;
	}
	public void setCorrelatedCustomFloat3(String correlatedCustomFloat3) {
		this.correlatedCustomFloat3 = correlatedCustomFloat3;
	}
	public String getCorrelatedCustomInt1() {
		return correlatedCustomInt1;
	}
	public void setCorrelatedCustomInt1(String correlatedCustomInt1) {
		this.correlatedCustomInt1 = correlatedCustomInt1;
	}
	public String getCorrelatedCustomInt2() {
		return correlatedCustomInt2;
	}
	public void setCorrelatedCustomInt2(String correlatedCustomInt2) {
		this.correlatedCustomInt2 = correlatedCustomInt2;
	}
	public String getCorrelatedCustomInt3() {
		return correlatedCustomInt3;
	}
	public void setCorrelatedCustomInt3(String correlatedCustomInt3) {
		this.correlatedCustomInt3 = correlatedCustomInt3;
	}
	public String getCorrelatedCustomText1() {
		return correlatedCustomText1;
	}
	public void setCorrelatedCustomText1(String correlatedCustomText1) {
		this.correlatedCustomText1 = correlatedCustomText1;
	}
	public String getCorrelatedCustomText10() {
		return correlatedCustomText10;
	}
	public void setCorrelatedCustomText10(String correlatedCustomText10) {
		this.correlatedCustomText10 = correlatedCustomText10;
	}
	public String getCorrelatedCustomText2() {
		return correlatedCustomText2;
	}
	public void setCorrelatedCustomText2(String correlatedCustomText2) {
		this.correlatedCustomText2 = correlatedCustomText2;
	}
	public String getCorrelatedCustomText3() {
		return correlatedCustomText3;
	}
	public void setCorrelatedCustomText3(String correlatedCustomText3) {
		this.correlatedCustomText3 = correlatedCustomText3;
	}
	public String getCorrelatedCustomText4() {
		return correlatedCustomText4;
	}
	public void setCorrelatedCustomText4(String correlatedCustomText4) {
		this.correlatedCustomText4 = correlatedCustomText4;
	}
	public String getCorrelatedCustomText5() {
		return correlatedCustomText5;
	}
	public void setCorrelatedCustomText5(String correlatedCustomText5) {
		this.correlatedCustomText5 = correlatedCustomText5;
	}
	public String getCorrelatedCustomText6() {
		return correlatedCustomText6;
	}
	public void setCorrelatedCustomText6(String correlatedCustomText6) {
		this.correlatedCustomText6 = correlatedCustomText6;
	}
	public String getCorrelatedCustomText7() {
		return correlatedCustomText7;
	}
	public void setCorrelatedCustomText7(String correlatedCustomText7) {
		this.correlatedCustomText7 = correlatedCustomText7;
	}
	public String getCorrelatedCustomText8() {
		return correlatedCustomText8;
	}
	public void setCorrelatedCustomText8(String correlatedCustomText8) {
		this.correlatedCustomText8 = correlatedCustomText8;
	}
	public String getCorrelatedCustomText9() {
		return correlatedCustomText9;
	}
	public void setCorrelatedCustomText9(String correlatedCustomText9) {
		this.correlatedCustomText9 = correlatedCustomText9;
	}
	public String getCorrelatedCustomTextBlock1() {
		return correlatedCustomTextBlock1;
	}
	public void setCorrelatedCustomTextBlock1(String correlatedCustomTextBlock1) {
		this.correlatedCustomTextBlock1 = correlatedCustomTextBlock1;
	}
	public String getCorrelatedCustomTextBlock2() {
		return correlatedCustomTextBlock2;
	}
	public void setCorrelatedCustomTextBlock2(String correlatedCustomTextBlock2) {
		this.correlatedCustomTextBlock2 = correlatedCustomTextBlock2;
	}
	public String getCorrelatedCustomTextBlock3() {
		return correlatedCustomTextBlock3;
	}
	public void setCorrelatedCustomTextBlock3(String correlatedCustomTextBlock3) {
		this.correlatedCustomTextBlock3 = correlatedCustomTextBlock3;
	}
	public String getCustomFloat1() {
		return customFloat1;
	}
	public void setCustomFloat1(String customFloat1) {
		this.customFloat1 = customFloat1;
	}
	public String getCustomFloat2() {
		return customFloat2;
	}
	public void setCustomFloat2(String customFloat2) {
		this.customFloat2 = customFloat2;
	}
	public String getCustomFloat3() {
		return customFloat3;
	}
	public void setCustomFloat3(String customFloat3) {
		this.customFloat3 = customFloat3;
	}
	public String getCustomInt1() {
		return customInt1;
	}
	public void setCustomInt1(String customInt1) {
		this.customInt1 = customInt1;
	}
	public String getCustomInt2() {
		return customInt2;
	}
	public void setCustomInt2(String customInt2) {
		this.customInt2 = customInt2;
	}
	public String getCustomInt3() {
		return customInt3;
	}
	public void setCustomInt3(String customInt3) {
		this.customInt3 = customInt3;
	}
	public String getCustomText1() {
		return customText1;
	}
	public void setCustomText1(String customText1) {
		this.customText1 = customText1;
	}
	public String getCustomText10() {
		return customText10;
	}
	public void setCustomText10(String customText10) {
		this.customText10 = customText10;
	}
	public String getCustomText11() {
		return customText11;
	}
	public void setCustomText11(String customText11) {
		this.customText11 = customText11;
	}
	public String getCustomText12() {
		return customText12;
	}
	public void setCustomText12(String customText12) {
		this.customText12 = customText12;
	}
	public String getCustomText13() {
		return customText13;
	}
	public void setCustomText13(String customText13) {
		this.customText13 = customText13;
	}
	public String getCustomText14() {
		return customText14;
	}
	public void setCustomText14(String customText14) {
		this.customText14 = customText14;
	}
	public String getCustomText15() {
		return customText15;
	}
	public void setCustomText15(String customText15) {
		this.customText15 = customText15;
	}
	public String getCustomText16() {
		return customText16;
	}
	public void setCustomText16(String customText16) {
		this.customText16 = customText16;
	}
	public String getCustomText17() {
		return customText17;
	}
	public void setCustomText17(String customText17) {
		this.customText17 = customText17;
	}
	public String getCustomText18() {
		return customText18;
	}
	public void setCustomText18(String customText18) {
		this.customText18 = customText18;
	}
	public String getCustomText19() {
		return customText19;
	}
	public void setCustomText19(String customText19) {
		this.customText19 = customText19;
	}
	public String getCustomText2() {
		return customText2;
	}
	public void setCustomText2(String customText2) {
		this.customText2 = customText2;
	}
	public String getCustomText20() {
		return customText20;
	}
	public void setCustomText20(String customText20) {
		this.customText20 = customText20;
	}
	public String getCustomText3() {
		return customText3;
	}
	public void setCustomText3(String customText3) {
		this.customText3 = customText3;
	}
	public String getCustomText4() {
		return customText4;
	}
	public void setCustomText4(String customText4) {
		this.customText4 = customText4;
	}
	public String getCustomText5() {
		return customText5;
	}
	public void setCustomText5(String customText5) {
		this.customText5 = customText5;
	}
	public String getCustomText6() {
		return customText6;
	}
	public void setCustomText6(String customText6) {
		this.customText6 = customText6;
	}
	public String getCustomText7() {
		return customText7;
	}
	public void setCustomText7(String customText7) {
		this.customText7 = customText7;
	}
	public String getCustomText8() {
		return customText8;
	}
	public void setCustomText8(String customText8) {
		this.customText8 = customText8;
	}
	public String getCustomText9() {
		return customText9;
	}
	public void setCustomText9(String customText9) {
		this.customText9 = customText9;
	}
	public String getCustomTextBlock1() {
		return customTextBlock1;
	}
	public void setCustomTextBlock1(String customTextBlock1) {
		this.customTextBlock1 = customTextBlock1;
	}
	public String getCustomTextBlock2() {
		return customTextBlock2;
	}
	public void setCustomTextBlock2(String customTextBlock2) {
		this.customTextBlock2 = customTextBlock2;
	}
	public String getCustomTextBlock3() {
		return customTextBlock3;
	}
	public void setCustomTextBlock3(String customTextBlock3) {
		this.customTextBlock3 = customTextBlock3;
	}
	public String getCustomTextBlock4() {
		return customTextBlock4;
	}
	public void setCustomTextBlock4(String customTextBlock4) {
		this.customTextBlock4 = customTextBlock4;
	}
	public String getCustomTextBlock5() {
		return customTextBlock5;
	}
	public void setCustomTextBlock5(String customTextBlock5) {
		this.customTextBlock5 = customTextBlock5;
	}
	public String getDealValue() {
		return dealValue;
	}
	public void setDealValue(String dealValue) {
		this.dealValue = dealValue;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getEstimatedDuration() {
		return estimatedDuration;
	}
	public void setEstimatedDuration(String estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}
	public String getExternalID() {
		return externalID;
	}
	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}
	public String getOpportunityMarkUp() {
		return opportunityMarkUp;
	}
	public void setOpportunityMarkUp(String opportunityMarkUp) {
		this.opportunityMarkUp = opportunityMarkUp;
	}
	public String getReasonClosed() {
		return reasonClosed;
	}
	public void setReasonClosed(String reasonClosed) {
		this.reasonClosed = reasonClosed;
	}
	public String getReportTo() {
		return reportTo;
	}
	public void setReportTo(String reportTo) {
		this.reportTo = reportTo;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSpecialty_categoryID() {
		return specialty_categoryID;
	}
	public void setSpecialty_categoryID(String specialty_categoryID) {
		this.specialty_categoryID = specialty_categoryID;
	}
	public String getWeightedDealValue() {
		return weightedDealValue;
	}
	public void setWeightedDealValue(String weightedDealValue) {
		this.weightedDealValue = weightedDealValue;
	}
	public String getWinProbabilityPercent() {
		return winProbabilityPercent;
	}
	public void setWinProbabilityPercent(String winProbabilityPercent) {
		this.winProbabilityPercent = winProbabilityPercent;
	}
	public String getPrimaryBusinessSector() {
		return primaryBusinessSector;
	}
	public void setPrimaryBusinessSector(String primaryBusinessSector) {
		this.primaryBusinessSector = primaryBusinessSector;
	}
	public String getPrimaryCategory() {
		return primaryCategory;
	}
	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}
	public ID getBusinessSector() {
		return businessSector;
	}
	public void setBusinessSector(ID businessSector) {
		this.businessSector = businessSector;
	}
	public String getBusinessSectors() {
		return businessSectors;
	}
	public void setBusinessSectors(String businessSectors) {
		this.businessSectors = businessSectors;
	}
	public ID getCategory() {
		return category;
	}
	public void setCategory(ID category) {
		this.category = category;
	}
	public long getCustomDate1() {
		return customDate1;
	}
	public void setCustomDate1(long customDate1) {
		this.customDate1 = customDate1;
	}
	public long getCustomDate2() {
		return customDate2;
	}
	public void setCustomDate2(long customDate2) {
		this.customDate2 = customDate2;
	}
	public long getCustomDate3() {
		return customDate3;
	}
	public void setCustomDate3(long customDate3) {
		this.customDate3 = customDate3;
	}
}
