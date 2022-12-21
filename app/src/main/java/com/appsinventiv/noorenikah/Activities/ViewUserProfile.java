package com.appsinventiv.noorenikah.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.UserPostsAdapter;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewUserProfile extends AppCompatActivity {

    CircleImageView image;
    TextView name, city, maritalStatus, education, cast, jobOrBusiness, about;
    private UserModel user;
    private String profileIdFromLink;
    private String profileId;


    DatabaseReference mDatabase;
    Button sendRequest, requestSent, friend;
    private HashMap<String, String> requestSentMap = new HashMap<>();
    RecyclerView recycler;
    private List<PostModel> itemList = new ArrayList<>();
    UserPostsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
//        this.setTitle("View Profile");
        recycler = findViewById(R.id.recycler);
        about = findViewById(R.id.about);
        image = findViewById(R.id.image);
        requestSent = findViewById(R.id.requestSent);
        friend = findViewById(R.id.friend);
        sendRequest = findViewById(R.id.sendRequest);
        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        maritalStatus = findViewById(R.id.maritalStatus);
        education = findViewById(R.id.education);
        cast = findViewById(R.id.cast);
        jobOrBusiness = findViewById(R.id.jobOrBusiness);
        mDatabase = Constants.M_DATABASE;
        profileId = getIntent().getStringExtra("phone");
        if (profileId != null) {
            getUserDataFromDB(profileId);
        }
        adapter = new UserPostsAdapter(this, itemList);
        onNewIntent(getIntent());
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSent.setVisibility(View.VISIBLE);
                sendRequest.setVisibility(View.GONE);
                sendNotification(user);
            }
        });
        getRequestSent();
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        recycler.setAdapter(adapter);
        getPostsFromDB();

    }

    private void getPostsFromDB() {
        itemList.clear();
        mDatabase.child("PostsByUsers").child(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String postId = snapshot.getValue(String.class);
                        getPostFromDb(postId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostFromDb(String postId) {
        mDatabase.child("Posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PostModel model = dataSnapshot.getValue(PostModel.class);
                if (model != null && model.getId() != null) {
                    itemList.add(model);
                }
                Collections.reverse(itemList);
                adapter.setItemList(itemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendNotification(UserModel user) {

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
                NotificationMessage, "request", user.getLivePicPath(), SharedPrefs.getUser().getPhone(), System.currentTimeMillis());
        mDatabase.child("Notifications").child(user.getPhone()).child(key).setValue(model);

    }

    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            profileIdFromLink = data.substring(data.lastIndexOf("/") + 1);
            profileId = profileIdFromLink.replace("profile?id=", "");
            getUserDataFromDB(CommonUtils.cleanUserId(profileId));
            getRequestSent();
        }

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
                        ArrayList<String> requestedList = new ArrayList<>(requestSentMap.values());
                        if (requestedList.size() > 0 && requestedList.contains(user.getPhone())) {
                            requestSent.setVisibility(View.VISIBLE);
                        } else {
                            sendRequest.setVisibility(View.VISIBLE);
                        }

                        if (SharedPrefs.getUser().getFriends() != null && SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
                            friend.setVisibility(View.VISIBLE);
                            sendRequest.setVisibility(View.GONE);
                            requestSent.setVisibility(View.GONE);


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getUserDataFromDB(String profileId) {
        mDatabase.child("Users").child(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    user = dataSnapshot.getValue(UserModel.class);
                    if (user != null) {
                        ViewUserProfile.this.setTitle(user.getName());
                        showUserData();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showUserData() {
        Glide.with(this)
                .load(user.getLivePicPath())
                .placeholder(R.drawable.picked)
                .into(image);

        name.setText("" + user.getName());
        city.setText("" + user.getCity());
        maritalStatus.setText("" + user.getMaritalStatus());
        education.setText("" + user.getEducation());
        cast.setText("" + user.getCast());
        jobOrBusiness.setText("" + user.getJobOrBusiness());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_chat:
                if (user != null) {
                    Intent i = new Intent(ViewUserProfile.this, ChatScreen.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("phone", user.getPhone());
                    startActivity(i);
                }
                return true;
            case R.id.action_block:
                if (user != null) {
                    showBlockALert();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showBlockALert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Take Action on this user");
        builder.setPositiveButton("Block", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("iBlocked")
                        .child(profileId).setValue(profileId);
                mDatabase.child("Users").child(profileId).child("blockedMe")
                        .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());

                CommonUtils.showToast("You have blocked this user");
                finish();

            }
        });
        builder.setNegativeButton("Report", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showReportAlert();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReportAlert() {
        final String[] reason = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a reason for report");

// add a radio button list
        String[] reasonList = {
                "It's Spam",
                "Hate Speech",
                "I don't like it",
                "False information",
                "Bully or harassment",
                "False information",
                "Scam or fraud",
                "Violence or Dangerous",
                "Scam or fraud",
                "Sexual or nudity",
                "Something else",

        };
        builder.setSingleChoiceItems(reasonList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                reason[0] = reasonList[which];
            }
        });

// add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                HashMap<String, String> map = new HashMap<>();
                map.put("reason", reason[0]);
                map.put("phone", SharedPrefs.getUser().getPhone());
                mDatabase.child("PostReports").child(profileId)
                        .child(SharedPrefs.getUser().getPhone()).setValue(map);
                mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("iBlocked")
                        .child(profileId).setValue(profileId);
                mDatabase.child("Users").child(profileId).child("blockedMe")
                        .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
                CommonUtils.showToast("User reported\nThanks for feedback\nWe will take action");
                finish();
            }
        });
        builder.setNegativeButton("Cancel", null);

// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

}