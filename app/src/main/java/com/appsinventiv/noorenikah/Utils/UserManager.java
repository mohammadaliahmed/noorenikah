package com.appsinventiv.noorenikah.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.appsinventiv.noorenikah.Models.MetaData;
import com.appsinventiv.noorenikah.Models.UserModel;


/**
 * Created by Zaid on 01/03/2018.
 */

public class UserManager {
    private static UserManager instance;
    public static final String PREFS_NAME = "app_prefs";
    public static final String PREFS_USER = "prefs_user";
    public static final String PREFS_METADATA = "metadtaa";
    public static final String PREFS_VIDEOTIME = "vidtime";

    public static final String PREFS_AUTHTOKEN = "authtoken";
    public static final int MaxAtemptstotal = 5;
    public static final int MaxChangePintotal = 3;
    public static final String MaxAtempts = "maxAtempts";

    public static final String MaxChangePinAtempts = "maxAChangePintempts";
    public static final String SigninBlockTime = "signinBlockTime";
    public static final String CHANGEPINTIME = "changepinTime";


    private static final String PREF_KEY_USER_LOGGED_IN = "PREF_KEY_USER_LOGGED_IN";
    private static final String PREF_KEY_USER_REMEBER = "PREF_KEY_USER_REMEBER";

    public static boolean firstTime = false;
    public MetaData metaData = null;

    private UserManager() {

    }

    public static UserManager getInstance() {
        if (instance == null)
            instance = new UserManager();

        return instance;
    }

    public void setUserLogin() {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(PREFS_USER, StringUtils.getGson().toJson(user));
        editor.putBoolean(PREF_KEY_USER_LOGGED_IN, true);

        editor.commit();
        //     this.user = user;
    }


    public boolean checkUserIfLoggedIn() {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        boolean userLoggedIn = sharedPreferences.getBoolean(PREF_KEY_USER_LOGGED_IN, false);
        return userLoggedIn;
    }

    public UserModel getUserIfLoggedIn() {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        String jsonUser = sharedPreferences.getString(PREFS_METADATA, null);
        if (!StringUtils.isNullOrEmpty(jsonUser)) {
            this.metaData = StringUtils.getGson().fromJson(jsonUser, MetaData.class);
            return metaData.getUser();
        } else {
            return null;
        }
    }
//    public UserModel getUserIfLoggedInn() {
//        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
//        String jsonUser = sharedPreferences.getString(PREFS_METADATA, null);
//        if (!StringUtils.isNullOrEmpty(jsonUser)) {
//            this.metaData = StringUtils.getGson().fromJson(jsonUser, MetaData.class);
//            return metaData.getUser();
//        } else {
//            return null;
//        }
//    }

    public void setMetaData(MetaData metaData) {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_METADATA, StringUtils.getGson().toJson(metaData));

        editor.commit();
    }
    public void setUserData(UserModel model) {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_USER, StringUtils.getGson().toJson(model));

        editor.commit();
    }
    public  void setFcmKey(String fcmKey){
        preferenceSetter("fcmKey", fcmKey);
    }
    public static String getFcmKey() {
        return preferenceGetter("fcmKey");
    }

    public MetaData getMetaData() {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        String jsonUser = sharedPreferences.getString(PREFS_METADATA, null);
        if (!StringUtils.isNullOrEmpty(jsonUser)) {
            this.metaData = StringUtils.getGson().fromJson(jsonUser, MetaData.class);
            return metaData;
        }
        return null;
    }

    public void logoutCurrentUser() {
        SharedPreferences sharedPreferences = ApplicationClass.getInstance().getPrefs();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_KEY_USER_LOGGED_IN);
        editor.remove(PREFS_METADATA);

        editor.commit();
        editor.clear();

//        this.user = null;
    }

    public void preferenceSetter(String key, String value) {
        SharedPreferences pref = ApplicationClass.getInstance().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String preferenceGetter(String key) {
        SharedPreferences pref;
        String value = "";
        pref = ApplicationClass.getInstance().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        value = pref.getString(key, "");
        return value;
    }

}
