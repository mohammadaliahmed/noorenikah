package com.appsinventiv.noorenikah.callWebRtc.CallListner;


import com.appsinventiv.noorenikah.Models.UserModel;

/**
 * Created by JunaidAy on 11/20/2017.
 */

public interface AudioCallBackInterface {
    void callEnded();

    void callerCancelledCall();

    void startTimer();

    void updateCallingToConnectingState();

    void callerEndedAudioCall();

    void holdAudioCall(UserModel user, boolean isOnNativeCall);

    void unholdAudioCall(UserModel user);

    void reConnectingCall();

    void finishCall();

    void reconnectionEstablished();

    void updateStats(double bandwidth, double bitrate, double totalBandwidth, long packets);

    void callRinging();

    void callRinging(UserModel user);

    void updateCallingToConnectingState(UserModel user);

    void reConnectingCall(UserModel user);

    void reconnectionEstablished(UserModel user);

    void startTimer(UserModel user);

    void userEndedCall(UserModel user);

    void finishCall(UserModel user);

    void userDisconnected(UserModel user);

    void userDisconnected();

    void muteCall(UserModel user);

    void unMuteCall(UserModel user);

    void disableVideoCall(UserModel user);

    void enableVideoCall(UserModel user);
}
