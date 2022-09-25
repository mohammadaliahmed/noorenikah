package com.appsinventiv.noorenikah.Activities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.User;
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

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ViewRequestProfile extends AppCompatActivity {

    CircleImageView image;
    TextView name, city, maritalStatus, education, cast, jobOrBusiness, about;
    private User user;
    private String profileIdFromLink;
    private String profileId;


    DatabaseReference mDatabase;
    Button sendRequest;
    private HashMap<String, String> requestSentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("View Profile");
        about = findViewById(R.id.about);
        image = findViewById(R.id.image);
        sendRequest = findViewById(R.id.sendRequest);
        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        maritalStatus = findViewById(R.id.maritalStatus);
        education = findViewById(R.id.education);
        cast = findViewById(R.id.cast);
        jobOrBusiness = findViewById(R.id.jobOrBusiness);
        mDatabase = Constants.M_DATABASE;
        profileId = getIntent().getStringExtra("phone");
        if(profileId!=null) {
            getUserDataFromDB(profileId);
        }

        onNewIntent(getIntent());
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest.setText("Request Sent!");
                sendRequest.setTextColor(getResources().getColor(R.color.colorAccent));
                sendRequest.setBackground(getResources().getDrawable(R.drawable.btn_red_outline));
                sendNotification(user);
            }
        });

    }

    private void sendNotification(User user) {

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

                            sendRequest.setText("Request Sent!");
                            sendRequest.setTextColor(getResources().getColor(R.color.colorAccent));
                            sendRequest.setBackground(getResources().getDrawable(R.drawable.btn_red_outline));
                            sendRequest.setEnabled(false);
                        } else {
                            sendRequest.setText("Send Request");
                            sendRequest.setTextColor(getResources().getColor(R.color.colorWhite));
                            sendRequest.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                            sendRequest.setEnabled(true);

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
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
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
                .apply(bitmapTransform(new BlurTransformation(50)))
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