package com.appsinventiv.noorenikah.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.appsinventiv.noorenikah.Adapters.ViewPagerAdapter;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.Utils.VerticalViewPager;
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
    private List<User> usersList = new ArrayList<>();
    HashMap<String,User> userMap=new HashMap<>();

    //    UsersRecyclerAdapter adapter;
    private AdView mAdView;
    private AdRequest adRequest;
    private InterstitialAd interstitialAda;
    private HashMap<String, String> requestSentMap = new HashMap<>();
    ProgressBar progress;


    VerticalViewPager viewpager;
    private ViewPagerAdapter adapter;
    int currentPage = 0;
    private ArrayList<String> requestedList=new ArrayList<>();
    int dataLimit=10;
    List<String> seenList=new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        adRequest = new AdRequest.Builder().build();
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mAdView = rootView.findViewById(R.id.adView);
        progress = rootView.findViewById(R.id.progress);
        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
        viewpager = rootView.findViewById(R.id.viewpager);


        adapter = new ViewPagerAdapter(getContext(), new ArrayList<>(), new ViewPagerAdapter.UsersAdapterCallbacks() {
            @Override
            public void onLikeClicked(User user) {

            }

            @Override
            public void onRequestClicked(User user) {
                sendNotification(user);
            }

            @Override
            public void onShown(User user) {
                seenList.add(user.getPhone());
            }
        });
        viewpager.setAdapter(adapter);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
//                seenList.add(new ArrayList<>(userMap.values()).get(position).getPhone());
                if(currentPage==userMap.size()-2){
                    dataLimit+=10;
                    getMoreData();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mDatabase = Constants.M_DATABASE;
        getFristDataFromDB();
        LoadInterstritial();

        return rootView;
    }

    private void getFristDataFromDB() {
        mDatabase.child("Users")
                .limitToLast(dataLimit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            userMap.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null && user.getName() != null &&
                                        !user.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                                    if (user.getEducation() != null) {
                                        userMap.put(user.getPhone(),user);
                                    }

                                }
                            }
                            usersList=new ArrayList<>(userMap.values());

                            Collections.shuffle(usersList);
                            progress.setVisibility(View.GONE);

                            adapter.setUserList(usersList);
                            getRequestSent();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getMoreData() {
        mDatabase.child("Users")
                .limitToLast(dataLimit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && user.getName() != null &&
                                    !user.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                                if (user.getEducation() != null) {
                                    userMap.put(user.getPhone(),user);
                                }
                            }
                        }
//                        Collections.shuffle(new ArrayList<>());
                        progress.setVisibility(View.GONE);

                        for(String item:seenList){
                            userMap.remove(item);
                        }
                        usersList=new ArrayList<>(userMap.values());
                        Collections.shuffle(usersList);
                        adapter.setData(usersList,requestedList);
//                        CommonUtils.showToast("data is here");

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

    private void sendNotification(User user) {

        showInterstitial();
        NotificationAsync notificationAsync = new NotificationAsync(getContext());
        String NotificationTitle = "New request from: " + user.getName();
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
                NotificationMessage, "request", user.getLivePicPath(), SharedPrefs.getUser().getPhone(), System.currentTimeMillis());
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