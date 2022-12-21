package com.appsinventiv.noorenikah.Utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appsinventiv.noorenikah.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;


/**
 * Created by AhadAs on 10/11/2017.
 */

public class GlobalMethods {
    private static Long INGOING_CALL_GROUP_ID = -1L;

    public static boolean isBackgroundRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        if (runningProcesses == null)
            return false;


        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    public static void playNotificationSound() {
//        Uri path = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
//                + "://" + StudentApp.getInstance().getApplicationContext().getPackageName() + "/raw/mymp3");
//
//        RingtoneManager.setActualDefaultRingtoneUri(
//                StudentApp.getInstance().getApplicationContext(), RingtoneManager.TYPE_RINGTONE,
//                path);
//
//        RingtoneManager.getRingtone(StudentApp.getInstance().getApplicationContext(), path)
//                .play();

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(ApplicationClass.getInstance().getApplicationContext(), notification);
            r.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ApplicationClass.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint("WrongConstant")
            public void run() {
                Toast.makeText(ApplicationClass.getInstance().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static void showDialoge(final String msg) {

    }



    public static String decodeMessageFromUtf8Format(String message) {
        String decodeMessage = message;
        if (decodeMessage != null) {
            try {
                decodeMessage = URLDecoder.decode(message, Constants.UTF_8_FORMAT);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                decodeMessage = message;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                decodeMessage = message;
            }
        }
        return decodeMessage;
    }

    public static String parseMessageToUtf8Format(String message) {
        String encodeMessage = message;
        try {
            encodeMessage = URLEncoder.encode(message, Constants.UTF_8_FORMAT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            encodeMessage = message;
        }
        return encodeMessage;
    }

    public static Bitmap convertBitmap(String image) {

        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }




    public static void vibrateMobile() {
        Vibrator v = (Vibrator) ApplicationClass.getInstance().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            try {
                v.vibrate(150);
            } catch (Exception e) {

            }
        }
    }

    public static void saveGroupIdForCall(Long groupId) {
        INGOING_CALL_GROUP_ID = groupId;
    }

    public static Long getIngoingCallGroupId() {
        return INGOING_CALL_GROUP_ID;
    }

    public static void updateUiToUpdateCallLogs() {
        INGOING_CALL_GROUP_ID = -1L;
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_UPDATE_CALL_LOGS);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    public static void callEnded() {
        INGOING_CALL_GROUP_ID = -1L;
        Intent intent = new Intent(Constants.Broadcasts.BROADCAST_CALL_Ended);
        LocalBroadcastManager.getInstance(ApplicationClass.getInstance().getApplicationContext()).sendBroadcast(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotifactionManger() {
        String CHANNEL_ID = "hub_channel";// The id of the channel.
        CharSequence name = ApplicationClass.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.enableLights(true);
        mChannel.setLightColor(ApplicationClass.getInstance().getResources().getColor(R.color.colorPrimary));
        NotificationManager manager = (NotificationManager) ApplicationClass.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(mChannel);
    }

}
