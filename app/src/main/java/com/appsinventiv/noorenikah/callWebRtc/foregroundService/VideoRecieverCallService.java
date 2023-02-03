package com.appsinventiv.noorenikah.callWebRtc.foregroundService;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

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
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.CallClossingTask;
import com.appsinventiv.noorenikah.callWebRtc.CallListner.AudioCallBackInterface;
import com.appsinventiv.noorenikah.callWebRtc.CallWebRTCManger;
import com.appsinventiv.noorenikah.callWebRtc.ReceiverVideoCallActivity;
import com.appsinventiv.noorenikah.callWebRtc.VideoProxyRenderer;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallEndType;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallWidgetState;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventFromService;
import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.EventsFromActivity;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class VideoRecieverCallService extends Service implements AudioCallBackInterface {
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
    boolean mHasDoubleClicked = false;
    long lastPressTime, mGroupId, mRoomId, mAttendentId, mCallId;
    private String mParticipantsJson, mUserName;
    boolean isCallStartedFlag = true;
    boolean mCallerOnHold, isReconnecting,
            mParticipantOnMute, ismParticipantOnVideoDisable,
            isIndividualNativeCall, endHoldCall, is_MuteButtonPressed, isSwappedFeeds, isEndCallButtonPressed;
    Enum videoCallState;
    Ringtone ringtone;
    Vibrator mVibrator;
    Chronometer mChronometer;
    Thread mThread;
    Handler mHoldCallEnd;
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

    /* Timertask declaration...
     */
    Timer mTimer = new Timer();
    /**
     * Save widget state.
     */
    Enum muteState = CallWidgetState.MUTE_BUTTON_RELEASE;
    Enum holdState = CallWidgetState.HOLD_BUTTON_RELEASE;
    Enum videoEnableState = CallWidgetState.VIDEO_BUTTON_ENABLE;

    /**
     * Tone Generator.
     */
    ToneGenerator mReconnectingTone;
    private String userstring;
//    private UserModel user;

    /**
     * /*
     * unused method...
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
        unRegisterBroadCastReciever();
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
            Objects.requireNonNull(mWindowManager).addView(mOverlayView, params);
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
                    finishForegroundService();

                }
            });

            ib_video = mOverlayView.findViewById(R.id.btn_video);
            ib_video.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent intent = new Intent(getApplicationContext(), ReceiverVideoCallActivity.class);
                    //   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
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
        fullScreenVideoView.setMirror(true);
        localVideoProxy.setTarget(fullScreenVideoView);
        // Setup remote Video Stream...

        bottomVideoView.init(rootEgl.getEglBaseContext(), null);
        bottomVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        bottomVideoView.setZOrderMediaOverlay(true);
        bottomVideoView.setEnableHardwareScaler(true);
        bottomVideoView.setMirror(true);
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
            case Constants.ForegroundService.ACCEPT_CALL:
                acceptVideoCallProcedure();
                break;
            case Constants.ForegroundService.CANCEL_INCOMING_CALL:
                stopIncommingCall();
                rejectIncomingIndividualVideoCall();
                break;
            case Constants.ForegroundService.END_CALL:
                finishConnectedIndividualCall();
                finishForegroundService();
                break;
        }
    }

    private void startVideoCallProcedure(Intent intent) {
        getValuesFromIntentForIndividualCall(intent);
        showView();
        initSurfaceRenderingView();
        createRtcCallManager();
        videoCallState = EventFromService.CALL_RINGING;
        checkIfMobileonSilent();
        createNotificationForIndividual("Calling", Constants.ForegroundService.ACCEPT_CALL, -1L);
    }

    private void getValuesFromIntentForIndividualCall(Intent intent) {
        mGroupId = intent.getLongExtra(Constants.IntentExtra.INTENT_GROUP_ID, -1);
        mAttendentId = intent.getLongExtra(Constants.IntentExtra.ATTENDENT_ID, -1);
        mCallId = intent.getLongExtra(Constants.IntentExtra.CALL_ID, -1);
        mParticipantsJson = intent.getStringExtra(Constants.IntentExtra.PARTICIPANTS);
        mRoomId = intent.getLongExtra(Constants.IntentExtra.INTENT_ROOM_ID, -1);
        mUserName = intent.getStringExtra(Constants.IntentExtra.USER_NAME);
        is_MuteButtonPressed = intent.getBooleanExtra(Constants.IntentExtra.IS_MUTE_PRESSED, false);
        userstring = intent.getStringExtra(Constants.IntentExtra.USERSTRING);
        Type listType = new TypeToken<UserModel>() {
        }.getType();
//        user = StringUtils.getGson().fromJson(userstring, listType);
//        mUserName = user.getName();

        if (is_MuteButtonPressed) {
            muteState = CallWidgetState.MUTE_BUTTON_PRESSED;
        }
    }


    private void createRtcCallManager() {
        rtcManger = new CallWebRTCManger(VideoRecieverCallService.this,
                CallManager.CallType.INDIVIDUAL_VIDEO,
                mRoomId, this, videoCapturer, localVideoProxy, remoteRenderers);
        rtcManger.startVideoCallForReciever();
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

    private void playSoundForIncommingcall() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(VideoRecieverCallService.this, notification);
            ringtone.play();
            startTimerTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCallLogs(Long mCallId, Long callStateCode, Long responseType) {
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallStatusAndResponse(mCallId, callStateCode, responseType, 0L, 0L);
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
        if (mVibrator != null) {
            mVibrator.vibrate(pattern, 0);
        }
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


    private void registerBroadCastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mNativeMobileIncomingCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_NATIVE_CALL_STARTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallRejected,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALL_REJECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastEventsFromActivity,
                new IntentFilter(Constants.Broadcasts.BROADCAST_FROM_ACTIVITY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCallerCancelCall,
                new IntentFilter(Constants.Broadcasts.BROADCAST_CALLER_CANCEL_AUDIO_CAll));

    }

    private void unRegisterBroadCastReciever() {
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
        Intent notificationIntent = new Intent(this, ReceiverVideoCallActivity.class);
        notificationIntent.putExtra(Constants.IntentExtra.IS_CALL_STARTED_FROM_SERVICE, true);
        notificationIntent.putExtra(Constants.IntentExtra.USER_NAME, mUserName);
        notificationIntent.putExtra(Constants.IntentExtra.TIME_ELAPSED, time);
        notificationIntent.putExtra(Constants.IntentExtra.USERSTRING, userstring);
        notificationIntent.putExtra(Constants.IntentExtra.CALL_STATE, videoCallState);
        notificationIntent.putExtra(Constants.IntentExtra.INTENT_GROUP_ID, mGroupId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_MUTABLE);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                Constants.CALL_TOTAL_TIME);
        Intent intent = new Intent(this, VideoRecieverCallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "hub_channel_call";// The id of the channel.
            CharSequence name = getString(R.string.channel_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager manager = (NotificationManager) VideoRecieverCallService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(mChannel);
            notificationBuilder = new NotificationCompat.Builder(VideoRecieverCallService.this, "hub_channel_call");
            notificationBuilder.setChannelId("hub_channel_call");
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder.setContentTitle("Ustad App");
        notificationBuilder.setContentTitle("Ustad App");
        notificationBuilder.setTicker("@Ustad App");
        notificationBuilder.setColor(getResources().getColor(R.color.colorAccent));
        notificationBuilder.setContentText(notificationState + " " + mUserName);
        notificationBuilder.setSmallIcon(R.drawable.ic_phone_white);
        notificationBuilder.setLargeIcon(icon);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setOngoing(true);
        //add notification actions
        if (action.equals(Constants.ForegroundService.ACCEPT_CALL)) {
            intent.setAction(Constants.ForegroundService.ACCEPT_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent,  PendingIntent.FLAG_MUTABLE);
            Intent cancelIntent = new Intent(this, VideoRecieverCallService.class);
            cancelIntent.setAction(Constants.ForegroundService.CANCEL_INCOMING_CALL);
            PendingIntent pendingIntent2 = PendingIntent.getService(this, 0,
                    cancelIntent,  PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.ic_call_accept, "Answer", pendingIntent1);
            notificationBuilder.addAction(R.drawable.ic_call_decline, "Cancel call", pendingIntent2);
        } else if (action.equals(Constants.ForegroundService.END_CALL)) {
            intent.setAction(Constants.ForegroundService.END_CALL);
            PendingIntent pendingIntent1 = PendingIntent.getService(this, 0,
                    intent,  PendingIntent.FLAG_MUTABLE);
            notificationBuilder.addAction(R.drawable.ic_call_decline, "End call", pendingIntent1);
        }
        Notification notification = notificationBuilder.build();
        startForeground(101, notification);
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
//        GlobalMethods.callEnded();
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

    private void prepareIndividualCallManager(Long mCallId, Long callStateId, Long responseTypeId) {
        stopIncommingCall();
        updateCallLogs(mCallId, callStateId, responseTypeId);
        sendEventToActivity(EventFromService.UPDATE_TO_CONNECTING_STATE);
        videoCallState = EventFromService.CALL_CONNNECTED;
        rtcManger.initializeSocketComponents();
        rtcManger.sendReadyToEventToPeer();
        createNotificationForIndividual("Connected to ", Constants.ForegroundService.END_CALL, -1L);
    }

    private void closePeerConnection() {
        Log.d("benchdohramda", "asdasddalla");
        if (rtcManger != null) {
            rtcManger.endCall();
            rtcManger = null;
//            GlobalMethods.updateUiToUpdateCallLogs();
            Log.d("benchdo", "asdasddalla");
        } else {
            finishForegroundService();
        }
    }

    public void acceptVideoCallProcedure() {
        stopIncommingCall();
        acceptIncommingIndividualCall();
        sendEventToActivity(EventFromService.CALL_ACCEPT);
        removeTimer();
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
        removeNativeRunnabletask();
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
            Intent intent = new Intent(getApplicationContext(), ReceiverVideoCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
            getLatestState(intent);
            getApplicationContext().startActivity(intent);
        }
    }

    private void removeNativeRunnabletask() {
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
            if (isCallStartedFlag) {
                rejectIncomingIndividualVideoCall();
            } else {
                nativeCallRecived();
            }
        }
    };

    BroadcastReceiver mCallRejected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    BroadcastReceiver mCallerCancelCall = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            stopIncommingCall();
            if (ringtone != null) {
                ringtone.stop();
            }
            if (rtcManger != null) {
                rtcManger.closeRecieverSocketConnection();
            }
//            GlobalMethods.updateUiToUpdateCallLogs();
            isCallStartedFlag = false;
            finishForegroundService();
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
                    finishForegroundService();

                    break;
                case ACCEPT_CALL_PRESSED:
                    acceptVideoCallProcedure();
                    break;
                case REJECT_CALL_PRESSED:
                    rejectIncomingIndividualVideoCall();
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
                    if (!isCallStartedFlag && isSwappedFeeds) {
                        showCallWiget();
                    } else {
                        isForeGround = false;
                    }
                    break;
            }

        }
    };

    // api calls...
    private void finishConnectedIndividualCall() {
        try {
            isEndCallButtonPressed = true;
            removeNativeRunnabletask();
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

        } catch (Exception e) {

        } finally {
            closePeerConnection();
        }
    }

    public void acceptIncommingIndividualCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        CallRepository callRepository = new CallRepository();
//        callRepository.acceptVideoCall(params, new INetworkRequestListener() {
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
////                        GlobalMethods.saveGroupIdForCall(mGroupId);
//                    } else {
////                        GlobalMethods.updateUiToUpdateCallLogs();
//                        finishForegroundService();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
////                GlobalMethods.updateUiToUpdateCallLogs();
//                finishForegroundService();
//            }
//        });
    }

    public void rejectIncomingIndividualVideoCall() {
        stopIncommingCall();
//        GlobalMethods.updateUiToUpdateCallLogs();
        if (rtcManger != null) {
            rtcManger.closeRecieverSocketConnection();
        }
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, CallEndType.RECEIVER_REJECT_INCOMIING_VIDEO_CALL);
        callClossingTask.startEndCallProcedure();
        finishForegroundService();
    }

    private void updateUserCallStatus() {
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
////        GlobalMethods.updateUiToUpdateCallLogs();
        finishForegroundService();
    }

    @Override
    public void callerCancelledCall() {
        stopIncommingCall();
        if (ringtone != null) {
            ringtone.stop();
        }
        if (rtcManger != null) {
            rtcManger.closeRecieverSocketConnection();
        }
////        GlobalMethods.updateUiToUpdateCallLogs();
        isCallStartedFlag = false;
        finishForegroundService();
    }

    @Override
    public void startTimer() {
        if (rtcManger != null)
            rtcManger.enableLoudSpeaker();
        isCallStartedFlag = false;
        removeTimer();
        stopIncommingCall();
        videoCallState = EventFromService.CALL_STARTED;
        createNotificationForIndividual("On call with ", Constants.ForegroundService.END_CALL, -1L);
        sendEventToActivity(EventFromService.CALL_STARTED, getTimeFromChronometer());
    }

    @Override
    public void updateCallingToConnectingState() {
        stopIncommingCall();
        removeTimer();
        createNotificationForIndividual("Connected to ", Constants.ForegroundService.END_CALL, -1L);
        videoCallState = EventFromService.CALL_CONNNECTED;
        sendEventToActivity(EventFromService.CALL_CONNNECTED);
    }

    @Override
    public void callerEndedAudioCall() {
        finishForegroundService();
    }

    @Override
    public void holdAudioCall(UserModel user, boolean isOnNativeCall) {
        mCallerOnHold = true;
        sendEventToActivity(EventFromService.HOLD_CALL);
    }

    @Override
    public void unholdAudioCall(UserModel user) {
        mCallerOnHold = true;
        sendEventToActivity(EventFromService.UNHOLD_CALL);
    }


    @Override
    public void reConnectingCall() {
        //todo handle loud speaker...
        isReconnecting = true;
//        if (loudSpeakerState.equals(CallWidgetState.LOUDSPEAKER_BUTTON_RELEASE)) {
//            reconnectingTone(60);
//        } else {
//            reconnectingTone(ToneGenerator.MAX_VOLUME);
//        }
        if (!isCallStartedFlag) {
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
//        GlobalMethods.callEnded();
        finishConnectedIndividualCall();
        finishForegroundService();
    }

    @Override
    public void reconnectionEstablished() {
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
        createNotificationForIndividual("On call with ", Constants.ForegroundService.END_CALL, getTimeFromChronometer());
        sendEventToActivity(EventFromService.CALL_RECONNECTED, getTimeFromChronometer());
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
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        long duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mChronometer.stop();
        CallClossingTask callClossingTask = new CallClossingTask(mCallId, currentTime, duration, 0, CallEndType.END_GROUP_CALL);
        callClossingTask.startEndCallProcedure();
//        CallRepository callRepository = new CallRepository();
//        callRepository.updateCallStatusAndResponse(mCallId, 703L, 803L, currentTime, duration);
        closePeerConnection();
        finishForegroundService();
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


    /*
    Timer task implementations ...
     */

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

    private void startChronometer() {
        mChronometer = new Chronometer(VideoRecieverCallService.this);
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
        mChronometer.start();
    }

    private Long getTimeFromChronometer() {
        if (mChronometer != null) {
            return SystemClock.elapsedRealtime() - mChronometer.getBase();
        } else {
            return -1L;
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
                finishForegroundService();

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

    private void removeTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private void purgeAllTimers() {
        stopReconnectingDialer();
        removeTimer();
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
        //   bottomVideoView.setVisibility(View.INVISIBLE);
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




