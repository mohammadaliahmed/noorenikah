package com.appsinventiv.noorenikah.callWebRtc.dbmodels;



public class CallSummary {
    public static final String TABLE_NAME = "call_summary";



    public Long getCall_id() {
        return call_id;
    }

    public void setCall_id(Long call_id) {
        this.call_id = call_id;
    }

    public Long getSummary_time() {
        return summary_time;
    }

    public void setSummary_time(Long summary_time) {
        this.summary_time = summary_time;
    }

    public String getSummary_message() {
        return summary_message;
    }

    public void setSummary_message(String summary_message) {
        this.summary_message = summary_message;
    }

    public String getSummary_type() {
        return summary_type;
    }

    public void setSummary_type(String summary_type) {
        this.summary_type = summary_type;
    }

    public Long getCall_summary_id() {
        return call_summary_id;
    }

    public void setCall_summary_id(Long call_summary_id) {
        this.call_summary_id = call_summary_id;
    }

    private Long call_summary_id;
    private Long call_id;
    private Long summary_time;
    private String summary_message;
    private String summary_type;

}

