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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.ApplicationClass;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.DynamicPermission;
import com.appsinventiv.noorenikah.Utils.GlobalMethods;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

public class InitiateCallScreenActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private final static int sREQUEST_CODE = 1001;
    Long mGroupId = -1L;
    Long mCallId = -1L;
    CallManager.CallType mCallType;
    CallWebRTCManger callWebRTCManger;
    //    private CallViewModel callViewModel;
    ImageView mBtnEndCall;
    ImageView mIvCallInfo;
    CircleImageView mAttendeDp;
    TextView mTxtAttendeName;
    TextView mTxtAttendePost;
    Chronometer mChronometer;
    ImageView mConnectedCallEnd;
    ImageView mBtn_Speaker;
    ImageView mBtn_microphone;
    ImageButton mBtn_msg;
    ImageButton mBtn_hold;
    ImageButton mBtn_addCall;
    ImageButton mBtn_keypad;
    TextView mTvStatus;
    Long callStartTime = -1L;
    ToneGenerator mToneGenerator;
    boolean isCallStartedflag = true;
    Timer mTimer = new Timer();
    boolean isMute = false;
    boolean isHold = false;
    boolean isSpeaker = false;
    // Enum callState;
    boolean isCallStartedflagFromService = false;
    String mUserName;
    String mUserPost;
    String mUserDpUrl;
    Enum mCallStatusFromService;
    SensorManager sensorManager;
    Sensor proximitySensor;
    SensorEventListener proximitySensorListener;
    private LinearLayout black_layout;
    private LinearLayout mCallStatusLinearLayouot;
    private String duration = "";
    private LinearLayout mLayoutCallInfo;
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
        mTvStatus = findViewById(R.id.tv_status);
        mBtnEndCall = findViewById(R.id.CallEndBtn);
        mAttendeDp = findViewById(R.id.ReceivingPlaceHolder);
        mTxtAttendeName = findViewById(R.id.CallerNameTxt);
        mTxtAttendePost = findViewById(R.id.CallerDept);
        mChronometer = findViewById(R.id.chronometer);
        black_layout = findViewById(R.id.black_layout);
        mLayoutCallInfo = ((LinearLayout) findViewById(R.id.layoutCallInfo));
//        callViewModel = ViewModelProviders.of(InitiateCallScreenActivity.this).get(CallViewModel.class);
        mConnectedCallEnd = findViewById(R.id.ConnectedCallEndBtn);
        mBtn_Speaker = findViewById(R.id.ic_speaker);
        mIvCallInfo = (ImageView) findViewById(R.id.ivCallInfo);
        mBtn_microphone = findViewById(R.id.ic_muteBtn);
        mBtn_msg = findViewById(R.id.btn_msg);
        mCallStatusLinearLayouot = findViewById(R.id.layout_call_status);
        
        mBtn_hold = (ImageButton) findViewById(R.id.ic_hold);
        mBtn_keypad = (ImageButton) findViewById(R.id.btn_keypad);
        mBtn_addCall = (ImageButton) findViewById(R.id.btn_addCall);
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
        mBtn_msg.setOnClickListener(this);
        mIvCallInfo.setOnClickListener(this);
        mBtn_addCall.setOnClickListener(this);
        mBtn_keypad.setOnClickListener(this);
        mBtn_hold.setOnClickListener(this);
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
        Glide.with(InitiateCallScreenActivity.this).load(userModel.getPicUrl()).into(mAttendeDp);


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
                
            } else if (callState.equals(EventFromService.CALL_CONNNECTED)) {
                mCallStatusLinearLayouot.setVisibility(View.VISIBLE);
                mTvStatus.setText("  Connecting");
                
            } else if (callState.equals(EventFromService.CALL_STARTED)) {
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
//                final Long packets = intent.getLongExtra(Constants.IntentExtra.PACKETS, 0);
//                final double bandwidth = intent.getDoubleExtra(Constants.IntentExtra.BANDWIDTH, 0);
//                final double totalBandwidth = intent.getDoubleExtra(Constants.IntentExtra.TOTAL_BANDWIDTH, 0);
//                final double bitrate = intent.getDoubleExtra(Constants.IntentExtra.BITRATE, 0);
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("Call Stats: ", "Total Data Used: " + totalBandwidth + " Current Bandwidth: " + bandwidth + " Bitrate: " + bitrate + " Packet Sent: " + packets);
////                        ((TextView) findViewById(R.id.tvTotalBandwidth)).setText("Data Used: " + Utils.getTwoDecimalplaces(totalBandwidth) + " kb/s");
////                        ((TextView) findViewById(R.id.tvBandwidth)).setText("Bandwidth: " + Utils.getTwoDecimalplaces(bandwidth) + " kb/s");
////                        ((TextView) findViewById(R.id.tvBitRate)).setText("Bit Rate: " + Utils.getTwoDecimalplaces(bitrate) + " b/s");
//
//                        ((ImageView) findViewById(R.id.ivStrength)).setVisibility(View.VISIBLE);
//                        if (bandwidth > 1 && bandwidth < 2.5) {
////                            ((ImageView) findViewById(R.id.ivStrength)).setImageDrawable(getDrawable(R.drawable.ic_signal_orange));
////                            ((TextView) findViewById(R.id.tvDuration)).setText("Signals: Normal");
//                        } else if (bandwidth < 1) {
////                            ((TextView) findViewById(R.id.tvDuration)).setText("Signals: Low");
////                            ((ImageView) findViewById(R.id.ivStrength)).setImageDrawable(getDrawable(R.drawable.ic_signal_red));
//                        } else {
////                            ((TextView) findViewById(R.id.tvDuration)).setText("Signals: High");
////                            ((ImageView) findViewById(R.id.ivStrength)).setImageDrawable(getDrawable(R.drawable.ic_signal_green));
//                        }
////                        ((TextView) findViewById(R.id.tvPacket)).setText(packets + "");
//                    }
//                });
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
            case R.id.btn_msg:
                startChatActivity(mGroupId);
                break;
            case R.id.CallEndBtn:
                closeConnectionFromSocket();
                //  individualMissedCall();
//                GlobalMethods.vibrateMobile();
                callerCancelAudioCall();
                Intent intent1 = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent1.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.END_CALL_INGOING);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent1);

                startHomeActivity();
                break;
            case R.id.ConnectedCallEndBtn:
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
            case R.id.ivCallInfo:
                mLayoutCallInfo.setVisibility(mLayoutCallInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.btn_addCall:
                startAddMemberActivity();
                break;
            case R.id.ic_hold:
                isMute = false;
////                Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
//                mBtn_microphone.setImageDrawable(replacer);
                if (isHold) {
                    isHold = false;
////                    replacer = getResources().getDrawable(R.drawable.ic_hold);
//                    mBtn_hold.setImageDrawable(replacer);
                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.UNHOLD_BUTTON_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                } else {
                    isHold = true;
////                    replacer = getResources().getDrawable(R.drawable.ic_unhold);
////                    mBtn_hold.setImageDrawable(replacer);
                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.HOLD_BUTTON_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case R.id.btn_keypad:
                //   Toast.makeText(this, "Dailer Appear", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;

        }
    }

    private final static String GROUPS_PATH = "groups";
    private final static String GROUPS_MEMEBVER = "memeber";

    public void initAudioCall(final Long mGroupId) {
//        GroupRepository groupRepository = new GroupRepository();
//        groupRepository.getCallGroup(mGroupId, new ICommandResultListener() {
//            @Override
//            public void onCommandResult(ICommandResult result) {
//                final CallsGroup callsGroup = (CallsGroup) result.getData();
////                GlideHelper.loadImage(InitiateCallScreenActivity.this, callsGroup.getmUserDpUrl(), mAttendeDp, R.drawable.ic_profile_plc);
//                mTxtAttendePost.setText(callsGroup.getUserPost());
//                mTxtAttendeName.setText(callsGroup.getmUserName());

//        DatabaseReference dataReference = FirebaseDatabase.getInstance().getReference();
//        dataReference.getRoot().child(GROUPS_MEMEBVER).child(mgroupName).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!flag) {
//                    GroupMembers groupMembers = dataSnapshot.getValue(GroupMembers.class);
//                    Long userId;
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                        Map<String, Object> map = (Map) postSnapshot.getValue();
//                        if (map != null) {
//                            String groupmember2FirebaseId = (String) map.get("groupmember2FirebaseId");
//                            Long groupMember1Id = (Long) map.get("groupMember1Id");
//                            Long groupMember2Id = (Long) map.get("groupMember2Id");
//                            String groupmember1FirebaseId = (String) map.get("groupmember1FirebaseId");
////                            String groupId = (String) map.get("groupId");
//                            String userfirebaseId;
//                            if (groupmember1FirebaseId.equalsIgnoreCase(UserManager.getInstance().getUser().getmFireBaseId())) {
//                                userId = Long.valueOf(groupMember2Id);
//                                userfirebaseId = groupmember2FirebaseId;
//                            } else {
//                                userId = Long.valueOf(groupMember1Id);
//                                userfirebaseId = groupmember1FirebaseId;
//                            }
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        mRoomId = 555L;
        mRoomId = Long.valueOf(String.valueOf(currentTime)) / 10;
        Random rand = new Random();
        int n = rand.nextInt(50) + 1;
        mRoomId = Long.valueOf(n);
//        mRoomId = 12345l;

//                            flag = true;

        startInitiateCallerService("admin", userModel.getName(), userModel.getPicUrl(), mgroupName, userId, mfirebaseId);
//
//                        }
//                    }
//
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


//            }
//        });
    }

    private void finishConnectedCall() {
//        final Long currentTime = Calendar.getInstance().getTimeInMillis();
//        final long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
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

//    private void individualMissedCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getmFireBaseId());
//        params.put("callId", mCallId);
//        callViewModel.individualMissedCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    callViewModel.updateCallStatusAndResponse(mCallId, callStateCode, responseType, 0L, 0L);
//                    removeTimer();
//                    closeConnectionFromSocket();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
////                showToast("Server Error");
//                //todo make discussion on this one...
////                GlobalMethods.updateUiToUpdateCallLogs();
//                InitiateCallScreenActivity.this.finish();
//            }
//        });
//    }

    private void callerCancelAudioCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getmFireBaseId());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        callViewModel.callerCancelAudioCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    callViewModel.updateCallStatusAndResponse(mCallId, callStateCode, responseType, 0L, 0L);
//                    removeTimer();
////                    GlobalMethods.updateUiToUpdateCallLogs();
//                    closeConnectionFromSocket();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
////                showToast("Server Error");
//                //todo make discussion on this one...
//                closeConnectionFromSocket();
////                GlobalMethods.updateUiToUpdateCallLogs();
//                InitiateCallScreenActivity.this.finish();
//            }
//        });
    }

    private void askForDangerousPermissions() {
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
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
//            View decorView = getWindow().getDecorView();
//            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
//            black_layout.setVisibility(View.VISIBLE);
//            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(uiOptions);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
//                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        } else {
//            if(!wakeLock.isHeld()) {
//                wakeLock.release();
//            }
//            black_layout.setVisibility(View.GONE);
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

    public void startChatActivity(long groupId) {
//        Intent intent = new Intent(InitiateCallScreenActivity.this, ChatActivity.class);
//        intent.putExtra("groupId", groupId);
//        startActivity(intent);
//        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void startAddMemberActivity() {
//        GroupRepository groupRepository = new GroupRepository();
//        groupRepository.getGroupsWithMembers(mGroupId, new ICommandResultListener() {
//
//
//            @Override
//            public void onCommandResult(ICommandResult result) {
//                GroupWithMembers groupWithMembers = (GroupWithMembers) result.getData();
//                ArrayList<User> userlist = new ArrayList<>();
//                Long userId = -1L;
//                for (GroupMembers groupMembers :
//                        groupWithMembers.memberIds) {
//                    if (!Objects.equals(groupMembers.getmFireBaseId(), UserManager.getInstance().getUserIfLoggedIn().getmFireBaseId())) {
//                        userId = groupMembers.getmFireBaseId();
//                    }
//                }
//                if (userId != -1L) {
//                    userlist.add(StudentApp.getInstance().getHubDatabase().userDao().getUser(userId));
//                    Intent intent = new Intent(InitiateCallScreenActivity.this, AddMemberToCallActivity.class);
//                    intent.putExtra("selectedContacts", StringUtils.getGson().toJson(userlist));
//                    intent.putExtra("noOfPresentParticipant", 2);
//                    startActivityForResult(intent, sREQUEST_CODE);
//                }
//
//            }
//        });
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
