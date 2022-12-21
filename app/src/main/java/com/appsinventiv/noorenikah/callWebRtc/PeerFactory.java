package com.appsinventiv.noorenikah.callWebRtc;

import android.content.Context;


import com.appsinventiv.noorenikah.call.CallManager;

import org.webrtc.PeerConnectionFactory;

/**
 * Created by JunaidAy on 11/7/2017.
 */

public class PeerFactory {
    private PeerConnectionFactory peerConnectionFactory;

    public void removePeerConnection() {
        this.peerConnectionFactory = null;
    }

    public PeerConnectionFactory getPeerConnectionFactory() {
        return peerConnectionFactory;
    }

    public void setPeerConnectionFactory(PeerConnectionFactory peerConnectionFactory) {
        this.peerConnectionFactory = peerConnectionFactory;
    }

    public void initPeerConnectionFactory(Context mApplicationContext, CallManager.CallType mCalltype) {
        switch (mCalltype) {
            case GROUP_AUDIO:
            case INDIVIDUAL_AUDIO:
                PeerConnectionFactory.initializeAndroidGlobals(mApplicationContext, true, false, true);
                break;
            case GROUP_VIDEO:
            case INDIVIDUAL_VIDEO:
                PeerConnectionFactory.initializeAndroidGlobals(mApplicationContext, true, true, true);
                break;
            default:
        }
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = new PeerConnectionFactory(options);
    }
}
