package com.appsinventiv.noorenikah.callWebRtc.foregroundService;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.annotation.SuppressLint;
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
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.ApplicationClass;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.GlobalMethods;
import com.appsinventiv.noorenikah.Utils.StringUtils;
import com.appsinventiv.noorenikah.Utils.UserManager;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.CallClossingTask;
import com.appsinventiv.noorenikah.callWebRtc.CallListner.AudioCallBackInterface;
import com.appsinventiv.noorenikah.callWebRtc.CallWebRTCManger;
import com.appsinventiv.noorenikah.callWebRtc.InitiatorVideoCallActivity;
import com.appsinventiv.noorenikah.callWebRtc.VideoProxyRenderer;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallEndType;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class VideoInitiatorCallService extends Service implements AudioCallBackInterface {
    /*
    Variable declaration...
     */
    private WindowManager mWindowManager;
    private View mOverlayView;
    VideoCapturer videoCapturer;
    private VideoProxyRenderer localVideoProxy = new VideoProxyRenderer();
    private VideoProxyRenderer remoteVideoProxy = new VideoProxyRenderer();
    private final List<VideoRenderer.Callbacks> remoteRenderers =
            new ArrayList<VideoRenderer.Callbacks>();
    SurfaceViewRenderer fullScreenVideoView, bottomVideoView;
    EglBase rootEgl;
    CallWebRTCManger rtcManger;
    NotificationCompat.Builder notificationBuilder;
    boolean isCallStartedflag = true;
    boolean mHasDoubleClicked, isAlreadyRing,
            isDialingTimerFlag, isRingingTimerFlag,
            mReciverOnHold, mCallerOnHold,
            isReconnecting, mParticipantOnMute, ismParticipantOnVideoDisable,
            isIndividualNativeCall, endHoldCall, is_MuteButtonPressed,
            isSwappedFeeds, isEndCallButtonPressed;
    long lastPressTime, mGroupId, mRoomId, mAttendentId, mCallId;
    private String mParticipantsJson, mUserName;
    Chronometer mChronometer;
    Enum videoCallState;
    Thread mThread;
    Handler mHoldCallEnd;
    private String attendedfirebaseID;

    /**
     * bottom layout widget
     */
    LinearLayout widgetLayout;
    ImageButton ib_mute;
    ImageButton ib_endCall;
    ImageButton ib_video;
    boolean isMute = false;
    boolean isMuteButtonPressed;
    boolean isForeGround = true;

    /**
     * Timer task
     */
    Timer mTimerDialing = new Timer();
    Timer mTimerRinging = new Timer();

    /**
     * Save widget state.
     */
    Enum muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
    Enum holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
    Enum videoEnableState = CallWidgetState.VIDEO_BUTTON_ENABLE;

    /**
     * Tone Generator.
     */
    ToneGenerator mToneGenerator;
    ToneGenerator mReconnectingTone;


    /*
    unused method...
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // inflateView();
        registerBroadCastReceiver();
    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null) {
            mWindowManager.removeView(mOverlayView);
            mWindowManager = null;
        }
        unRegisterBroadCastReceiver();
        purgeAllTimers();
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //  inflateView();
        getActionFromIntent(intent);
        return START_STICKY;
    }

    private void showView() {
        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.activity_initiator_video_call, null);
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            //Specify the view position
            params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 100;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mOverlayView, params);
//          Display display = mWindowManager.getDefaultDisplay();
//          final Point size = new Point();
//          display.getSize(size);
            fullScreenVideoView = mOverlayView.findViewById(R.id.fullScreen_video_call);
            bottomVideoView = mOverlayView.findViewById(R.id.bottom_video_view);
            widgetLayout = mOverlayView.findViewById(R.id.layout_call_widget);

            try {

                mOverlayView.setOnTouchListener(new View.OnTouchListener() {
                    private WindowManager.LayoutParams paramsF = params;
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:

                                // Get current time in nano seconds.
                                long pressTime = System.currentTimeMillis();


                                // If double click...
                                if (pressTime - lastPressTime <= 300) {
                                    //  createNotification();
                                    // ServiceFloating.this.stopSelf();
                                    mHasDoubleClicked = true;
                                } else {     // If not double click....
                                    mHasDoubleClicked = false;
                                }
                                lastPressTime = pressTime;
                                initialX = paramsF.x;
                                initialY = paramsF.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                            case MotionEvent.ACTION_MOVE:
                                paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                                paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                                mWindowManager.updateViewLayout(mOverlayView, paramsF);
                                break;
                        }
                        return false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            ib_mute = mOverlayView.findViewById(R.id.ic_muteBtn);
            ib_mute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    muteBtnPressed();
                }
            });

            ib_endCall = mOverlayView.findViewById(R.id.ic_callEnd);
            ib_endCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    finishConnectedIndividualCall();
                }
            });

            ib_video = mOverlayView.findViewById(R.id.btn_video);
            ib_video.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent intent = new Intent(getApplicationContext(), InitiatorVideoCallActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getLatestState(intent);
                    getApplicationContext().startActivity(intent);

                }
            });
            createVideoCapturer();
        }
    }

    //helper functions...

    public void muteBtnPressed() {

        if (muteState.equals(CallWidgetState.MUTE_BUTTON_RELEASE)) {
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
            ib_mute.setImageDrawable(replacer);
            muteButtonPressed();
        } else {
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
            ib_mute.setImageDrawable(replacer);
            unMuteButtonPressed();

        }
    }

    private void initSurfaceRenderingView() {
        rootEgl = EglBase.create();
        // setUp local Video Stream...
        fullScreenVideoView.init(rootEgl.getEglBaseContext(), null);
        fullScreenVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        fullScreenVideoView.setEnableHardwareScaler(true);
        fullScreenVideoView.setMirror(false);
        localVideoProxy.setTarget(fullScreenVideoView);

        // Setup remote Video Stream...

        bottomVideoView.init(rootEgl.getEglBaseContext(), null);
        bottomVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        bottomVideoView.setZOrderMediaOverlay(true);
        bottomVideoView.setEnableHardwareScaler(true);
        bottomVideoView.setMirror(false);
        remoteVideoProxy.setTarget(bottomVideoView);
        remoteRenderers.add(remoteVideoProxy);
    }

    private void getActionFromIntent(Intent intent) {
        if (intent == null)
            return;
        if (intent.getAction() == null)
            return;
        switch (intent.getAction()) {
            case Constants.ForegroundService.ACTION_MAIN:
                startVideoCallProcedure(intent);
                break;
            case Constants.ForegroundService.END_CALL:
                finishConnectedIndividualCall();
                break;
            case Constants.ForegroundService.CANCEL_INGOING_CALL:
                callerCancelVideCall();
                break;
        }
    }

    private void startVideoCallProcedure(Intent intent) {
        getValuesFromIntentForIndividualCall(intent);
        showView();
        initSurfaceRenderingView();
        createRtcCallManager();
        callVideoCallApi(mGroupId);
        startDilaingTimerTask();
        videoCallState = EventFromService.CALL_DIALING;
        createNotificationForIndividual("Dialing", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
    }

    private void getValuesFromIntentForIndividualCall(Intent intent) {
        mGroupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
        mAttendentId = intent.getLongExtra(Constants.IntentExtra.ATTENDENT_ID, -1);
        attendedfirebaseID = intent.getStringExtra(Constants.IntentExtra.USER_FIREBASEID);
        mParticipantsJson = intent.getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        mRoomId = intent.getLongExtra(Constants.IntentExtra.INTENT_ROOM_ID, -1);
        mUserName = intent.getStringExtra(Constants.IntentExtra.USER_NAME);
        is_MuteButtonPressed = intent.getBooleanExtra(Constants.IntentExtra.IS_MUTE_PRESSED, false);
        if (is_MuteButtonPressed) {
            muteState = CallWidgetState.MUTE_BUTTON_PRESSED;
        }
    }


    private void createRtcCallManager() {
        rtcManger = new CallWebRTCManger(VideoInitiatorCallService.this,
                CallManager.CallType.INDIVIDUAL_VIDEO, mRoomId, this, videoCapturer, localVideoProxy, remoteRenderers);
        rtcManger.startVideoCallForInitiator();
        EventBus.getDefault().post(new VideoRendererEvent(localVideoProxy));
    }

    private void sendLocalRenderingView() {
        isForeGround = true;
        if (isSwappedFeeds) {
            hideWidget();
            EventBus.getDefault().post(new VideoRendererEvent(remoteVideoProxy));
        } else {
            EventBus.getDefault().post(new VideoRendererEvent(localVideoProxy));
        }
    }

    private void registerBroadCastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mNativeMobileIncomingCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_NATIVE_CALL_STARTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallRejected,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALL_REJECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastEventsFromActivity,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY));
    }

    private void unRegisterBroadCastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNativeMobileIncomingCall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCallRejected);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastEventsFromActivity);
    }

    private void createVideoCapturer() {
        videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {

                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {

                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private void getLatestState(Intent intent) {
        intent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        intent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        intent.putExtra(Constants.IntentExtra.CALL_STATE, videoCallState);
        intent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
    }

    private void createNotificationForIndividual(String notificationState, String action, Long time) {
        Intent notificationIntent = new Intent(this, InitiatorVideoCallActivity.class);
        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        notificationIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, videoCallState);
        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                Constants.CALL_TOTAL_TIME);
        Intent intent = new Intent(this, VideoInitiatorCallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "hub_channel_call";// The id of the channel.
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager manager = (NotificationManager) VideoInitiatorCallService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(mChannel);
            notificationBuilder = new NotificationCompat.Builder(VideoInitiatorCallService.this, "hub_channel_call");
            notificationBuilder.setChannelId("hub_channel_call");
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder.setContentTitle("Wateen Hub");
        notificationBuilder.setContentTitle("Wateen Hub");
        notificationBuilder.setTicker("@Wateen Hub");
        notificationBuilder.setColor(getResources().getColor(R.color.colorAccent));
        notificationBuilder.setContentText(notificationState + " " + mUserName);
        notificationBuilder.setSmallIcon(R.drawable.phonereceiver);
        notificationBuilder.setLargeIcon(icon);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setOngoing(true);
        //add notification actions
//add notification actions
        if (action.equals(Constants.ForegroundService.CANCEL_INGOING_CALL)) {
            intent.setAction(Constants.ForegroundService.CANCEL_INGOING_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.ic_call_decline, "End call", pendingIntent1);
        } else if (action.equals(Constants.ForegroundService.END_CALL)) {
            intent.setAction(Constants.ForegroundService.END_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.ic_call_decline, "End call", pendingIntent1);
        }
        Notification notification = notificationBuilder.build();
        startForeground(101, notification);
    }

    private void sendEventToActivity(Enum event) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, event);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void sendEventToActivity(Enum event, Long time) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, event);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void closePeerConnection() {
        try {
            if (rtcManger != null) {
                rtcManger.endCall();
                rtcManger = null;
                GlobalMethods.updateUiToUpdateCallLogs();
            } else {
                finishForegroundService();
            }
            finishForegroundService();
        } catch (Exception e) {

        }
    }

    private void finishForegroundService() {
        if (remoteVideoProxy != null) {
            remoteVideoProxy.setTarget(null);
        }
        if (bottomVideoView != null) {
            bottomVideoView.release();
        }
        stopVideoCapturer();
        stopSelf();
        stopForeground(true);
        GlobalMethods.callEnded();

        sendEventToActivity(EventFromService.FINISH_ACTIVITY);
    }

    private void stopVideoCapturer() {
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            videoCapturer = null;
        }
    }


    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.MESSAGE);
        intent.putExtra(Constants.IntentExtra.MESSAGE, msg);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void closeConnectionFromSocket() {
        if (rtcManger != null) {
            rtcManger.closePeerConnection();
            rtcManger = null;
        }
        GlobalMethods.updateUiToUpdateCallLogs();
        finishForegroundService();
    }

    private void sendTime() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.SEND_LATEST_TIME);
        intent.putExtra(Constants.IntentExtra.TIME_ELAPSED, getTimeFromChronometer());
        intent.putExtra(Constants.IntentExtra.MICROPHONE_STATE, muteState);
        intent.putExtra(Constants.IntentExtra.HOLD_STATE, holdState);
        intent.putExtra(Constants.IntentExtra.VIDEO_STATE, videoEnableState);
        intent.putExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, mCallerOnHold);
        intent.putExtra(Constants.IntentExtra.PARTICIPANT_MUTE_STATUS, mParticipantOnMute);
        intent.putExtra(Constants.IntentExtra.PARTICIPANT_HOLD_STATUS, ismParticipantOnVideoDisable);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void startCallingActivity() {
        if (rtcManger != null) {
            Intent intent = new Intent(getApplicationContext(), InitiatorVideoCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
            getLatestState(intent);
            getApplicationContext().startActivity(intent);
        }
    }

    private void muteButtonPressed() {
        if (rtcManger != null) {
            rtcManger.muteCall();
            muteState = CallWidgetState.MUTE_BUTTON_PRESSED;
            rtcManger.disableMicrophone();
        }
    }

    private void unMuteButtonPressed() {
        if (rtcManger != null) {
            rtcManger.unMuteCall();
            muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
            rtcManger.enableMicrophone();
        }
    }

    public void holdButtonPressed() {
        if (rtcManger != null) {
            muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
            videoEnableState = CallWidgetState.VIDEO_BUTTON_DISABLE;
            holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
            rtcManger.holdIngoingVideoCall(false);
        }
    }

    public void unHoldButtonPressed() {
        removeNativeRunnable();
        if (rtcManger != null) {
            holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
            rtcManger.unHoldIngoingVideoCall(true);
        }
    }

    public void disableVideoButtonPressed() {
        if (rtcManger != null) {
            rtcManger.disableVideoCall();
            muteState = CallWidgetState.VIDEO_BUTTON_DISABLE;
            rtcManger.disableVideo();
        }
    }

    public void enableVideoButtonPressed() {
        if (rtcManger != null) {
            rtcManger.enableVideoCall();
            muteState = CallWidgetState.VIDEO_BUTTON_ENABLE;
            rtcManger.enableVideo();
        }
    }

    private void stopToneDialer() {
        isCallStartedflag = false;
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
            mThread.isInterrupted();
        } else {
            forceFullyStopDialingTone();
        }
    }

    private void forceFullyStopDialingTone() {
        try {
            mToneGenerator.stopTone();
            mThread.isInterrupted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getTimeFromChronometer() {
        if (mChronometer != null) {
            return SystemClock.elapsedRealtime() - mChronometer.getBase();
        } else {
            return -1L;
        }
    }

    private void userNotReahcable(String message) {
        GlobalMethods.updateUiToUpdateCallLogs();
        ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        mToneGenerator.startTone(ToneGenerator.TONE_SUP_BUSY, 600);
        GlobalMethods.vibrateMobile();
        GlobalMethods.showToast(message);
        callerCancelVideCall();
    }

    private void updateCallId() {
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_FROM_SERVICE);
        intent.putExtra(Constants.IntentExtra.EVENT_FROM_SERVICE, EventFromService.UPDATE_CALL_ID);
        intent.putExtra(Constants.IntentExtra.CALL_ID, mCallId);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    private void nativeCallRecived() {
        isIndividualNativeCall = true;
        endHoldCall = true;
        holdState = CallWidgetState.HOLD_BUTTON_PRESSED;
        if (rtcManger != null) {
            rtcManger.holdIngoingVideoCall(true);
        }
        holdCallEndSchedular();
        sendEventToActivity(EventFromService.NATIVE_CALL_HOLD);
    }

    // BroadCast Initialization

    BroadcastReceiver mNativeMobileIncomingCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isCallStartedflag) {
                callerCancelVideCall();
            } else {
                nativeCallRecived();
            }
        }
    };

    BroadcastReceiver mCallRejected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            Long callIds = intent.getLongExtra("callId", -1);
            if (Objects.equals(callIds, mCallId)) {
                if (rtcManger != null) {
                    stopToneDialer();
                    sendMessageToActivity("UserModel has rejected");
                    rtcManger.closePeerConnection();
                    GlobalMethods.updateUiToUpdateCallLogs();
                    finishForegroundService();
                }
            }
        }
    };

    BroadcastReceiver mBroadCastEventsFromActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            EventsFromActivity eventsFromActivity = (EventsFromActivity) intent.getSerializableExtra(Constants.IntentExtra.EVENT_FROM_UI);
            if (eventsFromActivity == null)
                return;
            switch (eventsFromActivity) {
                case LOCAL_RENDER_VIEW:
                    sendLocalRenderingView();
                    break;
                case END_CALL_PRESSED:
                    finishConnectedIndividualCall();
                    break;
                case END_CALL_INGOING:
                    callerCancelVideCall();
                    break;
                case MUTE_BUTTON_PRESSED:
                    muteButtonPressed();
                    break;
                case UNMUTE_BUTTON_PRESSD:
                    unMuteButtonPressed();
                    break;
                case HOLD_BUTTON_PRESSED:
                    holdButtonPressed();
                    break;
                case UNHOLD_BUTTON_PRESSED:
                    unHoldButtonPressed();
                    break;
                case DISABLE_VIDEO:
                    disableVideoButtonPressed();
                    break;
                case ENABLE_VIDEO:
                    enableVideoButtonPressed();
                    break;
                case GET_LATEST_TIME:
                    sendTime();
                    break;
                case START_CALLING_ACTIVITY:
                    startCallingActivity();
                    break;
                case SHOW_REMOTE_VIEW:
                    if (!isCallStartedflag && isSwappedFeeds) {
                        showCallWiget();
                    } else {
                        isForeGround = false;
                    }
                    break;
            }

        }
    };

    public class NotificationAsync extends AsyncTask<String, String, String> {
        String output = "";

        public String status = "";
        Context context;


        public final static String AUTH_KEY_FCM_LIVE = Constants.FirebaseMessaging;
        public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

        public NotificationAsync(VideoInitiatorCallService context) {
            this.context = context;
        }

        String s;
        String firebaseId;

        public NotificationAsync(VideoInitiatorCallService GroupAudioCallerServiceFirebase, String firebaseId) {

            this.firebaseId = firebaseId;
            this.s = s;

        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
//            String param1 = params[0];
            String sendTo = attendedfirebaseID;
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
                jsonObject.put("callerId", UserManager.getInstance().getUserIfLoggedIn().getId());
                jsonObject.put("type", "video");

                json.put("data", jsonObject);
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

    // api calls...
    public void callVideoCallApi(final Long mGroupId) {
        new NotificationAsync(VideoInitiatorCallService.this, attendedfirebaseID).execute();

        Long currentTime = Calendar.getInstance().getTimeInMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("callStartTime", currentTime);
        params.put("callerId", UserManager.getInstance().getUserIfLoggedIn().getId());
        params.put("groupId", mGroupId);
        params.put("isIndividual", 1);
        params.put("type", "video");
        params.put("attendentId", mAttendentId);
        params.put("duration", -1L);
        params.put("callEndTime", -1L);
        params.put("roomId", mRoomId);
//        final CallRepository callRepository = new CallRepository();
//        callRepository.iniateVideoCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    Long statusCode = jsonObject.getLong("statusCode");
//                    if (statusCode == 801L) {
//                        if (result.getmMessage().equalsIgnoreCase("LoggedOut")) {
//                            userNotReahcable("UserModel is not Reachable");
//                        } else {
//                            userNotReahcable("UserModel is Busy");
//                        }
//                        return;
//                    }
//                    jsonObject = jsonObject.getJSONObject("data");
//                    mRoomId = jsonObject.optLong("roomId");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    Calls call = StringUtils.getGson().fromJson(jsonCall.toString(), Calls.class);
//                    StudentApp.getInstance().getHubDatabase().callsDao().addCall(call);
//                    mCallId = call.getCallId();
//                    if (call.getCallStateCode() == 701 && call.getResponseTypeCode() == 804) {
//                        updateCallId();
//                        GlobalMethods.saveGroupIdForCall(mGroupId);
//                    } else {
//                        if (result.getmMessage().equalsIgnoreCase("LoggedOut")) {
//                            userNotReahcable("UserModel is not Reachable");
//                        } else {
//                            userNotReahcable("UserModel is Busy");
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//                callerCancelVideCall();
//            }
//        });
    }

    private void finishConnectedIndividualCall() {
        isEndCallButtonPressed = true;
        removeNativeRunnable();
        final Long currentTime = Calendar.getInstance().getTimeInMillis();
        final Long duration;
        if (mChronometer != null) {
            duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometer.stop();
        } else {
            duration = -1L;
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, currentTime, duration, CallEndType.END_INDIVIDUAL_VIDEO_CALL);
        callClossingTask.startEndCallProcedure();
        closePeerConnection();
    }

    private void callerCancelVideCall() {
        stopToneDialer();
        removeNativeRunnable();
        removeDialingTimer();
        removeRingingTimer();
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.CALLER_ENDED_INDIVIDUAL_VIDEO_CALL);
        callClossingTask.startEndCallProcedure();
        closeConnectionFromSocket();
    }

    private void callMissedCallApi() {
        removeRingingTimer();
        removeDialingTimer();
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.INDIVIDUAL_MISSED_VIDEO_CALL);
        callClossingTask.startEndCallProcedure();
        closeConnectionFromSocket();
    }


    //AudioCallBackInterface implementation...
    @Override
    public void callEnded() {
        isEndCallButtonPressed = true;
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        Long duration;
        if (mChronometer != null) {
            duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometer.stop();
        } else {
            duration = -1L;
        }
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
        GlobalMethods.updateUiToUpdateCallLogs();
        finishForegroundService();
    }

    @Override
    public void callerCancelledCall() {

    }

    @Override
    public void startTimer() {
        if (rtcManger != null)
            rtcManger.enableLoudSpeaker();
        stopToneDialer();
        removeDialingTimer();
        removeRingingTimer();
        videoCallState = EventFromService.CALL_STARTED;
        createNotificationForIndividual("On call with ", Constants.ForegroundService.END_CALL, -1L);
        sendEventToActivity(EventFromService.CALL_STARTED, getTimeFromChronometer());
    }

    @Override
    public void updateCallingToConnectingState() {
        stopToneDialer();
        removeDialingTimer();
        removeRingingTimer();
        videoCallState = EventFromService.CALL_CONNNECTED;
        createNotificationForIndividual("Connected to ", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
        sendEventToActivity(EventFromService.CALL_CONNNECTED, getTimeFromChronometer());
    }

    @Override
    public void callRinging() {
        //todo ask with ahad on this one....
//        int volume = 0;
//        if (is_LoudSpreakerPressed) {
//            rtcManger.enableLoudSpeaker();
//            volume = ToneGenerator.MAX_VOLUME;
//        } else {
//            volume = 30;
//        }
//        playToneGenerator(volume);
//        if (is_MuteButtonPressed) {
//            callWebRTCManger.disableMicrophone();
//        }
        playToneGenerator(ToneGenerator.MAX_VOLUME);
        removeDialingTimer();
        startRingingTimerTask();
        videoCallState = EventFromService.CALL_RINGING;
        createNotificationForIndividual("Ringing", Constants.ForegroundService.CANCEL_INGOING_CALL, -1L);
        sendEventToActivity(EventFromService.CALL_RINGING, -1L);
    }


    @Override
    public void callerEndedAudioCall() {
        finishForegroundService();
    }

    @Override
    public void holdAudioCall(UserModel user, boolean isOnNativeCall) {
        mReciverOnHold = true;
        sendEventToActivity(EventFromService.HOLD_CALL);
    }

    @Override
    public void unholdAudioCall(UserModel user) {
        mReciverOnHold = false;
        sendEventToActivity(EventFromService.UNHOLD_CALL);
    }

    @Override
    public void reConnectingCall() {
        //todo handle loudspeaker...
        isReconnecting = true;
//        if (loudSpeakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
//            reconnectingTone(60);
//        } else {
//
//        }

        if (!isEndCallButtonPressed) {
            reconnectingTone(ToneGenerator.MAX_VOLUME);
            videoCallState = EventFromService.CALL_RECONNECTING;
            createNotificationForIndividual("Reconnecting to ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
            sendEventToActivity(EventFromService.CALL_RECONNECTING, getTimeFromChronometer());
        }
    }

    @Override
    public void finishCall() {
        videoCallState = EventFromService.CALL_RECONTECTED_FAILED;
        stopReconnectingDialer();
        createNotificationForIndividual("Reconnection failed with ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
        GlobalMethods.callEnded();
        finishConnectedIndividualCall();
        finishForegroundService();
    }

    @Override
    public void reconnectionEstablished() {
        stopToneDialer();
        if (videoCallState.equals(EventFromService.CALL_STARTED)) {
            startChronometer();
        }
        if (bottomVideoView != null)
            bottomVideoView.setVisibility(View.VISIBLE);
        stopReconnectingDialer();
        videoCallState = EventFromService.CALL_RECONNECTED;
        isSwappedFeeds = true;
        if (isForeGround) {
            setSwappedFeeds();
        } else {
            showCallWiget();
        }
        createNotificationForIndividual("On call with", Constants.ForegroundService.END_CALL, 2L);
        sendEventToActivity(EventFromService.CALL_RECONNECTED, getTimeFromChronometer());
    }

    @Override
    public void muteCall(UserModel user) {
        mParticipantOnMute = true;
        sendEventToActivity(EventFromService.PARTICIPANT_MUTE);
    }

    @Override
    public void unMuteCall(UserModel user) {
        mParticipantOnMute = false;
        sendEventToActivity(EventFromService.PARTICIPANT_UNMUTE);
    }

    @Override
    public void disableVideoCall(UserModel user) {
        ismParticipantOnVideoDisable = true;
        sendEventToActivity(EventFromService.PARTICIPANT_VIDEO_DISABLE);
    }

    @Override
    public void enableVideoCall(UserModel user) {
        ismParticipantOnVideoDisable = false;
        sendEventToActivity(EventFromService.PARTICIPANT_VIDEO_ENABLE);
    }

    @Override
    public void updateStats(double bandwidth, double bitrate, double totalBandwidth,
                            long packets) {

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



    /*
    Timer tasks....
     */

    private void startChronometer() {
        mChronometer = new Chronometer(VideoInitiatorCallService.this);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
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
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
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

    private void startDilaingTimerTask() {
        final Long delay = 31000L;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                callMissedCallApi();
            }
        };
        if (!isDialingTimerFlag) {
            mTimerDialing.schedule(task, delay);
        }
    }

    private void startRingingTimerTask() {
        if (!isAlreadyRing) {
            isAlreadyRing = true;
            final Long delay = 61000L;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    callMissedCallApi();
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

    private void stopReconnectingDialer() {
        isReconnecting = false;
        if (mReconnectingTone != null) {
            mReconnectingTone.stopTone();
            mThread.isInterrupted();
        }
    }

    private void removeNativeRunnable() {
        if (isIndividualNativeCall) {
            isIndividualNativeCall = false;
            endHoldCall = false;
            if (mHoldCallEnd != null) {
                mHoldCallEnd.removeCallbacks(holdCallTimeOut);
                mHoldCallEnd.removeCallbacksAndMessages(null);
                mHoldCallEnd = null;
            }
        }
    }

    private void purgeAllTimers() {
        removeDialingTimer();
        removeRingingTimer();
        stopReconnectingDialer();
    }

    /*
    swap feed work...
     */

    private void setSwappedFeeds() {
        localVideoProxy.setTarget(bottomVideoView);
        EventBus.getDefault().post(new VideoRendererEvent(remoteVideoProxy));
    }

    private void showCallWiget() {
        isForeGround = false;
        localVideoProxy.setTarget(fullScreenVideoView);
        remoteVideoProxy.setTarget(bottomVideoView);
        setUpdatedWidgetState();
        widgetLayout.setVisibility(View.VISIBLE);
    }

    private void hideWidget() {
        isForeGround = true;
        localVideoProxy.setTarget(bottomVideoView);
        remoteVideoProxy.setTarget(fullScreenVideoView);
        widgetLayout.setVisibility(View.INVISIBLE);
    }

    private void setUpdatedWidgetState() {

        if (muteState.equals(CallWidgetState.MUTE_BUTTON_RELEASE)) {
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute_pressed);
            ib_mute.setImageDrawable(replacer);
        } else {
            Drawable replacer = getResources().getDrawable(R.drawable.ic_mute);
            ib_mute.setImageDrawable(replacer);
        }
    }
}
