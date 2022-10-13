package com.appsinventiv.noorenikah.Models;

public class PostModel {
    String id,type,imageUrl,text,userId,userName,userPicUrl;
    int likeCount,commentCount;
    long time;
    boolean approved;
    boolean liked;


    public PostModel(String id, String type, String imageUrl, String text, String userId,
                     String userName,String userPicUrl, int likeCount, int commentCount, long time, boolean approved) {
        this.id = id;
        this.type = type;
        this.imageUrl = imageUrl;
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.userPicUrl = userPicUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.time = time;
        this.approved = approved;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPicUrl() {
        return userPicUrl;
    }

    public void setUserPicUrl(String userPicUrl) {
        this.userPicUrl = userPicUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public PostModel() {
    }
}
