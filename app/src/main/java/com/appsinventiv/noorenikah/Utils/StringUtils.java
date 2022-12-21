package com.appsinventiv.noorenikah.Utils;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ZaidAs on 7/24/2017.
 */

public class StringUtils {

    private static Gson gson;

    // Method to get Gson Library to parse JSON
    public static Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

    // Method to check either string is null or Empty
    public static boolean isNullOrEmpty(String str) {
        if (str == null || str.isEmpty())
            return true;

        return false;
    }

    public static boolean isAllSpaces(String str) {
        if (str.trim().length()==0)
            return true;

        return false;
    }

    public static boolean passwordMatches(String password, String confirmPassword) {
        if (password.equals(confirmPassword))
            return true;
        return false;
    }

    public static boolean passwordLength(String password) {
        if (password.length() < 3 || password.length() > 15)
            return false;
        return true;
    }

    public static boolean birthdayFormat(String birthday) {
        return (birthday.equals("0 0 0"));
    }

    // Method to get string defined in Resources.
    public static String getStringFromResource(int id) {
        String str = ApplicationClass.getInstance().getResources().getString(id);
        if (str == null)
            str = "";
        return str;
    }

    public static String getMD5String(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
//                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
