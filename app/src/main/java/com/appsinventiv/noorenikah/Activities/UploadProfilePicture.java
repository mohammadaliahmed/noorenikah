package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.CompressImage;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UploadProfilePicture extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 23;
    CircleImageView pickedPicture;
    private String livePicPath;
    Button picPicture,upload;
    private ArrayList<String> mSelected = new ArrayList<>();
    private String imageUrl;
    ProgressBar progress;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile_pic);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Add Profile Photo");
        mDatabase= Constants.M_DATABASE;

        upload = findViewById(R.id.upload);
        progress = findViewById(R.id.progress);
        pickedPicture = findViewById(R.id.pickedPicture);

        picPicture = findViewById(R.id.picPicture);
        picPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUrl!=null){
                    uploadPicture();
                }
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
            CompressImage image = new CompressImage(UploadProfilePicture.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(UploadProfilePicture.this).load(mSelected.get(0)).into(pickedPicture);

        }
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
                                        mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("livePicPath").setValue(livePicPath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                User us = SharedPrefs.getUser();
                                                us.setLivePicPath(livePicPath);
                                                SharedPrefs.setUser(us);
                                                startActivity(new Intent(UploadProfilePicture.this,CompleteProfileScreen.class));
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
                        mDatabase.child("Errors").child("picUploadError").child(mDatabase.push().getKey()).setValue(exception.getMessage());

                        CommonUtils.showToast("There was some error uploading pic");


                    });
        } catch (Exception e) {
            mDatabase.child("Errors").child("mainError").child(mDatabase.push().getKey()).setValue(e.getMessage());
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