package com.appsinventiv.noorenikah.callWebRtc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
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
import com.appsinventiv.noorenikah.callWebRtc.foregroundService.VideoRecieverCallService;
import com.appsinventiv.noorenikah.callWebRtc.foregroundService.VideoRendererEvent;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.lang.reflect.Type;
import java.util.ArrayList;

@SuppressLint("SetTextI18n")
public class ReceiverVideoCallActivity extends AppCompatActivity implements View.OnClickListener {
    SurfaceViewRenderer fullScreenVideoView;
    EglBase rootEgl;
    private VideoProxyRenderer localVideoProxy = new VideoProxyRenderer();
    boolean isCallStartedflagFromService = false;
    Long mGroupId, mUserId, mCallId, mRoomId;
    CallManager.CallType mCallType;
    private String mParticipantJson, mCallerName;
    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    Chronometer mChrometer;
    Long timeElapsed = -1L;
    boolean isMuteButtonPressed = false;
    boolean isVideoEnablePressed = false;
    Enum currentCallState;
    /*
  include layout  params
   */
    private TextView userName, tvStatus;
    private ImageButton endIncomingCallBtn, endConnectedCallBtn, holdBtn, muteBtn,
            msgBtn, toggleVideoBtn,
            declineIncomingCall, acceptIncoming;
    private LinearLayout callWidgetLayout, acceptRejectLayout, layoutCallStatus;
    private boolean isMute, isHold, isVideoEnable;

    private boolean isPermissionFailed = false;
    private UserModel user;
    private String userstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stayAlive();
        setContentView(R.layout.activity_receiver_video_call);
        registerEventBus();
        registerLocalBroadCast();
        getValuesFromIntent();
        initViews();
        registerOnClick();
        addRenderOptToVideoView();
        if (GlobalMethods.isMyServiceRunning(VideoRecieverCallService.class)) {
            //service is running
            // ask for latest view...
//            userName.setText(mCallerName);
//            updateScreenState();
//            sendRequestToService(EventsFromActivity.LOCAL_RENDER_VIEW);
        } else {
            getCallerInfo();
            askForDangerousPermissions();
        }

    }

    // private helper functions...
    private void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    private void checkOverLayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(ReceiverVideoCallActivity.this)) {
                startServiceProcedure();
            } else {
                askForSystemOverlayPermission();
            }
        } else {
            askForDangerousPermissions();
//            startServiceProcedure();
        }
    }

    private void startServiceProcedure() {
        startVideoReciverService();
    }

    private void stayAlive() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void getCallerInfo() {
//        GroupMembers mGroupMembers = UstadApp.getInstance().getHubDatabase().groupMembersDao().
//                getMemberFromLocal(mGroupId, mUserId);
//        CallsGroup callsGroup = new CallsGroup(mGroupMembers);
//        displayCallerInfo(callsGroup);
    }

//    private void displayCallerInfo(CallsGroup callsGroup) {
//        mCallerName = callsGroup.getmUserName();
//        userName.setText(mCallerName);
//    }

    private void getValuesFromIntent() {
        if (GlobalMethods.isMyServiceRunning(VideoRecieverCallService.class)) {
            isCallStartedflagFromService = getIntent().getBooleanExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
            timeElapsed = getIntent().getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1L);
            currentCallState = (EventFromService) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_STATE);
        } else {
            isCallStartedflagFromService = getIntent().getBooleanExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
            mUserId = getIntent().getLongExtra(Constants.IntentExtra.CALLER_USER_ID, -1);
            mCallId = getIntent().getLongExtra(Constants.IntentExtra.CALL_ID, -1);
            mCallType = (CallManager.CallType) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_TYPE);
            mParticipantJson = getIntent().getStringExtra(Constants.IntentExtra.PARTICIPANTS);
            mRoomId = getIntent().getLongExtra(Constants.IntentExtra.INTENT_ROOM_ID, -1);
        }
        userstring = getIntent().getStringExtra(Constants.IntentExtra.USERSTRING);
        Type listType = new TypeToken<UserModel>() {
        }.getType();
        user = StringUtils.getGson().fromJson(userstring, listType);
//        mCallerName = user.getName();
        mGroupId = getIntent().getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
//        userName = findViewById(R.id.userName);

//        userName.setText(mCallerName);

    }

    private void initViews() {
        fullScreenVideoView = findViewById(R.id.fullScreen_video_call);
        fullScreenVideoView = findViewById(R.id.fullScreen_video_call);
        callWidgetLayout = findViewById(R.id.layout_call_widget);
        acceptRejectLayout = findViewById(R.id.layout_call_accept_reject);
        layoutCallStatus = findViewById(R.id.layout_call_status);
        layoutCallStatus.setVisibility(View.GONE);
        tvStatus = findViewById(R.id.tv_status);
        tvStatus.setVisibility(View.GONE);

        mChrometer = findViewById(R.id.chronometer);
        mChrometer.setVisibility(View.GONE);
        endIncomingCallBtn = findViewById(R.id.inGoingcallEnd);
        endIncomingCallBtn.setVisibility(View.GONE);
        acceptRejectLayout.setVisibility(View.VISIBLE);
        holdBtn = findViewById(R.id.ic_hold);
        muteBtn = findViewById(R.id.ic_muteBtn);
        msgBtn = findViewById(R.id.btn_msg);
        endConnectedCallBtn = findViewById(R.id.ic_callEnd);
        toggleVideoBtn = findViewById(R.id.stopVideo);
        userName = findViewById(R.id.userName);
        toggleVideoBtn.setVisibility(View.GONE);
        declineIncomingCall = findViewById(R.id.CallDeclineBtn);
        acceptIncoming = findViewById(R.id.CallAttendBtn);
    }

    private void registerOnClick() {
        holdBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);
        endConnectedCallBtn.setOnClickListener(this);
        toggleVideoBtn.setOnClickListener(this);
        declineIncomingCall.setOnClickListener(this);
        acceptIncoming.setOnClickListener(this);
        msgBtn.setOnClickListener(this);
    }

    private void registerLocalBroadCast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastFromService,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_SERVICE));
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoRendererEvent event) {
        localVideoProxy = event.getMessage();
        localVideoProxy.setTarget(fullScreenVideoView);
    }

    private void addRenderOptToVideoView() {
        rootEgl = EglBase.create();
        // setUp local Video Stream...
        fullScreenVideoView.init(rootEgl.getEglBaseContext(), null);
        fullScreenVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        fullScreenVideoView.setEnableHardwareScaler(true);
        fullScreenVideoView.setMirror(true);
    }

    public void sendRequestToService(Enum requestEvent) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, requestEvent);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void startVideoReciverService() {
        Intent startServiceIntent = new Intent(ReceiverVideoCallActivity.this, VideoRecieverCallService.class);
        startServiceIntent.setAction(Constants.ForegroundService.ACTION_MAIN);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        startServiceIntent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        startServiceIntent.putExtra(Constants.IntentExtra.CALL_TYPE, CallManager.CallType.INDIVIDUAL_AUDIO);
        startServiceIntent.putExtra(Constants.IntentExtra.CALLER_USER_ID, mUserId);
        startServiceIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantJson);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_ROOM_ID, mRoomId);
        startServiceIntent.putExtra(Constants.IntentExtra.USERSTRING, userstring);

        startServiceIntent.putExtra(Constants.IntentExtra.USER_NAME, "sdfs");
        ReceiverVideoCallActivity.this.startService(startServiceIntent);
    }

    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permission is not available. Display error text.
                    errorToast();
                    finish();
                } else {
                    askForDangerousPermissions();

//                    startVideoReciverService();
                }
            }
        } else if (requestCode == 1) {
            isPermissionFailed = true;
            askForDangerousPermissions();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                this);
        builder.setTitle("Requested Permission Required!");
        builder.setMessage("This app needs dynamic permissions for calling feature.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GlobalMethods.showToast("You don't have sufficient rights.");
                finish();
            }
        });
        if (requestCode == 11) {
            boolean isgranted = true;
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
                GlobalMethods.showToast("You don't have sufficient rights.");
            } else {
                checkOverLayPermission();
            }
        }
    }

    private void errorToast() {
        Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
    }

    public void startHomeActivity() {


//        sendRequestToService(EventsFromActivity.END_CALL_PRESSED);

        Intent intent = new Intent(ReceiverVideoCallActivity.this, MainActivity.class);
        intent.putExtra("call", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ReceiverVideoCallActivity.this.finish();
    }

    private void releaseVideoCall() {
        localVideoProxy.setTarget(null);
        fullScreenVideoView.release();
        GlobalMethods.vibrateMobile();
        startHomeActivity();
    }

    private void unregisterLocalBroadCast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastFromService);
    }

    private void showMessage(Intent intent) {
        String msg = intent.getStringExtra(Constants.IntentExtra.MESSAGE);
        Toast.makeText(ReceiverVideoCallActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startChronometerTime(Long elapsedTime) {
        if (elapsedTime != -1) {
            layoutCallStatus.setVisibility(View.GONE);
            mChrometer.setVisibility(View.VISIBLE);
            mChrometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @SuppressLint("SetTextI18n")
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
            mChrometer.setBase(SystemClock.elapsedRealtime() - elapsedTime);
            mChrometer.start();
        }
    }

    private void updateScreenState() {
        if (currentCallState == null)
            return;
        Long timeElapse = getIntent().getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1L);
        if (timeElapse != -1L) {
            if (currentCallState.equals(EventFromService.CALL_STARTED)) {
                setCallStartedStatus(timeElapse);
            } else if (currentCallState.equals(EventFromService.CALL_CONNNECTED)) {
                setCallConnectedStatus();
            } else if (currentCallState.equals(EventFromService.CALL_RECONNECTED)) {
                setReconnectedStatus();
            } else if (currentCallState.equals(EventFromService.CALL_RECONTECTED_FAILED)) {
                setReconnectedFailed();
            } else if (currentCallState.equals(EventFromService.CALL_RECONNECTING)) {
                setReconnectedStatus();
            }
        } else {
            if (currentCallState.equals(EventFromService.CALL_CONNNECTED)) {
                setCallConnectedStatus();
            }
        }
    }

    private void setCallConnectedStatus() {
        mChrometer.setVisibility(View.GONE);
        endIncomingCallBtn.setVisibility(View.GONE);
        acceptRejectLayout.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.VISIBLE);
        toggleVideoBtn.setVisibility(View.GONE);
        tvStatus.setText("   Connecting");
        tvStatus.setVisibility(View.VISIBLE);
        
    }

    private void setCallStartedStatus(Long elapsedTime) {
        endIncomingCallBtn.setVisibility(View.GONE);
        acceptRejectLayout.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.VISIBLE);
        if (elapsedTime != -1) {
            startChronometerTime(elapsedTime);
            GlobalMethods.vibrateMobile();
        }
    }

    private void setReconnectedStatus() {
        layoutCallStatus.setVisibility(View.GONE);
        toggleVideoBtn.setVisibility(View.VISIBLE);
        sendRequestToService(EventsFromActivity.GET_LATEST_TIME);
    }

    private void askForDangerousPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        DynamicPermission dynamicPermission = new DynamicPermission(this, permissions);
        boolean isPermissionGranted = dynamicPermission.checkAndRequestPermissions();
        if (isPermissionGranted) {
            checkOverLayPermission();
        } else {
            if (isPermissionFailed) {
                GlobalMethods.showToast("You don't have sufficient rights.");
                this.finish();
            }
        }
    }

    private void setReconnectedFailed() {
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Call-Disconnected ");
        
    }

    public void startChatActivity(long groupId) {
//        Intent intent = new Intent(ReceiverVideoCallActivity.this, ChatMessageActivity.class);
//        intent.putExtra("groupId", groupId);
//        startActivity(intent);
//        finish();
    }

    public void muteBtnPressed() {
//        if (!isHold) {
        if (isMute) {
            isMute = false;
            isMuteButtonPressed = false;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
            muteBtn.setImageDrawable(replacer);
            sendRequestToService(EventsFromActivity.UNMUTE_BUTTON_PRESSD);
        } else {
            isMute = true;
            isMuteButtonPressed = true;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
            muteBtn.setImageDrawable(replacer);
            sendRequestToService(EventsFromActivity.MUTE_BUTTON_PRESSED);
        }
//        } else {
//            Toast.makeText(this, "Call is onhold", Toast.LENGTH_SHORT).show();
//        }
    }

    public void videoDisableBtnPressed() {
//        if (!isHold) {
            if (isVideoEnable) {
                isVideoEnable = false;
                isVideoEnablePressed = false;
                Drawable replacer = getResources().getDrawable(R.drawable.ic_video_cam);
                toggleVideoBtn.setImageDrawable(replacer);
                sendRequestToService(EventsFromActivity.DISABLE_VIDEO);
            } else {
                isVideoEnable = true;
                isVideoEnablePressed = true;
                Drawable replacer = getResources().getDrawable(R.drawable.ic_video_cam);
                toggleVideoBtn.setImageDrawable(replacer);
                sendRequestToService(EventsFromActivity.ENABLE_VIDEO);
            }
//        } else {
//            Toast.makeText(this, "Call is onhold", Toast.LENGTH_SHORT).show();
//        }
    }

    public void holdBtnPressed() {
        isMute = false;
//        Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
//        muteBtn.setImageDrawable(replacer);
//        if (isHold) {
//            isHold = false;
//            replacer = getResources().getDrawable(R.drawable.ic_vid_hold);
//            holdBtn.setImageDrawable(replacer);
//            sendRequestToService(EventsFromActivity.UNHOLD_BUTTON_PRESSED);
//        } else {
//            isHold = true;
//            replacer = getResources().getDrawable(R.drawable.ic_vid_unhold);
//            holdBtn.setImageDrawable(replacer);
//            sendRequestToService(EventsFromActivity.HOLD_BUTTON_PRESSED);
//        }
    }

    private void setUpdatedWidgetState(Intent intent) {
        Long elapsedTime = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
        Enum micState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.MICROPHONE_STATE);
        Enum holdState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.HOLD_STATE);
        Enum videoState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.VIDEO_STATE);
        setCallWidget(videoState, micState, holdState);
        endIncomingCallBtn.setVisibility(View.GONE);
        acceptRejectLayout.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.VISIBLE);
        layoutCallStatus.setVisibility(View.GONE);
        startChronometerTime(elapsedTime);
        boolean isRecieverOnHold = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, false);
        checkIfReceiverOnHold(isRecieverOnHold);
        boolean isRecieverOnMute = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, false);
        checkIfReceiverOnMute(isRecieverOnMute);
        boolean isReciverOnDisableVideo = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, false);
        checkIfRecieverOnDisableVideo(isReciverOnDisableVideo);
        GlobalMethods.vibrateMobile();
    }

    private void checkIfReceiverOnHold(boolean isHold) {
        if (isHold) {
            mChrometer.setVisibility(View.GONE);
            layoutCallStatus.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(" Caller on hold");
            
        }
    }

    private void checkIfReceiverOnMute(boolean isHold) {
        if (isHold) {
            mChrometer.setVisibility(View.GONE);
            layoutCallStatus.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Caller on Mute");
            
        }
    }

    private void checkIfRecieverOnDisableVideo(boolean isVideoEnable) {
        if (isVideoEnable) {
            mChrometer.setVisibility(View.GONE);
            layoutCallStatus.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Caller on video disable-mode");
            
        }
    }

    private void setCallWidget(Enum videoState, Enum micState, Enum holdState) {
        if (videoState.equals(CallWidgetState.VIDEO_BUTTON_DISABLE)) {
            isVideoEnable = true;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_video_cam);
            toggleVideoBtn.setImageDrawable(replacer);
        } else if (videoState.equals(CallWidgetState.VIDEO_BUTTON_ENABLE)) {
            isVideoEnable = false;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_video_cam);
            toggleVideoBtn.setImageDrawable(replacer);
        }
        if (micState.equals(CallWidgetState.MUTE_BUTTON_PRESSED)) {
            isMute = true;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
            muteBtn.setImageDrawable(replacer);
        } else if (micState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
            isMute = false;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
            muteBtn.setImageDrawable(replacer);
        }
//        if (holdState.equals(CallWidgetState.HOLD_BUTTON_PRESSED)) {
//            isHold = true;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_vid_unhold);
//            holdBtn.setImageDrawable(replacer);
//        } else if (micState.equals(CallWidgetState.HOLD_BUTTON_RELEASE)) {
//            isHold = false;
//            Drawable replacer = getResources().getDrawable(R.drawable.ic_vid_hold);
//            holdBtn.setImageDrawable(replacer);
//        }
    }

    private void setReconnectingStatus() {
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("   Reconnecting");
        
    }

    private void setParticipantUnMute() {
        tvStatus.setVisibility(View.GONE);
        
        layoutCallStatus.setVisibility(View.GONE);
        mChrometer.setVisibility(View.VISIBLE);
    }

    private void setParticipantMute() {
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("  Caller on Mute");
        
    }

    private void setParticpantHoldCall() {
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(" Caller on hold");
        
    }

    private void setParticipantUnHold() {
        isHold = false;
        tvStatus.setVisibility(View.GONE);
        
        layoutCallStatus.setVisibility(View.GONE);
        mChrometer.setVisibility(View.VISIBLE);
    }

    private void setParticipantNativeHold() {
//        isHold = true;
//        Drawable replacer = getResources().getDrawable(R.drawable.ic_unhold);
//        holdBtn.setImageDrawable(replacer);
    }

    private void setParticipantVideoDisable() {
        isVideoEnable = true;
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(" Caller on video disable-mode");
        
    }

    private void setParticipantVideoUnable() {
        isVideoEnable = false;
        tvStatus.setVisibility(View.GONE);
        
        layoutCallStatus.setVisibility(View.GONE);
        mChrometer.setVisibility(View.VISIBLE);
    }

    /*
    BroadCast functions
     */
    private BroadcastReceiver mBroadCastFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            EventFromService callState = (EventFromService) intent.getSerializableExtra(Constants.IntentExtra.EVENT_FROM_SERVICE);
            switch (callState) {
                case FINISH_ACTIVITY:
                    releaseVideoCall();
                    break;
                case MESSAGE:
                    showMessage(intent);
                    break;
                case CALL_RINGING:

                    break;
                case CALL_CONNNECTED:
                    setCallConnectedStatus();
                    break;
                case CALL_STARTED:
                    Long time = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1L);
                    setCallStartedStatus(time);
                    break;
                case CALL_RECONNECTED:
                    setReconnectedStatus();
                    break;
                case SEND_LATEST_TIME:
                    setUpdatedWidgetState(intent);
                    break;
                case CALL_RECONNECTING:
                    setReconnectingStatus();
                    break;
                case PARTICIPANT_MUTE:
                    setParticipantMute();
                    break;
                case PARTICIPANT_UNMUTE:
                    setParticipantUnMute();
                    break;
                case HOLD_CALL:
                    setParticpantHoldCall();
                    break;
                case UNHOLD_CALL:
                    setParticipantUnHold();
                    break;
                case NATIVE_CALL_HOLD:
                    setParticipantNativeHold();
                    break;
                case PARTICIPANT_VIDEO_DISABLE:
                    setParticipantVideoDisable();
                    break;
                case PARTICIPANT_VIDEO_ENABLE:
                    setParticipantVideoUnable();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterLocalBroadCast();
        super.onDestroy();
    }

    public void acceptIncomingCall() {
        sendRequestToService(EventsFromActivity.ACCEPT_CALL_PRESSED);
    }

    public void rejectIncomingCall() {
        GlobalMethods.vibrateMobile();
        sendRequestToService(EventsFromActivity.REJECT_CALL_PRESSED);
        localVideoProxy.setTarget(null);
        fullScreenVideoView.release();
        startHomeActivity();
    }

    public void onCallHangUp() {
        GlobalMethods.vibrateMobile();
        sendRequestToService(EventsFromActivity.END_CALL_PRESSED);
        localVideoProxy.setTarget(null);
        fullScreenVideoView.release();
        startHomeActivity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_muteBtn:
                muteBtnPressed();
                break;
            case R.id.ic_hold:
                holdBtnPressed();
                break;
            case R.id.btn_msg:
                startChatActivity(mGroupId);
                break;
            case R.id.ic_callEnd:
                onCallHangUp();
                break;
            case R.id.CallDeclineBtn:
                rejectIncomingCall();
                break;
            case R.id.CallAttendBtn:
                acceptIncomingCall();
                break;
            case R.id.stopVideo:
                videoDisableBtnPressed();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        userName.setText(mCallerName);
        updateScreenState();
        sendRequestToService(EventsFromActivity.LOCAL_RENDER_VIEW);
    }

    @Override
    protected void onPause() {
        sendRequestToService(EventsFromActivity.SHOW_REMOTE_VIEW);
        super.onPause();
    }
}

