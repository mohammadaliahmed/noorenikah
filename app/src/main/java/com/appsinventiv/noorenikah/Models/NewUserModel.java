package com.appsinventiv.noorenikah.Models;

public class NewUserModel {
    String phone,name,gender,verified,livePicPath,fcmKey,city,sect,education,maritalStatus,matchMakerId;
    String lastLoginTime;
    boolean phoneVerified;
    boolean matchMakerProfile=false;
    boolean liked=false;


    public NewUserModel() {
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getMatchMakerId() {
        return matchMakerId;
    }


    public boolean isMatchMakerProfile() {
        return matchMakerProfile;
    }

    public void setMatchMakerProfile(boolean matchMakerProfile) {
        this.matchMakerProfile = matchMakerProfile;
    }

    public void setMatchMakerId(String matchMakerId) {
        this.matchMakerId = matchMakerId;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSect() {
        return sect;
    }

    public void setSect(String sect) {
        this.sect = sect;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getLivePicPath() {
        return livePicPath;
    }

    public void setLivePicPath(String livePicPath) {
        this.livePicPath = livePicPath;
    }

    public String getFcmKey() {
        return fcmKey;
    }

    public void setFcmKey(String fcmKey) {
        this.fcmKey = fcmKey;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
