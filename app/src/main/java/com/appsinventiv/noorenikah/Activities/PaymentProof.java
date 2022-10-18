package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.PaymentsModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.CompressImage;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class PaymentProof extends AppCompatActivity {


    private static final int REQUEST_CODE_CHOOSE = 23;
    Button pickPicture;
    Button submit;
    RelativeLayout wholeLayout;
    ImageView pickedPicture;
    private ArrayList<String> mSelected=new ArrayList<>();
    private String imageUrl;
    private DatabaseReference mDatabase;
    private String adminFcmkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_proof);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Payment");
        wholeLayout=findViewById(R.id.wholeLayout);
        submit=findViewById(R.id.submit);
        pickPicture=findViewById(R.id.pickPicture);
        pickedPicture=findViewById(R.id.pickedPicture);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUrl==null){
                    CommonUtils.showToast("Please Select Image");
                }else{
                    uploadImg();
                }
            }
        });

        pickPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendNotification();

                Options options = Options.init()
                        .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
                        .setCount(1)
                        .setExcludeVideos(true)
                        .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                        ;                                       //Custom Path For media Storage

                Pix.start(PaymentProof.this, options);
            }
        });
        getAdminFcm();

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
    private void sendNotification() {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New Payment Proof";
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                adminFcmkey,
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "payment");
    }


    private void uploadImg() {
        wholeLayout.setVisibility(View.VISIBLE);
        try {
            String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

            Uri file = Uri.fromFile(new File(imageUrl));

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

            final StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

            riversRef.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get a URL to the uploaded content
                        riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        sendNotification();
                                        String livePicPath = "" + uri;
                                        String key=""+System.currentTimeMillis();
                                        PaymentsModel model=new PaymentsModel(
                                                key,
                                                SharedPrefs.getUser().getName()
                                                ,SharedPrefs.getUser().getPhone(),
                                                livePicPath,System.currentTimeMillis()
                                        );
                                        mDatabase.child("Payments")
                                                .child(SharedPrefs.getUser().getPhone())
                                                .child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        wholeLayout.setVisibility(View.GONE);
                                                        CommonUtils.showToast("Proof submitted. Please wait for approval");
                                                        startActivity(new Intent(PaymentProof.this,PaymentsHistory.class));

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
            mDatabase.child("Errors").child("mainError").child(mDatabase.push().getKey()).setValue(e.getMessage());
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

            CompressImage image = new CompressImage(PaymentProof.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(PaymentProof.this).load(mSelected.get(0)).into(pickedPicture);

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