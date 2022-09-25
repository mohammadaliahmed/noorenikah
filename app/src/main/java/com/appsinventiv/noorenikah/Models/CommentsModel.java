package com.appsinventiv.noorenikah.Models;

public class CommentsModel {
    String id, comment,commentByName,phone,picUrl;
    long time;

    public CommentsModel() {
    }

    public CommentsModel(String id, String comment, String commentByName,String phone, String picUrl, long time) {
        this.id = id;
        this.comment = comment;
        this.commentByName = commentByName;
        this.phone = phone;
        this.picUrl = picUrl;
        this.time = time;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentByName() {
        return commentByName;
    }

    public void setCommentByName(String commentByName) {
        this.commentByName = commentByName;
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
