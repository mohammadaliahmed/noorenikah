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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Chronometer;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


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
import com.appsinventiv.noorenikah.callWebRtc.dbmodels.Calls;
import com.appsinventiv.noorenikah.callWebRtc.dbmodels.Participants;
import com.appsinventiv.noorenikah.callWebRtc.groupCallParticipant.GroupCallparticipantStatus;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class GroupAudioCallerServiceFirebase extends Service implements AudioCallBackInterface {
    //variable declaration...
    CallWebRTCManger callWebRTCManger;
    Long mGroupId = -1L;
    Long mCallId = -1L;
    Long mRoomId = -1L;
    CallManager.CallType mCallType;
    Timer mTimerDialing = new Timer();
    Timer mTimerRinging = new Timer();
    Timer mStatusDialingTask;
    Timer mRinginStatusDialingTask;
    ToneGenerator mToneGenerator;
    ToneGenerator mReconnectingTone;
    ToneGenerator mAlertTone;
    boolean alertTimeFlag;
    boolean isAlertPopUp = false;
    boolean isAlertTimeHasBeenPassed = false;
    boolean isCallStartedflag = true;
    boolean isReconnecting = true;
    Enum callState;
    Chronometer mChronometer;
    Long mCallStartTime = -1L;
    Enum muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
    Enum loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE;
    Enum holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
    Thread mThread;
    Boolean is_LoudSpreakerPressed = false;
    boolean isAlreadyRing = false;
    boolean isDialingTimerFlag = false;
    boolean isRingingTimerFlag = false;
    String mUserName;
    String mUserPost;
    String mUserDpUrl;
    //  private boolean isGroupNativeCall = false;
    Handler mHoldCallEnd;
    Boolean is_MuteButtonPressed = false;
    NotificationCompat.Builder notificationBuilder;
    boolean isCallRejoined = false;
    boolean isScheduleCall = false;
    Vibrator mVibrator;
    Ringtone ringtone;
    Timer mTimer = new Timer();
    boolean isCallStartedFlag = true;
    Long mSchCallEnd;
    Timer mScheduleCallEnd;
    Timer mCallNotConnectedTimer;
    Timer mScheduleCallAlert = new Timer();
    TimerTask endCallTask;
    TimerTask callNotConnectedTask; // if in 30 sec no call is connected than use this task...
    private ArrayList<UserModel> mParticipants;
    private HashMap<Long, GroupCallparticipantStatus> mCallParticipantMap = new HashMap<>();
    private String mGroupName;
    private String mGroupCover;
    private ArrayList<Participants> mCallParticipantList;
    private Calls mCall;
    private Long mAttendentId;
    private String mParticipantsJson;// todo incase of add call updater this user object.. ( make map for List )...
    private boolean mReciverOnHold = false;
    private boolean mParticipantOnMute = false;
    private boolean isIndividualNativeCall = false;
    private boolean endHoldCall = false;
    Runnable holdGroupCallTimeOut = new Runnable() {
        @Override
        public void run() {
            if (endHoldCall) {
                finishConnectedGroupCall(); //this function can change value of mInterval.
            }
        }
    };
    Runnable holdCallTimeOut = new Runnable() {
        @Override
        public void run() {
            if (endHoldCall) {
                finishConnectedIndividualCall(); //this function can change value of mInterval.
            }
        }
    };
    private Long mSchCallId;
    //local broadcast callBacks
    private BroadcastReceiver mNativeMobileIncomingCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCallType != null) {
                switch (mCallType) {
                    case GROUP_AUDIO:
                        if (isCallStartedflag) {
                            callerCancelGroupAudioCall();
                        } else {
                            //nativeCallOnGroupCall();
                            nativeCallRecived(mCallType);
                        }
                        break;
                    case INDIVIDUAL_AUDIO:
                        if (isCallStartedflag) {
                            callerCancelAudioCall();
                        } else {
                            nativeCallRecived(mCallType);
                            //   finishConnectedIndividualCall();
                        }
                        break;
                }
            }
        }
    };
    private BroadcastReceiver mCallRejected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCallType != null) {
                switch (mCallType) {

                    case INDIVIDUAL_AUDIO:
//                        Long roomId = Long.valueOf(intent.getStringExtra("roomId"));
//                        if (Objects.equals(roomId, mRoomId)) {
                            if (callWebRTCManger != null) {
                                sendMessageToActivity("User has rejected");
                                callWebRTCManger.closePeerConnection();
                                //  showToast("Call Rejected");
//                                GlobalMethods.updateUiToUpdateCallLogs();
                                //todo notifiy screen
                                finishForegroundService();
//                            }
                        }
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
                    mCallParticipantMap.get(user.getId()).setEventFromService(EventFromService.CALL_DIALING);
                    sendLatestParticipantsStatus();
                    updateSingleUserStatus(userId);
                }
            }

        }
    };
    private BroadcastReceiver mBroadCastEventsFromActivity = new BroadcastReceiver() {
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
                                    break;
                                case INDIVIDUAL_AUDIO:
                                    finishConnectedIndividualCall();
                                    break;
                            }
                        } else {
                            closePeerConnection();
                            finishForegroundService();
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.ACCEPT_CALL_PRESSED)) {
                        switch (mCallType) {
                            case GROUP_AUDIO:
                                acceptIncommingGroupAudioCallProcedure();
                                break;
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.END_CALL_INGOING)) {
                        switch (mCallType) {
                            case GROUP_AUDIO:
                                callerCancelGroupAudioCall();
                                break;
                            case INDIVIDUAL_AUDIO:
                                callerCancelAudioCall();
                                break;
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.END_CALL_INCOMING)) {
                        stopIncommingCall();
                        if (mCallType != null) {
                            switch (mCallType) {
                                case GROUP_AUDIO:
                                    rejectIncommingGroupAudioCall();
                                    break;
                            }
                        } else {
                            closePeerConnection();
                            finishForegroundService();
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.ENABLE_LOUD_SPEAKER_PRESSED)) {
                        changeToneVolume(ToneGenerator.MAX_VOLUME);
                        loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_PRESSED;
                        callWebRTCManger.enableDisableLoudSpeaker();
                    } else if (eventsFromActivity.equals(EventsFromActivity.DISABLE_LOUD_SPEAKER_PRESSED)) {
                        changeToneVolume(30);
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
                    } else if (eventsFromActivity.equals(EventsFromActivity.HOLD_BUTTON_PRESSED)) {
                        muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
                        holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
                        callWebRTCManger.holdIngoingAudioCall(false);
                    } else if (eventsFromActivity.equals(EventsFromActivity.UNHOLD_BUTTON_PRESSED)) {
                        removeNativeRunnabletask();
                        holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
                        callWebRTCManger.unHoldIngoingAudioCall(true);
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_LATEST_TIME)) {
                        intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.SEND_LATEST_TIME);
                        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
                        intent.putExtra(Constants.IntentExtra.MICROPHONE_STATE, muteState);
                        intent.putExtra(Constants.IntentExtra.LOUDSPEAKER_STATE, loudSpeakerState);
                        intent.putExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, mReciverOnHold);
                        intent.putExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, mParticipantOnMute);
                        intent.putExtra(Constants.IntentExtra.HOLD_STATE, holdState);
                        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_UPDATED_PARTICIPANTS)) {
                        sendLatestParticipantsStatus();
                    } else if (eventsFromActivity.equals(EventsFromActivity.DISCONNECT_USER)) {
                        callWebRTCManger.disconnectUser(Long.valueOf(attendedfirebaseID));
                    } else if (eventsFromActivity.equals(EventsFromActivity.REDIAL_USER)) {
                        Long userId = intent.getLongExtra(Constants.IntentExtra.USER_ID, -1);
                        redialGroupMember(userId);
                    } else if (eventsFromActivity.equals(EventsFromActivity.START_CALLING_ACTIVITY)) {
                        if (mCallType == null) {
                            closePeerConnection();
                            finishForegroundService();
                            return;
                        }
                        switch (mCallType) { //  todo check if mCallType is null...
                            case GROUP_AUDIO:
                                startGroupCallerActivity();
                                break;
                            case INDIVIDUAL_AUDIO:
                                startInitiateCallerActivity();
                                break;
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.ADD_PERSON_TO_CALL)) {
                        switch (mCallType) {
//                            case INDIVIDUAL_AUDIO:
//                                getAddParticipantForIndividual(intent);
//                                break;
//                            case GROUP_AUDIO:
//                                getAddParticipantForGroupCall(intent);
//                                break;
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_UPDATED_PARTICIPANTS)) {
                        sendLatestParticipantsStatus();
                    } else if (eventsFromActivity.equals(EventsFromActivity.ENABLE_LOUD_SPEAKER_PRESSED)) {
                        changeToneVolume(ToneGenerator.MAX_VOLUME);
                        loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_PRESSED;
                        callWebRTCManger.enableDisableLoudSpeaker();
                    } else if (eventsFromActivity.equals(EventsFromActivity.DISABLE_LOUD_SPEAKER_PRESSED)) {
                        changeToneVolume(30);
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
                    } else if (eventsFromActivity.equals(EventsFromActivity.HOLD_BUTTON_PRESSED)) {
                        muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
                        holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
                        callWebRTCManger.holdIngoingAudioCall(false);
                    } else if (eventsFromActivity.equals(EventsFromActivity.UNHOLD_BUTTON_PRESSED)) {
                        if (isIndividualNativeCall) {
                            isIndividualNativeCall = false;
                            endHoldCall = false;
                            if (mHoldCallEnd != null) {
                                mHoldCallEnd.removeCallbacks(holdCallTimeOut);
                                mHoldCallEnd.removeCallbacksAndMessages(null);
                                mHoldCallEnd = null;
                            }
                        }
                        holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
                        callWebRTCManger.unHoldIngoingAudioCall(true);
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_LATEST_TIME)) {
                        intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.SEND_LATEST_TIME);
                        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
                        intent.putExtra(Constants.IntentExtra.MICROPHONE_STATE, muteState);
                        intent.putExtra(Constants.IntentExtra.LOUDSPEAKER_STATE, loudSpeakerState);
                        intent.putExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, mReciverOnHold);
                        intent.putExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, mParticipantOnMute);
                        intent.putExtra(Constants.IntentExtra.HOLD_STATE, holdState);
                        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_UPDATED_PARTICIPANTS)) {
                        sendLatestParticipantsStatus();
                    } else if (eventsFromActivity.equals(EventsFromActivity.DISCONNECT_USER)) {
                        Long userId = intent.getLongExtra(Constants.IntentExtra.USER_ID, -1);
                        callWebRTCManger.disconnectUser(Long.valueOf(attendedfirebaseID));
                    } else if (eventsFromActivity.equals(EventsFromActivity.REDIAL_USER)) {
                        Long userId = intent.getLongExtra(Constants.IntentExtra.USER_ID, -1);
                        redialGroupMember(userId);
                    } else if (eventsFromActivity.equals(EventsFromActivity.START_CALLING_ACTIVITY)) {
                        if (mCallType != null) {
                            switch (mCallType) { //  todo check if mCallType is null...
                                case GROUP_AUDIO:
                                    startGroupCallerActivity();
                                    break;
                                case INDIVIDUAL_AUDIO:
                                    startInitiateCallerActivity();
                                    break;
                            }
                        } else {
                            finishConnectedIndividualCall(); // for now...
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.ADD_PERSON_TO_CALL)) {
                        switch (mCallType) {
//                            case INDIVIDUAL_AUDIO:
//                                getAddParticipantForIndividual(intent);
//                                break;
//                            case GROUP_AUDIO:
//                                getAddParticipantForGroupCall(intent);
//                                break;
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.GET_UPDATED_PARTICIPANTS)) {
                        sendLatestParticipantsStatus();
                    } else if (eventsFromActivity.equals(EventsFromActivity.REMOVE_USER_FROM_CALL_LIST)) {
                        Long userId = intent.getLongExtra(Constants.IntentExtra.USER_ID, -1);
                        removeUserFromCall(userId);
                    } else if (eventsFromActivity.equals(EventsFromActivity.IS_POP_INFALTED)) {
                        if (isScheduleCall) {
                            showAlertPopup();
                        }
                    } else if (eventsFromActivity.equals(EventsFromActivity.POP_UP_INFLATED)) {
                        isAlertPopUp = true;
                    } else if (eventsFromActivity.equals(EventsFromActivity.UPDATE_SCHEDULE_END)) {
                        Long updatedTime = intent.getLongExtra(Constants.IntentExtra.UPDATE_SCH_CALL_END, 0);
                        updateCallEndTime(updatedTime);
                    }
                }
            }
        }
    };
    private String attendedfirebaseID;
    private String groupname;
    private boolean flag = false;

    //default service constructor
    public GroupAudioCallerServiceFirebase() {
    }

    // service callBacks implementation
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
    public void onDestroy() {
        removeRingingStatusTask();
        removeDialingTimer();
        removeRingingTimer();
        removeRingingStatusTask();
        stopReconnectingDialer();
        removeCallEndingTask();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastEventsFromActivity);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNativeMobileIncomingCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCallRejected);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRedialToAGroupMember);
        super.onDestroy();
    }

    //register local broadcast
    private void registerLocalBroadCast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mNativeMobileIncomingCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_NATIVE_CALL_STARTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallRejected,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALL_REJECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastEventsFromActivity,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRedialToAGroupMember,
                new IntentFilter(Constants.Broadcasts.BROADCAST_REDIAL_TO_A_GROUP_MEMBER));
    }

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

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.MESSAGE);
        intent.putExtra(Constants.IntentExtra.MESSAGE, msg);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
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

    private void initiateGroupCallProcedure() {
        startStatusDialingTask();
        callState = EventFromService.CALLER_RININGING;
        createNotificationForGroupAudio("Call From ", mGroupName, Constants.ForegroundService.ACCEPT_CALL, -1L);
        checkIfMobileonSilent();
        createGroupReceiverCallManager();
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

    private void playAlertVibration() {
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 200, 200};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern\
        mVibrator.vibrate(pattern, 0);
    }

    private void playSoundForIncommingcall() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(GroupAudioCallerServiceFirebase.this, notification);
            ringtone.play();
            startTimerTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void updateUserCallStatus() {
//        if (UserManager.getInstance().getUserIfLoggedIn() != null) {
//            HashMap<String, Object> params = new HashMap<>();
//            params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId()()());
//            CallRepository callRepository = new CallRepository();
//            callRepository.updateUserCallStatus(params, new INetworkRequestListener() {
//                @Override
//                public void onSuccess(Response result) {
////                    GlobalMethods.updateUiToUpdateCallLogs();
//                    if (callWebRTCManger != null) {
//                        callWebRTCManger.closeRecieverSocketConnection();
//                    }
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        public void run() {
//                            finishForegroundService();
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(Response result) {
//                    //todo callTime out save logs in db
//                    //    StudentApp.getInstance().getHubDatabase().callsDao().updateCallState(mCallId, 704L, 802L, 0L, 0L);
////                    GlobalMethods.updateUiToUpdateCallLogs();
//                    if (callWebRTCManger != null) {
//                        callWebRTCManger.closeRecieverSocketConnection();
//                    }
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        public void run() {
//                            finishForegroundService();
//                        }
//                    });
//                }
//            });
//        }
    }

    private void createGroupReceiverCallManager() {
        //make callWebrtc Manager
        callWebRTCManger = new CallWebRTCManger(GroupAudioCallerServiceFirebase.this,
                mCallType,
                mRoomId, this);
        callWebRTCManger.startGroupCallForReciever();
        callWebRTCManger.disableLoudSpeaker();
    }

    private void stopIncommingCall() {
        if (ringtone != null) {
            ringtone.stop();
        }
        removeVibration();
    }

    private void removeVibration() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private void removeNativeRunnabletask() {
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

    private void startInitiateCallerActivity() {
        Intent notificationIntent = new Intent(this, InitiateCallScreenActivity.class);
        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
        notificationIntent.putExtra(Constants.IntentExtra.USER_POST, mUserPost);
        notificationIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        notificationIntent.putExtra(Constants.IntentExtra.USER_IMAGE, mUserDpUrl);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
        if (callState.equals(EventFromService.CALL_DIALING) || callState.equals(EventFromService.CALL_RINGING)) {
            notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
        } else {
            notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        }
        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        notificationIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantsJson);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(notificationIntent);
    }


    //helper function for events trigger by activity

    private void startGroupCallerActivity() {
//        Long time;
//        if (callState.equals(EventFromService.CALL_RINGING) || callState.equals(EventFromService.CALL_DIALING)) {
//            time = -1L;
//        } else {
//            time = 2L;
//        }
//        Intent notificationIntent = new Intent(this, GroupAudioCallerActivity.class);
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
//        GroupAudioCallerServiceFirebase.this.startActivity(notificationIntent);
    }

    private void sendLatestParticipantsStatus() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.USER_STATUS);
        intent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mCallParticipantMap));
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void getUpdatedGroupId() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UPDATED_GROUPID);
        intent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void changeToneVolume(int mToneVolume) {
        if (callState != null && callState.equals(EventFromService.CALL_DIALING))
            if (!is_LoudSpreakerPressed) {
                is_LoudSpreakerPressed = true;
            } else is_LoudSpreakerPressed = false;
        if (callState != null && callState.equals(EventFromService.CALL_RINGING)) {
            playToneGenerator(mToneVolume);
        }
    }

    private void playToneGenerator(final int mToneVolume) {
        final int BEEP_DURATION = 1500;
        final int WAITING_DURATION = 1100;
        mThread = new Thread(new Runnable() {
            public void run() {
                mToneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, mToneVolume);
                while (isCallStartedflag) {
                    mToneGenerator.startTone(ToneGenerator.TONE_SUP_DIAL, 600);
                    synchronized (mToneGenerator) {
                        try {
                            mToneGenerator.wait(BEEP_DURATION + WAITING_DURATION);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mThread.start();
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

    private void holdGroupCallEndSchedular() {
        mHoldCallEnd = new Handler(Looper.getMainLooper());
        //startHoldCallTimer();
        mHoldCallEnd.postDelayed(holdGroupCallTimeOut, 180000);
    }

    private void holdCallEndSchedular() {
        mHoldCallEnd = new Handler(Looper.getMainLooper());
        //startHoldCallTimer();
        mHoldCallEnd.postDelayed(holdCallTimeOut, 180000);
    }

    private void acceptIncommingGroupAudioCallProcedure() {
        stopIncommingCall();
        callWebRTCManger.stopHandler();
        acceptIncommingGroupAudioCall();
        updateUiForAcceptAction();
        removeTimer();
    }

    private void removeTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private void updateUiForAcceptAction() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_ACCEPT);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private Long getTimeFromChronometer() {
        if (mChronometer != null) {
            return SystemClock.elapsedRealtime() - mChronometer.getBase();
        } else {
            return -1L;
        }
    }

    // helper function related to onStartCommand...
    private void getActionFromIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ForegroundService.ACTION_MAIN)) {
                checkCallType(intent);
                //todo check call Type for procedure...
                switch (mCallType) {
                    case INDIVIDUAL_AUDIO:
                        initiateCallProcedureForIndividual();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.END_CALL)) {
                if (mCallType == null) {
                    finishConnectedIndividualCall(); // for now to save crash...
                    return;
                }
                switch (mCallType) {
                    case INDIVIDUAL_AUDIO:
                        finishConnectedIndividualCall();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.ACCEPT_CALL)) {
                switch (mCallType) {
                    case GROUP_AUDIO:
                        acceptIncommingGroupAudioCallProcedure();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.CANCEL_INGOING_CALL)) {
                switch (mCallType) {

                    case INDIVIDUAL_AUDIO:
                        callerCancelAudioCall();
                        break;
                }
            } else if (intent.getAction().equals(Constants.ForegroundService.CANCEL_INCOMING_CALL)) {
                stopIncommingCall();
                switch (mCallType) {
                    case GROUP_AUDIO:
                        rejectIncommingGroupAudioCall();
                        break;
                }
            }
        }
    }

    private void getValuesFromIntentForIndividualCall(Intent intent) {
        mGroupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
        mCallId = intent.getLongExtra(Constants.IntentExtra.CALL_ID, -1);
        mCallType = (CallManager.CallType) intent.getSerializableExtra(Constants.IntentExtra.CALL_TYPE);
        mUserDpUrl = intent.getStringExtra(Constants.IntentExtra.USER_IMAGE);
        mUserName = intent.getStringExtra(Constants.IntentExtra.USER_NAME);
        mUserPost = intent.getStringExtra(Constants.IntentExtra.USER_POST);
        groupname = intent.getStringExtra(Constants.IntentExtra.INTENT_GROUP_NAME);
        mAttendentId = intent.getLongExtra(Constants.IntentExtra.ATTENDENT_ID, -1);
        mParticipantsJson = intent.getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        attendedfirebaseID = intent.getStringExtra(Constants.IntentExtra.USER_FIREBASEID);
        mRoomId = intent.getLongExtra(Constants.IntentExtra.INTENT_ROOM_ID, -1);
        is_LoudSpreakerPressed = intent.getBooleanExtra(Constants.IntentExtra.IS_LOUDSPEAKER_PRESSED, false);
        is_MuteButtonPressed = intent.getBooleanExtra(Constants.IntentExtra.IS_MUTE_PRESSED, false);
        if (is_LoudSpreakerPressed) {
            loudSpeakerState = CallWidgetState.LOUDSPEAKER_BUTTON_PRESSED;
        }
        if (is_MuteButtonPressed) {
            muteState = CallWidgetState.MUTE_BUTTON_PRESSED;
        }
    }


    private void checkCallType(Intent intent) {
        mCallType = (CallManager.CallType) intent.getSerializableExtra(Constants.IntentExtra.CALL_TYPE);
        switch (mCallType) {
            case INDIVIDUAL_AUDIO:
                getValuesFromIntentForIndividualCall(intent);
                break;

        }
    }

    private void initiateCallProcedureForIndividual() {
        initAudioCall(mGroupId);
        createIndividualCallManager();
        startDilaingTimerTask();
        callState = EventFromService.CALL_DIALING;
        createNotificationForIndividualAudio("Dialing", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
    }


    private void createIndividualCallManager() {
        //make callWebrtc Manager
        callWebRTCManger = new CallWebRTCManger(GroupAudioCallerServiceFirebase.this,
                mCallType,
                mRoomId, this);
        callWebRTCManger.startCallForInitiator();
    }

    private void stopToneDialer() {
        isCallStartedflag = false;
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
            mThread.isInterrupted();
        }
    }

    private void updateFromDialingStatus() {
        HashMap<Long, GroupCallparticipantStatus> map = new HashMap<Long, GroupCallparticipantStatus>(mCallParticipantMap);
        for (Long key : map.keySet()) {
            if (Objects.equals(key, UserManager.getInstance().getUserIfLoggedIn().getId())) {
                continue;
            }
            if (map.get(key).getEventFromService().equals(EventFromService.CALL_DIALING)) {
                map.get(key).setEventFromService(EventFromService.USER_NOT_ACCESSIBLE);
                Long userId = Long.valueOf(map.get(key).getmUser().getId());
                CallClossingTask callClossingTask = new CallClossingTask(mCallId, userId, CallEndType.GROUP_MISS_CALL);
                callClossingTask.startEndCallProcedure();
            }
        }
        startStatusRingingTask();
        removeStatusTimerTask();
        sendLatestParticipantsStatus();
    }

    private void startStatusDialingTask() {
        final Long delay = 30000L;
        mStatusDialingTask = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateFromDialingStatus();
            }
        };
        mStatusDialingTask.schedule(task, delay);
    }

    private void updateFromRingingStatus() {
        HashMap<Long, GroupCallparticipantStatus> map = new HashMap<Long, GroupCallparticipantStatus>(mCallParticipantMap);
        for (Long key : map.keySet()) {
            if (Objects.equals(key, UserManager.getInstance().getUserIfLoggedIn().getId())) {
                continue;
            }
            if (map.get(key).getEventFromService().equals(EventFromService.CALL_RINGING)) {
                map.get(key).setEventFromService(EventFromService.USER_NOT_ANSWERING);
                Long userId = Long.valueOf(map.get(key).getmUser().getId());
                CallClossingTask callClossingTask = new CallClossingTask(mCallId, userId, CallEndType.GROUP_MISS_CALL);
                callClossingTask.startEndCallProcedure();
            }
        }
        sendLatestParticipantsStatus();
        removeRingingStatusTask();
    }

    private void startStatusRingingTask() {
        mRinginStatusDialingTask = new Timer();
        final Long delay = 60000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateFromRingingStatus();
            }
        };
        mRinginStatusDialingTask.schedule(task, delay);
    }


    private void startDilaingTimerTask() {
        final Long delay = 31000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                groupMissCall();
            }
        };
        if (!isDialingTimerFlag) {
            mTimerDialing.schedule(task, delay);
        }
    }

    private void startCallEndTimerTask(final Long callEnd) {
        endCallTask = new TimerTask() {
            @Override
            public void run() {
                if (endCallTask != null) {
                    finishConnectedGroupCall();
                }
            }
        };
        mScheduleCallEnd = new Timer();
        mScheduleCallEnd.schedule(endCallTask, callEnd);
    }

    private void startCallNotConnectedEndTask() {
        callNotConnectedTask = new TimerTask() {
            @Override
            public void run() {
                if (callNotConnectedTask != null) {
                    finishConnectedGroupCall();
                    finishForegroundService();
                }
            }
        };
        mCallNotConnectedTimer = new Timer();
        mCallNotConnectedTimer.schedule(callNotConnectedTask, 30000L);
    }

    private void startCallEndingAlert(final Long alertTime) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                callEndingAlert();
            }
        };
        mScheduleCallAlert.schedule(task, alertTime);
    }

    private void startAlertTone() {
        final int BEEP_DURATION = 200;
        final int WAITING_DURATION = 200;
        mThread = new Thread(new Runnable() {
            public void run() {
                mAlertTone = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 60);
                while (alertTimeFlag) {
                    mAlertTone.startTone(ToneGenerator.TONE_SUP_DIAL, 600);
                    synchronized (mAlertTone) {
                        try {
                            mAlertTone.wait(BEEP_DURATION + WAITING_DURATION);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mThread.start();
    }

    private void callEndingAlert() {
        isAlertTimeHasBeenPassed = true;
        startAlertTone();
        playAlertVibration();
        //todo show alert...
        showAlertPopup();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAlertBeeping();
                removeVibration();
                removeAlertScheduleTask();
            }
        }, 600);
    }

    private void showAlertPopup() {
        if (!isAlertPopUp && isAlertTimeHasBeenPassed) {
            isAlertTimeHasBeenPassed = false;
            Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
            intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.INFLATE_POP_UP);
            LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        }
    }


    private void removeCallEndingTask() {
        if (mScheduleCallEnd != null) {
            if (endCallTask != null) {
                endCallTask.cancel();
                endCallTask = null;
            }
            mScheduleCallEnd.cancel();
            mScheduleCallEnd.purge();
            mScheduleCallEnd = null;
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

    private void removeAlertScheduleTask() {
        if (mScheduleCallAlert != null) {
            mScheduleCallAlert.cancel();
            mScheduleCallAlert.purge();
            mScheduleCallAlert = null;
        }
    }

    private void startRingingTimerTask() {
        if (!isAlreadyRing) {
            isAlreadyRing = true;
            final Long delay = 61000L;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    //  todo make new event user is not answering call
                    groupMissCall();
                }
            };
            if (!isRingingTimerFlag) {
                mTimerRinging.schedule(task, delay);
            }
        }
    }

    private void removeDialingTimer() {
        if (mTimerDialing != null) {
            isDialingTimerFlag = true;
            mTimerDialing.cancel();
            mTimerDialing.purge();
            mTimerDialing = null;
        }
    }

    private void removeRingingTimer() {
        if (mTimerRinging != null) {
            isRingingTimerFlag = true;
            mTimerRinging.cancel();
            mTimerRinging.purge();
            mTimerRinging = null;
        }
    }

    private void removeStatusTimerTask() {
        if (mStatusDialingTask != null) {
            mStatusDialingTask.cancel();
            mStatusDialingTask.purge();
            mStatusDialingTask = null;
        }
    }

    private void removeRingingStatusTask() {
        if (mRinginStatusDialingTask != null) {
            mRinginStatusDialingTask.cancel();
            mRinginStatusDialingTask.purge();
            mRinginStatusDialingTask = null;
        }
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

    private void stopReconnectingDialer() {
        isReconnecting = false;
        if (mReconnectingTone != null) {
            mReconnectingTone.stopTone();
            mThread.isInterrupted();
        }
    }

    private void stopAlertBeeping() {
        alertTimeFlag = false;
        if (mAlertTone != null) {
            mAlertTone.stopTone();
            mThread.isInterrupted();
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

    private void finishForegroundService() {
        isCallStartedflag = false;
        GroupAudioCallerServiceFirebase.this.stopSelf();
        GroupAudioCallerServiceFirebase.this.stopForeground(true);
//        GlobalMethods.callEnded();
        // todo check if one to one call or many connected and updated ui
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.FINISH_ACTIVITY);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void startChronometer() {
        mChronometer = new Chronometer(GroupAudioCallerServiceFirebase.this);
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

    private void closePeerConnection() {
        if (callWebRTCManger != null) {
            callWebRTCManger.endCall();
            callWebRTCManger = null;
//            GlobalMethods.updateUiToUpdateCallLogs();
        }
        // finishForegroundService();
    }

    private void createNotificationForGroupAudio(String notificationState, String groupName, String action, Long time) {
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
//        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
//        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
//        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
//        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_COVER_URL, mGroupCover);
//        notificationIntent.putExtra(Constants.IntentExtra.GROUP_NAME_CHANGED, mGroupName);
//        notificationIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, StringUtils.getGson().toJson(mParticipants));
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                Constants.NOTIFICATION_HUB_ICON_FILTER);
//        Intent intent = new Intent(this, GroupAudioCallerServiceFirebase.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // mNotificationManager.createNotificationChannel(mChannel);
//            //createNotificationChannel();
//            String CHANNEL_ID = "hub_channel";// The id of the channel.
//            CharSequence name = getString(R.string.action_settings);// The user-visible name of the channel.
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//            NotificationManager manager = (NotificationManager) GroupAudioCallerServiceFirebase.this.getSystemService(Context.NOTIFICATION_SERVICE);
//            manager.createNotificationChannel(mChannel);
//            notificationBuilder = new NotificationCompat.Builder(GroupAudioCallerServiceFirebase.this, "hub_channel");
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
//        if (action.equals(Constants.ForegroundService.CANCEL_INGOING_CALL)) {
//            intent.setAction(Constants.ForegroundService.CANCEL_INGOING_CALL);
//            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
//                    intent, 0);
//            notificationBuilder.addAction(R.drawable.ic_cancel, "End call", pendingIntent1);
//        } else if (action.equals(Constants.ForegroundService.END_CALL)) {
//            intent.setAction(Constants.ForegroundService.END_CALL);
//            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
//                    intent, 0);
//            notificationBuilder.addAction(R.drawable.ic_cancel, "End call", pendingIntent1);
//        } else if (action.equals(Constants.ForegroundService.ACCEPT_CALL)) {
//            intent.setAction(Constants.ForegroundService.ACCEPT_CALL);
//            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
//                    intent, 0);
//            Intent cancelIntent = new Intent(this, MainActivity.class);
//            cancelIntent.setAction(Constants.ForegroundService.CANCEL_INCOMING_CALL);
//            PendingIntent pendingIntent2 = PendingIntent.getService(this, 0,
//                    cancelIntent, 0);
//            notificationBuilder.addAction(R.drawable.ic_accept_sm, "Answer", pendingIntent1);
//            notificationBuilder.addAction(R.drawable.ic_decline_sm, "Cancel call", pendingIntent2);
//        }
//        Notification notification = notificationBuilder.build();
//        startForeground(101, notification);
    }

    private void createNotificationForIndividualAudio(String notificationState, String action, Long time) {
        Intent notificationIntent = new Intent(this, InitiateCallScreenActivity.class);
        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_TYPE, mCallType);
        notificationIntent.putExtra(Constants.IntentExtra.USER_POST, mUserPost);
        notificationIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        notificationIntent.putExtra(Constants.IntentExtra.USER_IMAGE, mUserDpUrl);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, callState);
        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
        notificationIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantsJson);// todo provide list....
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        }else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        }
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                Constants.CALL_TOTAL_TIME);
        Intent intent = new Intent(this, GroupAudioCallerServiceFirebase.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // mNotificationManager.createNotificationChannel(mChannel);
            // createNotificationChannel();
            String CHANNEL_ID = "hub_channel";// The id of the channel.
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager manager = (NotificationManager) GroupAudioCallerServiceFirebase.this.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(mChannel);
            notificationBuilder = new NotificationCompat.Builder(GroupAudioCallerServiceFirebase.this, "hub_channel");
            notificationBuilder.setChannelId("hub_channel");
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notificationBuilder.setTicker(getResources().getString(R.string.app_name));
        notificationBuilder.setColor(getResources().getColor(R.color.colorAccent));
        notificationBuilder.setContentText(notificationState + " " + mUserName); // todo add groupName here...
        notificationBuilder.setSmallIcon(R.drawable.call);
        notificationBuilder.setLargeIcon(icon);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setOngoing(true);
        //add notification actions
        if (action.equals(Constants.ForegroundService.CANCEL_INGOING_CALL)) {
            intent.setAction(Constants.ForegroundService.CANCEL_INGOING_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent,  PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.call, "End call", pendingIntent1);
        } else if (action.equals(Constants.ForegroundService.END_CALL)) {
            intent.setAction(Constants.ForegroundService.END_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent,  PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.call, "End call", pendingIntent1);
        }
        Notification notification = notificationBuilder.build();
        startForeground(101, notification);
    }

    private void createNotification() {
        if (callState.equals(EventFromService.CALL_RINGING)) {
            createNotificationForGroupAudio("Call From ", mGroupName, Constants.ForegroundService.ACCEPT_CALL, -1L);
        } else if (callState.equals(EventFromService.CALL_DIALING)) {
            createNotificationForGroupAudio("Dialing ", mGroupName, Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
        } else if (callState.equals(EventFromService.CALL_RECONNECTED)) {
            createNotificationForGroupAudio("Group Call :", mGroupName, Constants.ForegroundService.END_CALL, 2L);
        } else if (callState.equals(EventFromService.CALL_CONNNECTED)) {
            createNotificationForGroupAudio("Connecting call with: ", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        } else if (callState.equals(EventFromService.CALL_STARTED)) {
            createNotificationForGroupAudio("Group call :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        } else {
            createNotificationForGroupAudio("Group call :", mGroupName, Constants.ForegroundService.END_CALL, 2L);// todo check what kind of call type based on that make notification.
        }
    }


    private void closeConnectionFromSocket() {
        if (callWebRTCManger != null) {
            callWebRTCManger.closePeerConnection();
            callWebRTCManger = null;
        }
//        GlobalMethods.updateUiToUpdateCallLogs();
        finishForegroundService();
    }

    private void updateConnectingStatus() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UPDATE_TO_CONNECTING_STATE);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }


    private void calculateTimeDifference() {
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        Long time = mSchCallEnd - currentTime;
        startCallEndTimerTask(time);
        Long differnce = mSchCallEnd - currentTime;
        Long percent = (differnce / 100) * 80;
        startCallEndingAlert(percent);
    }

    // api's implementation
    public void acceptIncommingGroupAudioCall() {
    }

    public void rejectIncommingGroupAudioCall() {
//        GlobalMethods.updateUiToUpdateCallLogs();
        if (callWebRTCManger != null) {
            callWebRTCManger.stopHandler();
            callWebRTCManger.closeRecieverSocketConnection();
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.RECIEVER_REJECT_GROUP_CALL);
        callClossingTask.startEndCallProcedure();
        finishForegroundService();
    }


    public class NotificationAsync extends AsyncTask<String, String, String> {
        String output = "";

        public String status = "";
        Context context;


        public final static String AUTH_KEY_FCM_LIVE = Constants.FirebaseMessaging;
        public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

        public NotificationAsync(GroupAudioCallerServiceFirebase context) {
            this.context = context;
        }

        String s;
        String firebaseId;

        public NotificationAsync(GroupAudioCallerServiceFirebase GroupAudioCallerServiceFirebase, String firebaseId) {

            this.firebaseId = firebaseId;
            this.s = s;

        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
//            String param1 = params[0];
            String sendTo = firebaseId;
//            String NotificationTitle = params[2];
//            String NotificationMessage = params[3];

            try {
                url = new URL(API_URL_FCM);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(60000);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM_LIVE);
                conn.setRequestProperty("Content-Type", "application/json");


                JSONObject json = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("roomId", mRoomId);
                jsonObject.put("userstring", StringUtils.getGson().toJson(UserManager.getInstance().getUserIfLoggedIn()));
                jsonObject.put("groupname", groupname);
                jsonObject.put("type", "audio");
                jsonObject.put("callerId", UserManager.getInstance().getUserIfLoggedIn().getId());

                json.put("data", jsonObject);
                json.put("type", "audio");
                json.put("to", sendTo);
                json.put("priority", "high");


                Log.d("json", "" + json);


                OutputStreamWriter wr = new OutputStreamWriter(
                        conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));


                while ((output = br.readLine()) != null) {
//            Toast.makeText(context, ""+output, Toast.LENGTH_SHORT).show();
                    Log.d("output", output);

                }

            } catch (Exception e) {
//        Toast.makeText(context, "erroor "+e, Toast.LENGTH_SHORT).show();
                Log.d("exception", "" + e);

            }

            return null;
        }
    }

    public class NotificationAsyncCancelCall extends AsyncTask<String, String, String> {
        String output = "";

        public String status = "";
        Context context;


        public final static String AUTH_KEY_FCM_LIVE = Constants.FirebaseMessaging;
        public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

        public NotificationAsyncCancelCall(GroupAudioCallerServiceFirebase context) {
            this.context = context;
        }

        String s;
        String firebaseId;

        public NotificationAsyncCancelCall(GroupAudioCallerServiceFirebase GroupAudioCallerServiceFirebase, String firebaseId) {

            this.firebaseId = firebaseId;
            this.s = s;

        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
//            String param1 = params[0];
            String sendTo = firebaseId;
//            String NotificationTitle = params[2];
//            String NotificationMessage = params[3];

            try {
                url = new URL(API_URL_FCM);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(60000);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM_LIVE);
                conn.setRequestProperty("Content-Type", "application/json");


                JSONObject json = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("roomId", mRoomId);
                jsonObject.put("userstring", StringUtils.getGson().toJson(UserManager.getInstance().getUserIfLoggedIn()));
                jsonObject.put("groupname", groupname);
                jsonObject.put("type", "callerCancelCall");
                jsonObject.put("callerId", UserManager.getInstance().getUserIfLoggedIn().getId());

                json.put("data", jsonObject);
                json.put("type", "audio");
                json.put("to", sendTo);
                json.put("priority", "high");


                Log.d("json", "" + json);


                OutputStreamWriter wr = new OutputStreamWriter(
                        conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));


                while ((output = br.readLine()) != null) {
//            Toast.makeText(context, ""+output, Toast.LENGTH_SHORT).show();
                    Log.d("output", output);

                }

            } catch (Exception e) {
//        Toast.makeText(context, "erroor "+e, Toast.LENGTH_SHORT).show();
                Log.d("exception", "" + e);

            }

            return null;
        }
    }

    public void initAudioCall(final Long mGroupId) {
//        if (!flag) {
//            final DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference().getRoot().child("users").child(attendedfirebaseID);
//            chatsReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    UserModel user = dataSnapshot.getValue(UserModel.class);
//                    if (user != null) {
//                        if (!flag) {
//                            flag=true;
//
        new NotificationAsync(GroupAudioCallerServiceFirebase.this, attendedfirebaseID).execute();
//                        }
//                        chatsReference.removeEventListener(this);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//        }
    }


    private void callerCancelGroupAudioCall() {

        removeNativeRunnabletask();
        removeDialingTimer();
        removeRingingTimer();
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.CALLER_ENDED_GROUP_CALL);
        callClossingTask.startEndCallProcedure();
        closeConnectionFromSocket();
    }

    private void callerCancelAudioCall() {
        new NotificationAsyncCancelCall(GroupAudioCallerServiceFirebase.this, attendedfirebaseID).execute();

        removeNativeRunnabletask();
        removeDialingTimer();
        removeRingingTimer();
        //todo notify screen / activity
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.CALLER_ENDED_INDIVIDUAL_CALL);
        callClossingTask.startEndCallProcedure();
        closeConnectionFromSocket();
    }

    private void finishConnectedIndividualCall() {
        removeNativeRunnabletask();
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

    private void groupMissCall() {
        removeRingingTimer();
        removeDialingTimer();
//        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.GROUP_MISS_CALL);
//        callClossingTask.startEndCallProcedure();
        closeConnectionFromSocket();
    }

    private void finishConnectedGroupCall() {
        removeNativeRunnabletask();
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
            finishForegroundService();
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

    public void removeUserFromCall(final Long userId) {
//        final HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", userId);
//        params.put("callId", mCallId);
//        params.put("adminId", UserManager.getInstance().getUserIfLoggedIn().getId()()());
//        params.put("roomId", mRoomId);
//        params.put("groupId", mGroupId);
//        CallRepository callRepository = new CallRepository();
//        callRepository.removeUserFromCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long removedParticipantId = jsonObject.getLong("removedParticipantId");
//                    if (mCallParticipantMap.containsKey(removedParticipantId)) {
//                        mCallParticipantMap.remove(removedParticipantId);
////                        UserModel user = StudentApp.getInstance().getHubDatabase().userDao().getUserIfLoggedIn(removedParticipantId);
////                        if (mParticipants.contains(user)) {
////                            mParticipants.remove(user);
////                        }
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

    public void redialGroupMember(final Long userId) {
//        final HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId()()());
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


    public void updateCallEndTime(final Long updatedTime) {
//        final HashMap<String, Object> params = new HashMap<>();
//        params.put("scheduledCallId", mSchCallId);
//        params.put("scheduledCallEndTime", updatedTime);
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallEndTime(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    int ok = jsonObject.getInt("ok");
//                    if (ok == 1) {
//                        removeCallEndingTask();
//                        Long duration = mSchCallEnd - Calendar.getInstance().getTimeInMillis();
//                        Long update = duration + updatedTime;
//                        startCallEndTimerTask(update);
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


    // AudioCallBackInterface implementation....
    @Override
    public void callEnded() {
//        Long currentTime = Calendar.getInstance().getTimeInMillis();
//        long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
//        mChronometer.stop();
//        switch (mCallType) {
//            case GROUP_AUDIO:
//                updateDbLogs(currentTime, duration);
//                break;
//            case INDIVIDUAL_AUDIO:
//                CallRepository callRepository = new CallRepository();
//                callRepository.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
////                GlobalMethods.updateUiToUpdateCallLogs();
//                break;
//        }
        finishForegroundService();
    }

    @Override
    public void callerCancelledCall() {

    }

    private void updateDbLogs(Long currentTime, Long duration) {
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
//        if (mCallParticipantMap.containsKey(UserManager.getInstance().getUserIfLoggedIn().getId()()())) {
//            Participants participants = mCallParticipantMap.get(UserManager.getInstance().getUserIfLoggedIn().getId()()()).getParticipants();
//            if (participants != null) {
//                participants.setCall_state_code(703L);
//                participants.setResponse_type_code(803L);
//                StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
//            }
//        }
    }

    @Override
    public void startTimer() {
        stopToneDialer();
        removeDialingTimer();
        removeRingingTimer();
        callState = EventFromService.CALL_STARTED;
        createNotificationForIndividualAudio("On call with ", Constants.ForegroundService.END_CALL, -1L);
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_STARTED);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void updateCallingToConnectingState() {
        stopToneDialer();
        removeDialingTimer();
        removeRingingTimer();
        callState = EventFromService.CALL_CONNNECTED;
        createNotificationForIndividualAudio("Connected to ", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_CONNNECTED);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void callerEndedAudioCall() {
        finishForegroundService();//
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
                if (mCallParticipantMap.containsKey(Long.valueOf(user.getId()))) {
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallHold(true);
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setmOnNativeCall(isOnNativeCall);
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallMuted(false);
                    sendLatestParticipantsStatus();
                }
                break;
            case INDIVIDUAL_AUDIO:
                mReciverOnHold = true;
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
                if (mCallParticipantMap.containsKey(Long.valueOf(user.getId()))) {
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallHold(false);
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setmCallMuted(false);
                    mCallParticipantMap.get(Long.valueOf(user.getId())).setmOnNativeCall(false);
                    sendLatestParticipantsStatus();
                }
                break;
            case INDIVIDUAL_AUDIO:
                mReciverOnHold = false;
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
        createNotificationForIndividualAudio("Reconnecting to ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTING);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void finishCall() {
        callState = EventFromService.CALL_RECONTECTED_FAILED;
        stopReconnectingDialer();
        createNotificationForIndividualAudio("Reconnection failed with ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
//        GlobalMethods.callEnded();
        finishConnectedIndividualCall();
        finishForegroundService();
    }

    @Override
    public void reconnectionEstablished() {
        if (callState.equals(EventFromService.CALL_STARTED)) {
            startChronometer();
        }
        stopReconnectingDialer();
        callState = EventFromService.CALL_RECONNECTED;
        createNotificationForIndividualAudio("On call with", Constants.ForegroundService.END_CALL, 2L);
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
        int volume = 0;
        if (is_LoudSpreakerPressed) {
            callWebRTCManger.enableLoudSpeaker();
            volume = ToneGenerator.MAX_VOLUME;
        } else {
            volume = 30;
        }
        playToneGenerator(volume);
        if (is_MuteButtonPressed) {
            callWebRTCManger.disableMicrophone();
        }
        playToneGenerator(volume);
        removeDialingTimer();
        startRingingTimerTask();
        callState = EventFromService.CALL_RINGING;
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RINGING);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        createNotificationForIndividualAudio("Ringing", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
    }

    @Override
    public void callRinging(UserModel user) {
        if (!isScheduleCall) {
            removeDialingTimer();
            updateSingleUserStatus(Long.valueOf(user.getId()));
            if (callState.equals(EventFromService.CALL_DIALING)) {
                int volume = 0;
                if (is_LoudSpreakerPressed) {
                    callWebRTCManger.enableLoudSpeaker();
                    volume = ToneGenerator.MAX_VOLUME;
                } else {
                    volume = 30;
                    callWebRTCManger.disableLoudSpeaker();
                }
                playToneGenerator(volume);
                if (is_MuteButtonPressed) {
                    callWebRTCManger.disableMicrophone();
                }
                callState = EventFromService.CALL_RINGING;
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RINGING);
                intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                createNotificationForGroupAudio("Ringing", "Group Name", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
                startRingingTimerTask();
            }
            updateUserStatusList(user, EventFromService.CALL_RINGING);
        } else {
            updateUserStatusList(user, EventFromService.CALL_RINGING);
            updateSingleUserStatus(Long.valueOf(user.getId()));
        }
    }

    @Override
    public void updateCallingToConnectingState(UserModel user) {
        if (!isScheduleCall) {
            if (callState.equals(EventFromService.CALL_RINGING) || callState.equals(EventFromService.CALL_DIALING)) {
                callState = EventFromService.CALL_CONNNECTED;
                stopToneDialer();
                removeRingingTimer();
                removeDialingTimer();
//                GlobalMethods.vibrateMobile();
                createNotificationForGroupAudio("Connected :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_STARTED);
                intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
            }
            updateUserStatusList(user, EventFromService.CALL_CONNNECTED);
        } else {

        }
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
            createNotificationForGroupAudio("Reconnecting :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
            Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
            intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTING);
            intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
            LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void reconnectionEstablished(UserModel user) {
        //todo check from which user it got reconnected and update ui
        if (callState.equals(EventFromService.CALL_STARTED)) {
            startChronometer();
        }
        stopReconnectingDialer();
        removeCallNotConnectedTask();
        callState = EventFromService.CALL_RECONNECTED;
        updateUserStatusList(user, EventFromService.CALL_RECONNECTED);
        createNotificationForGroupAudio("Group call :", mGroupName, Constants.ForegroundService.END_CALL, 2L);
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.CALL_RECONNECTED);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void startTimer(UserModel user) {
        stopToneDialer();
        removeDialingTimer();
        removeRingingTimer();
        if (callState.equals(EventFromService.CALL_CONNNECTED)) {
            callState = EventFromService.CALL_STARTED;
            updateParticipantStatusToConnected(UserManager.getInstance().getUserIfLoggedIn());
            createNotificationForGroupAudio("Group call :", mGroupName, Constants.ForegroundService.END_CALL, -1L);
        }
//        updateParticipantStatusToConnected(user);
        updateUserStatusList(user, EventFromService.CALL_STARTED);
    }

    //    private void updateParticipantStatusToConnected(UserModel user) {
//        Participants participants = mCallParticipantMap.get(user.getId()()()()).getParticipants();
//        participants.setCall_state_code(702L);
//        participants.setResponse_type_code(803L);
//        StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
//    }
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
//        //todo this user ended call... so handle accordingly....
//        if (mCallParticipantMap.containsKey(user.getId()()())) {
//            Participants participants = mCallParticipantMap.get(user.getId()()()).getParticipants();
//            if (participants != null) {
//                Long time = getTimeFromChronometer();
//                participants.setDuration(time);
//                participants.setCall_state_code(703L);
//                participants.setResponse_type_code(803L);
//                StudentApp.getInstance().getHubDatabase().participantsDao().updateParticipants(participants);
//            }
//        }
//        updateUserStatusList(user, EventFromService.CALL_END);
    }

    @Override
    public void finishCall(UserModel user) {
        createNotificationForGroupAudio("Reconnection Failed", mGroupName, Constants.ForegroundService.END_CALL, -1L);
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
        finishForegroundService();
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
                    if (mCallParticipantMap.containsKey(user.getId())) {
                        mCallParticipantMap.get(user.getId()).setmCallHold(false);
                        mCallParticipantMap.get(user.getId()).setmOnNativeCall(false);
                        mCallParticipantMap.get(user.getId()).setmCallMuted(true);
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
                    if (mCallParticipantMap.containsKey(user.getId())) {
                        mCallParticipantMap.get(user.getId()).setmCallHold(false);
                        mCallParticipantMap.get(user.getId()).setmOnNativeCall(false);
                        mCallParticipantMap.get(user.getId()).setmCallMuted(false);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        // Sets an ID for the notification, so it can be updated.
        String CHANNEL_ID = "hub_channel";// The id of the channel.
        CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
    }
}
