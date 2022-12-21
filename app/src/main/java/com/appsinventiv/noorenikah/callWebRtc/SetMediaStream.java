package com.appsinventiv.noorenikah.callWebRtc;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

//

/**
 * Created by JunaidAy on 11/5/2017.
 */

public class SetMediaStream {
    //todo
    // 1) init peeFactory
    //2) add / create videosoures
    //3) add / create audio source
    //4) add renderer in videoSource
    //5)disble video for audio call from local track
    //6) create media stream
    //7) add video track in stream
    //8) add audio track in stream
    private MediaStream mLocalMediaStream;
    private VideoTrack mLocalVideoTrack;
    private AudioTrack mLocalAudioTrack;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private PeerConnectionFactory mPeerConnectionFactory;

    public SetMediaStream(PeerConnectionFactory peerConnectionFactory){
       this.mPeerConnectionFactory = peerConnectionFactory;
    }

    public MediaStream getmLocalMediaStream() {
        return mLocalMediaStream;
    }

    public void setmLocalMediaStream(MediaStream mLocalMediaStream) {
        this.mLocalMediaStream = mLocalMediaStream;
    }

//    public void createVideoSource(VideoCapturerAndroid capturer) {
//        MediaConstraintManager mediaConstraintManager = new MediaConstraintManager();
//        mediaConstraintManager.setmVideoConstraint();
//        videoSource = mPeerConnectionFactory.createVideoSource(capturer, mediaConstraintManager.getmVideoConstraint());
//        mLocalVideoTrack =
//                mPeerConnectionFactory.createVideoTrack("1", videoSource);
//    }

    public void createAudioSource() {
        MediaConstraintManager mediaConstraintManager = new MediaConstraintManager();
        mediaConstraintManager.setmAudioContraint();
        audioSource = mPeerConnectionFactory.createAudioSource(mediaConstraintManager.getmAudioContraint());
        mLocalAudioTrack =
                mPeerConnectionFactory.createAudioTrack("2", audioSource);

    }

    public void createVideoAndAddStream(VideoCapturer videoCapturer, VideoProxyRenderer videoProxyRenderer){
        MediaConstraintManager mediaConstraintManager = new MediaConstraintManager();
        mediaConstraintManager.setmAudioContraint();
        audioSource = mPeerConnectionFactory.createAudioSource(mediaConstraintManager.getmAudioContraint());
        mLocalAudioTrack =
                mPeerConnectionFactory.createAudioTrack("2", audioSource);
        addAudioStream();
        mLocalMediaStream.addTrack(createVideoTrack(videoCapturer,videoProxyRenderer));
    }

    public void addVideoRenderer(VideoRenderer renderer) {
        this.mLocalVideoTrack.addRenderer(renderer);
    }

    public void isVideoCall(boolean flag) {
        //true for video / false for audio
        this.mLocalVideoTrack.setEnabled(flag);

    }

    public void initMediaStream() {
        //todo should have same peerConnectionFactory
        this.mLocalMediaStream = this.mPeerConnectionFactory.createLocalMediaStream("3");
    }

    public void addAudioStream() {
        this.mLocalMediaStream.addTrack(this.mLocalAudioTrack);
    }
    public AudioTrack getAudioSource(){
        return mLocalAudioTrack;
    }

    public void addVideoStream() {
        this.mLocalMediaStream.addTrack(this.mLocalVideoTrack);
    }

    private VideoTrack createVideoTrack(VideoCapturer capturer, VideoProxyRenderer videoProxyRenderer) {
        videoSource = mPeerConnectionFactory.createVideoSource(capturer);
        VideoTrack videoTrack = mPeerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        videoTrack.setEnabled(true);
        capturer.startCapture(1280, 720, 30);
        videoTrack.addRenderer(new VideoRenderer(videoProxyRenderer));
        return videoTrack;
    }
}
