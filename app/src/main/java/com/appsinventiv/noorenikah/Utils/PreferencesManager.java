package com.appsinventiv.noorenikah.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static PreferencesManager sInstance;
    private final SharedPreferences mPref;

    public static final String PREF_NAME = "studentApp-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_EMAIL_SEND ="emailsend";

    private PreferencesManager(Context context, String fileName) {
        mPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context, String fileName) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context, fileName);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (sInstance == null) {
//            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
//                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }


    public void setFirstTimeLaunch(boolean isFirstTime) {
        mPref.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).commit();
    }

    public boolean isFirstTimeLaunch() {
        return mPref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setEmailSend(boolean isFirstTime) {
        mPref.edit().putBoolean(IS_EMAIL_SEND, isFirstTime).commit();
    }

    public boolean isEmailSend() {
        return mPref.getBoolean(IS_EMAIL_SEND, true);
    }
}
