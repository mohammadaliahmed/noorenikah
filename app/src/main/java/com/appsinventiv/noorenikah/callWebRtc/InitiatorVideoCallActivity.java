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
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.DynamicPermission;
import com.appsinventiv.noorenikah.Utils.GlobalMethods;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;
import com.appsinventiv.noorenikah.callWebRtc.foregroundService.VideoInitiatorCallService;
import com.appsinventiv.noorenikah.callWebRtc.foregroundService.VideoRendererEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


@SuppressLint("SetTextI18n")
public class InitiatorVideoCallActivity extends AppCompatActivity implements View.OnClickListener {
    SurfaceViewRenderer fullScreenVideoView;
    EglBase rootEgl;
    VideoProxyRenderer localVideoProxy = new VideoProxyRenderer();
    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    private String mParticipantsJson, reciverName;
    private Long mGroupId;
    CallManager.CallType mCallType;
    Enum currentCallState;
    Long timeElapsed = -1L;
    boolean isMuteButtonPressed = false;
    boolean isVideoEnablePressed = false;
    private boolean isPermissionFailed = false;


    /*
    include layout  params
     */

    private TextView userName, tvStatus;
    Chronometer mChrometer;
    ImageView holdBtn, muteBtn;
    private ImageButton endIngoingCallBtn, endConnectedCallBtn, msgBtn;

    ImageView toggleVideoBtn;
    private LinearLayout callWidgetLayout, acceptRejectLayout, layoutCallStatus;
    private boolean isMute, isHold, isVideoEnable;
    private String mfirebaseId;
    private Long mRoomId;
    private UserModel userModel;
    private DatabaseReference mDatabase;
    private long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stayAlive();
        mDatabase=Constants.M_DATABASE;
        setContentView(R.layout.activity_receiver_video_call);
        registerEventBus();
        registerLocalBroadCast();
        // here check if started  from service...
        getValuesFromIntent();
        initViews();
        registerOnClick();
        addRenderOptToVideoView();
        if (GlobalMethods.isMyServiceRunning(VideoInitiatorCallService.class)) {
            //service is running
            // ask for latest view...
//            userName.setText(reciverName);
//            updateScreenState();
//            sendRequestToService(EventsFromActivity.LOCAL_RENDER_VIEW);
        } else {
            askForDangerousPermissions();
        }
    }

    // private helper functions...


    private void checkOverLayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(InitiatorVideoCallActivity.this)) {
                startServiceProcedure();
            } else {
                askForSystemOverlayPermission();
            }
        } else {
            startServiceProcedure();
        }
    }

    private void startServiceProcedure() {
        initVideoCall(mGroupId);
    }

    private void registerEventBus() {
        EventBus.getDefault().register(this);
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

    private void askForDangerousPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
//        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
//        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);

        DynamicPermission dynamicPermission = new DynamicPermission(this, permissions);
        boolean isPermissionGranted = dynamicPermission.checkAndRequestPermissions();
        if (isPermissionGranted) {
            checkOverLayPermission();
        } else {
            if (isPermissionFailed) {
//                GlobalMethods.showToast("You don't have sufficient rights.");
                this.finish();
            }
        }
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

    private void getValuesFromIntent() {
        if (GlobalMethods.isMyServiceRunning(VideoInitiatorCallService.class)) {
            reciverName = getIntent().getStringExtra(Constants.IntentExtra.USER_NAME);
            currentCallState = (EventFromService) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_STATE);
//            boolean isCallStartedflagFromService = getIntent().getBooleanExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, false);
            timeElapsed = getIntent().getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1L);
        } else {
            mCallType = (CallManager.CallType) getIntent().getSerializableExtra(Constants.IntentExtra.CALL_TYPE);
            mParticipantsJson = getIntent().getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        }
        mGroupId = getIntent().getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
        mfirebaseId = getIntent().getStringExtra(Constants.IntentExtra.USER_FIREBASEID);
        Type listType = new TypeToken<ArrayList<UserModel>>() {
        }.getType();
        ArrayList<UserModel> ustadArrayList = StringUtils.getGson().fromJson(mParticipantsJson, listType);
        userModel = ustadArrayList.get(0);
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

    public void sendRequestToService(Enum requestEvent) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_UI, requestEvent);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void initViews() {
        fullScreenVideoView = findViewById(R.id.fullScreen_video_call);
        callWidgetLayout = findViewById(R.id.layout_call_widget);
        acceptRejectLayout = findViewById(R.id.layout_call_accept_reject);
        layoutCallStatus = findViewById(R.id.layout_call_status);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus = findViewById(R.id.tv_status);
        tvStatus.setVisibility(View.VISIBLE);

        mChrometer = findViewById(R.id.chronometer);
        mChrometer.setVisibility(View.GONE);
        acceptRejectLayout.setVisibility(View.GONE);
        endIngoingCallBtn = findViewById(R.id.inGoingcallEnd);
        endIngoingCallBtn.setVisibility(View.VISIBLE);
        holdBtn = findViewById(R.id.ic_hold);
        muteBtn = findViewById(R.id.ic_muteBtn);
        msgBtn = findViewById(R.id.btn_msg);
        endConnectedCallBtn = findViewById(R.id.ic_callEnd);
        toggleVideoBtn = findViewById(R.id.stopVideo);
        toggleVideoBtn.setVisibility(View.GONE);
        userName = findViewById(R.id.userName);
    }

    private void registerOnClick() {
        endIngoingCallBtn.setOnClickListener(this);
        holdBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);
        endConnectedCallBtn.setOnClickListener(this);
        toggleVideoBtn.setOnClickListener(this);
        msgBtn.setOnClickListener(this);
    }

    private void addRenderOptToVideoView() {
        rootEgl = EglBase.create();
        // setUp local Video Stream...
        fullScreenVideoView.init(rootEgl.getEglBaseContext(), null);
        fullScreenVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        fullScreenVideoView.setEnableHardwareScaler(true);
        fullScreenVideoView.setMirror(true);
    }

    public void initVideoCall(final Long mGroupId) {

        Random rand = new Random();
        int n = rand.nextInt(998) + 1;
        mRoomId = Long.valueOf(n);
        startInitiateCallerService(mRoomId, mfirebaseId, reciverName);
        CallModel model=new CallModel(""+mRoomId,
                userModel.getPhone(),
                userModel.getName(),
                userModel.getLivePicPath(),
                Constants.CALL_OUTGOING,
                true,
                0,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        mDatabase.child("Calls").child(SharedPrefs.getUser().getPhone()).child(userModel.getPhone())
                .child(""+mRoomId).setValue(model);

        CallModel model2=new CallModel(""+mRoomId,
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath(),
                Constants.CALL_INCOMING,
                true,
                0,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        mDatabase.child("Calls").child(userModel.getPhone()).child(SharedPrefs.getUser().getPhone())
                .child(""+mRoomId).setValue(model2);

    }


    private void startInitiateCallerService(Long roomId, String attendentId, String userName) {
        Intent startServiceIntent = new Intent(InitiatorVideoCallActivity.this, VideoInitiatorCallService.class);
        startServiceIntent.setAction(Constants.ForegroundService.ACTION_MAIN);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        startServiceIntent.putExtra(Constants.IntentExtra.CALL_TYPE, CallManager.CallType.INDIVIDUAL_VIDEO);
        startServiceIntent.putExtra(Constants.IntentExtra.ATTENDENT_ID, attendentId);
        startServiceIntent.putExtra(Constants.IntentExtra.INTENT_ROOM_ID, roomId);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_FIREBASEID, mfirebaseId);
        startServiceIntent.putExtra(Constants.IntentExtra.PARTICIPANTS, mParticipantsJson);
        startServiceIntent.putExtra(Constants.IntentExtra.USER_NAME, userName);
        startService(startServiceIntent);
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
                    startServiceProcedure();
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

        Intent intent = new Intent(InitiatorVideoCallActivity.this, MainActivity.class);
        intent.putExtra("call", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        InitiatorVideoCallActivity.this.finish();
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
            if (currentCallState.equals(EventFromService.CALL_RINGING)) {
                setRingingStatus();
            }
        }
    }
    private void endCallInDb() {
        duration=SystemClock.elapsedRealtime() - mChrometer.getBase();
        HashMap<String,Object> map=new HashMap<>();
        map.put("endTime",System.currentTimeMillis());
        map.put("seconds",duration);

        mDatabase.child("Calls").child(SharedPrefs.getUser().getPhone()).child(userModel.getPhone())
                .child(""+mRoomId).updateChildren(map);

        mDatabase.child("Calls").child(userModel.getPhone()).child(SharedPrefs.getUser().getPhone())
                .child(""+mRoomId).updateChildren(map);


    }


    private void showMessage(Intent intent) {
        String msg = intent.getStringExtra(Constants.IntentExtra.MESSAGE);
        Toast.makeText(InitiatorVideoCallActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startChronometerTime(Long elapsedTime) {
        if (elapsedTime != -1) {
            layoutCallStatus.setVisibility(View.GONE);
            mChrometer.setVisibility(View.VISIBLE);
            mChrometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
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

    private void setRingingStatus() {
        mChrometer.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.GONE);
        toggleVideoBtn.setVisibility(View.GONE);
        tvStatus.setText("   Ringing");
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);

    }

    private void setCallConnectedStatus() {
        mChrometer.setVisibility(View.GONE);
        acceptRejectLayout.setVisibility(View.GONE);
        endIngoingCallBtn.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.VISIBLE);
        toggleVideoBtn.setVisibility(View.GONE);
        tvStatus.setText("   Connecting");
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setCallStartedStatus(Long time) {
        endIngoingCallBtn.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.VISIBLE);
        if (time != -1) {
            startChronometerTime(time);
            GlobalMethods.vibrateMobile();
        }
    }

    private void setReconnectedStatus() {
        layoutCallStatus.setVisibility(View.GONE);
        toggleVideoBtn.setVisibility(View.VISIBLE);
        sendRequestToService(EventsFromActivity.GET_LATEST_TIME);
    }

    private void setUpdatedWidgetState(Intent intent) {

        Long elapsedTime = intent.getLongExtra(Constants.IntentExtra.TIME_ELAPSED, -1);
        Enum micState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.MICROPHONE_STATE);
        Enum holdState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.HOLD_STATE);
        Enum videoState = (CallWidgetState) intent.getSerializableExtra(Constants.IntentExtra.VIDEO_STATE);
        setCallWidget(videoState, micState, holdState);
        endIngoingCallBtn.setVisibility(View.GONE);
        callWidgetLayout.setVisibility(View.VISIBLE);
        layoutCallStatus.setVisibility(View.GONE);
        startChronometerTime(elapsedTime);
        boolean isRecieverOnHold = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, false);
        checkIfReciverOnHold(isRecieverOnHold);
        boolean isRecieverOnMute = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, false);
        boolean isReciverOnDisableVideo = intent.getBooleanExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, false);
        checkIfRecieverOnMute(isRecieverOnMute);
        checkIfRecieverOnDisableVideo(isReciverOnDisableVideo);
        GlobalMethods.vibrateMobile();
    }

    private void checkIfReciverOnHold(boolean isHold) {
        if (isHold) {
            mChrometer.setVisibility(View.GONE);
            layoutCallStatus.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(" Receiver on hold");
        }
    }

    private void checkIfRecieverOnMute(boolean isHold) {
        if (isHold) {
            mChrometer.setVisibility(View.GONE);
            layoutCallStatus.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(" Receiver on Mute");
        }
    }

    private void checkIfRecieverOnDisableVideo(boolean isVideoEnable) {
        if (isVideoEnable) {
            mChrometer.setVisibility(View.GONE);
            layoutCallStatus.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(" Receiver on video disable-mode");
        }
    }

    private void setCallWidget(Enum videoState, Enum micState, Enum holdState) {
        if (videoState.equals(CallWidgetState.VIDEO_BUTTON_DISABLE)) {
            isVideoEnable = true;
            Drawable replacer = getResources().getDrawable(R.drawable.ic_speaker);
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
        tvStatus.setText("  Receiver on Mute");
    }

    private void setParticipantVideoDisable() {
        isVideoEnable = true;
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(" Receiver on video disable-mode");
    }

    private void setParticpantHoldCall() {
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(" Receiver on hold");
    }

    private void setParticipantUnHold() {
        isHold = false;
        tvStatus.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.GONE);
        mChrometer.setVisibility(View.VISIBLE);
    }

    private void setParticipantVideoUnable() {
        isVideoEnable = false;
        tvStatus.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.GONE);
        mChrometer.setVisibility(View.VISIBLE);
    }

    private void setParticipantNativeHold() {
        //        isHold = true;
//        Drawable replacer = getResources().getDrawable(R.drawable.ic_unhold);
//        holdBtn.setImageDrawable(replacer);

    }


    private void setReconnectedFailed() {
        mChrometer.setVisibility(View.GONE);
        layoutCallStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Call-Disconnected ");
    }

    public void startChatActivity(long groupId) {
//        Intent intent = new Intent(InitiatorVideoCallActivity.this, ChatMessageActivity.class);
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
        if (!isHold) {
            if (isVideoEnable) {
                isVideoEnable = false;
                isVideoEnablePressed = false;
                Drawable replacer = getResources().getDrawable(R.drawable.ic_video_cam);
                toggleVideoBtn.setImageDrawable(replacer);
                sendRequestToService(EventsFromActivity.DISABLE_VIDEO);
            } else {
                isVideoEnable = true;
                isVideoEnablePressed = true;
                Drawable replacer = getResources().getDrawable(R.drawable.ic_video_cam_dis);
                toggleVideoBtn.setImageDrawable(replacer);
                sendRequestToService(EventsFromActivity.ENABLE_VIDEO);
            }
        } else {
            Toast.makeText(this, "Call is onhold", Toast.LENGTH_SHORT).show();
        }
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
                    setRingingStatus();
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

    public void onCallHangUp() {
        GlobalMethods.vibrateMobile();
        sendRequestToService(EventsFromActivity.END_CALL_PRESSED);
        localVideoProxy.setTarget(null);
        fullScreenVideoView.release();
        startHomeActivity();

    }

    public void disConnectIngoingCall() {
        GlobalMethods.vibrateMobile();
        sendRequestToService(EventsFromActivity.END_CALL_INGOING);
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
            case R.id.stopVideo:
                videoDisableBtnPressed();
                break;
            case R.id.btn_msg:
                startChatActivity(mGroupId);
                break;
            case R.id.ic_callEnd:
                onCallHangUp();
                endCallInDb();
                break;
            case R.id.inGoingcallEnd:
                disConnectIngoingCall();
                endCallInDb();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalMethods.isMyServiceRunning(VideoInitiatorCallService.class)) {
            //service is running
            // ask for latest view...
            userName.setText(reciverName);
            updateScreenState();
            sendRequestToService(EventsFromActivity.LOCAL_RENDER_VIEW);
        }
    }

    @Override
    protected void onPause() {
        sendRequestToService(EventsFromActivity.SHOW_REMOTE_VIEW);
        super.onPause();
    }
}
