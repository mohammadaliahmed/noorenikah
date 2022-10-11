package com.appsinventiv.noorenikah.Activities.Posts;

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

import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Models.PostModel;
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

public class AddPost extends AppCompatActivity {
    ImageView pickedImage;
    Button addImage, submit;
    EditText text;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private ArrayList<String> mSelected = new ArrayList<>();
    private String imageUrl;
    private String livePicPath;
    ProgressBar progress;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Add Post");
        mDatabase = Constants.M_DATABASE;
        progress = findViewById(R.id.progress);
        pickedImage = findViewById(R.id.pickedImage);
        addImage = findViewById(R.id.addImage);
        submit = findViewById(R.id.submit);
        text = findViewById(R.id.text);

        addImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Options options = Options.init()
                        .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
                        .setCount(1)
                        .setExcludeVideos(true)

                        .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                        ;                                       //Custom Path For media Storage

                Pix.start(AddPost.this, options);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUrl != null) {
                    uploadPicture();
                } else {
                    if (text.getText().length() == 0) {
                        text.setText("Enter Text");
                    } else {
                        uploadPost();
                    }
                }

            }
        });
    }


    private void uploadPost() {
        String key = "" + System.currentTimeMillis();
        PostModel model = new PostModel(
                key,
                imageUrl == null ? "text" : "image",
                imageUrl == null ? "" : livePicPath,
                text.getText().toString(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath(),
                0, 0, System.currentTimeMillis(), true
        );

        mDatabase.child("Posts").child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent i = new Intent(AddPost.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            CompressImage image = new CompressImage(AddPost.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(AddPost.this).load(mSelected.get(0)).into(pickedImage);

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
                                        uploadPost();


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


}