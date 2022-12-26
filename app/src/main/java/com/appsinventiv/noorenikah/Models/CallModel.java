package com.appsinventiv.noorenikah.Models;

public class CallModel {
    String id, callFrom, callFromName,callTo, callToName, callType;
    boolean video;
    long seconds;
    long startTime;
    long endTime;

    public CallModel(String id, String callFrom, String callFromName, String callTo,
                     String callToName, String callType, boolean video, long seconds, long startTime, long endTime) {
        this.id = id;
        this.callFrom = callFrom;
        this.callFromName = callFromName;
        this.callTo = callTo;
        this.callToName = callToName;
        this.callType = callType;
        this.video = video;
        this.seconds = seconds;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getCallFromName() {
        return callFromName;
    }

    public void setCallFromName(String callFromName) {
        this.callFromName = callFromName;
    }

    public String getCallToName() {
        return callToName;
    }

    public void setCallToName(String callToName) {
        this.callToName = callToName;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
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

    public CallModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCallFrom() {
        return callFrom;
    }

    public void setCallFrom(String callFrom) {
        this.callFrom = callFrom;
    }

    public String getCallTo() {
        return callTo;
    }

    public void setCallTo(String callTo) {
        this.callTo = callTo;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }
}
