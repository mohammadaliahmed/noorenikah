package com.appsinventiv.noorenikah.Utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class DynamicPermission {

    private Activity mContext;
    private List<String> mPermissionList;
    public final int TAG_PERMISSION = 11;


    public DynamicPermission(Activity mContext, List<String> permissionList) {
        this.mContext = mContext;
        this.mPermissionList = permissionList;
    }


    public boolean isCompatibleOS() {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkAndRequestPermissions() {
        if (isCompatibleOS()) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String permission :
                    mPermissionList) {
                if (!isPermissionGranted(permission)) {
                    listPermissionsNeeded.add(permission);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(mContext,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), TAG_PERMISSION);
                return false;
            }
        }
        return true;
    }

    public boolean checkFragmentAndRequestPermissions(Fragment fragment) {
        if (isCompatibleOS()) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String permission :
                    mPermissionList) {
                if (!isPermissionGranted(permission)) {
                    listPermissionsNeeded.add(permission);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                fragment.requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), TAG_PERMISSION);
                return false;
            }
        }
        return true;
    }

    public boolean isPermissionGranted(String permission) {
        int permissionCAMERA = ContextCompat.checkSelfPermission(mContext,
                permission);
        if (permissionCAMERA != PackageManager.PERMISSION_GRANTED)
            return false;
        else return true;
    }

    public void requestSinglePermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                permission)) {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{permission},
                    TAG_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{permission},
                    TAG_PERMISSION);
        }
    }

}
