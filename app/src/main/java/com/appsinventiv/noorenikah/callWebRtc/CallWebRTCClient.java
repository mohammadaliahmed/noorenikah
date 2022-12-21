package com.appsinventiv.noorenikah.callWebRtc;



import com.appsinventiv.noorenikah.Models.UserModel;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

/**
 * Created by JunaidAy on 11/5/2017.
 */

public class CallWebRTCClient {
    //    public boolean isRoomCreated = false;
    public boolean isRemoteStreamAdded = false;
    public UserModel mUser;
    public PeerConnection mPeerConnection;
    public MediaStream mMediaStream;
    private String remoteSocketId;
    private String localSocketId;
    private boolean isInitiator;

    public CallWebRTCClient() {
    }

    public String getLocalSocketId() {
        return localSocketId;
    }

    public void setLocalSocketId(String localSocketId) {
        this.localSocketId = localSocketId;
    }


    //    public boolean isRoomCreated() {
//        return isRoomCreated;
//    }
//
//    public void setRoomCreated(boolean roomCreated) {
//        isRoomCreated = roomCreated;
//    }

    public boolean isRemoteStreamAdded() {
        return isRemoteStreamAdded;
    }

    public void setRemoteStreamAdded(boolean remoteStreamAdded) {
        isRemoteStreamAdded = remoteStreamAdded;
    }

    public UserModel getmUser() {
        return mUser;
    }

    public void setmUser(UserModel mUser) {
        this.mUser = mUser;
    }

    public PeerConnection getmPeerConnection() {
        return mPeerConnection;
    }

    public void setmPeerConnection(PeerConnection mPeerConnection) {
        this.mPeerConnection = mPeerConnection;
    }

    public MediaStream getmMediaStream() {
        return mMediaStream;
    }

    public void setmMediaStream(MediaStream mMediaStream) {
        this.mMediaStream = mMediaStream;
    }

    public String getRemoteSocketId() {
        return remoteSocketId;
    }

    public void setRemoteSocketId(String remoteSocketId) {
        this.remoteSocketId = remoteSocketId;
    }

    public boolean isInitiator() {
        return isInitiator;
    }

    public void setInitiator(boolean initiator) {
        isInitiator = initiator;
    }
}
