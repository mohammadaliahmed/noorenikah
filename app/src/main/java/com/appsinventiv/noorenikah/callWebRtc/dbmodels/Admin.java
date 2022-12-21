package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


/**
 * Created by AhadAs on 8/30/2017.
 */


public class Admin implements Serializable {
    public static final String TABLE_NAME = "admin";

    @SerializedName("groupInfoId")
    private Long group_info_id;
    @SerializedName("groupUserId")
    private Long group_user_id;
    private Long muted;
    private Long admin;
    @SerializedName("groupId")
    private Long group_id;

    public Admin() {
    }


    public static String getTableName() {

        return TABLE_NAME;
    }

    public Long getGroup_info_id() {
        return group_info_id;
    }

    public void setGroup_info_id(Long group_info_id) {
        this.group_info_id = group_info_id;
    }

    public Long getGroup_user_id() {
        return group_user_id;
    }

    public void setGroup_user_id(Long group_user_id) {
        this.group_user_id = group_user_id;
    }

    public Long getMuted() {
        return muted;
    }

    public void setMuted(Long muted) {
        this.muted = muted;
    }

    public Long getAdmin() {
        return admin;
    }

    public void setAdmin(Long admin) {
        this.admin = admin;
    }

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }
}
