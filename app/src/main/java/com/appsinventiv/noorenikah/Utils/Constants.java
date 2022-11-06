package com.appsinventiv.noorenikah.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants {
    public static boolean ACCEPTED=false;
    public static boolean REQUEST_RECEIVED=false;
    public static int PAYOUT_AMOUNT=20;
    public static boolean MARKETING_MSG=false;
    public static String MARKETING_MSG_TITLE="";
    public static String MARKETING_MSG_MESSAGE="";
    public static String MARKETING_MSG_IMAGE="";
    public static String MESSAGE_TYPE_IMAGE="IMAGE";
    public static String MESSAGE_TYPE_REPLY="REPLY";
    public static String MESSAGE_TYPE_TEXT="TEXT";
    public static String MESSAGE_TYPE_AUDIO="AUDIO";
    public static String MESSAGE_TYPE_VIDEO="VIDEO";
    public static String MESSAGE_TYPE_DELETED="DELETED";
    public static String MESSAGE_TYPE_DOCUMENT="DOCUMENT";
    public static String MESSAGE_TYPE_STICKER="STICKER";
    public static String MESSAGE_TYPE_LOCATION="MESSAGE_LOCATION";
    public static String MESSAGE_TYPE_CONTACT="MESSAGE_CONTACT";
    public static String MESSAGE_TYPE_TRANSLATED="MESSAGE_TRANSLATION";
    public static String MESSAGE_TYPE_POST="MESSAGE_POST";
    public static String MESSAGE_TYPE_STORY="MESSAGE_STORY";
    public static DatabaseReference M_DATABASE= FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
}
