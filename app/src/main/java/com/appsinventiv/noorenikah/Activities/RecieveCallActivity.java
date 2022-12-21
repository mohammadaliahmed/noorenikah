package com.appsinventiv.noorenikah.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.ApplicationClass;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.DynamicPermission;
import com.appsinventiv.noorenikah.Utils.GlobalMethods;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.Utils.UserManager;

import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.call.CallState;
import com.appsinventiv.noorenikah.callWebRtc.CallListner.AudioCallBackInterface;
import com.appsinventiv.noorenikah.callWebRtc.CallWebRTCManger;
import com.appsinventiv.noorenikah.callWebRtc.GroupAudioRecieverServiceFirebase;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

//import org.webrtc.VideoCapturerAndroid;

public class RecieveCallActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener, AudioCallBackInterface {
    private ImageView mBtnAcceptCall;
    private ImageView mBtnDeclineCall;
    private TextView mTxtCallerName;
    private TextView mTxtCallerPost;
    private float x1, x2;
    private CircleImageView mCallerDp;
    Long mGroupId = -1L;
    Long mUserId = -1L;
    Long mCallId = -1L;
    String userstring = "";
    UserModel user;
    String callingType = "";
    CallManager.CallType mCallType;
    CallWebRTCManger callWebRTCManger;
    Chronometer mChronometer;
    LinearLayout mLayoutCallAcceptReject;
    LinearLayout mLayoutCallWidget;
    ImageView mConnectedCallEnd;
    ImageButton mBtn_microphone;
    ImageButton mBtn_Speaker;
    //    ImageButton mBtn_hold;
    TextView mTvStatus;
    Long callStartTime = -1L;
    Ringtone ringtone;
    Timer mTimer = new Timer();
    boolean isCallStartedFlag = false;
    boolean isMute = false;
    boolean isSpeaker = false;
    boolean isHold = false;
    Enum callState;
    SensorManager sensorManager;
    Sensor proximitySensor;
    SensorEventListener proximitySensorListener;
    private static final int SENSOR_SENSITIVITY = 4;
    private LinearLayout black_layout;
    String mUserName;
    String mUserPost;
    String mUserDpUrl;
    boolean isCallStartedflagFromService = false;
    Enum mCallStatusFromService;
    ImageButton mcallCenterBtn;
    LinearLayout mCallStatusLayout;
    private ImageView mIvCallInfo;
    private LinearLayout mLayoutCallInfo;
    private String mParticipantJson;
    private Long mRoomId;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;
    private String mGroupname;
    private String mCallerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieve_call_screen);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        initViews();
        registerViews();
        getValuesFromIntent();
        registerLocalBroadCast();
        showActivity();
        if (!isCallStartedflagFromService) {
            getCallerInfo();
            startCallRecieverService("", "", "");
        } else {
            updateUserAttributesOnUi();
        }

        askForDangerousPermissions();
        //  playSoundForIncommingcall();
        sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        powerManager = (PowerManager) RecieveCallActivity.this.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());

    }

    private void updateUserAttributesOnUi() {
        populateCallerInfo();
        if (mCallStatusFromService != null) {
            Long timeElapse = getIntent().getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1L);
            if (timeElapse != -1L) {
                if (mCallStatusFromService.equals(EventFromService.CALL_STARTED)) {
                    mLayoutCallAcceptReject.setVisibility(View.GONE);
                    mLayoutCallWidget.setVisibility(View.VISIBLE);
                    mConnectedCallEnd.setVisibility(View.VISIBLE);
                } else if (mCallStatusFromService.equals(EventFromService.CALL_CONNNECTED)) {
                    mCallStatusLayout.setVisibility(View.VISIBLE);
                    mTvStatus.setText("  Connecting ");
                    mLayoutCallAcceptReject.setVisibility(View.GONE);
                    mLayoutCallWidget.setVisibility(View.VISIBLE);
                    mConnectedCallEnd.setVisibility(View.VISIBLE);
                } else if (mCallStatusFromService.equals(EventFromService.CALL_RECONNECTED)) {
                    Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.GET_LATEST_TIME);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    mCallStatusLayout.setVisibility(View.GONE);
                    mLayoutCallAcceptReject.setVisibility(View.GONE);
                    mLayoutCallWidget.setVisibility(View.VISIBLE);
                    mConnectedCallEnd.setVisibility(View.VISIBLE);
                } else if (mCallStatusFromService.equals(EventFromService.CALL_RECONTECTED_FAILED)) {
                    mCallStatusLayout.setVisibility(View.VISIBLE);
                    mTvStatus.setText("Call-Disconnected ");
                    mChronometer.setVisibility(View.GONE);
                    mLayoutCallAcceptReject.setVisibility(View.GONE);
                    mLayoutCallWidget.setVisibility(View.VISIBLE);
                } else if (mCallStatusFromService.equals(EventFromService.CALL_RECONNECTING)) {
                    mChronometer.setVisibility(View.GONE);
                    mCallStatusLayout.setVisibility(View.VISIBLE);
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("   Reconnecting");
                    mLayoutCallAcceptReject.setVisibility(View.GONE);
                    mLayoutCallWidget.setVisibility(View.VISIBLE);
                }
            } else {
                if (mCallStatusFromService.equals(EventFromService.CALL_CONNNECTED)) {
                    mCallStatusLayout.setVisibility(View.VISIBLE);
                    mTvStatus.setText("  Connecting ");
                    mLayoutCallAcceptReject.setVisibility(View.GONE);
                    mLayoutCallWidget.setVisibility(View.VISIBLE);
                    mConnectedCallEnd.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void getCallerInfo() {
//        GroupMembers mGroupMembers = ApplicationClass.getInstance().getHubDatabase().groupMembersDao().
//                getMemberFromLocal(mGroupId, mUserId);
//        mCallGroupDetail = new CallsGroup(mGroupMembers);
        displayCallerInfo();
    }

    private void playSoundForIncommingcall() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(RecieveCallActivity.this, notification);
            ringtone.play();
            startTimerTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopIncommingCall() {
        ringtone.stop();
    }

    private void removeTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }

    private void startTimerTask() {
        final Long delay = 60000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!isCallStartedFlag) {
//                    ApplicationClass.getInstance().getHubDatabase().callsDao().updateCallState(mCallId, 704L, 802L, 0L, 0L);
//                    GlobalMethods.updateUiToUpdateCallLogs();
                    // showToast("TimeOut");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            startHomeActivity();
                            RecieveCallActivity.this.finish();
                        }
                    });
                }
            }
        };
        mTimer.schedule(task, delay);
    }

    public void startHomeActivity() {
        unregisterSensor();
        Intent intent = new Intent(RecieveCallActivity.this, MainActivity.class);
        intent.putExtra("call", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        RecieveCallActivity.this.finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        mTvStatus = findViewById(R.id.tv_status);
        mBtnAcceptCall = findViewById(R.id.CallAttendBtn);
        mBtnDeclineCall = findViewById(R.id.CallDeclineBtn);
        mTxtCallerName = findViewById(R.id.callerName);
        mTxtCallerPost = findViewById(R.id.callerPost);
        mCallerDp = findViewById(R.id.callerDp);
        black_layout = findViewById(R.id.black_layout);
        mcallCenterBtn = findViewById(R.id.callCenterBtn);
        mCallStatusLayout = findViewById(R.id.layout_call_status);
        mLayoutCallInfo = ((LinearLayout) findViewById(R.id.layoutCallInfo));
        mIvCallInfo = (ImageView) findViewById(R.id.ivCallInfo);
//        mBtn_hold = (ImageButton) findViewById(R.id.ic_hold);
        //mcallCenterBtn.setClickable(false);
        mBtnAcceptCall.setOnClickListener(this);
        mIvCallInfo.setOnClickListener(this);
        mBtnDeclineCall.setOnClickListener(this);
        mChronometer = findViewById(R.id.chronometer);
        mLayoutCallAcceptReject = findViewById(R.id.layout_call_accept_reject);
        mLayoutCallWidget = findViewById(R.id.layout_call_widget);
        mBtn_microphone = findViewById(R.id.ic_muteBtn);
        mBtn_Speaker = findViewById(R.id.ic_speaker);
        mConnectedCallEnd = findViewById(R.id.ConnectedCallEndBtn);
    }

    private void registerViews() {
//        mBtnAcceptCall.setOnClickListener(this);
//        mBtnDeclineCall.setOnClickListener(this);
        mConnectedCallEnd.setOnClickListener(this);
        mBtn_microphone.setOnClickListener(this);
        mBtn_Speaker.setOnClickListener(this);
//        mBtn_hold.setOnClickListener(this);
    }


    private void displayCallerInfo() {
//        GlideHelper.loadImage(RecieveCallActivity.this, user.getLogo(), mCallerDp, R.drawable.ic_profile_plc);
        if (user.getPicUrl() != null) {
            Glide.with(this).load(user.getPicUrl()).placeholder(R.drawable.ic_profile).into(mCallerDp);
        }
        mTxtCallerName.setText(user.getName());
        mUserName = user.getName();
    }

    private void populateCallerInfo() {
//        GlideHelper.loadImage(RecieveCallActivity.this, mUserDpUrl, mCallerDp, R.drawable.ic_profile_plc);
        Glide.with(this).load(user.getPicUrl()).placeholder(R.drawable.ic_profile).into(mCallerDp);
        mTxtCallerName.setText(mUserName);
    }

    public void callAttend() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.ACCEPT_CALL_PRESSED);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    public void callDecline() {
        new NotificationAsyncRejectCall(RecieveCallActivity.this, user.getFcmKey()).execute();


        Intent callIntent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
//        GlobalMethods.vibrateMobile();
        callIntent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
        callIntent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.END_CALL_INCOMING);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(callIntent);
        startHomeActivity();
    }

    public class NotificationAsyncRejectCall extends AsyncTask<String, String, String> {
        String output = "";

        public String status = "";
        Context context;


        public final static String AUTH_KEY_FCM_LIVE = Constants.FirebaseMessaging;
        public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

        public NotificationAsyncRejectCall(RecieveCallActivity context) {
            this.context = context;
        }

        String s;
        String firebaseId;

        public NotificationAsyncRejectCall(RecieveCallActivity GroupAudioCallerServiceFirebase, String firebaseId) {

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
                jsonObject.put("groupname", "abc");
                jsonObject.put("type", "callRejected");
                jsonObject.put("callerId", UserManager.getInstance().getUserIfLoggedIn().getId());

                json.put("data", jsonObject);
                json.put("type", "callRejected");
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CallAttendBtn:
                callAttend();
//                acceptIncommingCall();
//                stopIncommingCall();
//                mLayoutCallAcceptReject.setVisibility(View.GONE);
//                mLayoutCallConnected.setVisibility(View.VISIBLE);
                Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
//                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.ACCEPT_CALL_PRESSED);
//                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                break;
            case R.id.callCenterBtn:
                break;
            case R.id.CallDeclineBtn:
                callDecline();
                // terminateCall();
//                rejectIncomingCall();
//                stopIncommingCall();
//                GlobalMethods.vibrateMobile();
//                intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
//                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.END_CALL_INCOMING);
//                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                break;
            case R.id.ConnectedCallEndBtn:
                // finishConnectedCall();
//                GlobalMethods.vibrateMobile();
                intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.END_CALL_PRESSED);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                startHomeActivity();
                break;
            case R.id.ic_muteBtn:
                if (!isHold) {
                    if (isMute) {
                        isMute = false;
                        Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
                        mBtn_microphone.setImageDrawable(replacer);
                        // callWebRTCManger.enableMicrophone();
                        intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.UNMUTE_BUTTON_PRESSD);
                        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    } else {
                        isMute = true;
                        Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
                        mBtn_microphone.setImageDrawable(replacer);
                        //  callWebRTCManger.disableMicrophone();
                        intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.MUTE_BUTTON_PRESSED);
                        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                    }
                } else {
                    Toast.makeText(this, "Call is onHold", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ic_speaker:
                if (isSpeaker) {
                    isSpeaker = false;
                    Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker);
                    mBtn_Speaker.setImageDrawable(replacer);
                    // callWebRTCManger.enableDisableLoudSpeaker();
                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.DISABLE_LOUD_SPEAKER_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                } else {
                    isSpeaker = true;
                    Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker_pressed);
                    mBtn_Speaker.setImageDrawable(replacer);
                    // callWebRTCManger.enableDisableLoudSpeaker();
                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.ENABLE_LOUD_SPEAKER_PRESSED);
                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                }
                break;
//            case R.id.ic_hold:
//                isMute = false;
////                Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
////                mBtn_microphone.setImageDrawable(replacer);
//                if (isHold) {
//                    isHold = false;
////                    replacer = getResources().getDrawable(R.drawable.ic_hold);
//////                    mBtn_hold.setImageDrawable(replacer);
//                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
//                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.UNHOLD_BUTTON_PRESSED);
//                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
//                } else {
//                    isHold = true;
////                    replacer = getResources().getDrawable(R.drawable.ic_unhold);
//////                    mBtn_hold.setImageDrawable(replacer);
//                    intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
//                    intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.HOLD_BUTTON_PRESSED);
//                    LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
//                }
//                break;
            case R.id.ivCallInfo:
                mLayoutCallInfo.setVisibility(mLayoutCallInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            default:
        }
    }

    private void terminateCall() {
        if (callWebRTCManger != null) {
            callWebRTCManger.endCall();
            callWebRTCManger = null;
        }
    }

    private void startCallRecieverService(String mUserPost, String mUserName, String mUserDpUrl) {
        Intent startServiceIntent = new Intent(RecieveCallActivity.this, GroupAudioRecieverServiceFirebase.class);
        startServiceIntent.setAction(Constants.ForegroundService.ACTION_MAIN);
        startServiceIntent.putExtra(Constants.IntentExtra.CALL_TYPE, CallManager.CallType.INDIVIDUAL_AUDIO);
        startServiceIntent.putExtra(Constants.IntentExtra.USERSTRING, userstring);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_POST, mUserPost);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        startServiceIntent.putExtra(Constants.IntentExtra.CALLER_USER_ID, mCallerId);

        startServiceIntent.putExtra(Constants.IntentExtra.USER_IMAGE, mUserDpUrl);
        startServiceIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantJson);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_ROOM_ID, mRoomId);
        RecieveCallActivity.this.startService(startServiceIntent);
    }

   /* private void captureStream() {
        //  VideoCapturerAndroid capturer = VideoCapturerAndroid.create();
        // Returns the number of camera devices
//        VideoCapturerAndroid.getDeviceCount();
//
//// Returns the front face device name
//        VideoCapturerAndroid.getNameOfFrontFacingDevice();
//// Returns the back facing device name
//        VideoCapturerAndroid.getNameOfBackFacingDevice();

// Creates a VideoCapturerAndroid instance for the device name
        VideoCapturerAndroid capturer = VideoCapturerAndroid.create("hello", new VideoCapturerAndroid.CameraEventsHandler() {
            @Override
            public void onCameraError(String s) {

            }

            @Override
            public void onCameraFreezed(String s) {

            }

            @Override
            public void onCameraOpening(int i) {

            }

            @Override
            public void onFirstFrameAvailable() {

            }

            @Override
            public void onCameraClosed() {

            }
        });
    }*/

    private BroadcastReceiver mIncomingCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (callWebRTCManger != null) {
//                callWebRTCManger.endCall();
//            }
            finishConnectedCall();
        }
    };


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

    private BroadcastReceiver mMissedCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //close screen...
            isCallStartedFlag = false;
            callState = CallState.CALL_ENDED;
//            GlobalMethods.updateUiToUpdateCallLogs();
            if (ringtone != null) {
                ringtone.stop();
            }
            Long callId = intent.getLongExtra("callId", -1);
            if (Objects.equals(callId, mCallId)) {
//                startHomeActivity();
                RecieveCallActivity.this.finish();
                //   showToast("You missed a call");
            }
        }
    };

    private BroadcastReceiver mCallerCancelCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //close screen...
            //   RecieveCallActivity.this.finishConnectedCall();
            callState = CallState.CALL_ENDED;
            if (ringtone != null) {
                ringtone.stop();
            }
//            GlobalMethods.updateUiToUpdateCallLogs();
//            callDecline();
            startHomeActivity();
            RecieveCallActivity.this.finish();
            showToast("Call has been canceled by caller");
            isCallStartedFlag = false;

        }
    };


    //register local broadcast
    private void registerLocalBroadCast() {
//        LocalBroadcastManager.getInstance(this).registerReceiver(mIncomingCall,
//                new IntentFilter(Constants.Broadcasts.BROADCAST_NATIVE_CALL_STARTED));
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMissedCall,
//                new IntentFilter(Constants.Broadcasts.BROADCAST_MISSED_CALL));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallerCancelCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALLER_CANCEL_AUDIO_CAll));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastFromService,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_SERVICE));
    }

    //VideoCapturerAndroid capturer = VideoCapturerAndroid.create("hello");
    private void getValuesFromIntent() {
        isCallStartedflagFromService = getIntent().getBooleanExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
        mGroupname = getIntent().getStringExtra(Constants.IntentExtra.INTENT_GROUP_NAME);
        mParticipantJson = getIntent().getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        userstring = getIntent().getStringExtra(Constants.IntentExtra.USERSTRING);
        Type listType = new TypeToken<UserModel>() {
        }.getType();
        user = StringUtils.getGson().fromJson(userstring, listType);
        mRoomId = getIntent().getLongExtra("room", -1);
        mCallerId = getIntent().getStringExtra(Constants.IntentExtra.CALLER_USER_ID);
        if (isCallStartedflagFromService) {
            if (GlobalMethods.isMyServiceRunning(GroupAudioRecieverServiceFirebase.class)) {
                mCallStatusFromService = (EventFromService) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_STATE);
            } else {
                startHomeActivity();
            }

//            mCallStatusFromService = (EventFromService) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_STATE);
        }
//        displayCallerInfo();

    }

    private BroadcastReceiver mBroadCastFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventFromService callState = (EventFromService) intent.getSerializableExtra(Constants.IntentExtra.EVENT_FROM_SERVICE);
            if (callState == null) {
                return;
            }
            if (callState.equals(EventFromService.CALL_RINGING)) {

            } else if (callState.equals(EventFromService.CALL_CONNNECTED)) {
                mCallStatusLayout.setVisibility(View.VISIBLE);
                mTvStatus.setText("  Connecting");
                mLayoutCallAcceptReject.setVisibility(View.GONE);
                mLayoutCallWidget.setVisibility(View.VISIBLE);
                mConnectedCallEnd.setVisibility(View.VISIBLE);
            } else if (callState.equals(EventFromService.CALL_STARTED)) {
                Long elapsedTime = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
                if (elapsedTime != -1)
                    mCallStatusLayout.setVisibility(View.GONE);
                mLayoutCallAcceptReject.setVisibility(View.GONE);
                mLayoutCallWidget.setVisibility(View.VISIBLE);
                mConnectedCallEnd.setVisibility(View.VISIBLE);
                startChronometerTime(elapsedTime);
            } else if (callState.equals(EventFromService.CALL_RECONNECTED)) {
                intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
                intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, EventsFromActivity.GET_LATEST_TIME);
                LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
                mLayoutCallAcceptReject.setVisibility(View.GONE);
                mLayoutCallWidget.setVisibility(View.VISIBLE);
                mConnectedCallEnd.setVisibility(View.VISIBLE);
//                GlobalMethods.vibrateMobile();
            } else if (callState.equals(EventFromService.CALL_RECONTECTED_FAILED)) {
                mCallStatusLayout.setVisibility(View.VISIBLE);
                mTvStatus.setText("call-Disconnected");
                mChronometer.setVisibility(View.GONE);
            } else if (callState.equals(EventFromService.CALL_END)) {

            } else if (callState.equals(EventFromService.CALL_RECONNECTING)) {
                mChronometer.setVisibility(View.GONE);
                mCallStatusLayout.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText("   Reconnecting");
            } else if (callState.equals(EventFromService.PARTICIPANT_MUTE)) {
                mChronometer.setVisibility(View.GONE);
                mCallStatusLayout.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText("  Caller on Mute");
            } else if (callState.equals(EventFromService.PARTICIPANT_UNMUTE)) {
                mTvStatus.setVisibility(View.GONE);
                mCallStatusLayout.setVisibility(View.GONE);
                mChronometer.setVisibility(View.VISIBLE);
            } else if (callState.equals(EventFromService.HOLD_CALL)) {
                //  isHold = false;
//                //   Drawable replacer = getResources().getDrawable(R.drawable.ic_unhold);
////                //  mBtn_hold.setImageDrawable(replacer);
                mChronometer.setVisibility(View.GONE);
                mCallStatusLayout.setVisibility(View.VISIBLE);
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText(" Caller on Hold");
            } else if (callState.equals(EventFromService.NATIVE_CALL_HOLD)) {
                isHold = true;
//                Drawable replacer = getResources().getDrawable(R.drawable.user);
////                mBtn_hold.setImageDrawable(replacer);
            } else if (callState.equals(EventFromService.UNHOLD_CALL)) {
                isHold = false;
//                //   Drawable replacer = getResources().getDrawable(R.drawable.ic_hold);
////                //   mBtn_hold.setImageDrawable(replacer);
                mCallStatusLayout.setVisibility(View.GONE);
                mTvStatus.setVisibility(View.GONE);
                mChronometer.setVisibility(View.VISIBLE);
            } else if (callState.equals(EventFromService.FINISH_ACTIVITY)) {
//                GlobalMethods.vibrateMobile();
                startHomeActivity();
                RecieveCallActivity.this.finish();
            } else if (callState.equals(EventFromService.SEND_LATEST_TIME)) {
                Long elapsedTime = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
                Enum speakerState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.LOUDSPEAKER_STATE);
                Enum micState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.MICROPHONE_STATE);
                Enum holdState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.HOLD_STATE);
                setCallWidget(speakerState, micState, holdState);
                mTvStatus.setVisibility(View.GONE);
                startChronometerTime(elapsedTime);
                boolean isCallerOnHold = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, false);
                checkIfCallerOnHold(isCallerOnHold);
                boolean isRecieverOnMute = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, false);
                checkIfCallerOnMute(isRecieverOnMute);
            } else if (callState.equals(EventFromService.FINISH_ACTIVITY_WITHOUT_INITIATE)) {
                startHomeActivity();
                RecieveCallActivity.this.finish();
            } else if (callState.equals(EventFromService.CALL_ACCEPT)) {
                mLayoutCallAcceptReject.setVisibility(View.GONE);
                mLayoutCallWidget.setVisibility(View.VISIBLE);
                mConnectedCallEnd.setVisibility(View.VISIBLE);
                mCallStatusLayout.setVisibility(View.VISIBLE);
                mTvStatus.setText("  Connecting");
            } else if (callState.equals(EventFromService.UPDATE_TO_CONNECTING_STATE)) {
                updateConnectingStatus();
            } else if (callState.equals(EventFromService.UPDATE_CALL_STATS)) {
                final Long packets = intent.getLongExtra(Constants.IntentExtra.PACKETS, 0);
                final double bandwidth = intent.getDoubleExtra(Constants.IntentExtra.BANDWIDTH, 0);
                final double totalBandwidth = intent.getDoubleExtra(Constants.IntentExtra.TOTAL_BANDWIDTH, 0);
                final double bitrate = intent.getDoubleExtra(Constants.IntentExtra.BITRATE, 0);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        Log.i("Call Stats: ", "Total Bandwidth: " + totalBandwidth + " Current Bandwidth: " + bandwidth + " Bitrate: " + bitrate + " Packet Sent: " + packets);
//                        ((TextView) findViewById(R.id.tvTotalBandwidth)).setText("Data Used: " + Utils.getTwoDecimalplaces(totalBandwidth) + " kb/s");
//                        ((TextView) findViewById(R.id.tvBandwidth)).setText("Bandwidth: " + Utils.getTwoDecimalplaces(bandwidth) + " kb/s");
//                        ((TextView) findViewById(R.id.tvBitRate)).setText("Bit Rate: " + Utils.getTwoDecimalplaces(bitrate) + " b/s");
                        ((ImageView) findViewById(R.id.ivStrength)).setVisibility(View.VISIBLE);
                        if (bandwidth > 1 && bandwidth < 2.5) {
//                            ((ImageView) findViewById(R.id.ivStrength)).setImageDrawable(getDrawable(R.drawable.ic_signal_orange));
//                            ((TextView) findViewById(R.id.tvDuration)).setText("Signals: Normal");
                        } else if (bandwidth < 1) {
//                            ((TextView) findViewById(R.id.tvDuration)).setText("Signals: Low");
//                            ((ImageView) findViewById(R.id.ivStrength)).setImageDrawable(getDrawable(R.drawable.ic_signal_red));
                        } else {
//                            ((TextView) findViewById(R.id.tvDuration)).setText("Signals: High");
//                            ((ImageView) findViewById(R.id.ivStrength)).setImageDrawable(getDrawable(R.drawable.ic_signal_green));
                        }
//                        ((TextView) findViewById(R.id.tvPacket)).setText(packets + "");
                    }
                });
            }
        }
    };

    private void setCallWidget(Enum speakerState, Enum micState, Enum holdState) {
        if (speakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_PRESSED)) {
            isSpeaker = true;
////            Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker_pressed);
//            mBtn_Speaker.setImageDrawable(replacer);
        } else if (speakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
            isSpeaker = false;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker);
//            mBtn_Speaker.setImageDrawable(replacer);
        }
        if (micState.equals(CallWidgetState.MUTE_BUTTON_PRESSED)) {
            isMute = true;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
//            mBtn_microphone.setImageDrawable(replacer);
        } else if (micState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
            isMute = false;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
//            mBtn_microphone.setImageDrawable(replacer);
        }
        if (holdState.equals(CallWidgetState.HOLD_BUTTON_PRESSED)) {
            isHold = true;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_unhold);
////            mBtn_hold.setImageDrawable(replacer);
        } else if (micState.equals(CallWidgetState.HOLD_BUTTON_RELEASE)) {
            isHold = false;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_hold);
////            mBtn_hold.setImageDrawable(replacer);
        }
    }

    private void startChronometerTime(Long elapsedTime) {
        if (elapsedTime != -1) {
            mCallStatusLayout.setVisibility(View.GONE);
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

    public void checkCallState() {
//        Calls calls = ApplicationClass.getInstance().getHubDatabase().callsDao().checkCallInDb(mCallId);
//        if (calls != null) {
//            if (!calls.getType().equalsIgnoreCase(callingType)) {
//                RecieveCallActivity.this.finish();
//            }
//        }
    }

    private void createCallManager() {
        //make callWebrtc Manager
        callWebRTCManger = new CallWebRTCManger(RecieveCallActivity.this,
                mCallType,
                mGroupId, this);
        callWebRTCManger.startCallForInitiator();
    }

    private void showActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

//    public void rejectIncomingCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        callViewModel.rejectCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    callViewModel.updateCallStatusAndResponse(mCallId, callStateCode, responseType, 0L, 0L);
//                    callState = CallState.CALL_ENDED;
//                    GlobalMethods.updateUiToUpdateCallLogs();
//                    startHomeActivity();
//                    RecieveCallActivity.this.finish();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
////                showToast("Server Error");
//                //todo make discussion on this one...
//                GlobalMethods.updateUiToUpdateCallLogs();
//                startHomeActivity();
//                RecieveCallActivity.this.finish();
//            }
//        });
//    }

    private void finishConnectedCall() {
        final Long currentTime = Calendar.getInstance().getTimeInMillis();
        final long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mChronometer.stop();
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
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
//                    callState = CallState.CALL_ENDED;
        closePeerConnection();
        startHomeActivity();
        RecieveCallActivity.this.finish();
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
//                GlobalMethods.updateUiToUpdateCallLogs();
//                startHomeActivity();
//                RecieveCallActivity.this.finish();
//            }
//        });
    }

//    public void acceptIncommingCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        callViewModel.acceptCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    if (responseType == 803L) {
//                        callViewModel.updateCallStatusAndResponse(mCallId, callStateCode, responseType);
//                        updateConnectingStatus();
//                        createCallManager();
//                    } else {
//                        showToast("Call ended");
//                        GlobalMethods.updateUiToUpdateCallLogs();
//                        startHomeActivity();
//                        RecieveCallActivity.this.finish();
//                    }
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
//                startHomeActivity();
//                RecieveCallActivity.this.finish();
//            }
//        });
//    }

    public void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(RecieveCallActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unregisterSensor() {
        try {
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

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
//        if (ringtone != null) {
//            ringtone.stop();
//        }
//        if (callState != null) {
//            if (callState.equals(CallState.CALL_CONNECTED)) {
//                callViewModel.updateCallStatusAndResponse(mCallId, 704L, 801L);
//                closePeerConnection();
//                finishConnectedCall();
//            } else if (callState.equals(CallState.CALLING)) {
//                callViewModel.updateCallStatusAndResponse(mCallId, 703L, 803L, 0L, 0L);
//                rejectIncomingCall();
//            }
//        }
        //  removeTimer();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIncomingCall);
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCallerCancelCall);
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMissedCall);
        unregisterSensor();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastFromService);
        super.onDestroy();
    }

    //interface implementaion

    @Override
    public void startTimer() {
        isCallStartedFlag = true;
        removeTimer();
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
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mTvStatus.setVisibility(View.GONE);
        mChronometer.setVisibility(View.VISIBLE);
        mChronometer.start();
        callStartTime = Calendar.getInstance().getTimeInMillis();
        callState = CallState.CALL_CONNECTED;
    }

    private void closePeerConnection() {
        if (callWebRTCManger != null) {
            callWebRTCManger.endCall();
            callWebRTCManger = null;
//            GlobalMethods.updateUiToUpdateCallLogs();
        }
    }

    private void updateConnectingStatus() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mTvStatus.setVisibility(View.VISIBLE);
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void updateCallingToConnectingState() {
        //todo update to conntecting state...
        mTvStatus.setText("Connecting . . . ");
        removeTimer();

    }

    @Override
    public void callerEndedAudioCall() {
        //callViewModel.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
        // InitiateCallScreenActivity.this.finish();
    }

    @Override
    public void holdAudioCall(UserModel user, boolean isOnNativeCall) {
        callWebRTCManger.holdIngoingAudioCall(false);
    }

    @Override
    public void unholdAudioCall(UserModel user) {
        callWebRTCManger.unHoldIngoingAudioCall(false);
    }

    @Override
    public void reConnectingCall() {
        mChronometer.setVisibility(View.GONE);
        mTvStatus.setVisibility(View.VISIBLE);
        mTvStatus.setText("Reconnecting . . .");
    }

    @Override
    public void finishCall() {
        Log.i("test", "event_failed");
        mTvStatus.setText("call-Disconnected");
        RecieveCallActivity.this.finishConnectedCall();
    }

    @Override
    public void reconnectionEstablished() {
        mTvStatus.setVisibility(View.GONE);
        mChronometer.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateStats(double bandwidth, double bitrate, double totalBandwidth, long packets) {

    }

    @Override
    public void callRinging() {

    }

    @Override
    public void callRinging(UserModel user) {

    }

    @Override
    public void updateCallingToConnectingState(UserModel user) {

    }

    @Override
    public void reConnectingCall(UserModel user) {

    }

    @Override
    public void reconnectionEstablished(UserModel user) {

    }

    @Override
    public void startTimer(UserModel user) {

    }

    @Override
    public void userEndedCall(UserModel user) {

    }

    @Override
    public void finishCall(UserModel user) {

    }

    @Override
    public void userDisconnected(UserModel user) {

    }

    @Override
    public void userDisconnected() {

    }

    @Override
    public void muteCall(UserModel user) {

    }

    @Override
    public void unMuteCall(UserModel user) {

    }

    @Override
    public void disableVideoCall(UserModel user) {

    }

    @Override
    public void enableVideoCall(UserModel user) {

    }

    @Override
    public void callEnded() {
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mChronometer.stop();
//        callViewModel.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
        callState = CallState.CALL_ENDED;
//        GlobalMethods.updateUiToUpdateCallLogs();
        startHomeActivity();
        RecieveCallActivity.this.finish();
    }

    @Override
    public void callerCancelledCall() {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (sensorEvent.values[0] >= -SENSOR_SENSITIVITY && sensorEvent.values[0] <= SENSOR_SENSITIVITY) {
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
//        } else {
//            black_layout.setVisibility(View.GONE);
//        }
        if (sensorEvent.values[0] >= -SENSOR_SENSITIVITY && sensorEvent.values[0] <= SENSOR_SENSITIVITY) {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void askForDangerousPermissions() {
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        DynamicPermission dynamicPermission = new DynamicPermission(this, permissions);
        boolean isPermissionGranted = dynamicPermission.checkAndRequestPermissions();
        if (isPermissionGranted) {
            // initAudioCall(mGroupId);
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
//        if (requestCode == 1) {
//            for (int i = 0; i < permissions.length; i++) {
//                String permission = permissions[i];
//                int grantResult = grantResults[i];
//                if (grantResult != PackageManager.PERMISSION_GRANTED) {
//                    GlobalMethods.showToast("This app needs storage permission.");
//                  //  finish();
//
//                }else {
//                }
//            }
//        }
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
                //  initAudioCall(mGroupId);
            }
        }
    }

    public void startChatActivity(long groupId) {
//        Intent intent = new Intent(RecieveCallActivity.this, ChatMessageActivity.class);
//        intent.putExtra("groupId", groupId);
//        startActivity(intent);
//        finish();
    }

    private void checkIfCallerOnHold(boolean isHold) {
        if (isHold) {
            mChronometer.setVisibility(View.GONE);
            mCallStatusLayout.setVisibility(View.VISIBLE);
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText(" Caller on Hold");
        }
    }

    private void checkIfCallerOnMute(boolean isHold) {
        if (isHold) {
            mChronometer.setVisibility(View.GONE);
            mCallStatusLayout.setVisibility(View.VISIBLE);
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText(" Caller on Mute");
        }
    }

}
