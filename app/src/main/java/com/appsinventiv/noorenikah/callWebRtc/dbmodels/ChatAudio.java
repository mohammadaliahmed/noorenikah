package com.appsinventiv.noorenikah.callWebRtc.dbmodels;

import java.io.Serializable;


/**
 * Created by ZaidAs on 24/01/2018.
 */


public class ChatAudio implements Serializable {
    public static final String TABLE_NAME = "chat_audio";

    private Long chat_audio_id;
    private Long group_id;
    private Long file_size;
    private String title;
    private String extension;
    private String audio_state;
    private long duration;
    private String url;

    public ChatAudio() {
    }

    public Long getChat_audio_id() {
        return chat_audio_id;
    }

    public void setChat_audio_id(Long chat_audio_id) {
        this.chat_audio_id = chat_audio_id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getAudio_state() {
        return audio_state;
    }

    public void setAudio_state(String audio_state) {
        this.audio_state = audio_state;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
