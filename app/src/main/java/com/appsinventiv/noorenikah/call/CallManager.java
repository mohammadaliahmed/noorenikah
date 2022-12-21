package com.appsinventiv.noorenikah.call;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Context;
import android.content.Intent;


import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.callWebRtc.InitiateCallScreenActivity;
import com.appsinventiv.noorenikah.callWebRtc.InitiatorVideoCallActivity;

import java.util.ArrayList;

/**
 * Created by JunaidAy on 11/1/2017.
 */

public class CallManager implements CallHandler {

 String firebaseid;
    String groupname;
    private Context mContext;
    private Long mGroupId;
    private Long mUserId;
    private String mGroupName;
    private String mGroupCover;
    private CallType mCallType;
    private Long callId;
    private ArrayList<UserModel> mCallParticipantList;
    private Long callerId;

    public CallManager(Context context,String groupname, Long groupId, Long userId, ArrayList<UserModel> callParticipantList) {
        this.mContext = context;
        this.mGroupId = groupId;
        this.mUserId = userId;
        this.groupname=groupname;
        mCallParticipantList = callParticipantList;
    }

    public enum CallType {
        INDIVIDUAL_AUDIO,
        INDIVIDUAL_VIDEO,
        GROUP_AUDIO,
        GROUP_VIDEO,
        REJOIN_ADMIN,
        PARTICIPANT_REJOIN
    }

    public CallManager(String firebaseid, Context context, String test, Long groupId, Long userId, ArrayList<UserModel> callParticipantList) {
        this.mContext = context;
        this.mGroupId = groupId;
        this.mUserId = userId;
        this.firebaseid = firebaseid;
        mCallParticipantList = callParticipantList;
    }

    public CallManager(Context context, Long groupId, Long userId, ArrayList<UserModel> mCallParticipantList, String groupName, String groupCover) {
        this.mContext = context;
        this.mGroupId = groupId;
        this.mUserId = userId;
        this.mGroupCover = groupCover;
        this.mGroupName = groupName;
        this.mCallParticipantList = mCallParticipantList;
    }

    public CallManager(Context context, Long callId, Long groupId, Long userId, ArrayList<UserModel> mCallParticipantList, String groupName, String groupCover) {
        this.mContext = context;
        this.mGroupId = groupId;
        this.mUserId = userId;
        this.mGroupCover = groupCover;
        this.mGroupName = groupName;
        this.mCallParticipantList = mCallParticipantList;
        this.callId = callId;
    }

    public CallManager(Context context, Long callId,Long callerId, Long groupId, Long userId, ArrayList<UserModel> mCallParticipantList, String groupName, String groupCover) {
        this.mContext = context;
        this.mGroupId = groupId;
        this.mUserId = userId;
        this.mGroupCover = groupCover;
        this.mGroupName = groupName;
        this.mCallParticipantList = mCallParticipantList;
        this.callId = callId;
        this.callerId = callerId;
    }

    private void initiateAudioCall() {
//        Intent intent = new Intent(mContext, InitiateCallScreenActivity.class);
//        intent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
//        intent.putExtra(Constants.IntentExtra.CALL_TYPE, CallType.INDIVIDUAL_AUDIO);
//        intent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
//        intent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantList));
//        mContext.startActivity(intent);
        Intent intent = new Intent(mContext, InitiateCallScreenActivity.class);
        intent.putExtra(Constants.IntentExtra.INTENT_GROUP_NAME, groupname);
        intent.putExtra(Constants.IntentExtra.USER_FIREBASEID, firebaseid);
        intent.putExtra(Constants.IntentExtra.USER_ID, mUserId);
        intent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        intent.putExtra(Constants.IntentExtra.CALL_TYPE, CallType.INDIVIDUAL_AUDIO);
        intent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
        intent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantList));
        mContext.startActivity(intent);

    }



    private void initiateVideoCall() {
        Intent intent = new Intent(mContext, InitiatorVideoCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        intent.putExtra(Constants.IntentExtra.USER_FIREBASEID, firebaseid);

        intent.putExtra(Constants.IntentExtra.CALL_TYPE, CallType.INDIVIDUAL_VIDEO);
        intent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
        intent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantList));
        mContext.startActivity(intent);
    }

    // interface implementation
    @Override
    public void callHandler(CallType callType) {
        this.mCallType = callType;
        switch (callType) {
            case INDIVIDUAL_AUDIO:
               initiateAudioCall();
                break;
            case INDIVIDUAL_VIDEO:
                initiateVideoCall();
                break;
//            case GROUP_AUDIO:
//                initiateGroupCall();
//                break;
//            case GROUP_VIDEO:
//                break;
//            case REJOIN_ADMIN:
//                initiateRejoinAdminCall();
//                break;
//            case PARTICIPANT_REJOIN:
//                initiateParticipantRejoinCall();
            default:
                break;
        }
    }


}
