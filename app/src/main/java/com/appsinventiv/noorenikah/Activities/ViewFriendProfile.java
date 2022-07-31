package com.appsinventiv.noorenikah.Activities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ViewFriendProfile extends AppCompatActivity {

    CircleImageView image;
    TextView name, age, city, maritalStatus, education, cast, jobOrBusiness;
    private String phone;
    private DatabaseReference mDatabase;
    private User user;
    ImageView chat;

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
        age = findViewById(R.id.age);
        image = findViewById(R.id.image);
        chat = findViewById(R.id.chat);
        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        maritalStatus = findViewById(R.id.maritalStatus);
        education = findViewById(R.id.education);
        cast = findViewById(R.id.cast);
        jobOrBusiness = findViewById(R.id.jobOrBusiness);
//        user = (User) getIntent().getSerializableExtra("user");
        getDataFromDB();
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(ViewFriendProfile.this,ChatScreen.class);
                i.putExtra("phone",user.getPhone());

                startActivity(i);
            }
        });

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
                            .into(image);

                    age.setText("" + user.getAge());
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