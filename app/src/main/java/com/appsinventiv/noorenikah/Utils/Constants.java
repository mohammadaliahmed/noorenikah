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
    public static DatabaseReference M_DATABASE= FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
}
