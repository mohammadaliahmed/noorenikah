package com.appsinventiv.noorenikah.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.appsinventiv.noorenikah.ContactsFragment;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.fragments.ChatFragment;
import com.appsinventiv.noorenikah.fragments.HomeFragment;
import com.appsinventiv.noorenikah.fragments.MenuFragment;
import com.appsinventiv.noorenikah.fragments.RequestsFragment;
import com.appsinventiv.noorenikah.fragments.SearchFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        String myReferralCode = CommonUtils.getRandomCode(7);

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
        getUserFromDb();
        buy = findViewById(R.id.buy);
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                intent.putExtra("price", "5.00");
                startActivity(intent);
            }
        });

    }

    public static void setChatCount() {
        navigation.getOrCreateBadge(R.id.navigation_chat).setNumber(1);
    }

    private void getUserFromDb() {
        mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                SharedPrefs.setUser(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                case R.id.navigation_search:
                    fragment = new SearchFragment();
                    loadFragment(fragment);

                    return true;
                case R.id.navigation_menu:
                    fragment = new MenuFragment();
                    loadFragment(fragment);

                    return true;
                case R.id.navigation_chat:
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
                    String token = task.getResult();
                    SharedPrefs.setFcmKey(token);
                    mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("fcmKey").setValue(token);

                }
            });
        }
    }
}