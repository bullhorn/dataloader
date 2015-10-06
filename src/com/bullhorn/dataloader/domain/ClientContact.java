package com.bullhorn.dataloader.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({ "categories", 
						"clientContactID"})

public class ClientContact {
	
	@TranslatedType(isID = true)
	public String clientContactID;
	public ID clientCorporation;
	public String firstName;
	public String lastName;
	public String name;
	public String email;
	public String status;
	public String preferredContact;
	public String comments;
	public String isDeleted;
	public String isEditable;
	public String username;
	public String password;
	public String categories;	
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPreferredContact() {
		return preferredContact;
	}
	public void setPreferredContact(String preferredContact) {
		this.preferredContact = preferredContact;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public String getClientContactID() {
		return clientContactID;
	}
	public void setClientContactID(String clientContactID) {
		this.clientContactID = clientContactID;
	}
	public ID getClientCorporation() {
		return clientCorporation;
	}
	public void setClientCorporation(ID clientCorporation) {
		this.clientCorporation = clientCorporation;
	}

}
