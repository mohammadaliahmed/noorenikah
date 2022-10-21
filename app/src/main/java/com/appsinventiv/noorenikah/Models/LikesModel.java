package com.appsinventiv.noorenikah.Models;

public class LikesModel {
    String id,phone,name,picUrl;

    public LikesModel(String id, String phone, String name, String picUrl) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.picUrl = picUrl;
    }

    public LikesModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
