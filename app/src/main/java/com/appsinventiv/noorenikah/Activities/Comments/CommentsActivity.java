package com.appsinventiv.noorenikah.Activities.Comments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.CompleteProfileScreen;
import com.appsinventiv.noorenikah.Activities.LoginScreen;
import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Adapters.CommentsAdapter;
import com.appsinventiv.noorenikah.Models.CommentsModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.KeyboardUtils;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    RecyclerView recycler;
    EditText comment;
    ImageView send;
    AdView adView;
    DatabaseReference mDatabase;
    String id;
    private String cmnt;
    private List<CommentsModel> itemList = new ArrayList<>();
    CommentsAdapter adapter;
    private User otherUser;
    private AdView mAdView;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Comments");
        adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.adView);
        recycler = findViewById(R.id.recycler);
        send = findViewById(R.id.send);
        comment = findViewById(R.id.comment);
        mDatabase = Constants.M_DATABASE;
        adapter = new CommentsAdapter(this, itemList);
        recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recycler.setAdapter(adapter);
        id = getIntent().getStringExtra("id");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                recycler.scrollToPosition(itemList.size() - 1);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getText().length() == 0) {
                    comment.setError("Enter comment");
                } else {
                    cmnt = comment.getText().toString();
                    comment.setText("");
                    sendCommentToDb();
                }
            }
        });
        getDataFromDB();
        getUserFromDB();
    }

    private void getUserFromDB() {
        mDatabase.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherUser = dataSnapshot.getValue(User.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataFromDB() {
        mDatabase.child("Comments").child(id).limitToLast(150).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentsModel model = snapshot.getValue(CommentsModel.class);
                    itemList.add(model);
                }
                adapter.setItemList(itemList);
                recycler.scrollToPosition(itemList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendCommentToDb() {
        String key = "" + System.currentTimeMillis();
        CommentsModel model = new CommentsModel(
                key,
                cmnt,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                System.currentTimeMillis()
        );
        mDatabase.child("Comments").child(id).child(key).setValue(model);
        sendNotification();
    }

    private void sendNotification() {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New comment posted by " + SharedPrefs.getUser().getName();
        String NotificationMessage = "Comment: " + cmnt;
        notificationAsync.execute(
                "ali",
                otherUser.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                id,
                "comment");
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