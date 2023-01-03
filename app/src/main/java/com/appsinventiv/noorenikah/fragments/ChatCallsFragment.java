package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.appsinventiv.noorenikah.Adapters.SimpleFragmentPagerAdapter;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

public class ChatCallsFragment extends Fragment {
    private TabLayout tabLayout;
    ViewPager viewPager;
    SimpleFragmentPagerAdapter adapter;
    private View rootView;
    ImageView promotionBanner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chat_call_fragment, container, false);
        tabLayout = rootView.findViewById(R.id.sliding_tabs);
        viewPager = rootView.findViewById(R.id.viewpager);
        adapter = new SimpleFragmentPagerAdapter(getContext(), getChildFragmentManager());
        promotionBanner=rootView.findViewById(R.id.promotionBanner);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        if (SharedPrefs.getPromotionalBanner("chatScreen") != null) {
            Glide.with(getContext()).load(SharedPrefs.getPromotionalBanner("chatScreen").getImgUrl())
                    .into(promotionBanner);

        }
        promotionBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(SharedPrefs.getPromotionalBanner("chatScreen").getUrl()));
                getContext().startActivity(i);
            }
        });


        return rootView;
    }
}
