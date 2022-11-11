package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.AlertsUtils;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    EditText phone, password;
    Button login, register;

    DatabaseReference mDatabase;

    TextView textt;
    CheckBox checkit;
    boolean checked = false;
    ProgressBar progress;
    ImageView google;
    GoogleApiClient apiClient;
    public static GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        google = findViewById(R.id.google);
        textt = findViewById(R.id.textt);
        checkit = findViewById(R.id.checkit);

        register = findViewById(R.id.register);
        progress = findViewById(R.id.progress);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        mDatabase = Constants.M_DATABASE;

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, Register.class));
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phone.getText().length() == 0) {
                    phone.setError("Enter Phone");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter Password");
                } else if (!checked) {
                    CommonUtils.showToast("Please accept terms and conditions");
                } else if (phone.getText().length() < 7 && phone.getText().length() > 15) {
                    CommonUtils.showToast("Please enter correct phone number");
                } else {
                    String ph = phone.getText().toString().substring(phone.length() - 10);
                    loginNow(ph, password.getText().toString());
                }
            }
        });

        AlertsUtils.customTextView(LoginScreen.this, textt);
        checkit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    checked = true;
                }
            }
        });
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        apiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                signin();
            }
        });
    }

    private void signin() {
        Intent i = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        startActivityForResult(i, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(googleSignInResult);
        }

    }

    private void handleResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()) {
            account = googleSignInResult.getSignInAccount();
            String userId = account.getId();

            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getPhone() != null) {
                            if (!user.isRejected()) {
                                SharedPrefs.setUser(user);
                                startActivity(new Intent(LoginScreen.this, MainActivity.class));
                                CommonUtils.showToast("Login Successful");
                                finish();
                            } else {
                                CommonUtils.showToast("Your account is disabled. You cannot login");
                            }

                        }


                    } else {
                        CommonUtils.showToast("Signin Successful\nPlease complete your profile");
                        Intent i = new Intent(LoginScreen.this, SocialRegister.class);
                        i.putExtra("userId", userId);
                        i.putExtra("name", account.getDisplayName());
                        i.putExtra("userId", userId);
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {

                }
            });


        }
    }

    private void loginNow(String phone, String pass) {
        progress.setVisibility(View.VISIBLE);
        mDatabase.child("Users").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress.setVisibility(View.INVISIBLE);
                if (dataSnapshot.getValue() != null) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getPhone() != null) {
                        if (user.getPassword().equals(pass)) {
                            if (!user.isRejected()) {
                                SharedPrefs.setUser(user);
                                if (user.getLivePicPath() == null) {
                                    startActivity(new Intent(LoginScreen.this, CompleteProfileScreen.class));

                                } else {
                                    startActivity(new Intent(LoginScreen.this, MainActivity.class));
                                }
                                CommonUtils.showToast("Login Successful");
                                finish();
                            } else {
                                CommonUtils.showToast("Your account is disabled. You cannot login");
                            }
                        } else {
                            CommonUtils.showToast("Wrong Password");
                        }
                    }
                } else {
                    CommonUtils.showToast("Account does not exist\n Please register");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}