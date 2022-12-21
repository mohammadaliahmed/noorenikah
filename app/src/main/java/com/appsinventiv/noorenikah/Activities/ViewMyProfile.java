package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.UserPostsAdapter;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMyProfile extends AppCompatActivity {

    CircleImageView image;
    TextView name, city, maritalStatus, education, cast, jobOrBusiness, about;
    private UserModel user;
    DatabaseReference mDatabase;
    RecyclerView recycler;
    private List<PostModel> itemList = new ArrayList<>();
    UserPostsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_request_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("My Profile");
        user=SharedPrefs.getUser();
        recycler = findViewById(R.id.recycler);
        about = findViewById(R.id.about);
        image = findViewById(R.id.image);

        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        maritalStatus = findViewById(R.id.maritalStatus);
        education = findViewById(R.id.education);
        cast = findViewById(R.id.cast);
        jobOrBusiness = findViewById(R.id.jobOrBusiness);
        mDatabase = Constants.M_DATABASE;

        adapter = new UserPostsAdapter(this, itemList);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        recycler.setAdapter(adapter);
        showUserData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPostsFromDB();

    }

    private void getPostsFromDB() {
        itemList.clear();
        mDatabase.child("PostsByUsers").child(SharedPrefs.getUser().getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
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
            case R.id.action_edit:
                if (user != null) {
                    Intent i = new Intent(ViewMyProfile.this, EditProfile.class);
                    startActivity(i);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

}