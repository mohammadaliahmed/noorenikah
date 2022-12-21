package com.appsinventiv.noorenikah.Utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class ApplicationClass extends Application {
    private static ApplicationClass instance;

    private SharedPreferences prefs;

    public static PreferencesManager preferencesManager;

    public static ApplicationClass getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        EmojiManager.install(new GoogleEmojiProvider());
        preferencesManager = PreferencesManager.getInstance();
        prefs = getSharedPreferences(UserManager.PREFS_NAME, Context.MODE_PRIVATE);


    }
    public SharedPreferences getPrefs() {
        return prefs;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}