package com.bullhorn.dataloader.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"clientCorporationID",
        "address1",
        "address2",
        "city",
        "state",
        "zip",
        "country",
        "parentClientCorporationID"})

public class ClientCorporation {

    @TranslatedType(isID = true)
    public String clientCorporationID;
    public ID clientCorporation;
    public Address address;
    public String address1;
    public String address2;
    public String city;
    public String state;
    public String zip;
    public String country;
    public String annualRevenue;
    public String feeArrangement;
    public String name;
    public String numEmployees;
    public String numOffices;
    public String status;
    public String companyDescription;
    public String companyURL;
    public String competitors;
    @TranslatedType(isDate = true)
    public long customDate1 = -1;
    @TranslatedType(isDate = true)
    public long customDate2 = -1;
    @TranslatedType(isDate = true)
    public long customDate3 = -1;
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
    public String externalID;
    public String fax;
    public String funding;
    public String ownership;
    public ID parentClientCorporation;
    public String parentClientCorporationID;
    public String phone;
    public String preferredHousing;
    public String taxRate;
    public String territoryID;
    public String tickerSymbol;
    public String workWeekStart;

    public String getClientCorporationID() {
        return clientCorporationID;
    }

    public void setClientCorporationID(String clientCorporationID) {
        this.clientCorporationID = clientCorporationID;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(String annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public String getFeeArrangement() {
        return feeArrangement;
    }

    public void setFeeArrangement(String feeArrangement) {
        this.feeArrangement = feeArrangement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumEmployees() {
        return numEmployees;
    }

    public void setNumEmployees(String numEmployees) {
        this.numEmployees = numEmployees;
    }

    public String getNumOffices() {
        return numOffices;
    }

    public void setNumOffices(String numOffices) {
        this.numOffices = numOffices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public ID getClientCorporation() {
        return clientCorporation;
    }

    public void setClientCorporation(ID clientCorporation) {
        this.clientCorporation = clientCorporation;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyURL() {
        return companyURL;
    }

    public void setCompanyURL(String companyURL) {
        this.companyURL = companyURL;
    }

    public String getCompetitors() {
        return competitors;
    }

    public void setCompetitors(String competitors) {
        this.competitors = competitors;
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

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getFunding() {
        return funding;
    }

    public void setFunding(String funding) {
        this.funding = funding;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public ID getParentClientCorporation() {
        return parentClientCorporation;
    }

    public void setParentClientCorporation(ID parentClientCorporation) {
        this.parentClientCorporation = parentClientCorporation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPreferredHousing() {
        return preferredHousing;
    }

    public void setPreferredHousing(String preferredHousing) {
        this.preferredHousing = preferredHousing;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getTerritoryID() {
        return territoryID;
    }

    public void setTerritoryID(String territoryID) {
        this.territoryID = territoryID;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public String getWorkWeekStart() {
        return workWeekStart;
    }

    public void setWorkWeekStart(String workWeekStart) {
        this.workWeekStart = workWeekStart;
    }

    public String getParentClientCorporationID() {
        return parentClientCorporationID;
    }

    public void setParentClientCorporationID(String parentClientCorporationID) {
        this.parentClientCorporationID = parentClientCorporationID;
    }
}
