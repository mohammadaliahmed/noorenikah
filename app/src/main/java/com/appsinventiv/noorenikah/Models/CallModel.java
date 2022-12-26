package com.appsinventiv.noorenikah.Models;

public class CallModel {
    String id, phone, name, pic, callType;
    boolean video;
    long seconds;
    long startTime;
    long endTime;


    public CallModel(String id, String phone, String name,
                     String pic, String callType, boolean video, long seconds, long startTime, long endTime) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.pic = pic;
        this.callType = callType;
        this.video = video;
        this.seconds = seconds;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public CallModel() {
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
