
package com.appsinventiv.noorenikah.callWebRtc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.widget.Chronometer;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.appsinventiv.noorenikah.Activities.RecieveCallActivity;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.ApplicationClass;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.Utils.UserManager;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.CallListner.AudioCallBackInterface;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallEndType;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;
import com.appsinventiv.noorenikah.callWebRtc.dbmodels.Participants;
import com.appsinventiv.noorenikah.callWebRtc.groupCallParticipant.GroupCallparticipantStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class GroupAudioRecieverServiceFirebase extends Service implements AudioCallBackInterface {
    //variable declaration...
    CallWebRTCManger callWebRTCManger;
    Long mGroupId = -1L;
    Long mCallId = -1L;
    CallManager.CallType mCallType;
    Timer mTimer = new Timer();
    ToneGenerator mToneGenerator;
    Enum callState;
    String mUserName;
    String mUserPost;
    String mUserDpUrl;
    Long mUserID;
    String callingType;
    Chronometer mChronometer;
    Long mCallStartTime = -1L;
    Ringtone ringtone;
    boolean isCallStartedFlag = true;
    Enum muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
    Enum loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE;
    Enum holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
    boolean isReconnecting = true;
    ToneGenerator mReconnectingTone;
    Thread mThread;
    private ArrayList<UserModel> mParticipants;
    private String mGroupName;
    private String mGroupCover;
    private HashMap<Long, GroupCallparticipantStatus> mCallParticipantMap = new HashMap<>();
    private String mCallerId;
    private ArrayList<Participants> mCallParticipantList;
    private Long mRoomId;
    Timer mStatusDialingTask = new Timer();
    Timer mRinginStatusDialingTask = new Timer();
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private String mParticipantJson;
    private boolean mCallerOnHold = false;
    private boolean isIndividualNativeCall = false;
    Handler mHoldCallEnd;
    private boolean endHoldCall = false;
    private boolean mParticipantOnMute = false;
    private boolean isScheduleCall = false;
    private boolean isRejoined = false;
    Vibrator mVibrator;
    NotificationCompat.Builder notificationBuilder;
    private Long mScheduleCallEnd;
    private Timer mScheculeCallTimer;
    Timer mCallNotConnectedTimer;
    private TimerTask callEndTask;
    TimerTask callNotConnectedTask; // if in 30 sec no call is connected than use this task...


    //default service constructor
    public GroupAudioRecieverServiceFirebase() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerLocalBroadCast();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getActionFromIntent(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        removeTimer();
        stopReconnectingDialer();
        stopReconnectingDialer();
        removeCallEndingTask();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastEventFromActivity);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMissedCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCallerCancelCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNativeMobileIncomingCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRedialToAGroupMember);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAddParticipantToGroupCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRemoveMemberFromCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateSchdeuleCall);
        super.onDestroy();
    }

    private void updateFromDialingStatus() {
        //todo for each...
        HashMap<Long, GroupCallparticipantStatus> map = new HashMap<Long, GroupCallparticipantStatus>(mCallParticipantMap);
        for (Long key : map.keySet()) {
            if (Objects.equals(key, UserManager.getInstance().getUserIfLoggedIn().getId())) {
                continue;
            }
            if (!Objects.equals(map.get(key).getmUser().getId(), mCallerId))
                if (map.get(key).getEventFromService().equals(EventFromService.CALL_DIALING)) {
                    map.get(key).setEventFromService(EventFromService.USER_NOT_ACCESSIBLE);
                }
        }
        startStatusRingingTask();
        sendLatestParticipantsStatus();
    }

    private void startStatusDialingTask() {
        final Long delay = 30000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //todo make new event user is not reciving call..
                updateFromDialingStatus();
            }
        };
        mStatusDialingTask.schedule(task, delay);
    }

    private void updateFromRingingStatus() {
        //todo for each...
        HashMap<Long, GroupCallparticipantStatus> map = new HashMap<Long, GroupCallparticipantStatus>(mCallParticipantMap);
        for (Long key : map.keySet()) {
            if (Objects.equals(key, Long.valueOf(UserManager.getInstance().getUserIfLoggedIn().getId()))) {
                continue;
            }
            if (map.get(key).getEventFromService().equals(EventFromService.CALL_RINGING)) {
                map.get(key).setEventFromService(EventFromService.USER_NOT_ANSWERING);
            }
        }
        sendLatestParticipantsStatus();
    }

    private void startStatusRingingTask() {
        final Long delay = 60000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //todo make new event user is not reciving call..
                updateFromRingingStatus();
            }
        };
        mRinginStatusDialingTask.schedule(task, delay);
    }

    private void playVibration() {
        startTimerTask();
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 1000, 500};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern\
        mVibrator.vibrate(pattern, 0);
    }

    private void removeVibration() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private void checkIfMobileonSilent() {
        AudioManager audioManager = (AudioManager) (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    playSoundForIncommingcall();
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    startTimerTask();
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    playVibration();
                    break;
            }
        } else {
            playSoundForIncommingcall();
        }
    }


    // register Local Broadcast
    private void registerLocalBroadCast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mNativeMobileIncomingCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_NATIVE_CALL_STARTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastEventFromActivity,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMissedCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_MISSED_CALL));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallerCancelCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALLER_CANCEL_AUDIO_CAll));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallRejected,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALL_REJECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRedialToAGroupMember,
                new IntentFilter(Constants.Broadcasts.BROADCAST_REDIAL_TO_A_GROUP_MEMBER));
        LocalBroadcastManager.getInstance(this).registerReceiver(mAddParticipantToGroupCall,
                new IntentFilter(Constants.Broadcasts.ADD_MEMBER_TO_GROUP_CALL));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRemoveMemberFromCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_REMOVE_MEMBER_FROM_CAll));
        LocalBroadcastManager.getInstance(this).registerReceiver(mParticipantRejoin,
                new IntentFilter(Constants.Broadcasts.BROADCAST_PARTICIPANT_REJOIN_CALL));
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateSchdeuleCall, new IntentFilter(
                Constants.Broadcasts.BROADCAST_UPDATE_CALL_END_TIME));
    }

    //local Broadcast callbacks

    private BroadcastReceiver mNativeMobileIncomingCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    if (isCallStartedFlag) {
                        rejectIncomingIndividualCall();
                    } else {
                        nativeCallRecived(mCallType);
                    }
                    break;
                case GROUP_AUDIO:
                    if (isCallStartedFlag) {
                        rejectIncommingGroupAudioCall();
                    } else {
                        nativeCallRecived(mCallType);
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mCallRejected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCallType != null) {
                switch (mCallType) {
                    case GROUP_AUDIO:
                        Long callId = intent.getLongExtra("callId", -1L);
                        Long userId = intent.getLongExtra("userId", -1L);
                        if (Objects.equals(callId, mCallId)) {
                            if (userId != -1L) {
                                if (mCallParticipantMap.containsKey(userId)) {
                                    UserModel user = mCallParticipantMap.get(userId).getmUser();
                                    updateUserStatusToBusy(userId);
                                    updateUserStatusList(user, EventFromService.CALL_REJECTED);
                                }
                            }
                        }
                        break;
                    case INDIVIDUAL_AUDIO:
                        break;
                }

            }
        }
    };

    private BroadcastReceiver mRedialToAGroupMember = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long callId = intent.getLongExtra("callId", -1L);
            Long userId = intent.getLongExtra("participantId", -1L);
            if (Objects.equals(callId, mCallId)) {
                if (mCallParticipantMap.get(userId).getmUser() != null) {
                    UserModel user = mCallParticipantMap.get(userId).getmUser();
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setEventFromService(EventFromService.CALL_DIALING);
                    sendLatestParticipantsStatus();
                    updateSingleUserStatus(userId);
                }
            }

        }
    };

    private BroadcastReceiver mAddParticipantToGroupCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    addParticipantinGroupCall(intent);
                    break;
                case GROUP_AUDIO:
                    addNewPartipantinGroupCall(intent);
                    break;
            }
        }
    };

    private void nativeCallRecived(CallManager.CallType callType) {
        isIndividualNativeCall = true;
        endHoldCall = true;
        holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
        callWebRTCManger.holdIngoingAudioCall(true);
        if (callType != null) {
            if (callType.equals(CallManager.CallType.INDIVIDUAL_AUDIO)) {
                holdCallEndSchedular();
            } else if (callType.equals(CallManager.CallType.GROUP_AUDIO)) {
                holdGroupCallEndSchedular();
            }
        }
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.NATIVE_CALL_HOLD);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }


    private void holdGroupCallEndSchedular() {
        mHoldCallEnd = new Handler(Looper.getMainLooper());
        mHoldCallEnd.postDelayed(holdGroupCallTimeOut, 180000);
    }

    private void holdCallEndSchedular() {
        mHoldCallEnd = new Handler(Looper.getMainLooper());
        mHoldCallEnd.postDelayed(holdCallTimeOut, 180000);
    }

    Runnable holdCallTimeOut = new Runnable() {
        @Override
        public void run() {
            if (endHoldCall) {
                finishConnectedIndividualCall(); //this function can change value of mInterval.
            }
        }
    };

    Runnable holdGroupCallTimeOut = new Runnable() {
        @Override
        public void run() {
            if (endHoldCall) {
                finishConnectedGroupCall(); //this function can change value of mInterval.
            }
        }
    };


    private void addParticipantinGroupCall(Intent intent) {
        Long callId = intent.getLongExtra("call_id", -1L);
        if (Objects.equals(callId, mCallId)) {
//            String participantsJson = intent.getStringExtra(Constants.IntentExtra.PARTICIPANTS);
//            Type listType = new TypeToken<ArrayList<Long>>() {
//            }.getType();
//            ArrayList<Long> userList = StringUtils.getGson().fromJson(participantsJson, listType);
//            mParticipants = new ArrayList<>();
//            for (Long id :
//                    userList) {
//                if (!Objects.equals(id, UserManager.getInstance().getUserIfLoggedIn().getId()())) {
//                    mParticipants.add(StudentApp.getInstance().getHubDatabase().userDao().getUser(id));
//                }
//            }
//            String callParticipantJson = intent.getStringExtra(Constants.IntentExtra.CALL_PARTICIPANTS);
//            listType = new TypeToken<ArrayList<Participants>>() {
//            }.getType();
//            mCallParticipantList = StringUtils.getGson().fromJson(callParticipantJson, listType);
//            // createParticipantMap();
//            createParticipantMapForIndividual();
//            mGroupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
//            Groups group = StudentApp.getInstance().getHubDatabase().groupDao().getGroupById(mGroupId);
//            if (group != null) {
//                mGroupName = group.getGroupName();
//            }
//            Intent startIntent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
//            intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.FINISH_ACTIVITY);
//            LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(startIntent);
//            mCallType = CallManager.CallType.GROUP_AUDIO;
//            callWebRTCManger.updateCallType(mCallType);
//            createNotification();
//            startGroupReciverActivity();
        }
    }

    private void addNewPartipantinGroupCall(Intent intent) {
//        Long callId = intent.getLongExtra("call_id", -1L);
//        if (Objects.equals(callId, mCallId)) {
//            String callParticipantJson = intent.getStringExtra(Constants.IntentExtra.CALL_PARTICIPANTS);
//            Type listType = new TypeToken<ArrayList<Participants>>() {
//            }.getType();
//            ArrayList<Participants> callParticipantList = StringUtils.getGson().fromJson(callParticipantJson, listType);
//            for (Participants participant : callParticipantList) {
//                if (!Objects.equals(participant.getUser_id(), UserManager.getInstance().getUserIfLoggedIn().getId()())) {
//                    StudentApp.getInstance().getHubDatabase().participantsDao().addParticipants(participant);
//                    if (!mCallParticipantMap.containsKey(participant.getUser_id())) {
//                        if (participant.getIs_removed() == 0) {
//                            mCallParticipantList.add(participant);
//                            mParticipants.add(StudentApp.getInstance().getHubDatabase().userDao().getUser(participant.getUser_id()));
//                            mCallParticipantMap.put(participant.getUser_id(), GroupCallparticipantStatus.getInstance(StudentApp.getInstance().getHubDatabase().userDao().getUser(participant.getUser_id()), EventFromService.CALL_DIALING));
//                            if (participant.getResponse_type_code() == 801L) {
//                                mCallParticipantMap.get(participant.getUser_id()).setEventFromService(EventFromService.USER_BUSY);
//                            }
//                            if (participant.getResponse_type_code() == 804L) {
//                                mCallParticipantMap.get(participant.getUser_id()).setEventFromService(EventFromService.CALL_DIALING);
//                                updateSingleUserStatus(participant.getUser_id());
//                            }
//                        }
//                    }
//                }
//            }
//            Long groupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
//            Groups group = StudentApp.getInstance().getHubDatabase().groupDao().getGroupById(groupId);
//            if (group != null) {
//                mGroupId = groupId;
//                mGroupName = group.getGroupName();
//            }
//            createNotification();
//            Intent updateListIntent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
//            updateListIntent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.NEW_MEMBER_ADDED);
//            updateListIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantMap));
//            LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(updateListIntent);
//        }
    }

    private void createNotification() {
        if (callState.equals(EventFromService.CALL_RINGING)) {
            createNotificationForGroupCall("Call From ", mGroupName, Constants.ForegroundService.ACCEPT_CALL, -1L);
        } else if (callState.equals(EventFromService.CALL_DIALING)) {

        } else if (callState.equals(EventFromService.CALL_RECONNECTED)) {
            createNotificationForGroupCall("Group Call :", mGroupName, Constants.ForegroundService.END_CALL, 2L);
        } else if (callState.equals(EventFromService.CALL_CONNNECTED)) {
            createNotificationForGroupCall("Connecting call with: ", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        } else if (callState.equals(EventFromService.CALL_STARTED)) {
            createNotificationForGroupCall("Group call :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        } else {
            createNotificationForGroupCall("Group call :", mGroupName, Constants.ForegroundService.END_CALL, 2L);// todo check what kind of call type based on that make notification.
        }
    }



    private void updateUserStatusToBusy(Long userId) {
        if (mCallParticipantMap.containsKey(userId)) {
            Participants participants = mCallParticipantMap.get(userId).getParticipants();
            if (participants != null) {
                Long time = getTimeFromChronometer();
                participants.setDuration(time);
                participants.setCall_state_code(703L);
                participants.setResponse_type_code(802L);
//                StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
            }
        }
    }

    private BroadcastReceiver mUpdateSchdeuleCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long updateTime = intent.getLongExtra("updateTime", 0);
            if (isScheduleCall) {
                removeCallEndingTask();
                Long duration = mScheduleCallEnd - Calendar.getInstance().getTimeInMillis();
                Long update = duration + updateTime;
                startCallEndTimerTask(update);
            }
        }
    };


    private BroadcastReceiver mBroadCastEventFromActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                EventsFromActivity eventsFromActivity = (EventsFromActivity) intent.getSerializableExtra(Constants.IntentExtra.EVENT_FROM_UI);
                if (eventsFromActivity != null) {
                    if (eventsFromActivity.equals(EventsFromActivity.END_CALL_PRESSED)) {
                        if (mCallType != null) {
                            switch (mCallType) {
                                case GROUP_AUDIO:
                                    finishConnectedGroupCall();
                                    closeForeGroundService();
                                    break;
                                case INDIVIDUAL_AUDIO:
                                    finishConnectedIndividualCall();
                                    closeForeGroundService();
                                    break;
                            }
                        } else {
                            closePeerConnection();
                            closeForeGroundService();
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.END_CALL_INCOMING)) {
                        stopIncommingCall();
                        if (mCallType != null) {
                            switch (mCallType) {
                                case GROUP_AUDIO:
                                    rejectIncommingGroupAudioCall();
                                    break;
                                case INDIVIDUAL_AUDIO:
                                    rejectIncomingIndividualCall();
                                    break;
                            }
                        } else {
                            closePeerConnection();
                            closeForeGroundService();
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.ENABLE_LOUD_SPEAKER_PRESSED)) {
                        loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_PRESSED;
                        callWebRTCManger.enableDisableLoudSpeaker();
                    } else if (eventsFromActivity.equals(EventsFromActivity.DISABLE_LOUD_SPEAKER_PRESSED)) {
                        loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE;
                        callWebRTCManger.enableDisableLoudSpeaker();
                    } else if (eventsFromActivity.equals(EventsFromActivity.MUTE_BUTTON_PRESSED)) {
                        callWebRTCManger.muteCall();
                        muteState = CallWidgetState.MUTE_BUTTON_PRESSED;
                        callWebRTCManger.disableMicrophone();
                    } else if (eventsFromActivity.equals(EventsFromActivity.UNMUTE_BUTTON_PRESSD)) {
                        callWebRTCManger.unMuteCall();
                        muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
                        callWebRTCManger.enableMicrophone();
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_LATEST_TIME)) {
                        sendTime();
                    } else if (eventsFromActivity.equals(EventsFromActivity.ACCEPT_CALL_PRESSED)) {
                        switch (mCallType) {
                            case GROUP_AUDIO:
                                acceptIncommingGroupAudioCallProcedure();
                                break;
                            case INDIVIDUAL_AUDIO:
                                acceptIncommingCallIndividualProcedure();
                                break;
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_UPDATED_PARTICIPANTS)) {
                        sendLatestParticipantsStatus();
                    } else if (eventsFromActivity.equals(EventsFromActivity.HOLD_BUTTON_PRESSED)) {
                        muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
                        holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
                        callWebRTCManger.holdIngoingAudioCall(false);
                    } else if (eventsFromActivity.equals(EventsFromActivity.UNHOLD_BUTTON_PRESSED)) {
                        removeNativeIncommingCallTask();
                        holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
                        callWebRTCManger.unHoldIngoingAudioCall(true);
                    } else if (eventsFromActivity.equals(EventsFromActivity.DISCONNECT_USER)) {
                        Long userId = intent.getLongExtra(Constants.IntentExtra.USER_ID, -1);
                        callWebRTCManger.disconnectUser(Long.valueOf(mCallerId));
                    } else if (eventsFromActivity.equals(EventsFromActivity.REDIAL_USER)) {
                        Long userId = intent.getLongExtra(Constants.IntentExtra.USER_ID, -1);
                        redialGroupMember(userId);
                    } else if (eventsFromActivity.equals(EventsFromActivity.START_CALLING_ACTIVITY)) {
                        if (mCallType != null) {
                            switch (mCallType) {
                                case INDIVIDUAL_AUDIO:
                                    startCallReciverActivity();
                                    break;
                                case GROUP_AUDIO:
                                    startGroupReciverActivity();
                                    break;
                            }
                        } else {
                            //  finishConnectedIndividualCall();
                            closePeerConnection();
                            closeForeGroundService();
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_UPDATED_PARTICIPANTS)) {
                        sendLatestParticipantsStatus();
                    }
                }
            }
        }
    };

    private void removeNativeIncommingCallTask() {
        if (isIndividualNativeCall) {
            isIndividualNativeCall = false;
            endHoldCall = false;
            if (mCallType != null) {
                switch (mCallType) {
                    case GROUP_AUDIO:
                        if (mHoldCallEnd != null) {
                            mHoldCallEnd.removeCallbacks(holdGroupCallTimeOut);
                            mHoldCallEnd.removeCallbacksAndMessages(null);
                            mHoldCallEnd = null;
                        }
                        break;
                    case INDIVIDUAL_AUDIO:
                        if (mHoldCallEnd != null) {
                            mHoldCallEnd.removeCallbacks(holdCallTimeOut);
                            mHoldCallEnd.removeCallbacksAndMessages(null);
                            mHoldCallEnd = null;
                        }
                        break;
                }
            }
        }
    }

    private void startCallReciverActivity() {
        Intent notificationIntent = new Intent(this, RecieveCallActivity.class);
        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
        notificationIntent.putExtra(Constants.IntentExtra.USER_POST, mUserPost);
        notificationIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        notificationIntent.putExtra(Constants.IntentExtra.USER_IMAGE, mUserDpUrl);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
        notificationIntent.putExtra(Constants.IntentExtra.CALLING_TYPE, callingType);
        notificationIntent.putExtra(Constants.IntentExtra.CALLER_USER_ID, mUserID);
        if (callState.equals(EventFromService.CALL_DIALING) || callState.equals(EventFromService.CALL_RINGING)) {
            notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
        } else {
            notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        }
        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(notificationIntent);
    }


    private void startGroupReciverActivity() {
        Long time;
        if (callState.equals(EventFromService.CALL_RINGING) || callState.equals(EventFromService.CALL_DIALING)) {
            time = -1L;
        } else {
            time = 2L;
        }
//        Intent notificationIntent = new Intent(this, GroupAudioRecieverActivity.class);
//        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
//        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
//        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
//        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_COVER_URL, mGroupCover);
//        notificationIntent.putExtra(Constants.IntentExtra.GROUP_NAME_CHANGED, mGroupName);
//        notificationIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mParticipants));
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        GroupAudioRecieverServiceFirebase.this.startActivity(notificationIntent);
    }

    private void sendLatestParticipantsStatus() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.USER_STATUS);
        intent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantMap));
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private BroadcastReceiver mMissedCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCallType == null) {
                return;
            }
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    isCallStartedFlag = false;
//                    GlobalMethods.updateUiToUpdateCallLogs();
                    if (ringtone != null) {
                        ringtone.stop();
                    }
                    Long callId = intent.getLongExtra("callId", -1);
                    if (Objects.equals(callId, mCallId)) {
                        closeForeGroundService(); // todo make a local broadcast in which service will be close but home actitivy will not be created
                    }
                    if (callWebRTCManger != null) {
                        callWebRTCManger.closeRecieverSocketConnection();
                    }
                    break;
                case GROUP_AUDIO:
                    missedCallCloseProcedure(intent);
                    break;
            }

        }
    };

    private BroadcastReceiver mRemoveMemberFromCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long callId = intent.getLongExtra("callId", -1L);
            long userId = intent.getLongExtra("userId", -1L);
            if (Objects.equals(callId, mCallId)) {
                if (mCallParticipantMap.containsKey(userId)) {
                    mCallParticipantMap.remove(userId);
//                    UserModel user = StudentApp.getInstance().getHubDatabase().userDao().getUser(userId);
//                    if (mParticipants.contains(user)) {
//                        mParticipants.remove(user);
//                    }
                    removeMemberFromCallParticipantList(userId);
                }
            }
        }
    };

    private BroadcastReceiver mParticipantRejoin = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Long userId = intent.getLongExtra("userId", -1);
            Long callId = intent.getLongExtra("callId", -1);
            if (Objects.equals(mCallId, callId)) {
                if (mCallParticipantMap.containsKey(userId)) {
                    mCallParticipantMap.get(userId).setEventFromService(EventFromService.CALL_CONNNECTED);
                    sendLatestParticipantsStatus();
                }
            }
        }
    };


    private BroadcastReceiver mCallerCancelCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCallType == null) {
                stopIncommingCall();
                if (callWebRTCManger != null) {
                    callWebRTCManger.closeRecieverSocketConnection();
                }
//                GlobalMethods.updateUiToUpdateCallLogs();
                isCallStartedFlag = false;
                closeForeGroundService();
            }
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    stopIncommingCall();
                    if (ringtone != null) {
                        ringtone.stop();
                    }
                    if (callWebRTCManger != null) {
                        callWebRTCManger.closeRecieverSocketConnection();
                    }
//                    GlobalMethods.updateUiToUpdateCallLogs();
                    isCallStartedFlag = false;
                    closeForeGroundService();
                    break;
                case GROUP_AUDIO:
                    stopIncommingCall();
                    if (callState.equals(EventFromService.CALL_DIALING) || callState.equals(EventFromService.CALL_RINGING)) {
                        if (callWebRTCManger != null) {
                            //todo maintain logs that caller has canceled call
                            callWebRTCManger.stopHandler();
                            callWebRTCManger.closeRecieverSocketConnection();
                        }
//                        GlobalMethods.updateUiToUpdateCallLogs();
                        isCallStartedFlag = false;
                        closeForeGroundService();
                    }
                    break;
            }
        }
    };


    //helper function of onStartCommand
    private void getActionFromIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ForegroundService.ACTION_MAIN)) {
                checkCallType(intent);
                switch (mCallType) {
                    case GROUP_AUDIO:
                        if (isRejoined) {
                            iniatiateRejoinCallProcedure();
                        } else {
                            if (callState != null && callState.equals(EventFromService.CALL_RINGING)) {
                                individualToGroupCallProcedure();
                            } else {
                                initiateGroupCallProcedure();
                            }
                        }
                        break;
                    case INDIVIDUAL_AUDIO:
                        initiateIndividualCallProcedure();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.ACCEPT_CALL)) {
                switch (mCallType) {
                    case GROUP_AUDIO:
                        acceptIncommingGroupAudioCallProcedure();
                        break;
                    case INDIVIDUAL_AUDIO:
                        acceptIncommingCallIndividualProcedure();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.END_CALL)) {
                if (mCallType == null) {
                    finishConnectedIndividualCall(); // for now to save crash...
                }
                switch (mCallType) { // todo make check of mcallType check if null or not
                    case GROUP_AUDIO:
                        finishConnectedGroupCall();
                        closeForeGroundService();
                        break;
                    case INDIVIDUAL_AUDIO:
                        finishConnectedIndividualCall();
                        closeForeGroundService();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.CANCEL_INCOMING_CALL)) {
                stopIncommingCall();
                switch (mCallType) {
                    case GROUP_AUDIO:
                        rejectIncommingGroupAudioCall();
                        break;
                    case INDIVIDUAL_AUDIO:
                        rejectIncomingIndividualCall();
                        break;
                }
            }
        }
    }

    private void iniatiateRejoinCallProcedure() {
        createRejoinGroupCallManager();
        callState = EventFromService.CALL_CONNNECTED;
        createNotificationForGroupCall("Connecting ", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        startCallNotConnectedEndTask();
    }

    private void createRejoinGroupCallManager() {
        callWebRTCManger = new CallWebRTCManger(GroupAudioRecieverServiceFirebase.this,
                mCallType,
                mRoomId, this);
        callWebRTCManger.startGroupCallForReciever();
        callWebRTCManger.disableLoudSpeaker();
        callWebRTCManger.initializeSocketComponents();
        callWebRTCManger.sendReadyToEventToPeer();
    }

    private void checkCallType(Intent intent) {
        mCallType = (CallManager.CallType) intent.getSerializableExtra(Constants.IntentExtra.CALL_TYPE);
        switch (mCallType) {
            case GROUP_AUDIO:
//                getValuesFromIntentForGroupAudioCall(intent);
                break;
            case INDIVIDUAL_AUDIO:
                getValuesFromIntentForIndividual(intent);
                break;
        }
    }

    private void removeMemberFromCallParticipantList(Long userId) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.REMOVE_MEMBER_FROM_CALL);
        intent.putExtra(Constants.IntentExtra.USER_ID, userId);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void getValuesFromIntentForGroupAudioCall(Intent intent) {
//        mGroupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
//        mRoomId = intent.getLongExtra(Constants.IntentExtra.INTENT_ROOM_ID, -1);
//        mCallId = intent.getLongExtra(Constants.IntentExtra.CALL_ID, -1);
//        callingType = intent.getStringExtra(Constants.IntentExtra.CALLING_TYPE);
//        mGroupCover = intent.getStringExtra(Constants.IntentExtra.INTENT_GROUP_COVER_URL);
//        mGroupName = intent.getStringExtra(Constants.IntentExtra.GROUP_NAME_CHANGED);
////        mGroupName = GlobalMethods.decodeMessageFromUtf8Format(mGroupName);
//        mCallerId = intent.getLongExtra(Constants.IntentExtra.CALLER_USER_ID, -1);
//        String callObject = intent.getStringExtra(Constants.IntentExtra.CALL_OBJECT);
//        Type callType = new TypeToken<Calls>() {
//        }.getType();
//        mCall = StringUtils.getGson().fromJson(callObject, callType);
//        String participantsJson = intent.getStringExtra(Constants.IntentExtra.PARTICIPANTS);
//        Type listType = new TypeToken<ArrayList<UserModel>>() {
//        }.getType();
//        mParticipants = StringUtils.getGson().fromJson(participantsJson, listType);
//        String callParticipantJson = intent.getStringExtra(Constants.IntentExtra.CALL_PARTICIPANTS);
//        listType = new TypeToken<ArrayList<Participants>>() {
//        }.getType();
//        mCallParticipantList = StringUtils.getGson().fromJson(callParticipantJson, listType);
//        createParticipantMap();
//        isRejoined = intent.getBooleanExtra(Constants.IntentExtra.IS_CALL_REJOINED, false);
//        isScheduleCall = intent.getBooleanExtra(Constants.IntentExtra.IS_SCHEDULE_CALL, false);
//        mScheduleCallEnd = intent.getLongExtra(Constants.IntentExtra.SCH_CALL_END_TIME,0);
    }

    private void getValuesFromIntentForIndividual(Intent intent) {
        mGroupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
        mCallId = intent.getLongExtra(Constants.IntentExtra.CALL_ID, -1);
        mUserDpUrl = intent.getStringExtra(Constants.IntentExtra.USER_IMAGE);
        mUserName = intent.getStringExtra(Constants.IntentExtra.USER_NAME);
        mUserPost = intent.getStringExtra(Constants.IntentExtra.USER_POST);
        callingType = intent.getStringExtra(Constants.IntentExtra.CALLING_TYPE);
        mUserID = intent.getLongExtra(Constants.IntentExtra.CALLER_USER_ID, -1);
        mParticipantJson = intent.getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        mRoomId = intent.getLongExtra(Constants.IntentExtra.INTENT_ROOM_ID, -1);
        mCallerId = intent.getStringExtra(Constants.IntentExtra.CALLER_USER_ID);
    }

    // if manager is already created...

    private void individualToGroupCallProcedure() {
        startStatusDialingTask();
        createNotificationForGroupCall("Call From ", mGroupName, Constants.ForegroundService.ACCEPT_CALL, -1L);
        if (callWebRTCManger != null) {
            mCallType = CallManager.CallType.GROUP_AUDIO;
            callWebRTCManger.updateCallType(mCallType);
            callWebRTCManger.updateCallType(mCallType);
        }
    }

    private void initiateGroupCallProcedure() {
        startStatusDialingTask();
        callState = EventFromService.CALL_RINGING;
        createNotificationForGroupCall("Call From ", mGroupName, Constants.ForegroundService.ACCEPT_CALL, -1L);
        //  playSoundForIncommingcall();
        checkIfMobileonSilent();
        createGroupReceiverCallManager();
    }

    private void initiateIndividualCallProcedure() {
        callState = EventFromService.CALL_RINGING;
        createNotificationForIndividual("Call From ", Constants.ForegroundService.ACCEPT_CALL, -1L);
        checkIfMobileonSilent();
        createIndividualCallManager();
    }


    private void playSoundForIncommingcall() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(GroupAudioRecieverServiceFirebase.this, notification);
            ringtone.play();
            startTimerTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGroupReceiverCallManager() {
        //make callWebrtc Manager
        callWebRTCManger = new CallWebRTCManger(GroupAudioRecieverServiceFirebase.this,
                mCallType,
                mRoomId, this);
        callWebRTCManger.startGroupCallForReciever();
        callWebRTCManger.disableLoudSpeaker();
    }

    private void createIndividualCallManager() {
        //make callWebrtc Manager
        callWebRTCManger = new CallWebRTCManger(GroupAudioRecieverServiceFirebase.this,
                CallManager.CallType.INDIVIDUAL_AUDIO,
                mRoomId, this);
        callWebRTCManger.startCallForReciever();
        callWebRTCManger.disableLoudSpeaker();
    }


    private void startTimerTask() {
        final Long delay = 60000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                stopIncommingCall();
                updateUserCallStatus();
            }
        };
        if (mTimer != null) {
            mTimer.schedule(task, delay);
        }
    }

    // helper functions of call back
    private void missedCallCloseProcedure(Intent intent) {
        isCallStartedFlag = false;
//        GlobalMethods.updateUiToUpdateCallLogs();
        stopIncommingCall();
        Long callId = intent.getLongExtra("callId", -1);
        if (Objects.equals(callId, mCallId)) {
            closeForeGroundService(); // todo make a local broadcast in which service will be close but home actitivy will not be created
        }
        if (callWebRTCManger != null) {
            callWebRTCManger.stopHandler();
            callWebRTCManger.closeRecieverSocketConnection();

        }
    }

    private void closeForeGroundService() {
        isCallStartedFlag = false;
        GroupAudioRecieverServiceFirebase.this.stopSelf();
        GroupAudioRecieverServiceFirebase.this.stopForeground(true);
//        GlobalMethods.callEnded();
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.FINISH_ACTIVITY);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void stopIncommingCall() {
        if (ringtone != null) {
            ringtone.stop();
        }
        removeVibration();
    }

    private void sendTime() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.SEND_LATEST_TIME);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        intent.putExtra(Constants.IntentExtra.MICROPHONE_STATE, muteState);
        intent.putExtra(Constants.IntentExtra.LOUDSPEAKER_STATE, loudSpeakerState);
        intent.putExtra(Constants.IntentExtra.HOLD_STATE, holdState);
        intent.putExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, mCallerOnHold);
        intent.putExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, mParticipantOnMute);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private Long getTimeFromChronometer() {
        if (mChronometer != null) {
            return SystemClock.elapsedRealtime() - mChronometer.getBase();
        } else {
            return -1L;
        }
    }

    private void updateSingleUserStatus(final Long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Handler dialingHandler = new Handler(Looper.getMainLooper());
                dialingHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (mCallParticipantMap.containsKey(userId)) {
                            if (mCallParticipantMap.get(userId) != null && mCallParticipantMap.get(userId).getmUser().getId().equals(userId)) {
                                if (mCallParticipantMap.get(userId).getEventFromService().equals(EventFromService.CALL_DIALING)) {
                                    mCallParticipantMap.get(userId).setEventFromService(EventFromService.USER_NOT_ACCESSIBLE);
                                    sendLatestParticipantsStatus();
                                }
                            }
                        }
                    }
                }, 30000L);//30000L
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallParticipantMap.containsKey(userId)) {
                            if (mCallParticipantMap.get(userId) != null && mCallParticipantMap.get(userId).getmUser().getId().equals(userId)) {
                                if (mCallParticipantMap.get(userId).getEventFromService().equals(EventFromService.CALL_RINGING)) {
                                    mCallParticipantMap.get(userId).setEventFromService(EventFromService.USER_NOT_ANSWERING);
                                    sendLatestParticipantsStatus();
                                }
                            }
                        }
                    }
                }, 90000L);//90000L
            }
        }).start();
    }


    private void updateUiForAcceptAction() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_ACCEPT);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void acceptIncommingGroupAudioCallProcedure() {
        stopIncommingCall();
        callWebRTCManger.stopHandler();
        acceptIncommingGroupAudioCall();
        updateUiForAcceptAction();
        removeTimer();
    }

    private void acceptIncommingCallIndividualProcedure() {
        stopIncommingCall();
        acceptIncommingIndividualCall();
        updateUiForAcceptAction();
        removeTimer();
    }

    private void updateConnectingStatus() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UPDATE_TO_CONNECTING_STATE);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void prepareGroupAudioCallManager() {
        stopIncommingCall();
        //todo update connecting and db logs
        updateConnectingStatus();
        callWebRTCManger.initializeSocketComponents();
        callWebRTCManger.sendReadyToEventToPeer();
        callState = EventFromService.CALL_CONNNECTED;
        createNotificationForGroupCall("Connecting call with: ", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        if (isScheduleCall) {
            calculateTimeDifference();
            startCallNotConnectedEndTask();
        }
    }

    private void calculateTimeDifference() {
        Long currentTime = mScheduleCallEnd - Calendar.getInstance().getTimeInMillis();
        startCallEndTimerTask(currentTime);
    }

    private void startCallEndTimerTask(final Long callEnd) {
        callEndTask = new TimerTask() {
            @Override
            public void run() {
                if (callEndTask != null) {
                    finishConnectedGroupCall();
                }
            }
        };
        mScheculeCallTimer = new Timer();
        mScheculeCallTimer.schedule(callEndTask, callEnd);
    }


    private void removeCallEndingTask() {
        if (mScheculeCallTimer != null) {
            if (callEndTask != null) {
                callEndTask.cancel();
                callEndTask = null;
            }
            mScheculeCallTimer.cancel();
            mScheculeCallTimer.purge();
            mScheculeCallTimer = null;
        }
    }

    private void removeCallNotConnectedTask() {
        if (mCallNotConnectedTimer != null) {
            if (callNotConnectedTask != null) {
                callNotConnectedTask.cancel();
                callNotConnectedTask = null;
            }
            mCallNotConnectedTimer.cancel();
            mCallNotConnectedTimer.purge();
            mCallNotConnectedTimer = null;
        }
    }

    private void startCallNotConnectedEndTask() {
        callNotConnectedTask = new TimerTask() {
            @Override
            public void run() {
                if (callNotConnectedTask != null) {
                    finishConnectedGroupCall();
                    closeForeGroundService();
                }
            }
        };
        mCallNotConnectedTimer = new Timer();
        mCallNotConnectedTimer.schedule(callNotConnectedTask, 30000L);
    }

    private void updateCallLogs(Long mCallId, Long callStateCode, Long responseType) {
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallStatusAndResponse(mCallId, callStateCode, responseType, 0L, 0L);
    }

    private void prepareIndividualCallManager(Long mCallId, Long callStateId, Long responseTypeId) {
        stopIncommingCall();
//        updateCallLogs(mCallId, callStateId, responseTypeId);
        updateConnectingStatus();


        callWebRTCManger.initializeSocketComponents();
        callWebRTCManger.sendReadyToEventToPeer();
        createNotificationForIndividual("Connected to ", Constants.ForegroundService.END_CALL, -1L);
    }


    // api implementations

    public void rejectIncomingIndividualCall() {


//        GlobalMethods.updateUiToUpdateCallLogs();
        if (callWebRTCManger != null) {
            callWebRTCManger.closeRecieverSocketConnection();
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.RECEIVER_REJECT_INCOMIING_CALL);
        callClossingTask.startEndCallProcedure();
        closeForeGroundService();

    }


    public void rejectIncommingGroupAudioCall() {
//        GlobalMethods.updateUiToUpdateCallLogs();

        if (callWebRTCManger != null) {
            callWebRTCManger.stopHandler();
            callWebRTCManger.closeRecieverSocketConnection();
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.RECIEVER_REJECT_GROUP_CALL);
        callClossingTask.startEndCallProcedure();
        closeForeGroundService();
    }

    public void acceptIncommingGroupAudioCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId()());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        CallRepository callRepository = new CallRepository();
//        callRepository.acceptGroupAudioCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    JSONArray jsonParticipants = jsonObject.getJSONArray("participants");
//                    mCallId = jsonCall.getLong("callId");
//                    Log.e("result", result.toString());
//                    Calls call = StringUtils.getGson().fromJson(jsonCall.toString(), Calls.class);
//                    StudentApp.getInstance().getHubDatabase().callsDao().updateCall(call);
//                    callWebRTCManger.stopHandler();
//                    for (int i = 0; i < jsonParticipants.length(); i++) {
//                        JSONObject object = jsonParticipants.getJSONObject(i);
//                        Participants participant = StringUtils.getGson().fromJson(object.toString(), Participants.class);
//                        StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participant);                    }
//                    prepareGroupAudioCallManager();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//                //todo make discussion on this one...
////                GlobalMethods.updateUiToUpdateCallLogs();
//                if (callWebRTCManger != null) {
//                    callWebRTCManger.closeRecieverSocketConnection();
//                }
//                closeForeGroundService();
//            }
//        });
    }

    public void acceptIncommingIndividualCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId()());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        CallRepository callRepository = new CallRepository();
//        callRepository.acceptCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    if (responseType == 803L) {
        prepareIndividualCallManager(mCallId, 0L, 0L);
//                    } else {
////                        GlobalMethods.updateUiToUpdateCallLogs();
//                        closeForeGroundService();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//                //todo make discussion on this one...
////                GlobalMethods.updateUiToUpdateCallLogs();
//                closeForeGroundService();
//            }
//        });
    }

    public void redialGroupMember(final Long userId) {
//        final HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId()());
//        params.put("callId", mCallId);
//        params.put("participantId", userId);
//        params.put("roomId", mRoomId);
//        CallRepository callRepository = new CallRepository();
//        callRepository.redialGroupMember(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    JSONArray jsonParticipants = jsonObject.getJSONArray("participants");
//                    mCallId = jsonCall.getLong("callId");
//                    for (int i = 0; i < jsonParticipants.length(); i++) {
//                        JSONObject object = jsonParticipants.getJSONObject(i);
//                        Participants participant = StringUtils.getGson().fromJson(object.toString(), Participants.class);
//                        if (Objects.equals(participant.getUser_id(), userId)) {
//                            mCallParticipantMap.get(participant.getUser_id()).setEventFromService(EventFromService.CALL_DIALING);
//                            sendLatestParticipantsStatus();
//                            updateSingleUserStatus(userId);
//                            break;
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
    }

    private void finishConnectedGroupCall() {
        removeNativeIncommingCallTask();
        final Long currentTime = Calendar.getInstance().getTimeInMillis();
        final Long duration;
        if (mChronometer != null) {
            duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometer.stop();
        } else {
            duration = -1L;
        }
        if (callWebRTCManger == null) {
            closePeerConnection();
            closeForeGroundService();
            return;
        }
        int numberofConnection = callWebRTCManger.getNumberOfConnections();
        if (numberofConnection > 1) {
            numberofConnection = 0;
        } else {
            numberofConnection = 1;
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, currentTime, duration, numberofConnection, CallEndType.END_GROUP_CALL);
        callClossingTask.startEndCallProcedure();
        closePeerConnection();
    }

    private void closePeerConnection() {
        if (callWebRTCManger != null) {
            callWebRTCManger.endCall();
            callWebRTCManger = null;
//            GlobalMethods.updateUiToUpdateCallLogs();
            //todo notify screen
        }
        //  closeForeGroundService(); todo ( for testing)
    }

    private void finishConnectedIndividualCall() {
        removeNativeIncommingCallTask();
        final Long currentTime = Calendar.getInstance().getTimeInMillis();
        final Long duration;
        if (mChronometer != null) {
            duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometer.stop();
        } else {
            duration = -1L;
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, currentTime, duration, CallEndType.END_INDIVIDUAL_CALL);
        callClossingTask.startEndCallProcedure();
        closePeerConnection();
    }


    private void stopReconnectingDialer() {
        isReconnecting = false;
        if (mReconnectingTone != null) {
            mReconnectingTone.stopTone();
            mThread.isInterrupted();
        }
    }

    private void startChronometer() {
        mChronometer = new Chronometer(GroupAudioRecieverServiceFirebase.this);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(hh + ":" + mm + ":" + ss);
            }
        });
        mCallStartTime = Calendar.getInstance().getTimeInMillis();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    private void removeTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private void updateUserStatusList(UserModel user, EventFromService eventFromService) {
        if (mCallParticipantMap.containsKey(user.getId())) {
            mCallParticipantMap.get(user.getId()).setEventFromService(eventFromService);
            Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
            intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.USER_STATUS);
            intent.putExtra(Constants.IntentExtra.USER_ID, user.getId());
            intent.putExtra(Constants.IntentExtra.USER_STATE, eventFromService);
            intent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantMap));
            LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void createNotificationForGroupCall(String notificationState, String groupName, String action, Long time) {
//        Intent notificationIntent = new Intent(this, GroupAudioRecieverActivity.class);
//        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
//        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
//        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_COVER_URL, mGroupCover);
//        notificationIntent.putExtra(Constants.IntentExtra.GROUP_NAME_CHANGED, mGroupName);
//        notificationIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mParticipants));
//        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                Constants.NOTIFICATION_HUB_ICON_FILTER);
//        Intent intent = new Intent(this, GroupAudioRecieverServiceFirebase.class);
//        //  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // mNotificationManager.createNotificationChannel(mChannel);
//            // createNotificationChannel();
//            String CHANNEL_ID = "hub_channel";// The id of the channel.
//            CharSequence name = getString(R.string.action_settings);// The user-visible name of the channel.
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//            NotificationManager manager = (NotificationManager) GroupAudioRecieverServiceFirebase.this.getSystemService(Context.NOTIFICATION_SERVICE);
//            manager.createNotificationChannel(mChannel);
//            notificationBuilder = new NotificationCompat.Builder(GroupAudioRecieverServiceFirebase.this, "hub_channel");
//            notificationBuilder.setChannelId("hub_channel");
//        } else {
//            notificationBuilder = new NotificationCompat.Builder(this);
//        }
//        notificationBuilder.setContentTitle("Wateen Hub");
//        notificationBuilder.setContentTitle("Wateen Hub");
//        notificationBuilder.setTicker("@Wateen Hub");
//        notificationBuilder.setColor(getResources().getColor(R.color.colorAccent));
//        notificationBuilder.setContentText(notificationState + " " + groupName); // todo add groupName here...
//        notificationBuilder.setSmallIcon(R.drawable.ic_call_notification);
//        notificationBuilder.setLargeIcon(icon);
//        notificationBuilder.setContentIntent(pendingIntent);
//        notificationBuilder.setOngoing(true);
//        //add notification actions
//        if (action.equals(Constants.ForegroundService.ACCEPT_CALL)) {
//            intent.setAction(Constants.ForegroundService.ACCEPT_CALL);
//            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
//                    intent, 0);
//            Intent cancelIntent = new Intent(this, GroupAudioRecieverServiceFirebase.class);
//            cancelIntent.setAction(Constants.ForegroundService.CANCEL_INCOMING_CALL);
//            PendingIntent pendingIntent2 = PendingIntent.getService(this, 0,
//                    cancelIntent, 0);
//            notificationBuilder.addAction(R.drawable.ic_accept_sm, "Answer", pendingIntent1);
//            notificationBuilder.addAction(R.drawable.ic_decline_sm, "Cancel call", pendingIntent2);
//        } else if (action.equals(Constants.ForegroundService.END_CALL)) {
//            intent.setAction(Constants.ForegroundService.END_CALL);
//            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
//                    intent, 0);
//            notificationBuilder.addAction(R.drawable.ic_cancel, "End call", pendingIntent1);
//        }
//        Notification notification = notificationBuilder.build();
//        startForeground(101, notification);
    }

    private void createNotificationForIndividual(String notificationState, String action, Long time) {
        Intent notificationIntent = new Intent(this, RecieveCallActivity.class);
        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
        notificationIntent.putExtra(Constants.IntentExtra.USER_POST, mUserPost);
        notificationIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        notificationIntent.putExtra(Constants.IntentExtra.USER_IMAGE, mUserDpUrl);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
        notificationIntent.putExtra(Constants.IntentExtra.CALLING_TYPE, callingType);
        notificationIntent.putExtra(Constants.IntentExtra.CALLER_USER_ID, mUserID);
        notificationIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantJson);
        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                Constants.CALL_TOTAL_TIME);
        Intent intent = new Intent(this, GroupAudioRecieverServiceFirebase.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // mNotificationManager.createNotificationChannel(mChannel);
            // createNotificationChannel();
            String CHANNEL_ID = "hub_channel";// The id of the channel.
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager manager = (NotificationManager) GroupAudioRecieverServiceFirebase.this.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(mChannel);
            notificationBuilder = new NotificationCompat.Builder(GroupAudioRecieverServiceFirebase.this, "hub_channel");
            notificationBuilder.setChannelId("hub_channel");
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setTicker(getResources().getString(R.string.app_name));
        notificationBuilder.setColor(getResources().getColor(R.color.colorAccent));
        notificationBuilder.setContentText(notificationState + " " + mUserName); // todo add groupName here...
        notificationBuilder.setSmallIcon(R.drawable.users);
        notificationBuilder.setLargeIcon(icon);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setOngoing(true);
        //add notification actions
        if (action.equals(Constants.ForegroundService.ACCEPT_CALL)) {
            intent.setAction(Constants.ForegroundService.ACCEPT_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_MUTABLE);
            Intent cancelIntent = new Intent(this, GroupAudioRecieverServiceFirebase.class);
            cancelIntent.setAction(Constants.ForegroundService.CANCEL_INCOMING_CALL);
            PendingIntent pendingIntent2 = PendingIntent.getService(this, 0,
                    cancelIntent, PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.user, "Answer", pendingIntent1);
            notificationBuilder.addAction(R.drawable.user, "Cancel call", pendingIntent2);
        } else if (action.equals(Constants.ForegroundService.END_CALL)) {
            intent.setAction(Constants.ForegroundService.END_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.user, "End call", pendingIntent1);
        }
        Notification notification = notificationBuilder.build();
        startForeground(101, notification);
    }

    private void reconnectingTone(final int mToneVolume) {
        final int BEEP_DURATION = 1000;
        final int WAITING_DURATION = 1000;
        mThread = new Thread(new Runnable() {
            public void run() {
                mReconnectingTone = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, mToneVolume);
                while (isReconnecting) {
                    mReconnectingTone.startTone(ToneGenerator.TONE_SUP_DIAL, 600);
                    synchronized (mReconnectingTone) {
                        try {
                            mReconnectingTone.wait(BEEP_DURATION + WAITING_DURATION);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mThread.start();
    }

    private boolean checkIfReconnectingToAll() {
        for (UserModel user :
                mParticipants) {
            if (mCallParticipantMap.containsKey(user.getId())) {
                if (mCallParticipantMap.get(user.getId()).getEventFromService().equals(EventFromService.CALL_RECONNECTED)) {//todo fix crashes
                    return false;
                }
            }
        }
        return true;

    }

    //interface implementation
    @Override
    public void callEnded() {
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mChronometer.stop();
        switch (mCallType) {
            case INDIVIDUAL_AUDIO:
//                CallRepository callRepository = new CallRepository();
//                callRepository.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
//                GlobalMethods.updateUiToUpdateCallLogs();
                break;
            case GROUP_AUDIO:
                updateDbLogs(currentTime, duration);
                break;
        }
        closeForeGroundService();
    }

    @Override
    public void callerCancelledCall() {
        if (isScheduleCall) {
            // continue the call...
        } else {
            if (mCallType == null) {
                stopIncommingCall();
                if (callWebRTCManger != null) {
                    callWebRTCManger.closeRecieverSocketConnection();
                }
//                GlobalMethods.updateUiToUpdateCallLogs();
                isCallStartedFlag = false;
                closeForeGroundService();
                return;
            }
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    stopIncommingCall();
                    if (ringtone != null) {
                        ringtone.stop();
                    }
                    if (callWebRTCManger != null) {
                        callWebRTCManger.closeRecieverSocketConnection();
                    }
//                    GlobalMethods.updateUiToUpdateCallLogs();
                    isCallStartedFlag = false;
                    closeForeGroundService();
                    break;
                case GROUP_AUDIO:
                    stopIncommingCall();
                    if (callState.equals(EventFromService.CALL_DIALING) || callState.equals(EventFromService.CALL_RINGING)) {
                        if (callWebRTCManger != null) {
                            //todo maintain logs that caller has canceled call
                            callWebRTCManger.stopHandler();
                            callWebRTCManger.closeRecieverSocketConnection();
                        }
//                        GlobalMethods.updateUiToUpdateCallLogs();
                        isCallStartedFlag = false;
                        closeForeGroundService();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void updateDbLogs(Long currentTime, Long duration) {
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
//        if (mCallParticipantMap.containsKey(UserManager.getInstance().getUserIfLoggedIn().getId()())) {
//            Participants participants = mCallParticipantMap.get(UserManager.getInstance().getUserIfLoggedIn().getId()()).getParticipants();
//            if (participants != null) {
//                participants.setCall_state_code(703L);
//                participants.setResponse_type_code(803L);
////                StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
//            }
//        }
    }


    private void updateUserCallStatus() {
//        if (UserManager.getInstance().getUserIfLoggedIn() != null) {
//            HashMap<String, Object> params = new HashMap<>();
//            params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId()());
//            CallRepository callRepository = new CallRepository();
//            callRepository.updateUserCallStatus(params, new INetworkRequestListener() {
//                @Override
//                public void onSuccess(Response result) {
//                    // if (!isCallStartedFlag) {
//                    //todo callTime out save logs in db
//                    //    StudentApp.getInstance().getHubDatabase().callsDao().updateCallState(mCallId, 704L, 802L, 0L, 0L);
////                    GlobalMethods.updateUiToUpdateCallLogs();
//                    if (callWebRTCManger != null) {
//                        callWebRTCManger.closeRecieverSocketConnection();
//                    }
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        public void run() {
//                            closeForeGroundService();
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(Response result) {
//                    if (!isCallStartedFlag) {
//                        //todo callTime out save logs in db
//                        //    StudentApp.getInstance().getHubDatabase().callsDao().updateCallState(mCallId, 704L, 802L, 0L, 0L);
//////                        GlobalMethods.updateUiToUpdateCallLogs();
//                        if (callWebRTCManger != null) {
//                            callWebRTCManger.closeRecieverSocketConnection();
//                        }
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            public void run() {
//                                closeForeGroundService();
//                            }
//                        });
//                    }
//                }
//            });
//        }
    }

    @Override
    public void startTimer() {
        isCallStartedFlag = true;
        removeTimer();
        stopIncommingCall();
        callState = EventFromService.CALL_STARTED;
        createNotificationForIndividual("On call with ", Constants.ForegroundService.END_CALL, -1L);
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_STARTED);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        //todo check from which user is trigrred and handle it like wise ....
    }

    @Override
    public void updateCallingToConnectingState() {
        stopIncommingCall();
        removeTimer();
        createNotificationForIndividual("Connected to ", Constants.ForegroundService.END_CALL, -1L);
        callState = EventFromService.CALL_CONNNECTED;
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_CONNNECTED);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void callerEndedAudioCall() {
        closeForeGroundService();
    }

    @Override
    public void holdAudioCall(UserModel user, boolean isOnNativeCall) {
        //todo broadcast of sate...
//        holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
//        callWebRTCManger.holdIngoingAudioCall(false);
//        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
//        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.HOLD_CALL);
//        intent.putExtra(Constants.IntentExtra.HOLD_STATE, holdState);
//        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        switch (mCallType) {
            case GROUP_AUDIO:
                if (mCallParticipantMap.containsKey(user.getId())) {
                    mCallParticipantMap.get(user.getId()).setmCallHold(true);
                    mCallParticipantMap.get(user.getId()).setmOnNativeCall(isOnNativeCall);
                    mCallParticipantMap.get(user.getId()).setmCallMuted(false);
                    sendLatestParticipantsStatus();
                }
                break;
            case INDIVIDUAL_AUDIO:
                mCallerOnHold = true;
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.HOLD_CALL);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                break;
        }
    }

    @Override
    public void unholdAudioCall(UserModel user) {
        //todo broadcast of state...
//        holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
//        callWebRTCManger.unHoldIngoingAudioCall(false);
//        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
//        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UNHOLD_CALL);
//        intent.putExtra(Constants.IntentExtra.HOLD_STATE, holdState);
//        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        switch (mCallType) {
            case GROUP_AUDIO:
                if (mCallParticipantMap.containsKey(user.getId())) {
                    mCallParticipantMap.get(user.getId()).setmCallHold(false);
                    mCallParticipantMap.get(user.getId()).setmOnNativeCall(false);
                    mCallParticipantMap.get(user.getId()).setmCallMuted(false);
                    sendLatestParticipantsStatus();
                }
                break;
            case INDIVIDUAL_AUDIO:
                mCallerOnHold = true;
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UNHOLD_CALL);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                break;
        }
    }

    @Override
    public void reConnectingCall() {
        isReconnecting = true;
        if (loudSpeakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
            reconnectingTone(60);
        } else {
            reconnectingTone(ToneGenerator.MAX_VOLUME);
        }
        callState = EventFromService.CALL_RECONNECTING;
        createNotificationForIndividual("Reconnecting to ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTING);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void finishCall() {
        callState = EventFromService.CALL_RECONTECTED_FAILED;
        stopReconnectingDialer();
        createNotificationForIndividual("Reconnection failed with ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
////        GlobalMethods.callEnded();
        finishConnectedIndividualCall();
        closeForeGroundService();
    }

    @Override
    public void reconnectionEstablished() {
        if (callState.equals(EventFromService.CALL_STARTED)) {
            startChronometer();
        }
        stopReconnectingDialer();
        callState = EventFromService.CALL_RECONNECTED;
        createNotificationForIndividual("On call with ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTED);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void updateStats(double bandwidth, double bitrate, double totalBandwidth, long packets) {
//        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
//        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UPDATE_CALL_STATS);
//        intent.putExtra(Constants.IntentExtra.BANDWIDTH, bandwidth);
//        intent.putExtra(Constants.IntentExtra.BITRATE, bitrate);
//        intent.putExtra(Constants.IntentExtra.TOTAL_BANDWIDTH, totalBandwidth);
//        intent.putExtra(Constants.IntentExtra.PACKETS, packets);
//        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void callRinging() {

    }

    @Override
    public void callRinging(UserModel user) {
        updateUserStatusList(user, EventFromService.CALL_RINGING);
        updateSingleUserStatus(Long.valueOf(user.getId()));
    }

    @Override
    public void updateCallingToConnectingState(UserModel user) {
//        Log.e("UserModel IS:", user.getDisplayName());
    }


    @Override
    public void reConnectingCall(UserModel user) {
        updateUserStatusList(user, EventFromService.CALL_RECONNECTING);
        if (checkIfReconnectingToAll()) {
            isReconnecting = true;
            if (loudSpeakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
                reconnectingTone(60);
            } else {
                reconnectingTone(ToneGenerator.MAX_VOLUME);
            }

            callState = EventFromService.CALL_RECONNECTING;
            createNotificationForGroupCall("Reconnecting :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
            Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
            intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTING);
            intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
            LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void reconnectionEstablished(UserModel user) {
//        Log.e("UserModel IS:", user.getDisplayName());
        //todo check from which user it got reconnected and update ui
        if (callState.equals(EventFromService.CALL_STARTED)) {
            startChronometer();
        }
        stopReconnectingDialer();
        removeCallNotConnectedTask();
        callState = EventFromService.CALL_RECONNECTED;
        updateUserStatusList(user, EventFromService.CALL_RECONNECTED);
        createNotificationForGroupCall("Group Call :", mGroupName, Constants.ForegroundService.END_CALL, 2L);
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTED);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void startTimer(UserModel user) {
        removeTimer();
        stopIncommingCall();
        if (callState.equals(EventFromService.CALL_CONNNECTED)) {
            isCallStartedFlag = false;
            callWebRTCManger.stopHandler();
            callState = EventFromService.CALL_STARTED;
            updateParticipantStatusToConnected(user);
            createNotificationForGroupCall("Group call :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        }
        updateParticipantStatusToConnected(user);
        updateUserStatusList(user, EventFromService.CALL_STARTED);
    }

    private void updateParticipantStatusToConnected(UserModel user) {
        if (mCallParticipantMap.get(user.getId()) != null && mCallParticipantMap.get(user.getId()).getParticipants() != null) {
            Participants participants = mCallParticipantMap.get(user.getId()).getParticipants();
            participants.setCall_state_code(702L);
            participants.setResponse_type_code(803L);
//            StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
        }
    }

    @Override
    public void userEndedCall(UserModel user) {
        if (mCallParticipantMap.containsKey(user.getId())) {
            Participants participants = mCallParticipantMap.get(user.getId()).getParticipants();
            if (participants != null) {
                Long time = getTimeFromChronometer();
                participants.setDuration(time);
                participants.setCall_state_code(703L);
                participants.setResponse_type_code(803L);
//                StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
            }
        }
        updateUserStatusList(user, EventFromService.CALL_END);
    }

    @Override
    public void finishCall(UserModel user) {
        createNotificationForGroupCall("Reconnection Failed", "Grouyp Name", Constants.ForegroundService.END_CALL, -1L);
        callState = EventFromService.CALL_RECONTECTED_FAILED;
        stopReconnectingDialer();
        finishConnectedGroupCall();
    }

    @Override
    public void userDisconnected(UserModel user) {
        updateUserStatusList(user, EventFromService.CALL_RECONTECTED_FAILED);
    }

    @Override
    public void userDisconnected() {
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mChronometer.stop();
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, currentTime, duration, 0, CallEndType.END_GROUP_CALL);
        callClossingTask.startEndCallProcedure();
        updateDbLogs(currentTime, duration);
        closePeerConnection();
        closeForeGroundService();
    }


    @Override
    public void muteCall(UserModel user) {
        if (mCallType != null) {
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    //todo participant has updated mute..
                    mParticipantOnMute = true;
                    Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.PARTICIPANT_MUTE);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    break;
                case GROUP_AUDIO:
                    if (mCallParticipantMap.containsKey(Long.valueOf(user.getId()))) {
                        mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallHold(false);
                        mCallParticipantMap.get(Long.valueOf(user.getId())).setmOnNativeCall(false);
                        mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallMuted(true);
                        sendLatestParticipantsStatus();
                    }
                    break;
            }
        }
    }

    @Override
    public void unMuteCall(UserModel user) {
        if (mCallType != null) {
            switch (mCallType) {
                case INDIVIDUAL_AUDIO:
                    mParticipantOnMute = false;
                    Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.PARTICIPANT_UNMUTE);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    break;
                case GROUP_AUDIO:
                    if (mCallParticipantMap.containsKey(Long.valueOf(user.getId()))) {
                        mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallHold(false);
                        mCallParticipantMap.get(Long.valueOf(user.getId())).setmOnNativeCall(false);
                        mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallMuted(false);
                        sendLatestParticipantsStatus();
                    }
                    break;
            }
        }
    }

    @Override
    public void disableVideoCall(UserModel user) {

    }

    @Override
    public void enableVideoCall(UserModel user) {

    }
}
