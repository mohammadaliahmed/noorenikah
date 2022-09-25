package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendProfile extends AppCompatActivity {

    CircleImageView image;
    TextView name, city, maritalStatus, education, cast, jobOrBusiness, about;
    private String phone;
    private DatabaseReference mDatabase;
    private User user;

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
        phone = getIntent().getStringExtra("phone");
        image = findViewById(R.id.image);
        name = findViewById(R.id.name);
        about = findViewById(R.id.about);
        city = findViewById(R.id.city);
        maritalStatus = findViewById(R.id.maritalStatus);
        education = findViewById(R.id.education);
        cast = findViewById(R.id.cast);
        jobOrBusiness = findViewById(R.id.jobOrBusiness);
//        user = (User) getIntent().getSerializableExtra("user");
        getDataFromDB();


    }

    private void getDataFromDB() {
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        mDatabase.child("Users").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    user = dataSnapshot.getValue(User.class);

                    Glide.with(ViewFriendProfile.this)
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_chat:
                Intent i = new Intent(ViewFriendProfile.this, ChatScreen.class);
                i.putExtra("phone", user.getPhone());

                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


}