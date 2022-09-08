package com.appsinventiv.noorenikah.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.fragments.ChatFragment;
import com.appsinventiv.noorenikah.fragments.HomeFragment;
import com.appsinventiv.noorenikah.fragments.InviteFragment;
import com.appsinventiv.noorenikah.fragments.MenuFragment;
import com.appsinventiv.noorenikah.fragments.RequestsFragment;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    private Fragment fragment;
    public static BottomNavigationView navigation;
    Button buy;
    ImageView notifications;
    ImageView search;
    private RewardedAd mRewardedAd;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        String myReferralCode = CommonUtils.getRandomCode(7);
        search = findViewById(R.id.search);
        notifications = findViewById(R.id.notifications);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        navigation = (BottomNavigationView) findViewById(R.id.customBottomBar);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
        if (Constants.REQUEST_RECEIVED) {
            fragment = new RequestsFragment();

        } else {
            fragment = new HomeFragment();
        }

        loadFragment(fragment);
        updateFcmKey();
        buy = findViewById(R.id.buy);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NotificationHistory.class));
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchScreen.class));
            }
        });
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                intent.putExtra("price", "5.00");
                startActivity(intent);
            }
        });
        adRequest = new AdRequest.Builder().build();


//        showBadge(this,"6");
        loadRewardAd();
    }

    private void loadRewardAd() {
        RewardedAd.load(this, getResources().getString(R.string.reward_ad_unit_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        setCallbacksOfRewardAd();
                    }
                });

    }

    private void setCallbacksOfRewardAd() {
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                mRewardedAd = null;
                loadRewardAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                mRewardedAd = null;
                loadRewardAd();
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                loadRewardAd();
            }
        });
    }

    public void showReward() {
        if (mRewardedAd != null) {
            mRewardedAd.show(this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    loadRewardAd();

                }
            });
        } else {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserFromDb();

    }

    public static void showBadge(Context context, String value) {
        removeBadge();
        BottomNavigationItemView itemView = navigation.findViewById(R.id.navigation_chat);
        View badge = LayoutInflater.from(context).inflate(R.layout.layout_news_badge, navigation,
                false);

        TextView text = badge.findViewById(R.id.badge_text_view);
        text.setText("•");

        itemView.addView(badge);

    }

    public static void removeBadge() {
        BottomNavigationItemView itemView = navigation.findViewById(R.id.navigation_chat);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }
    }


    private void getUserFromDb() {
        if (SharedPrefs.getUser().getPhone() != null) {
            mDatabase.child("Users").child(SharedPrefs.getUser().getPhone())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            SharedPrefs.setUser(user);
                            if (user.isRejected()) {
                                SharedPrefs.logout();
                                Intent i = new Intent(MainActivity.this, Splash.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }


    private NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = new NavigationBarView.OnItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_invite:
                    fragment = new InviteFragment();
                    loadFragment(fragment);

                    return true;
                case R.id.navigation_menu:
                    fragment = new MenuFragment();
                    loadFragment(fragment);

                    return true;
                case R.id.navigation_chat:
                    showReward();
                    fragment = new ChatFragment();
                    loadFragment(fragment);

                    return true;
                case R.id.navigation_notification:
                    fragment = new RequestsFragment();
                    loadFragment(fragment);
                    return true;


            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateFcmKey() {
        if (SharedPrefs.getUser() != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    try {
                        String token = task.getResult();
                        SharedPrefs.setFcmKey(token);
                        mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("fcmKey").setValue(token);
                    } catch (Exception e) {

                    }
                }
            });
        }
    }
}