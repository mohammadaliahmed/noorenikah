package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

/**
 * Created by JunaidAy on 11/10/2017.
 */
public class Calls {
    public static final String TABLE_NAME = "calls";
    private Long callId;
    private Long callStartTime;
    private Long callEndTime;
    private Long groupId;
    private Long callerId;
    private Long attendentId;
    private Long callStateCode;
    private Long responseTypeCode;
    private Long duration;
    private String type;
    private int isIndividual;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsIndividual() {
        return isIndividual;
    }

    public void setIsIndividual(int isIndividual) {
        this.isIndividual = isIndividual;
    }

    public Calls() {
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public Long getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(Long callStartTime) {
        this.callStartTime = callStartTime;
    }

    public Long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(Long callEndTime) {
        this.callEndTime = callEndTime;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getCallerId() {
        return callerId;
    }

    public void setCallerId(Long callerId) {
        this.callerId = callerId;
    }

    public Long getAttendentId() {
        return attendentId;
    }

    public void setAttendentId(Long attendentId) {
        this.attendentId = attendentId;
    }

    public Long getCallStateCode() {
        return callStateCode;
    }

    public void setCallStateCode(Long callStateCode) {
        this.callStateCode = callStateCode;
    }

    public Long getResponseTypeCode() {
        return responseTypeCode;
    }

    public void setResponseTypeCode(Long responseTypeCode) {
        this.responseTypeCode = responseTypeCode;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

}
