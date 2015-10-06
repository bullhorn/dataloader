package com.bullhorn.dataloader.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"clientCorporationID",
						"address1",
						"address2",
						"city",
						"state",
						"zip",
						"country"})

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
	
}
