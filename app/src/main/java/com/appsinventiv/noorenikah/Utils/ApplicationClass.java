package com.appsinventiv.noorenikah.Utils;

import android.app.Application;
import android.content.Context;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class ApplicationClass extends Application {
    private static ApplicationClass instance;


    public static ApplicationClass getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        EmojiManager.install(new GoogleEmojiProvider());



    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}