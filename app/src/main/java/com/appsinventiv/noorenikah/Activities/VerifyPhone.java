package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.ReferralCodePaidModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VerifyPhone extends AppCompatActivity {

    Button verify;
    PhoneAuthProvider phoneAuth;
    Pinview pin;
    String phoneNumber;
    TextView number;
    TextView change, changen;
    Button validate;
    TextView sendAgain;
    private String smsCode;
    DatabaseReference mDatabase;
    private String mVerificationId;
    private HashMap<String, User> usersMap = new HashMap<>();

    RelativeLayout wholeLayout;
    String name, password, referralCode;
    private String myReferralCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        getSupportActionBar().setElevation(0);
        this.setTitle("Verify Phone");
//        verify = findViewById(R.id.verify);
        pin = findViewById(R.id.pinview);
        number = findViewById(R.id.number);
        change = findViewById(R.id.change);
        sendAgain = findViewById(R.id.sendAgain);
        changen = findViewById(R.id.changen);
        validate = findViewById(R.id.validate);
        wholeLayout = findViewById(R.id.wholeLayout);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        phoneNumber = getIntent().getStringExtra("number");
        name = getIntent().getStringExtra("name");
        password = getIntent().getStringExtra("password");
        referralCode = getIntent().getStringExtra("referralCode");
        number.setText(phoneNumber);


        getUsersFromDB();

        sendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        requestCode();
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkUser(); //test

                if (!pin.getValue().equalsIgnoreCase("")) {
                    wholeLayout.setVisibility(View.VISIBLE);
                    PhoneAuthCredential provider = PhoneAuthProvider.getCredential(mVerificationId, pin.getValue());
                    final FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithCredential(provider).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
//                        CommonUtils.showToast("" + authResult);
                            wholeLayout.setVisibility(View.GONE);
                            CommonUtils.showToast("Successfully verified");
                            checkUser();
//                            SharedPrefs.setPhone(phoneNumber);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            CommonUtils.showToast(e.getMessage());
                            wholeLayout.setVisibility(View.GONE);


                            CommonUtils.showToast("Invalid Pin");
                        }
                    });
                } else {
                    CommonUtils.showToast("Enter pin");
                }


//
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        changen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        pin.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                //Make api calls here or what not
//                CommonUtils.showToast(pinview.getValue());
            }
        });

    }

    private void checkUser() {
        String ph = phoneNumber.substring(phoneNumber.length() - 10);
        if (usersMap.containsKey(ph)) {
            SharedPrefs.setUser(usersMap.get(ph));
            Intent i;

            if (usersMap.get(ph).getLivePicPath() == null) {
                i = new Intent(VerifyPhone.this, CompleteProfileScreen.class);
            } else {
                i = new Intent(VerifyPhone.this, MainActivity.class);

            }
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            CommonUtils.showToast("Logged in successfully");
            finish();
        } else {
            myReferralCode = CommonUtils.getRandomCode(7);
            User user = new User(
                    name,
                    ph,
                    password, referralCode, myReferralCode);
            if (referralCode.length() > 0) {
                ReferralCodePaidModel codePaid = new ReferralCodePaidModel(ph, referralCode, false);
                mDatabase.child("ReferralCodesHistory")
                        .child(referralCode)
                        .child(ph)
                        .setValue(codePaid);
            }
            mDatabase.child("Users").child(ph).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    CommonUtils.showToast("Successfully registered");
                    SharedPrefs.setUser(user);
                    Intent i = new Intent(VerifyPhone.this, CompleteProfileScreen.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }
            });
        }

    }

    private void getUsersFromDB() {
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        usersMap.put(user.getPhone(), user);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requestCode() {

        phoneAuth = PhoneAuthProvider.getInstance();

        phoneAuth.verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        smsCode = phoneAuthCredential.getSmsCode();
                        if (phoneAuthCredential.getSmsCode() != null) {
                            pin.setValue(phoneAuthCredential.getSmsCode());
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        CommonUtils.showToast(e.getMessage());

                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        CommonUtils.showToast("Code sent");
                        mVerificationId = verificationId;
                        // Save verification ID and resending token so we can use them later


                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        CommonUtils.showToast("Time out");
//                            sendCode.setText("Resend");
//                            progress.setVisibility(View.GONE);
                        finish();

                    }
                }
        );
    }


}