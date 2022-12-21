package com.appsinventiv.noorenikah.callWebRtc.dbmodels;


import java.io.Serializable;

/**
 * Created by AhadAs on 8/30/2017.
 */

public class Chatimage implements Serializable {
    public static final String TABLE_NAME = "image";

    private Long chat_image_id;
    private Long group_id;
    private Long file_size;
    private String title;
    private String thumbnail;
    private String extension;
    private String image_state;
    private String url;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Chatimage() {
    }


    public Long getChat_image_id() {
        return chat_image_id;
    }

    public void setChat_image_id(Long chat_image_id) {
        this.chat_image_id = chat_image_id;
    }

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public Long getFile_size() {
        return file_size;
    }

    public void setFile_size(Long file_size) {
        this.file_size = file_size;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getImage_state() {
        return image_state;
    }

    public void setImage_state(String image_state) {
        this.image_state = image_state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
