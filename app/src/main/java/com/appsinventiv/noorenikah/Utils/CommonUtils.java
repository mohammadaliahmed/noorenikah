package com.appsinventiv.noorenikah.Utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;

import java.security.SecureRandom;
import java.util.Calendar;

/**
 * Created by AliAh on 29/03/2018.
 */

public class CommonUtils {

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint("WrongConstant")
            public void run() {
                Toast.makeText(ApplicationClass.getInstance().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getUserShareId(String phone) {
        String abc = "N,Q,ex,R,V,wU,T,kxw,R,,P,T,N,M,L,K,J,I,H,G,F,Z,e,C,B,A";
        String[] abcArr = abc.split(",");
        String[] foneArr = phone.split("");
        String newProfileId = "";
        for (int i = 0; i < foneArr.length; i++) {
            newProfileId = newProfileId + foneArr[i] + abcArr[i];
        }
        return newProfileId;
    }

    public static String cleanUserId(String id) {
        return id.replaceAll("[^\\d.]", "");
    }

    public static String getRandomCode(int len) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private static final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";


    public static String getFormattedDate(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "dd MMM ";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "" + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Yesterday ";
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("dd MMM , h:mm aa", smsTime).toString();
        }
    }

    public static String getFormattedDateOnly(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        return DateFormat.format("dd MMM, h:mm aa", smsTime).toString();

    }  public static String getFormattedDateOnlyy(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        return DateFormat.format("dd MMM y, h:mm:s aa", smsTime).toString();

    }

    public static String getDuration(long seconds) {
        seconds = (seconds / 1000);
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d", m, s);
    }


    public static void sendCustomerStatus(String online) {
        if (SharedPrefs.getUser() != null) {
            DatabaseReference mDatabase = Constants.M_DATABASE;
            mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("lastLoginTime").setValue(online);
        }
    }
}
