package com.appsinventiv.noorenikah.callWebRtc.groupCallParticipant;


import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.dbmodels.Participants;

public class GroupCallparticipantStatus {

    public Participants participants;
    public UserModel mUser;
    public EventFromService eventFromService;
    public boolean mCallHold;
    public boolean mCallMuted;
    public boolean mNativeCallRecived;

    public boolean ismOnNativeCall() {
        return mOnNativeCall;
    }

    public void setmOnNativeCall(boolean mOnNativeCall) {
        this.mOnNativeCall = mOnNativeCall;
    }

    public boolean mOnNativeCall;


    public static GroupCallparticipantStatus getInstance(UserModel user, EventFromService eventFromService) {
        GroupCallparticipantStatus groupCallparticipantStatus = new GroupCallparticipantStatus();
        groupCallparticipantStatus.setmUser(user);
        groupCallparticipantStatus.setEventFromService(eventFromService);
        return groupCallparticipantStatus;
    }

    public Participants getParticipants() {
        return participants;
    }

    public void setParticipants(Participants participants) {
        this.participants = participants;
    }


    public EventFromService getEventFromService() {
        return eventFromService;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getmUser().getId().equals(((GroupCallparticipantStatus) obj).getmUser().getId());
    }

    public void setEventFromService(EventFromService eventFromService) {
        this.eventFromService = eventFromService;
    }


    public UserModel getmUser() {
        return mUser;
    }

    public void setmUser(UserModel mUser) {
        this.mUser = mUser;
    }

    public boolean ismCallMuted() {
        return mCallMuted;
    }

    public void setmCallMuted(boolean mCallMuted) {
        this.mCallMuted = mCallMuted;
    }



    public boolean ismCallHold() {
        return mCallHold;
    }

    public void setmCallHold(boolean mCallHold) {
        this.mCallHold = mCallHold;
    }



}
