package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

/**
 * Created by AhadAs on 9/17/2017.
 */

public class GroupType {
    public static final String TABLE_NAME = "group_type";

    private Long groupTypeId;
    private String groupTypeName;

    public Long getGroupTypeId() {
        return groupTypeId;
    }

    public GroupType() {
    }

    public void setGroupTypeId(Long groupTypeId) {
        this.groupTypeId = groupTypeId;
    }

    public String getGroupTypeName() {
        return groupTypeName;
    }

    public void setGroupTypeName(String groupTypeName) {
        this.groupTypeName = groupTypeName;
    }
}
