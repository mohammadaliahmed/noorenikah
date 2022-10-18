package com.appsinventiv.noorenikah.Activities.MatchMaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.CreatedProfilesAdapter;
import com.appsinventiv.noorenikah.Models.MatchMakerModel;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchMakerProfile extends AppCompatActivity {

    DatabaseReference mDatabase;
    Button createProfile;
    ImageView image;
    TextView mbName;
    RecyclerView recyclerView;
    private List<NewUserModel> itemList = new ArrayList<>();
    CreatedProfilesAdapter adapter;
    TextView pendingApproval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_maker_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Match Maker Profile");
        mDatabase = Constants.M_DATABASE;
        createProfile = findViewById(R.id.createProfile);
        image = findViewById(R.id.image);
        pendingApproval = findViewById(R.id.pendingApproval);
        recyclerView = findViewById(R.id.recycler);
        mbName = findViewById(R.id.mbName);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CreatedProfilesAdapter(this, itemList);
        recyclerView.setAdapter(adapter);


        createProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MatchMakerProfile.this, CreateProfile.class));
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        getMbData();
    }

    private void getMbData() {
        mDatabase.child("MatchMakers").child(SharedPrefs.getUser().getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                MatchMakerModel user = dataSnapshot.getValue(MatchMakerModel.class);
                try {
                    Glide.with(MatchMakerProfile.this).load(user.getPicUrl()).into(image);
                } catch (Exception e) {

                }
                if (user.isApproved()) {
                    pendingApproval.setVisibility(View.GONE);
                    createProfile.setEnabled(true);

                } else {
                    pendingApproval.setVisibility(View.VISIBLE);
                    createProfile.setEnabled(false);
                    createProfile.setBackground(getResources().getDrawable(R.drawable.grey_bg));

                }
                mbName.setText(user.getMbName());
                if (user.getProfilesCreated() != null) {
                    for (Map.Entry<String, Object> entry : user.getProfilesCreated().entrySet()) {
                        String key = entry.getKey();
                        getUserFromDB(key);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserFromDB(String key) {
        mDatabase.child("Users").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                NewUserModel userModel = dataSnapshot.getValue(NewUserModel.class);
                if (userModel != null) {
                    itemList.add(userModel);
                    adapter.setItemList(itemList);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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