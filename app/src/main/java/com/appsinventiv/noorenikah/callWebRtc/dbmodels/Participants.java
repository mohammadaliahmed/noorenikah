package com.appsinventiv.noorenikah.callWebRtc.dbmodels;


/**
 * Created by JunaidAy on 11/10/2017.
 */

public class Participants {
    public static final String TABLE_NAME = "participants";
    private Long participantId;
    private Long callId;
    private Long groupId;
    private Long user_id;
    private Long start_call_time;

    public Long getStart_call_time() {
        return start_call_time;
    }

    public void setStart_call_time(Long start_call_time) {
        this.start_call_time = start_call_time;
    }

    public Long getEnd_call_time() {
        return end_call_time;
    }

    public void setEnd_call_time(Long end_call_time) {
        this.end_call_time = end_call_time;
    }

    private Long end_call_time;

    public int getIs_removed() {
        return is_removed;
    }

    public void setIs_removed(int is_removed) {
        this.is_removed = is_removed;
    }

    private int is_removed;

    // 6th Migration...
    private Long call_state_code;
    private Long response_type_code;
    private Long duration;
    private Long notification_sent;

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Participants() {

    }

    public Long getCall_state_code() {
        return call_state_code;
    }

    public void setCall_state_code(Long call_state_code) {
        this.call_state_code = call_state_code;
    }

    public Long getResponse_type_code() {
        return response_type_code;
    }

    public void setResponse_type_code(Long response_type_code) {
        this.response_type_code = response_type_code;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getNotification_sent() {
        return notification_sent;
    }

    public void setNotification_sent(Long notification_sent) {
        this.notification_sent = notification_sent;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

}
