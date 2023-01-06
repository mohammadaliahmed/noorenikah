package com.appsinventiv.noorenikah.callWebRtc;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Models.CallModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.ApplicationClass;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.DynamicPermission;
import com.appsinventiv.noorenikah.Utils.GlobalMethods;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;

public class InitiateCallScreenActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private final static int sREQUEST_CODE = 1001;
    Long mGroupId = -1L;
    Long mCallId = -1L;
    CallManager.CallType mCallType;
    CallWebRTCManger callWebRTCManger;
    ImageView mBtnEndCall;
    TextView mTxtAttendeName;
    Chronometer mChronometer;
    ImageView mConnectedCallEnd;
    ImageView mBtn_Speaker;
    ImageView mBtn_microphone;
    TextView mTvStatus;
    ToneGenerator mToneGenerator;
    boolean isCallStartedflag = true;
    Timer mTimer = new Timer();
    boolean isMute = false;
    boolean isHold = false;
    boolean isSpeaker = false;
    boolean isCallStartedflagFromService = false;

    Enum mCallStatusFromService;
    SensorManager sensorManager;
    Sensor proximitySensor;
    SensorEventListener proximitySensorListener;
    private LinearLayout mCallStatusLinearLayouot;
    private String mParticipantsJson;
    private Long mRoomId;
    boolean isloudButtonPressed = false;
    boolean isMuteButtonPressed = false;

    //todo start timer call and call api...
//todo call api for cancel and save db state...
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;
    boolean isActive = true;
    private String mgroupName;
    private boolean flag = false;
    private long userId;
    private String mfirebaseId;
    private UserModel userModel;
    ImageView image;
    DatabaseReference mDatabase;
    private long duration;
    LinearLayout layout_widget_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiate_call_screen);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        mDatabase = Constants.M_DATABASE;
        showActivity();
        initViews();
        registerViews();
        getValuesFromIntent();
        registerLocalBroadCast();
        sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());

        switch (mCallType) {
            case INDIVIDUAL_AUDIO:
                if (!isCallStartedflagFromService) {
                    askForDangerousPermissions();
                } else {
                    updateUserAttributesOnUi();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void updateUserAttributesOnUi() {
////        GlideHelper.loadImage(InitiateCallScreenActivity.this, mUserDpUrl, mAttendeDp, R.drawable.ic_profile_plc);
//        if (userModel.getPrice() == null || userModel.getPrice().equalsIgnoreCase("")) {
//            mTxtAttendePost.setVisibility(View.GONE);
//
//        } else {
//            mTxtAttendePost.setVisibility(View.VISIBLE);
//
//            mTxtAttendePost.setText("RP. " + ustad.getPrice() + ", / minute");
//        }
        mTxtAttendeName.setText(userModel.getName());
        if (mCallStatusFromService != null) {
            Long timeElapse = getIntent().getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1L);
            if (timeElapse != -1L) {
                if (mCallStatusFromService.equals(EventFromService.CALL_STARTED)) {
                    Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.GET_LATEST_TIME);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    mCallStatusLinearLayouot.setVisibility(View.GONE);
                } else if (mCallStatusFromService.equals(EventFromService.CALL_CONNNECTED)) {
                    mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                    mTvStatus.setVisibility(View.VISIBLE);
                    mChronometer.setVisibility(View.GONE);
                    mTvStatus.setText("  Connecting");
                } else if (mCallStatusFromService.equals(EventFromService.CALL_RECONNECTED)) {
                    Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.GET_LATEST_TIME);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    mCallStatusLinearLayouot.setVisibility(View.GONE);
                } else if (mCallStatusFromService.equals(EventFromService.CALL_RECONTECTED_FAILED)) {
                    mChronometer.setVisibility(View.GONE);
                    mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                    mTvStatus.setText("Call-Disconnected ");
                } else if (mCallStatusFromService.equals(EventFromService.CALL_RECONNECTING)) {
                    mChronometer.setVisibility(View.GONE);
                    mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("   Reconnecting");

                }
            } else {
                if (mCallStatusFromService.equals(EventFromService.CALL_RINGING)) {
                    mChronometer.setVisibility(View.GONE);
                    mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("   Ringing");

                }
            }


        }
    }

    private void playToneGenerator() {
        final int BEEP_DURATION = 1300;
        final int WAITING_DURATION = 1000;
        new Thread(new Runnable() {
            public void run() {
                mToneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 30);
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
        }).start();
    }

    private void initViews() {
        layout_widget_call = findViewById(R.id.layout_widget_call);
        image = findViewById(R.id.image);
        mTvStatus = findViewById(R.id.tv_status);
        mBtnEndCall = findViewById(R.id.CallEndBtn);
        mTxtAttendeName = findViewById(R.id.CallerNameTxt);
        mChronometer = findViewById(R.id.chronometer);

//        callViewModel = ViewModelProviders.of(InitiateCallScreenActivity.this).get(CallViewModel.class);
        mConnectedCallEnd = findViewById(R.id.ConnectedCallEndBtn);
        mBtn_Speaker = findViewById(R.id.ic_speaker);
        mBtn_microphone = findViewById(R.id.ic_muteBtn);
        mCallStatusLinearLayouot = findViewById(R.id.layout_call_status);

    }

    private void startInitiateCallerService(String mUserPost, String mUserName, String mUserDpUrl, String mgroupName, Long attendentId, String userfirebaseId) {
        Intent startServiceIntent = new Intent(InitiateCallScreenActivity.this, GroupAudioCallerServiceFirebase.class);
        startServiceIntent.setAction(Constants.ForegroundService.ACTION_MAIN);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        startServiceIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_NAME, mgroupName);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_FIREBASEID, userfirebaseId);
        startServiceIntent.putExtra(Constants.IntentExtra.CALL_TYPE, CallManager.CallType.INDIVIDUAL_AUDIO);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_POST, mUserPost);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_IMAGE, mUserDpUrl);
        startServiceIntent.putExtra(Constants.IntentExtra.ATTENDENT_ID, attendentId);
        startServiceIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantsJson);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_ROOM_ID, mRoomId);
        startServiceIntent.putExtra(Constants.IntentExtra.IS_LOUDSPEAKER_PRESSED, isloudButtonPressed);
        startServiceIntent.putExtra(Constants.IntentExtra.IS_MUTE_PRESSED, isMuteButtonPressed);
        InitiateCallScreenActivity.this.startService(startServiceIntent);
    }

    private void registerViews() {
        mBtnEndCall.setOnClickListener(this);
        mConnectedCallEnd.setOnClickListener(this);
        mBtn_Speaker.setOnClickListener(this);
        mBtn_microphone.setOnClickListener(this);

    }


    private void getValuesFromIntent() {
        isCallStartedflagFromService = getIntent().getBooleanExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
        mGroupId = getIntent().getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
        userId = getIntent().getLongExtra(Constants.IntentExtra.USER_ID, -1);
        mfirebaseId = getIntent().getStringExtra(Constants.IntentExtra.USER_FIREBASEID);
        mgroupName = "test";
        mCallType = (CallManager.CallType) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_TYPE);
        mParticipantsJson = getIntent().getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        Type listType = new TypeToken<ArrayList<UserModel>>() {
        }.getType();
        ArrayList<UserModel> ustadArrayList = StringUtils.getGson().fromJson(mParticipantsJson, listType);
        userModel = ustadArrayList.get(0);
        if (isCallStartedflagFromService) {
            if (GlobalMethods.isMyServiceRunning(GroupAudioCallerServiceFirebase.class)) {
                mCallStatusFromService = (EventFromService) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_STATE);
            } else {
                startHomeActivity();
            }
        }

        mTxtAttendeName.setText(userModel.getName());
        Glide.with(InitiateCallScreenActivity.this).load(userModel.getLivePicPath()).into(image);


    }

    //register local broadcast
    private void registerLocalBroadCast() {
        //  LocalBroadcastManager.getInstance(this).registerReceiver(mNativeMobileIncomingCall,
//                new IntentFilter(Constants.Broadcasts.BROADCAST_NATIVE_CALL_STARTED));
//        LocalBroadcastManager.getInstance(this).registerReceiver(mCallRejected,
//                new IntentFilter(Constants.Broadcasts.BROADCAST_CALL_REJECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastFromService,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_SERVICE));
    }

    private BroadcastReceiver mNativeMobileIncomingCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isCallStartedflag) {
                callerCancelAudioCall();
            } else {
                finishConnectedCall();
            }
        }
    };

    private BroadcastReceiver mBroadCastFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventFromService callState = (EventFromService) intent.getSerializableExtra(Constants.IntentExtra.EVENT_FROM_SERVICE);
            if (callState.equals(EventFromService.CALL_RINGING)) {
                mChronometer.setVisibility(View.GONE);
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText("   Ringing");
                playToneGenerator();

            } else if (callState.equals(EventFromService.CALL_CONNNECTED)) {
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setText("  Connecting");
                stopTone();


            } else if (callState.equals(EventFromService.CALL_STARTED)) {
                layout_widget_call.setVisibility(View.VISIBLE);
                Long elapsedTime = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
                mBtnEndCall.setVisibility(View.GONE);
                mConnectedCallEnd.setVisibility(View.VISIBLE);
                if (elapsedTime != -1)
                    mCallStatusLinearLayouot.setVisibility(View.GONE);
                startChronometerTime(elapsedTime);
//                GlobalMethods.vibrateMobile();
            } else if (callState.equals(EventFromService.CALL_RECONNECTED)) {
                mCallStatusLinearLayouot.setVisibility(View.GONE);
                intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.GET_LATEST_TIME);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
            } else if (callState.equals(EventFromService.CALL_RECONTECTED_FAILED)) {
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setText("call-Disconnected");

                mChronometer.setVisibility(View.GONE);
            } else if (callState.equals(EventFromService.CALL_END)) {
                CommonUtils.showToast("Call end");
            } else if (callState.equals(EventFromService.CALL_RECONNECTING)) {
                mChronometer.setVisibility(View.GONE);
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText("   Reconnecting");

            } else if (callState.equals(EventFromService.PARTICIPANT_MUTE)) {
                mChronometer.setVisibility(View.GONE);
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText("  Receiver on Mute");

            } else if (callState.equals(EventFromService.PARTICIPANT_UNMUTE)) {
                mTvStatus.setVisibility(View.GONE);

                mCallStatusLinearLayouot.setVisibility(View.GONE);
                mChronometer.setVisibility(View.VISIBLE);
            } else if (callState.equals(EventFromService.HOLD_CALL)) {
                mChronometer.setVisibility(View.GONE);
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText(" Receiver on hold");

            } else if (callState.equals(EventFromService.NATIVE_CALL_HOLD)) {
                isHold = true;
////                Drawable replacer = getResources().getDrawable(R.drawable.ic_unhold);
//                mBtn_hold.setImageDrawable(replacer);
            } else if (callState.equals(EventFromService.UNHOLD_CALL)) {
                isHold = false;
////                //   Drawable replacer = getResources().getDrawable(R.drawable.ic_hold);
//                //  mBtn_hold.setImageDrawable(replacer);
                mTvStatus.setVisibility(View.GONE);

                mCallStatusLinearLayouot.setVisibility(View.GONE);
                mChronometer.setVisibility(View.VISIBLE);
            } else if (callState.equals(EventFromService.MESSAGE)) {
                String msg = intent.getStringExtra(Constants.IntentExtra.MESSAGE);
                Toast.makeText(InitiateCallScreenActivity.this, msg, Toast.LENGTH_SHORT).show();
            } else if (callState.equals(EventFromService.FINISH_ACTIVITY)) {
//                //  GlobalMethods.vibrateMobile();
                startHomeActivity();
            } else if (callState.equals(EventFromService.FINISH_ACTIVITY_INIATOR_SCREEN)) {
                startHomeActivity();
            } else if (callState.equals(EventFromService.SEND_LATEST_TIME)) {
                Long elapsedTime = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
                Enum speakerState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.LOUDSPEAKER_STATE);
                Enum micState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.MICROPHONE_STATE);
                Enum holdState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.HOLD_STATE);
                setCallWidget(speakerState, micState, holdState);
                mBtnEndCall.setVisibility(View.GONE);
                mConnectedCallEnd.setVisibility(View.VISIBLE);
                mCallStatusLinearLayouot.setVisibility(View.GONE);
                startChronometerTime(elapsedTime);
                boolean isRecieverOnHold = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, false);
                checkIfReciverOnHold(isRecieverOnHold);
                boolean isRecieverOnMute = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, false);
                checkIfRecieverOnMute(isRecieverOnMute);
//                GlobalMethods.vibrateMobile();
            } else if (callState.equals(EventFromService.UPDATE_CALL_ID)) {
                mCallId = intent.getLongExtra(Constants.IntentExtra.CALL_ID, -1L);
            } else if (callState.equals(EventFromService.UPDATE_CALL_STATS)) {

            }
        }
    };

    private void setCallWidget(Enum speakerState, Enum micState, Enum holdState) {
        if (speakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_PRESSED)) {
            isSpeaker = true;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker_pressed);
            mBtn_Speaker.setImageDrawable(replacer);
        } else if (speakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
            isSpeaker = false;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker);
            mBtn_Speaker.setImageDrawable(replacer);
        }
        if (micState.equals(CallWidgetState.MUTE_BUTTON_PRESSED)) {
            isMute = true;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
            mBtn_microphone.setImageDrawable(replacer);
        } else if (micState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
            isMute = false;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
            mBtn_microphone.setImageDrawable(replacer);
        }
        if (holdState.equals(CallWidgetState.HOLD_BUTTON_PRESSED)) {
            isHold = true;
////            Drawable replacer = getResources().getDrawable(R.drawable.ic_unhold);
//            mBtn_hold.setImageDrawable(replacer);
        } else if (micState.equals(CallWidgetState.HOLD_BUTTON_RELEASE)) {
            isHold = false;
////            Drawable replacer = getResources().getDrawable(R.drawable.ic_hold);
//            mBtn_hold.setImageDrawable(replacer);
        }
    }

    private void startChronometerTime(Long elapsedTime) {
        if (elapsedTime != -1) {
            mCallStatusLinearLayouot.setVisibility(View.GONE);
            mChronometer.setVisibility(View.VISIBLE);
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
            mChronometer.setBase(SystemClock.elapsedRealtime() - elapsedTime);
            mChronometer.start();
        }
    }

    private BroadcastReceiver mCallRejected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long callId = intent.getLongExtra("callId", -1);
            if (Objects.equals(callId, mCallId)) {
                if (callWebRTCManger != null) {
                    callWebRTCManger.closePeerConnection();
                    showToast("Call Rejected");
//                    GlobalMethods.updateUiToUpdateCallLogs();
                    InitiateCallScreenActivity.this.finish();
                }
            }
        }
    };


    private void showActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(proximitySensorListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static final int SENSOR_SENSITIVITY = 4;


    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
//        if (callState != null) {
//            if (callState.equals(CALL_STATE.CALL_CONNECTED)) {
//                callViewModel.updateCallStatusAndResponse(mCallId, 703L, 803L);
//                closePeerConnection();
//                finishConnectedCall();
//            } else if (callState.equals(CALL_STATE.CALLING)) {
//                callViewModel.updateCallStatusAndResponse(mCallId, 703L, 803L, 0L, 0L);
//                closeConnectionFromSocket();
//                callerCancelAudioCall();
//            }
//        }
//        isCallStartedflag = false;
//        if (mToneGenerator != null) {
//            mToneGenerator.stopTone();
//        }
//        removeTimer();
        unregisterSensor();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNativeMobileIncomingCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCallRejected);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastFromService);
        super.onDestroy();
    }

    public void stopTone() {
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
        }
    }

    public void unregisterSensor() {
        try {
            isActive = false;
            if (proximitySensor != null) {
                sensorManager.unregisterListener(this, proximitySensor);
            }
            powerManager = null;
            wakeLock = null;
            proximitySensor = null;
            proximitySensorListener = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }

    private void closeConnectionFromSocket() {
        if (callWebRTCManger != null) {
            isCallStartedflag = false;
            if (mToneGenerator != null) {
                mToneGenerator.stopTone();
            }
            callWebRTCManger.closePeerConnection();
            callWebRTCManger = null;
            //  callState = CALL_ENDED;
//            GlobalMethods.updateUiToUpdateCallLogs();
            this.finish();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CallEndBtn:
                endCallInDb();
                closeConnectionFromSocket();
                callerCancelAudioCall();
                Intent intent1 = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent1.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.END_CALL_INGOING);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent1);
                startHomeActivity();
                break;
            case R.id.ConnectedCallEndBtn:
                duration = SystemClock.elapsedRealtime() - mChronometer.getBase();

                endCallInDb();
                // finishConnectedCall();
//                GlobalMethods.vibrateMobile();
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.END_CALL_PRESSED);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                startHomeActivity();
                break;
            case R.id.ic_speaker:
                if (isSpeaker) {
                    isSpeaker = false;
                    isloudButtonPressed = false;
                    Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker);
                    mBtn_Speaker.setImageDrawable(replacer);
                    //  callWebRTCManger.enableDisableLoudSpeaker();
                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.DISABLE_LOUD_SPEAKER_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                } else {
                    isSpeaker = true;
                    isloudButtonPressed = true;
                    Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker_pressed);
                    mBtn_Speaker.setImageDrawable(replacer);
                    //callWebRTCManger.enableDisableLoudSpeaker();
                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.ENABLE_LOUD_SPEAKER_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case R.id.ic_muteBtn:
                if (!isHold) {
                    if (isMute) {
                        isMute = false;
                        isMuteButtonPressed = false;
                        Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
                        mBtn_microphone.setImageDrawable(replacer);
                        //  callWebRTCManger.unHoldIngoingAudioCall(true);
                        //  callWebRTCManger.enableMicrophone();
                        intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.UNMUTE_BUTTON_PRESSD);
                        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    } else {
                        isMute = true;
                        isMuteButtonPressed = true;
                        Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
                        mBtn_microphone.setImageDrawable(replacer);
                        //  callWebRTCManger.disableMicrophone();
                        intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.MUTE_BUTTON_PRESSED);
                        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    }
                } else {
                    Toast.makeText(this, "Call is onhold", Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.ic_hold:
                isMute = false;

                if (isHold) {
                    isHold = false;

                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.UNHOLD_BUTTON_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                } else {
                    isHold = true;

                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.HOLD_BUTTON_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                }
                break;

            default:
                break;

        }
    }

    private void endCallInDb() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("endTime", System.currentTimeMillis());
        map.put("seconds", duration);

        mDatabase.child("Calls").child(SharedPrefs.getUser().getPhone()).child(userModel.getPhone())
                .child("" + mRoomId).updateChildren(map);

        mDatabase.child("Calls").child(userModel.getPhone()).child(SharedPrefs.getUser().getPhone())
                .child("" + mRoomId).updateChildren(map);


    }

    public void initAudioCall(final Long mGroupId) {
        Random rand = new Random();
        int n = rand.nextInt(998) + 1;
        mRoomId = Long.valueOf(n);
        startInitiateCallerService("admin", userModel.getName(), userModel.getPicUrl(), mgroupName, userId, mfirebaseId);
        CallModel model = new CallModel("" + mRoomId,
                userModel.getPhone(),
                userModel.getName(),
                userModel.getLivePicPath(),
                Constants.CALL_OUTGOING,
                false,
                0,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        mDatabase.child("Calls").child(SharedPrefs.getUser().getPhone()).child(userModel.getPhone())
                .child("" + mRoomId).setValue(model);

        CallModel model2 = new CallModel("" + mRoomId,
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath(),
                Constants.CALL_INCOMING,
                false,
                0,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        mDatabase.child("Calls").child(userModel.getPhone()).child(SharedPrefs.getUser().getPhone())
                .child("" + mRoomId).setValue(model2);


    }

    private void finishConnectedCall() {
//        final Long currentTime = Calendar.getInstance().getTimeInMillis();
        duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
//        mChronometer.stop();
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getmFireBaseId());
//        params.put("callId", mCallId);
//        params.put("duration", duration);
//        params.put("callEndTime", currentTime);
//        params.put("lastActivityTime", currentTime);
//        callViewModel.endOneToOneCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    callViewModel.updateCallStatusAndResponse(mCallId, callStateCode, responseType, currentTime, duration);
//                    //callState = CallState.CALL_ENDED;
//                    closePeerConnection();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
////                showToast("Server Error");
//                //todo make discussion on this one...
//                closePeerConnection();
//                //   callState = CallState.CALL_ENDED;
//                InitiateCallScreenActivity.this.finish();
//            }
//        });
    }

    private void closePeerConnection() {
        if (callWebRTCManger != null) {
            callWebRTCManger.endCall();
            callWebRTCManger = null;
//            GlobalMethods.updateUiToUpdateCallLogs();
            this.finish();
        }
    }

    public void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(InitiateCallScreenActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void callerCancelAudioCall() {

    }

    private void askForDangerousPermissions() {
        ArrayList<String> permissions = new ArrayList<String>();
//        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
//        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        DynamicPermission dynamicPermission = new DynamicPermission(this, permissions);
        boolean isPermissionGranted = dynamicPermission.checkAndRequestPermissions();
        if (isPermissionGranted) {
            initAudioCall(mGroupId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                this);
        builder.setTitle("Need Permission");
        builder.setMessage("This app needs dynamic permissions for calling feature.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                GlobalMethods.showToast("To call feature you have to provide these permission");
                //askForDangerousPermissions();
                finish();
            }
        });
        if (requestCode == 11) {
            boolean isgranted = true;
            int counter = 0;
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    boolean showRationale = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        showRationale = shouldShowRequestPermissionRationale(permission);
                    }
                    if (!showRationale) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 1);
                        isgranted = false;
                        break;
                    } else {
                        builder.show();
                    }

                    isgranted = false;
                    break;
                }
            }
            if (!isgranted) {
//                GlobalMethods.showToast("To call feature you have to provide these permission");
            } else {
                initAudioCall(mGroupId);
            }


        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values[0] >= -SENSOR_SENSITIVITY && sensorEvent.values[0] <= SENSOR_SENSITIVITY) {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void startHomeActivity() {
        Intent intent = new Intent(InitiateCallScreenActivity.this, MainActivity.class);
        intent.putExtra("call", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        unregisterSensor();
        InitiateCallScreenActivity.this.finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == sREQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String participantJson = data.getStringExtra("result");
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.ADD_PERSON_TO_CALL);
                intent.putExtra(Constants.IntentExtra.NEW_CALL_PARTICIPANT, participantJson);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkIfReciverOnHold(boolean isHold) {
        if (isHold) {
            mChronometer.setVisibility(View.GONE);
            mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText(" Receiver on hold");

        }
    }

    private void checkIfRecieverOnMute(boolean isHold) {
        if (isHold) {
            mChronometer.setVisibility(View.GONE);
            mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText(" Receiver on Mute");

        }
    }

}
