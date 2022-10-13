package com.appsinventiv.noorenikah.Models;

public class NotificationModel {
    String id, title, message, type, picUrl;
    String hisId,postId;
    long time;

    public NotificationModel(String id, String title, String message, String type, String picUrl, String hisId, long time) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.hisId = hisId;
        this.type = type;
        this.picUrl = picUrl;
        this.time = time;
    }

    public NotificationModel() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
