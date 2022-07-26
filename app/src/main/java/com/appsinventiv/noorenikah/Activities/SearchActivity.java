package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
    private List<User> usersList = new ArrayList<>();
    ProgressBar progress;
    TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        this.setTitle("Search results");

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
        adapter = new UsersRecyclerAdapter(this, usersList, new UsersRecyclerAdapter.UsersAdapterCallbacks() {
            @Override
            public void onLikeClicked(User user) {

            }

            @Override
            public void onRequestClicked(User user) {
                sendNotification(user);
            }
        });
        recycler.setAdapter(adapter);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress.setVisibility(View.GONE);
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getName() != null) {
                            String myGender = SharedPrefs.getUser().getGender();
                            if (!myGender.equals(user.getGender())) {
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
                                )
                                    usersList.add(user);
                            }
                        }
                    }
                    if (usersList.size() > 0) {
                        adapter.setUserList(usersList);
                    } else {
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

    private void sendNotification(User user) {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New request";
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                user.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "");
        mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone())
                .child("sent").child(user.getPhone()).setValue(user.getPhone());
        mDatabase.child("Requests").child(user.getPhone()).child("received")
                .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
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