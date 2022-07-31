package com.appsinventiv.noorenikah.Models;

public class ChatModel {
    String id, message, senderId, sendTo, name, picUrl, myName, myPic, hisName, hisPic,
            myPhone, hisPhone;
    long time;


    public ChatModel() {
    }

    public ChatModel(String id, String message, String senderId, String sendTo, String name, String picUrl,
                     String myName, String myPic,String myPhone, String hisName,String hisPhone, String hisPic,
                     long time) {
        this.id = id;
        this.message = message;
        this.senderId = senderId;
        this.sendTo = sendTo;
        this.name = name;
        this.picUrl = picUrl;
        this.time = time;
        this.myName = myName;
        this.myPic = myPic;
        this.hisName = hisName;
        this.hisPic = hisPic;
        this.hisPhone = hisPhone;
        this.myPhone = myPhone;
    }

    public String getMyPhone() {
        return myPhone;
    }

    public void setMyPhone(String myPhone) {
        this.myPhone = myPhone;
    }

    public String getHisPhone() {
        return hisPhone;
    }

    public void setHisPhone(String hisPhone) {
        this.hisPhone = hisPhone;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public String getMyPic() {
        return myPic;
    }

    public void setMyPic(String myPic) {
        this.myPic = myPic;
    }

    public String getHisName() {
        return hisName;
    }

    public void setHisName(String hisName) {
        this.hisName = hisName;
    }

    public String getHisPic() {
        return hisPic;
    }

    public void setHisPic(String hisPic) {
        this.hisPic = hisPic;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
