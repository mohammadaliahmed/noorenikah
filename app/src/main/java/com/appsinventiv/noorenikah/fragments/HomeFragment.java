package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.appsinventiv.noorenikah.Adapters.UsersRecyclerAdapter;
import com.appsinventiv.noorenikah.Adapters.ViewPagerAdapter;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.Utils.VerticalViewPager;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    private View rootView;
    RecyclerView recycler;
    DatabaseReference mDatabase;
    private List<NewUserModel> usersList = new ArrayList<>();
    HashMap<String, NewUserModel> userMap = new HashMap<>();
    private AdView mAdView;
    private AdRequest adRequest;
    private InterstitialAd interstitialAda;
    private HashMap<String, String> requestSentMap = new HashMap<>();
    ProgressBar progress;

    private UsersRecyclerAdapter adapter;
    private ArrayList<String> requestedList = new ArrayList<>();
    List<String> seenList = new ArrayList<>();
    ImageView promotionBanner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        adRequest = new AdRequest.Builder().build();
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recycler = rootView.findViewById(R.id.recycler);
        mAdView = rootView.findViewById(R.id.adView);
        promotionBanner = rootView.findViewById(R.id.promotionBanner);
        progress = rootView.findViewById(R.id.progress);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (SharedPrefs.getPromotionalBanner() != null) {
            Glide.with(getContext()).load(SharedPrefs.getPromotionalBanner().getImgUrl())
                    .into(promotionBanner);

        }
        promotionBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(SharedPrefs.getPromotionalBanner().getUrl()));
                getContext().startActivity(i);
            }
        });

        adapter = new UsersRecyclerAdapter(getContext(),
                new ArrayList<>(), new UsersRecyclerAdapter.UsersAdapterCallbacks() {
            @Override
            public void onLikeClicked(NewUserModel user) {
                sendLikeNotification(user);
            }

            @Override
            public void onRequestClicked(NewUserModel user) {
                sendNotification(user);
            }

            @Override
            public void onShown(NewUserModel user) {
                seenList.add(user.getPhone());
            }
        });
        recycler.setAdapter(adapter);
        mDatabase = Constants.M_DATABASE;
        List<NewUserModel> list = SharedPrefs.getUsersList();
        if (list != null && list.size() > 0) {
            if (list.size() > 100) {
                usersList = list.subList(0, 100);
            }
            Collections.shuffle(usersList);
            progress.setVisibility(View.GONE);
            adapter.setUserList(usersList);
            getRequestSent();
            if (!SharedPrefs.getLastTime().equalsIgnoreCase("")) {
                if (System.currentTimeMillis() >= (Long.parseLong(SharedPrefs.getLastTime()) + 864000000L)) {
                    getFristDataFromDB();
                }
            } else {
                SharedPrefs.setLastTime("" + System.currentTimeMillis());
            }

        } else {
            progress.setVisibility(View.GONE);
            getFristDataFromDB();
        }
//        testUser();

        LoadInterstritial();

        return rootView;
    }

    private void testUser() {
        mDatabase.child("Users").child("3097748424").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                NewUserModel user = dataSnapshot.getValue(NewUserModel.class);
                usersList.add(user);
                adapter.setUserList(usersList);
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFristDataFromDB() {
        mDatabase.child("Users")
                .limitToLast(1000)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
//                            CommonUtils.showToast("Here");
                            userMap.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                NewUserModel user = snapshot.getValue(NewUserModel.class);
                                if (user != null && user.getName() != null &&
                                        !user.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {

                                    userMap.put(user.getPhone(), user);


                                }
                            }
                            usersList = new ArrayList<>(userMap.values());

                            Collections.shuffle(usersList);
                            SharedPrefs.setUsersList(usersList);
                            progress.setVisibility(View.GONE);
                            if (usersList.size() > 100) {
                                usersList = usersList.subList(0, 100);
                            }

                            adapter.setUserList(usersList);
                            SharedPrefs.setLastTime("" + System.currentTimeMillis());

                            getRequestSent();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void getRequestSent() {
        mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone())
                .child("sent").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getValue(String.class);
                            requestSentMap.put(key, key);
                        }
                        requestedList = new ArrayList<>(requestSentMap.values());
                        adapter.setRequestedList(requestedList);
                        progress.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendLikeNotification(NewUserModel user) {

        showInterstitial();
        NotificationAsync notificationAsync = new NotificationAsync(getContext());
        String NotificationTitle = SharedPrefs.getUser().getName() + " liked your profile";
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                user.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "like");
        String key = "" + System.currentTimeMillis();
        NotificationModel model = new NotificationModel(key, NotificationTitle,
                NotificationMessage, "like", SharedPrefs.getUser().getLivePicPath(), SharedPrefs.getUser().getPhone(), System.currentTimeMillis());
        mDatabase.child("Notifications").child(user.getPhone()).child(key).setValue(model);

    }

    private void sendNotification(NewUserModel user) {

        showInterstitial();
        NotificationAsync notificationAsync = new NotificationAsync(getContext());
        String NotificationTitle = "New request from: " + SharedPrefs.getUser().getName();
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                user.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "request");
        mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone())
                .child("sent").child(user.getPhone()).setValue(user.getPhone());
        mDatabase.child("Requests").child(user.getPhone()).child("received")
                .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());

        String key = "" + System.currentTimeMillis();
        NotificationModel model = new NotificationModel(key, NotificationTitle,
                NotificationMessage, "request", SharedPrefs.getUser().getLivePicPath(), SharedPrefs.getUser().getPhone(), System.currentTimeMillis());
        mDatabase.child("Notifications").child(user.getPhone()).child(key).setValue(model);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public void LoadInterstritial() {
        InterstitialAd.load(
                getContext(),
                getResources().getString(R.string.interstital_ad_unit_id),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        interstitialAda = interstitialAd;


                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        interstitialAda = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        interstitialAda = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        interstitialAda = null;

                    }
                });


    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAda != null) {
            interstitialAda.show(getActivity());
        } else {
//            Toast.makeText(getContext(), "Ad did not load", Toast.LENGTH_SHORT).show();

        }
    }

}