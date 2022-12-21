package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

/**
 * Created by JunaidAy on 11/10/2017.
 */

public class CallState {
    public static final String TABLE_NAME = "call_state";
    private Long callStateId;
    private String callStateType;
    private Long callStateCode;

    public CallState(){

    }

    public Long getCallStateId() {
        return callStateId;
    }

    public void setCallStateId(Long callStateId) {
        this.callStateId = callStateId;
    }

    public String getCallStateType() {
        return callStateType;
    }

    public void setCallStateType(String callStateType) {
        this.callStateType = callStateType;
    }

    public Long getCallStateCode() {
        return callStateCode;
    }

    public void setCallStateCode(Long callStateCode) {
        this.callStateCode = callStateCode;
    }

}
