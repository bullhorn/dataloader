package com.bullhorn.dataloader.domain;

import java.util.HashMap;

public class MasterData {

    private HashMap<Integer, String> categories;
    private HashMap<Integer, String> skills;
    private HashMap<Integer, String> businessSectors;
    private HashMap<Integer, String> internalUsers;

    public HashMap<Integer, String> getCategories() {
        return categories;
    }

    public void setCategories(HashMap<Integer, String> categories) {
        this.categories = categories;
    }

    public HashMap<Integer, String> getSkills() {
        return skills;
    }

    public void setSkills(HashMap<Integer, String> skills) {
        this.skills = skills;
    }

    public HashMap<Integer, String> getBusinessSectors() {
        return businessSectors;
    }

    public void setBusinessSectors(HashMap<Integer, String> businessSectors) {
        this.businessSectors = businessSectors;
    }

    public HashMap<Integer, String> getInternalUsers() {
        return internalUsers;
    }

    public void setInternalUsers(HashMap<Integer, String> internalUsers) {
        this.internalUsers = internalUsers;
    }

}
