package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

/**
 * Created by AhadAs on 9/17/2017.
 */


public class Groups {
    public static final String TABLE_NAME = "groups";

    private Long groupId;
    private String groupName;
    private Long createdAt;
    private Long updatedAt;
    private Long typeId;
    private int isIndividual;
    private int isLocalGroup;
    private int hasMessages;
    private int muted;

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    private String groupType;
    public int getMuted() {
        return muted;
    }

    public void setMuted(int muted) {
        this.muted = muted;
    }
    public static String getTableName() {
        return TABLE_NAME;
    }

    private String groupPicUrl;
    private Long createdBy;
    private String groupDescription;


    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }


    public Groups() {
    }
//    public List<User> getMembers() {
//        return getUserFromGroup;
//    }
//
//    public void setMembers(List<User> getUserFromGroup) {
//        this.getUserFromGroup = getUserFromGroup;
//    }


    public int getHasMessages() {
        return hasMessages;
    }

    public void setHasMessages(int hasMessages) {
        this.hasMessages = hasMessages;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getGroupPicUrl() {
        return groupPicUrl;
    }

    public void setGroupPicUrl(String groupPicUrl) {
        this.groupPicUrl = groupPicUrl;
    }

    public int getIsIndividual() {
        return isIndividual;
    }

    public void setIsIndividual(int isIndividual) {
        this.isIndividual = isIndividual;
    }

    public boolean isIndividualGroup() {
        return isIndividual == 1;
    }

    public boolean isLocalGroup() {
        return isLocalGroup == 1;
    }

    public int getIsLocalGroup() {
        return isLocalGroup;
    }

    public void setIsLocalGroup(int isLocalGroup) {
        this.isLocalGroup = isLocalGroup;
    }
}
