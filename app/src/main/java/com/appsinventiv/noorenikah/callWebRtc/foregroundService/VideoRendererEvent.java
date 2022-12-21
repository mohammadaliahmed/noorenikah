package com.appsinventiv.noorenikah.callWebRtc.foregroundService;


import com.appsinventiv.noorenikah.callWebRtc.VideoProxyRenderer;

public class VideoRendererEvent {
    private final VideoProxyRenderer videoProxyRenderer;

    public VideoRendererEvent(VideoProxyRenderer videoProxyRenderer) {
        this.videoProxyRenderer = videoProxyRenderer;
    }

    public VideoProxyRenderer getMessage() {
        return videoProxyRenderer;
    }
}
