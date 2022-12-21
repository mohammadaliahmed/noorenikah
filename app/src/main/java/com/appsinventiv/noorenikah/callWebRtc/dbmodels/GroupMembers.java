package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

/**
 * Created by AhadAs on 9/17/2017.
 */

public class GroupMembers {

    private Long groupMember1Id;
    private Long groupId;
    private Long groupMember2Id;
    private String groupmember1FirebaseId;
    private String groupmember2FirebaseId;

    public GroupMembers(Long groupMember1Id, Long groupId, Long groupMember2Id, String groupmember1FirebaseId, String groupmember2FirebaseId) {
        this.groupMember1Id = groupMember1Id;
        this.groupId = groupId;
        this.groupMember2Id = groupMember2Id;
        this.groupmember1FirebaseId = groupmember1FirebaseId;
        this.groupmember2FirebaseId = groupmember2FirebaseId;
    }

    public GroupMembers() {
    }

    public Long getGroupMember1Id() {
        return groupMember1Id;
    }

    public void setGroupMember1Id(Long groupMember1Id) {
        this.groupMember1Id = groupMember1Id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getGroupMember2Id() {
        return groupMember2Id;
    }

    public void setGroupMember2Id(Long groupMember2Id) {
        this.groupMember2Id = groupMember2Id;
    }

    public String getGroupmember1FirebaseId() {
        return groupmember1FirebaseId;
    }

    public void setGroupmember1FirebaseId(String groupmember1FirebaseId) {
        this.groupmember1FirebaseId = groupmember1FirebaseId;
    }

    public String getGroupmember2FirebaseId() {
        return groupmember2FirebaseId;
    }

    public void setGroupmember2FirebaseId(String groupmember2FirebaseId) {
        this.groupmember2FirebaseId = groupmember2FirebaseId;
    }
}

