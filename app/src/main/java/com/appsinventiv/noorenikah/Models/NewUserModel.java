package com.appsinventiv.noorenikah.Models;

public class NewUserModel {
    String phone,name,details,verified,livePicPath,fcmKey;
    boolean phoneVerified;
    boolean liked=false;

    public NewUserModel(String phone, String name, String details, String verified, String livePicPath, String fcmKey) {
        this.phone = phone;
        this.name = name;
        this.details = details;
        this.verified = verified;
        this.livePicPath = livePicPath;
        this.fcmKey = fcmKey;
    }

    public NewUserModel() {
    }

    public String getPhone() {
        return phone;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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
