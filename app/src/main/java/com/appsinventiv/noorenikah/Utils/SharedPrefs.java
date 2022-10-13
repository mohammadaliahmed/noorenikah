package com.appsinventiv.noorenikah.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.Models.PromotionBanner;
import com.appsinventiv.noorenikah.Models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

public class SharedPrefs {
    Context context;

    private SharedPrefs() {

    }

    public static void setReadMap(HashMap<String, String> itemList) {

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        preferenceSetter("unread", json);
    }

    public static HashMap<String, String> getReadMap() {
        Gson gson = new Gson();
        HashMap<String, String> playersList = (HashMap<String, String>) gson.fromJson(preferenceGetter("unread"),
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        return playersList;
    }


    public static void setPromotionalBanner(PromotionBanner banner) {

        Gson gson = new Gson();
        String json = gson.toJson(banner);
        preferenceSetter("setPromotionalBanner", json);
    }

    public static PromotionBanner getPromotionalBanner() {
        Gson gson = new Gson();
        return gson.fromJson(preferenceGetter("setPromotionalBanner"),PromotionBanner.class);

    }







    public static void setLikedMap(HashMap<String, String> itemList) {

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        preferenceSetter("liked", json);
    }

    public static HashMap<String, String> getLikedMap() {
        Gson gson = new Gson();
        HashMap<String, String> playersList = (HashMap<String, String>) gson.fromJson(preferenceGetter("liked"),
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        return playersList;
    }


    public static void setPostLikedMap(HashMap<String, String> itemList) {

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        preferenceSetter("postliked", json);
    }

    public static HashMap<String, String> getPostLikedMap() {
        Gson gson = new Gson();
        HashMap<String, String> playersList = (HashMap<String, String>) gson.fromJson(preferenceGetter("postliked"),
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        return playersList;
    }
//
    public static void setUsersList(List<NewUserModel> itemList) {

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        preferenceSetter("setUsersList", json);
    }

    public static List<NewUserModel> getUsersList() {
        Gson gson = new Gson();
        List<NewUserModel> playersList = (List<NewUserModel>) gson.fromJson(preferenceGetter("setUsersList"),
                new TypeToken<List<NewUserModel>>() {
                }.getType());
        return playersList;
    }


    public static String getLastMsg(String key) {
        return preferenceGetter(key);
    }

    public static void setLastMsg(String key, String value) {
        preferenceSetter(key, value);
    }

    public static String getFcmKey() {
        return preferenceGetter("getFcmKey");
    }

    public static void setFcmKey(String username) {
        preferenceSetter("getFcmKey", username);
    }


    public static String getLat() {
        return preferenceGetter("getLat");
    }

    public static void setLat(String username) {
        preferenceSetter("getLat", username);
    }

    public static String getPhone() {
        return preferenceGetter("setPhone");
    }

    public static void setPhone(String username) {
        preferenceSetter("setPhone", username);
    }

    public static String getLastTime() {
        return preferenceGetter("getLastTime");
    }

    public static void setLastTime(String username) {
        preferenceSetter("getLastTime", username);
    }

    public static String getLon() {
        return preferenceGetter("getLon");
    }

    public static void setLon(String username) {
        preferenceSetter("getLon", username);
    }


    public static void setUser(User model) {

        Gson gson = new Gson();
        String json = gson.toJson(model);
        preferenceSetter("customerModel", json);
    }

    public static User getUser() {
        Gson gson = new Gson();
        User customer = gson.fromJson(preferenceGetter("customerModel"), User.class);

        return customer;
    }

    public static void setTempUser(User model) {

        Gson gson = new Gson();
        String json = gson.toJson(model);
        preferenceSetter("setTempUser", json);
    }

    public static User getTempUser() {
        Gson gson = new Gson();
        User customer = gson.fromJson(preferenceGetter("setTempUser"), User.class);

        return customer;
    }

    public static void preferenceSetter(String key, String value) {
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

    public static void clearApp() {
        SharedPreferences pref = ApplicationClass.getInstance().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        pref.edit().clear().commit();


    }

    public static void logout() {
        SharedPreferences pref = ApplicationClass.getInstance().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

    }
}
