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

import com.appsinventiv.noorenikah.Adapters.UsersRecyclerAdapter;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
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
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    int minAge = 18, maxAge = 50;
    int maxIncome = 999999, minIncome = 0;
    float minHeight = 4, maxHeight = 7;
    String city = "";
    String jobOrBusiness = "";
    String selectedHomeType = "";
    String education = "";
    String cast = "";
    private DatabaseReference mDatabase;
    RecyclerView recycler;
    private UsersRecyclerAdapter adapter;
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

        minHeight = getIntent().getFloatExtra("minHeight", 4.0f);
        maxHeight = getIntent().getFloatExtra("maxHeight", 7.0f);
        minAge = getIntent().getIntExtra("minAge", 18);
        minIncome = getIntent().getIntExtra("minIncome", 0);
        maxIncome = getIntent().getIntExtra("maxIncome", 9999999);
        maxAge = getIntent().getIntExtra("maxAge", 50);
        city = getIntent().getStringExtra("city");
        jobOrBusiness = getIntent().getStringExtra("jobOrBusiness");
        selectedHomeType = getIntent().getStringExtra("selectedHomeType");
        education = getIntent().getStringExtra("education");
        cast = getIntent().getStringExtra("cast");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        noData = findViewById(R.id.noData);
        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
//        adapter = new UsersRecyclerAdapter(this, usersList, new UsersRecyclerAdapter.UsersAdapterCallbacks() {
//            @Override
//            public void onLikeClicked(User user) {
//
//            }
//
//            @Override
//            public void onRequestClicked(User user) {
//                sendNotification(user);
//            }
//        });
        LoadInterstritial();
        recycler.setAdapter(adapter);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress.setVisibility(View.GONE);
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null && user.getName() != null && user.getGender() != null &&
                                !user.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                            try {
                                if (

                                        user.getCity().equalsIgnoreCase(city)
                                                && user.getEducation().equalsIgnoreCase(education)
                                                && user.getHomeType().equalsIgnoreCase(selectedHomeType)
                                                && user.getJobOrBusiness().equalsIgnoreCase(jobOrBusiness)
                                                && user.getCast().equalsIgnoreCase(cast)
                                                && user.getHeight() > minHeight
                                                && user.getHeight() < maxHeight
                                                && user.getAge() > minAge
                                                && user.getAge() < maxAge
                                                && user.getIncome() > minIncome
                                                && user.getIncome() < maxIncome

                                ) {
                                    usersList.add(user);
                                }
                            } catch (Exception e) {

                            }

                        }
                    }
                    if (usersList.size() > 0) {
                        adapter.setUserList(new ArrayList<>());
                        getRequestSent();

                    } else {
                        if (adapter != null) {
                            adapter.setUserList(new ArrayList<>());
                            noData.setVisibility(View.VISIBLE);
                        }

                    }

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
                        List<String> requestedList = new ArrayList<>(requestSentMap.values());
                        adapter.setRequestedList(requestedList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendNotification(UserModel user) {
        showInterstitial();
        NotificationAsync notificationAsync = new NotificationAsync(this);
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
                NotificationMessage, "request", user.getLivePicPath(), SharedPrefs.getUser().getPhone(),
                System.currentTimeMillis());
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