package com.appsinventiv.noorenikah.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.appsinventiv.noorenikah.Activities.ChatScreen;
import com.appsinventiv.noorenikah.Activities.Comments.CommentsActivity;
import com.appsinventiv.noorenikah.Activities.InviteHistory;
import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Activities.Splash;
import com.appsinventiv.noorenikah.Activities.ViewFriendProfile;
import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by AliAh on 01/03/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String msg;
    String title, message, type;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    String Id;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("message payload", "Message data payload: " + remoteMessage.getData());
            msg = "" + remoteMessage.getData();

            Map<String, String> map = remoteMessage.getData();
            Log.d("orderId", "" + map.get("orderDetails" +
                    ""));
            message = map.get("Message");
            title = map.get("Title");
            type = map.get("Type");
            Id = map.get("Id");

            handleNow(title, message);


        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("body", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void handleNow(String notificationTitle, String messageBody) {

        int num = (int) System.currentTimeMillis();
        /**Creates an explicit intent for an Activity in your app**/
        Intent resultIntent = null;
//        if (type.equalsIgnoreCase("adActivated")) {
//            resultIntent = new Intent(this, MyAds.class);
//        } else if (type.equalsIgnoreCase("marketing")) {
        if (type.equals("accepted")) {
            resultIntent = new Intent(this, ViewFriendProfile.class);
            resultIntent.putExtra("phone", Id);

        } else if (type.equals("request")) {
            Constants.REQUEST_RECEIVED = true;

            resultIntent = new Intent(this, MainActivity.class);
        } else if (type.equals("msg")) {

            resultIntent = new Intent(this, ChatScreen.class);
            resultIntent.putExtra("phone", Id);
        } else if (type.equals("payout")) {

            resultIntent = new Intent(this, InviteHistory.class);
        } else if (type.equals("profile")) {

            resultIntent = new Intent(this, MainActivity.class);
        } else if (type.equals("signout")) {
            SharedPrefs.logout();
            resultIntent = new Intent(this, Splash.class);

        } else if (type.equals("marketing")) {
            Constants.MARKETING_MSG = true;
            Constants.MARKETING_MSG_TITLE = notificationTitle;
            Constants.MARKETING_MSG_MESSAGE = messageBody;
            resultIntent = new Intent(this, MainActivity.class);
        } else if (type.equals("comment")) {
            resultIntent = new Intent(this, CommentsActivity.class);
            resultIntent.putExtra("id", Id);
        } else if (type.equals("like")) {
            resultIntent = new Intent(this, ViewRequestProfile.class);
            resultIntent.putExtra("id", Id);
        } else {
            resultIntent = new Intent(this, MainActivity.class);
        }
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, resultIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
        }

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(notificationTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pendingIntent);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.WHITE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(num /* Request Code */, mBuilder.build());
    }
}
