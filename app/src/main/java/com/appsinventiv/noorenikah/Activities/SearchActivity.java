package com.appsinventiv.noorenikah.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.SearchUsersRecyclerAdapter;
import com.appsinventiv.noorenikah.Adapters.UsersRecyclerAdapter;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    int minAge = 18, maxAge = 50;
    String city = "Lahore", gender = "male";
    String maritalStatus = "";
    private DatabaseReference mDatabase;
    RecyclerView recycler;
    private SearchUsersRecyclerAdapter adapter;
    private List<UserModel> usersList = new ArrayList<>();
    ProgressBar progress;
    TextView noData;

    private AdView mAdView;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private AdRequest adRequest;
    private InterstitialAd interstitialAda;
    private HashMap<String, String> requestSentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        this.setTitle("Search results");
        adRequest = new AdRequest.Builder().build();
        minAge = getIntent().getIntExtra("minAge", 18);

        maxAge = getIntent().getIntExtra("maxAge", 50);
        city = getIntent().getStringExtra("city");
        gender = getIntent().getStringExtra("gender");
        maritalStatus = getIntent().getStringExtra("maritalStatus");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        noData = findViewById(R.id.noData);
        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter=new SearchUsersRecyclerAdapter(this, usersList, new SearchUsersRecyclerAdapter.UsersAdapterCallbacks() {
            @Override
            public void onLikeClicked(UserModel user) {
                sendLikeNotification(user);
            }

            @Override
            public void onRequestClicked(UserModel user) {
                sendNotification(user);
            }

            @Override
            public void onShown(UserModel user) {

            }
        });
        LoadInterstritial();
        recycler.setAdapter(adapter);
        mDatabase = Constants.M_DATABASE;
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress.setVisibility(View.GONE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null && user.getName() != null && user.getGender() != null &&
                            !user.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                        try {
                            if (user.getCity().toLowerCase().contains(city.toLowerCase())
                                    && user.getGender().equalsIgnoreCase(gender)
                                    && user.getMaritalStatus().toLowerCase().contains(maritalStatus.toLowerCase())) {
                                usersList.add(user);
                            }
                        } catch (Exception e) {

                        }

                    }
                }
                if (usersList.size() > 0 ) {
                    Collections.shuffle(usersList);
                    if (usersList.size() > 200) {
                        usersList = usersList.subList(0, 200);
                    }
                    adapter.setUserList(usersList);

                } else {
                    if (adapter != null) {
                        adapter.setUserList(new ArrayList<>());
                        noData.setVisibility(View.VISIBLE);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }




    private void sendLikeNotification(UserModel user) {

        showInterstitial();
        NotificationAsync notificationAsync = new NotificationAsync(this);
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

    private void sendNotification(UserModel user) {

        showInterstitial();
        NotificationAsync notificationAsync = new NotificationAsync(this);
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

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAda != null) {
            interstitialAda.show(this);
        } else {
//            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();

        }
    }

    public void LoadInterstritial() {
        InterstitialAd.load(
                this,
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);

    }
}