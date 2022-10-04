package com.appsinventiv.noorenikah.Models;

import java.util.HashMap;

public class MatchMakerModel {
    String id, name, mbName, experience, city, contactNo,phone,picUrl;
    HashMap<String,Object> profilesCreated;

    public MatchMakerModel(String id, String name, String mbName,
                           String experience, String city, String contactNo, String phone, String picUrl) {
        this.id = id;
        this.name = name;
        this.mbName = mbName;
        this.experience = experience;
        this.city = city;
        this.phone = phone;
        this.picUrl = picUrl;
        this.contactNo = contactNo;
    }

    public MatchMakerModel() {
    }

    public HashMap<String, Object> getProfilesCreated() {
        return profilesCreated;
    }

    public void setProfilesCreated(HashMap<String, Object> profilesCreated) {
        this.profilesCreated = profilesCreated;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMbName() {
        return mbName;
    }

    public void setMbName(String mbName) {
        this.mbName = mbName;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
}
