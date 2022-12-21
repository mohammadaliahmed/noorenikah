package com.appsinventiv.noorenikah.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.Models.PromotionBanner;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

public class SharedPrefs {
    Context context;

    private SharedPrefs() {

    }


    public static void setPromotionalBanner(PromotionBanner banner,String Key) {

        Gson gson = new Gson();
        String json = gson.toJson(banner);
        preferenceSetter(Key, json);
    }

    public static PromotionBanner getPromotionalBanner(String key) {
        Gson gson = new Gson();
        return gson.fromJson(preferenceGetter(key),PromotionBanner.class);

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



    public static void setChatMap(HashMap<String, ChatModel> itemList) {

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        preferenceSetter("getChatMap", json);
    }

    public static HashMap<String, ChatModel> getChatMap() {
        Gson gson = new Gson();
        HashMap<String, ChatModel> playersList = (HashMap<String, ChatModel>) gson.fromJson(preferenceGetter("getChatMap"),
                new TypeToken<HashMap<String, ChatModel>>() {
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
 public static void setChatList(List<ChatModel> itemList,String key) {

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        preferenceSetter("setChatList"+key, json);
    }

    public static List<ChatModel> getChatList(String key) {
        Gson gson = new Gson();
        List<ChatModel> playersList = (List<ChatModel>) gson.fromJson(preferenceGetter("setChatList"+key),
                new TypeToken<List<ChatModel>>() {
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


    public static String getDemoShown() {
        return preferenceGetter("getDemoShown");
    }

    public static void setDemoShown(String username) {
        preferenceSetter("getDemoShown", username);
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


    public static void setUser(UserModel model) {

        Gson gson = new Gson();
        String json = gson.toJson(model);
        preferenceSetter("customerModel", json);
    }

    public static UserModel getUser() {
        Gson gson = new Gson();
        UserModel customer = gson.fromJson(preferenceGetter("customerModel"), UserModel.class);

        return customer;
    }

    public static void setTempUser(UserModel model) {

        Gson gson = new Gson();
        String json = gson.toJson(model);
        preferenceSetter("setTempUser", json);
    }

    public static UserModel getTempUser() {
        Gson gson = new Gson();
        UserModel customer = gson.fromJson(preferenceGetter("setTempUser"), UserModel.class);

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
        editor.clear().apply();


    }
}
