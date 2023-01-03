package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.appsinventiv.noorenikah.fragments.CallsLogsFragment;
import com.appsinventiv.noorenikah.fragments.ChatFragment;

public class SimpleFragmentPagerAdapter   extends FragmentPagerAdapter {

    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ChatFragment();
        }
        else {
            return new CallsLogsFragment();
        }


    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Calls";

            default:
                return null;
        }
    }

}
