package com.appsinventiv.noorenikah.callWebRtc;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;


import com.appsinventiv.noorenikah.Utils.ApplicationClass;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.CallListner.AudioCallBackInterface;

import org.webrtc.MediaConstraints;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JunaidAy on 11/7/2017.
 */

public class CallWebRTCManger {
    //steps for initiating call
    //1) create PeerFactory..
    //2) get Mian Constraint on the base of audio / video ( group +  invidual )
    //3) create MediaStream Object
    //4) Get type of Media stream  that you need audio / video //Note Local track already added to that stream
    //5 ) Create Socket Connection... ( it will use all upper things )

    private PeerFactory mPeerFactory;
    private SetMediaStream mSetMediaStream;
    private SocketConnectionManager mSocketManager;
    private CallManager.CallType mCallType;
    private Context mContext;// most Probably it will be off application level, but also check for activity....
    private MediaConstraints mediaConstraints; // main media Contraints .....
    private Long mGroupId;
    private AudioManager audioManager;
    private AudioCallBackInterface mCallInterface;
    int oldAudioMode;
    int oldRingerMode;
    boolean isSpeakerPhoneOn;
 //   int oldAudioMode;
    private boolean previousMicrophoneMute;
    private VideoProxyRenderer videoProxyRenderer;
    private VideoCapturer videoCapturer;
    private List<VideoRenderer.Callbacks> remoteRenderers =
            new ArrayList<VideoRenderer.Callbacks>();
    boolean isLoudSpeakerEanabled;



    private void saveOldAudioMangerState() {
        oldAudioMode = audioManager.getMode();
        oldRingerMode = audioManager.getRingerMode();
        isSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        previousMicrophoneMute = audioManager.isMicrophoneMute();
    }

    public CallWebRTCManger(Context context, CallManager.CallType callType, Long groupId, AudioCallBackInterface mCallInterface) {
        this.mContext = context;
        this.mCallType = callType;
        this.mGroupId = groupId;
        this.mCallInterface = mCallInterface;
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public CallWebRTCManger(Context context, CallManager.CallType callType, Long groupId,
                            AudioCallBackInterface mCallInterface,
                            VideoCapturer videoCapturer,
                            VideoProxyRenderer proxyRenderer,
                            List<VideoRenderer.Callbacks> remoteRenderers) {
        this.mContext = context;
        this.mCallType = callType;
        this.mGroupId = groupId;
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        this.mCallInterface = mCallInterface;
        this.videoCapturer = videoCapturer;
        this.videoProxyRenderer = proxyRenderer;
        this.remoteRenderers = remoteRenderers;
    }


    //create peerFactory
    private void createPeerFactory() {
        mPeerFactory = new PeerFactory();
        mPeerFactory.initPeerConnectionFactory(ApplicationClass.getInstance().getApplicationContext(), mCallType);
    }

    // get mediaConstraint
    private void createMainConstraints() {
        mediaConstraints = MediaConstraintManager.getInstance().getMediaConstraints(this.mCallType);
    }
    //create MediaStream ( Also take account which mediaStream you want to get...)
    // 1) simple stream allow call / not
    // 2) localAudio stream ( depend on call you making...)
    //3) LocalVideo stream ( depend on call you making...)

    private void createMediaStream() {
        mSetMediaStream = new SetMediaStream(this.mPeerFactory.getPeerConnectionFactory());
        mSetMediaStream.initMediaStream();
        //todo add local audio / video track in this stream
        switch (mCallType) {
            case GROUP_AUDIO:
            case INDIVIDUAL_AUDIO:
                mSetMediaStream.createAudioSource();
                mSetMediaStream.addAudioStream();
                //     mSetMediaStream.isVideoCall(false);
                break;
            case GROUP_VIDEO:
            case INDIVIDUAL_VIDEO:
                mSetMediaStream.createVideoAndAddStream(videoCapturer,videoProxyRenderer);
                break;
        }
    }

    private void createSocketConnection() {
        mSocketManager = new SocketConnectionManager(mSetMediaStream, mPeerFactory.getPeerConnectionFactory(),
                mediaConstraints, mGroupId, this.mCallType, this.mCallInterface);
    }

    private void createVideoSocketConnection() {
        mSocketManager = new SocketConnectionManager(mSetMediaStream, mPeerFactory.getPeerConnectionFactory(),
                mediaConstraints, mGroupId, this.mCallType, this.mCallInterface,remoteRenderers);
    }

    private void createRecieverSocketConnection() { // simple call reciever constructor...
        mSocketManager = new SocketConnectionManager(mGroupId, this.mCallType, this.mCallInterface);
    }

    private void createVideoRecieverSocketConnection() { // simple call reciever constructor...
        mSocketManager = new SocketConnectionManager(mGroupId, this.mCallType, this.mCallInterface, this.remoteRenderers);
    }

    private void createRecieverSocketConnection(boolean isInitialize) { // Group call Reciver constructor..
        mSocketManager = new SocketConnectionManager(mGroupId, this.mCallType, this.mCallInterface, isInitialize);
    }

    public void initializeSocketComponents() {
        mSocketManager.initializeComponents(mSetMediaStream, mPeerFactory.getPeerConnectionFactory(),
                mediaConstraints, mGroupId, this.mCallType, this.mCallInterface);
    }

    public void updateCallType(CallManager.CallType callType) {
        this.mCallType = callType;
        if (mSocketManager != null) {
            mSocketManager.updateCallType(callType);
        }
    }

    public void sendReadyToEventToPeer() {
        mSocketManager.sendReadyEvent();
    }

    public void holdIngoingAudioCall(boolean isOnNativeCall) {
        for (org.webrtc.AudioTrack track : mSetMediaStream.getmLocalMediaStream().audioTracks) {
            track.setEnabled(false);
        }
        mSocketManager.callHoldEvent(isOnNativeCall);
    }

    public void holdIngoingVideoCall(boolean isOnNativeCall) {
        for (org.webrtc.AudioTrack track : mSetMediaStream.getmLocalMediaStream().audioTracks) {
            track.setEnabled(false);
        }
        for (org.webrtc.VideoTrack track : mSetMediaStream.getmLocalMediaStream().videoTracks) {
            track.setEnabled(false);
        }
        mSocketManager.callHoldEvent(isOnNativeCall);
    }

    public void disconnectUser(Long userId) {
        if (mSocketManager != null) {
            mSocketManager.disconnectUser(userId, true);
        }
    }

//    public void muteIngoingCall(){
//        for(org.webrtc.AudioTrack track :mSetMediaStream.getmLocalMediaStream().audioTracks){
//            track.setEnabled(false);
//        }
//    }
//
//    public void

    public void unHoldIngoingAudioCall(boolean flag) {
        for (org.webrtc.AudioTrack track : mSetMediaStream.getmLocalMediaStream().audioTracks) {
            track.setEnabled(true);
        }
        if (flag) {
            mSocketManager.callUnholdEvent();
        }
    }

    public void unHoldIngoingVideoCall(boolean flag) {
        for (org.webrtc.VideoTrack track : mSetMediaStream.getmLocalMediaStream().videoTracks) {
            track.setEnabled(true);
        }
        if (flag) {
            mSocketManager.callUnholdEvent();
        }
    }


    private void initSocketMangerConnection() {
        mSocketManager.initializeSocketConnection();
    }

    public void startCallForInitiator() {
        createPeerFactory();
        createMainConstraints();
        createMediaStream();
        createSocketConnection();
        initSocketMangerConnection();
        disableLoudSpeaker();
    }

    public void startVideoCallForInitiator(){
        createPeerFactory();
        createMainConstraints();
        createMediaStream();
        createVideoSocketConnection();
        initializeSocketComponents();
        initSocketMangerConnection();
    }

    public void startGroupCallForInitiator() {
        createPeerFactory();
        createMainConstraints();
        createMediaStream();
        createSocketConnection();
        initSocketMangerConnection();
        disableLoudSpeaker();
    }

    public void startCallForReciever() {
        createPeerFactory();
        createMainConstraints();
        createMediaStream();
        createRecieverSocketConnection();
        initSocketMangerConnection();
        disableLoudSpeaker();
    }

    public void startVideoCallForReciever() {
        createPeerFactory();
        createMainConstraints();
        createMediaStream();
        createVideoRecieverSocketConnection();
        initSocketMangerConnection();
    }

    public void startGroupCallForReciever() {
        createPeerFactory();
        createMainConstraints();
        createMediaStream();
        createRecieverSocketConnection(false);
        initSocketMangerConnection();
        disableLoudSpeaker();
    }


    //todo in ending of call...
    // close socket ccnnection..
    //getTime of call...
    //remove calling client
    //set socket object to null
    //set stream to null
    //media to null
    // peer factory to null..
    public void endCall() {
        removeClient();
        getCallTime();
//        removepeerFactory();
//        closeSocketConnection();
//        removeSocketManager();
//        removeStreamMedia();
//        removeMediaConstraints();
        disableLoudSpeaker();
        closeAll();
    }

    public void endAllClient(boolean flag) {
        disableLoudSpeaker();
        closeAllClient(flag);
    }

    public void closeRecieverSocketConnection() {
        removeClient();
        disableLoudSpeaker();
        //createPeerFactory();
        if (mPeerFactory != null) {
            mPeerFactory.getPeerConnectionFactory().dispose();
            mPeerFactory = null;
        }
        if (mSetMediaStream != null) {
            mSetMediaStream.getAudioSource().dispose();
            mSetMediaStream = null;
            mediaConstraints = null;
        }
        mGroupId = -1L;
        if (mSocketManager != null)
            mSocketManager.getmSocket().close();
        mSocketManager = null;
    }

    public void closePeerConnection() {
        disableLoudSpeaker();
        removeClient();
        closeConnection();
    }

    private void closeConnection() {
        if (mSocketManager != null) {
            mSocketManager.closePeerConnection();
        }
    }

    private void closeAll() {
        if (mSocketManager != null) {
            mSocketManager.closeAll();
        }
    }

    private void closeAllClient(boolean flag) {
        if (mSocketManager != null) {
            mSocketManager.endAllClients(flag);
        }
    }

    private void closeSocketConnection() {
        if (mSocketManager != null) {
            mSocketManager.closeSocket();
        }
    }

    private void getCallTime() {
        //todo make a mechanism to get call time...
    }

    private void removeClient() {
        if (mSocketManager != null) {
            this.mSocketManager.removeClientInfo();
        }
    }

    private void removeSocketManager() {
        this.mSocketManager = null;
    }

    private void removeStreamMedia() {
        this.mSetMediaStream = null;
    }

    private void removeMediaConstraints() {

        this.mediaConstraints = null;
    }

    private void removepeerFactory() {
        this.mPeerFactory.removePeerConnection();
        this.mPeerFactory = null;
    }

    public void enableDisableLoudSpeaker() {
        assert audioManager != null;
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setMicrophoneMute(previousMicrophoneMute);
            audioManager.abandonAudioFocus(null);
        } else {
            saveOldAudioMangerState();
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMicrophoneMute(false);
            requestAudioFocus();
        }
    }

    public void enableLoudSpeaker() {
        isLoudSpeakerEanabled =true;
        assert audioManager != null;
        if (!audioManager.isSpeakerphoneOn()) {
            saveOldAudioMangerState();
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            requestAudioFocus();
        }
    }

    public void restoreOldStateAudioManager(){
        if(audioManager!=null && isLoudSpeakerEanabled ){
            audioManager.setMode(oldAudioMode);
            audioManager.setRingerMode(oldRingerMode);
            audioManager.setSpeakerphoneOn(isSpeakerPhoneOn);
            audioManager.setMicrophoneMute(previousMicrophoneMute);
        }
    }

    public void disableLoudSpeaker() {
        assert audioManager != null;
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setMicrophoneMute(previousMicrophoneMute);
        audioManager.abandonAudioFocus(null);
    }

    public void enableMicrophone() {
//        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        assert audioManager != null;
//        if (!audioManager.isMicrophoneMute()) {
//            audioManager.setMicrophoneMute(true);
//        }
        for (org.webrtc.AudioTrack track : mSetMediaStream.getmLocalMediaStream().audioTracks) {
            track.setEnabled(true);
        }
    }

    public int getNumberOfConnections() {
        return mSocketManager.getNumberOfConnections();
    }

    public void disableMicrophone() {
//        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        assert audioManager != null;
//        if (audioManager.isMicrophoneMute()) {
//            audioManager.setMicrophoneMute(false);
//        }

        for (org.webrtc.AudioTrack track : mSetMediaStream.getmLocalMediaStream().audioTracks) {
            track.setEnabled(false);
        }
    }

    public void disableVideo(){
        for (org.webrtc.VideoTrack track : mSetMediaStream.getmLocalMediaStream().videoTracks) {
            track.setEnabled(false);
        }
    }

    public void enableVideo(){
        for (org.webrtc.VideoTrack track : mSetMediaStream.getmLocalMediaStream().videoTracks) {
            track.setEnabled(true);
        }
    }

    public void stopHandler() {
        if (mSocketManager != null) {
            mSocketManager.stopRepeatingTask();
        }
    }

    public void muteCall() {
        if (mSocketManager != null) {
            mSocketManager.muteCall();
        }
    }

    public void unMuteCall() {
        if (mSocketManager != null) {
            mSocketManager.unMuteCall();
        }
    }

    public void disableVideoCall() {
        if (mSocketManager != null) {
            mSocketManager.disableVideCall();
        }
    }

    public void enableVideoCall() {
        if (mSocketManager != null) {
            mSocketManager.enableVideoCall();
        }
    }


    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            AudioFocusRequest focusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(
                                    new AudioManager.OnAudioFocusChangeListener() {
                                        @Override
                                        public void onAudioFocusChange(int i) {
                                        }
                                    })
                            .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

}