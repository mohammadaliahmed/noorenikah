package com.appsinventiv.noorenikah.Activities.MatchMaker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.MatchMakerModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.CompressImage;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class RegisterMatchMaker extends AppCompatActivity {

    ProgressBar progress;
    private static final int REQUEST_CODE_CHOOSE = 23;
    EditText name, mbName, experience, city, contactNo;
    Button register,picPicture;
    ImageView pickedPicture;
    private ArrayList<String> mSelected = new ArrayList<>();
    private String imageUrl;
    DatabaseReference mDatabase;
    private String livePicPath;
    private String adminFcmkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_match_maker);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Register as Match maker");
        mDatabase = Constants.M_DATABASE;
        name = findViewById(R.id.name);
        picPicture = findViewById(R.id.picPicture);
        progress = findViewById(R.id.progress);
        pickedPicture = findViewById(R.id.pickedPicture);
        mbName = findViewById(R.id.mbName);
        experience = findViewById(R.id.experience);
        city = findViewById(R.id.city);
        contactNo = findViewById(R.id.contactNo);
        register = findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().length() == 0) {
                    name.setError("Enter name");
                } else if (mbName.getText().length() == 0) {
                    mbName.setError("Enter name");
                } else if (experience.getText().length() == 0) {
                    experience.setError("Enter experience");
                } else if (city.getText().length() == 0) {
                    city.setError("Enter city");
                } else if (contactNo.getText().length() == 0) {
                    contactNo.setError("Enter contactNo");
                } else if (imageUrl == null) {
                    CommonUtils.showToast("Please select picture");
                } else {
                    uploadPicture();
                }
            }
        });
        picPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });
        getAdminFcm();


    }

    private void uploadPicture() {
        progress.setVisibility(View.VISIBLE);
        try {
            String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

            Uri file = Uri.fromFile(new File(imageUrl));

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

            final StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

            riversRef.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get a URL to the uploaded content

                        String downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        livePicPath = "" + uri;
                                        String key = "" + SharedPrefs.getUser().getPhone();
                                        MatchMakerModel model = new MatchMakerModel(
                                                key,
                                                name.getText().toString(),
                                                mbName.getText().toString(),
                                                experience.getText().toString(),
                                                city.getText().toString(),
                                                contactNo.getText().toString(),
                                                SharedPrefs.getUser().getPhone(),
                                                livePicPath,
                                                false
                                        );
                                        mDatabase.child("MatchMakers").child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                mDatabase.child("Users")
                                                        .child(SharedPrefs.getUser().getPhone()).child("matchMakerProfile").setValue(true);
                                                sendNotification();
                                                CommonUtils.showToast("Profile Created");
                                                startActivity(new Intent(RegisterMatchMaker.this, MatchMakerProfile.class));
                                                finish();
                                            }
                                        });

                                    }
                                });


                            }
                        });


                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        // ...

                        CommonUtils.showToast("There was some error uploading pic");


                    });
        } catch (Exception e) {
        }


    }
    private void sendNotification() {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New Match Maker";
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                adminFcmkey,
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "matchmaker");
    }
    private void getAdminFcm() {
        mDatabase.child("Admin").child("fcmKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    adminFcmkey=dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initMatisse() {
        Options options = Options.init()
                .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
                .setCount(1)
                .setExcludeVideos(true)

                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                ;                                       //Custom Path For media Storage

        Pix.start(this, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            CompressImage image = new CompressImage(RegisterMatchMaker.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(RegisterMatchMaker.this).load(mSelected.get(0)).into(pickedPicture);

        }
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