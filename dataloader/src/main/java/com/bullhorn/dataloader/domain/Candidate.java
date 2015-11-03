package com.bullhorn.dataloader.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"categories"})
public class Candidate extends User {

    public String firstName;
    public String lastName;
    public String name;
    public String email;
    public String status;
    public String preferredContact;
    public String comments;
    public String employeeType;
    public String isDeleted;
    public String isEditable;
    public String customText5;
    public String categories;

    public String getCustomText5() {
        return customText5;
    }

    public void setCustomText5(String customText5) {
        this.customText5 = customText5;
    }

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

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
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

}
