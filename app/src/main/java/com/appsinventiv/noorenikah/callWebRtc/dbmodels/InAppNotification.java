package com.appsinventiv.noorenikah.callWebRtc.dbmodels;


/**
 * Created by AhadAs on 8/30/2017.
 */

public class InAppNotification {
    public static final String TABLE_NAME = "notification";

    private int notification_id;
    private Long message_id;
    private String type;
    private String messageText;
    private Long group_id;
    private Long user_id;

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }


    public int getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(int notification_id) {
        this.notification_id = notification_id;
    }


    public InAppNotification() {

    }

    public Long getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Long message_id) {
        this.message_id = message_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

}
