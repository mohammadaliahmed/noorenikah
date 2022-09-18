package com.appsinventiv.noorenikah.Models;

public class ChatModel {
    String id, message, senderId, sendTo, name, picUrl, myName, myPic, hisName, hisPic,
            myPhone, hisPhone;
    long time;
    boolean read;
    String type, imageUrl,videoUrl,audioUrl;
    long mediaTime;


    public ChatModel() {
    }



    public ChatModel(String id, String message, String senderId, String sendTo, String name, String picUrl,
                     String myName, String myPic, String myPhone, String hisName, String hisPhone, String hisPic,
                     boolean read,
                     long time, String type, String imageUrl, String videoUrl, String audioUrl,long mediaTime) {
        this.id = id;
        this.message = message;
        this.senderId = senderId;
        this.sendTo = sendTo;
        this.name = name;
        this.picUrl = picUrl;
        this.time = time;
        this.type = type;
        this.myName = myName;
        this.myPic = myPic;
        this.read = read;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.audioUrl = audioUrl;
        this.hisName = hisName;
        this.hisPic = hisPic;
        this.hisPhone = hisPhone;
        this.mediaTime = mediaTime;
        this.myPhone = myPhone;
    }

    public long getMediaTime() {
        return mediaTime;
    }

    public void setMediaTime(long mediaTime) {
        this.mediaTime = mediaTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
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
