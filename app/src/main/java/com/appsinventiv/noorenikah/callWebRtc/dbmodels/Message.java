package com.appsinventiv.noorenikah.callWebRtc.dbmodels;


import java.io.Serializable;

/**
 * Created by AhadAs on 8/30/2017.
 */

public class Message implements Serializable {
    public static final String TABLE_NAME = "messages";

    private Long message_id;
    private String type;
    private Long created_at;
    private String status;
    private Long group_id;
    private Long user_id;
    private String messageText;
    private Long video_id;
    private Long image_id ;
    private Long document_id ;
    private String m_status ;
    private Long updated_by ;
    private Long updated_at ;
    private Long audio_id ;
    private String video_url;
    private String image_url;
    private String audio_url;

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }



    public Long getVideo_id() {
        return video_id;
    }

    public void setVideo_id(Long video_id) {
        this.video_id = video_id;
    }

    public Long getImage_id() {
        return image_id;
    }

    public void setImage_id(Long image_id) {
        this.image_id = image_id;
    }

    public Long getAudio_id() {
        return audio_id;
    }

    public void setAudio_id(Long audio_id) {
        this.audio_id = audio_id;
    }

    public Long getDocument_id() {
        return document_id;
    }

    public void setDocument_id(Long document_id) {
        this.document_id = document_id;
    }

    public Long getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(Long updated_by) {
        this.updated_by = updated_by;
    }

    public Long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Long updated_at) {
        this.updated_at = updated_at;
    }


    public String getM_status() {
        return m_status;
    }

    public void setM_status(String m_status) {
        this.m_status = m_status;
    }


    public Message() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Long getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Long message_id) {
        this.message_id = message_id;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (message_id != null ? !message_id.equals(message.message_id) : message.message_id != null)
            return false;

        return true;
//        if (type != null ? !type.equals(message.type) : message.type != null) return false;
//        if (created_at != null ? !created_at.equals(message.created_at) : message.created_at != null)
//            return false;
//        if (status != null ? !status.equals(message.status) : message.status != null) return false;
//        if (group_id != null ? !group_id.equals(message.group_id) : message.group_id != null)
//            return false;
//        if (user_id != null ? !user_id.equals(message.user_id) : message.user_id != null)
//            return false;
//        if (messageText != null ? !messageText.equals(message.messageText) : message.messageText != null)
//            return false;
//        if (video_url != null ? !video_url.equals(message.video_url) : message.video_url != null)
//            return false;
//        if (image_url != null ? !image_url.equals(message.image_url) : message.image_url != null)
//            return false;
//        return audio_url != null ? audio_url.equals(message.audio_url) : message.audio_url == null;

    }

    public boolean isEqual(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (message_id != null ? !message_id.equals(message.message_id) : message.message_id != null)
            return false;

        if (type != null ? !type.equals(message.type) : message.type != null) return false;
        if (created_at != null ? !created_at.equals(message.created_at) : message.created_at != null)
            return false;
//        if (status != null ? !status.equals(message.status) : message.status != null) return false;
        if (group_id != null ? !group_id.equals(message.group_id) : message.group_id != null)
            return false;
        if (user_id != null ? !user_id.equals(message.user_id) : message.user_id != null)
            return false;
        if (messageText != null ? !messageText.equals(message.messageText) : message.messageText != null)
            return false;
        if (video_id != null ? !video_id.equals(message.video_id) : message.video_id != null)
            return false;
        if (image_id != null ? !image_id.equals(message.image_id) : message.image_id != null)
            return false;
        return audio_id != null ? audio_id.equals(message.audio_id) : message.audio_id == null;

    }


    @Override
    public int hashCode() {
        int result = message_id != null ? message_id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (created_at != null ? created_at.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (group_id != null ? group_id.hashCode() : 0);
        result = 31 * result + (user_id != null ? user_id.hashCode() : 0);
        result = 31 * result + (messageText != null ? messageText.hashCode() : 0);
        result = 31 * result + (video_id != null ? video_id.hashCode() : 0);
        result = 31 * result + (image_id != null ? image_id.hashCode() : 0);
        result = 31 * result + (audio_id != null ? audio_id.hashCode() : 0);
        return result;
    }
}
