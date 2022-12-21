package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

/**
 * Created by JunaidAy on 11/10/2017.
 */

public class ResponseType {
    public static final String TABLE_NAME = "response_type";
    private Long ResponseTypeId;
    private String responseType;
    private Long responseTypeCode;

    public ResponseType(){

    }

    public Long getResponseTypeId() {
        return ResponseTypeId;
    }

    public void setResponseTypeId(Long responseTypeId) {
        ResponseTypeId = responseTypeId;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public Long getResponseTypeCode() {
        return responseTypeCode;
    }

    public void setResponseTypeCode(Long responseTypeCode) {
        this.responseTypeCode = responseTypeCode;
    }

}
