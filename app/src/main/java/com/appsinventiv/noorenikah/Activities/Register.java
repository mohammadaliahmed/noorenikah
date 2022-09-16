package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.AlertsUtils;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    EditText phone, password, name, referralCode;
    Button register;
    TextView login;
    DatabaseReference mDatabase;
    private HashMap<String, User> usersMap = new HashMap<>();
    private CountryCodePicker ccp;
    private String foneCode;
    private String referalIdFromLink;
    private String referalId;
    TextView textt;
    CheckBox checkit;
    boolean checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        login = findViewById(R.id.login);
        textt = findViewById(R.id.textt);
        register = findViewById(R.id.register);
        referralCode = findViewById(R.id.referralCode);
        password = findViewById(R.id.password);
        checkit = findViewById(R.id.checkit);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        foneCode = "+" + ccp.getDefaultCountryCode();
        onNewIntent(getIntent());
        AlertsUtils.customTextView(Register.this,textt);

        checkit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    checked = true;
                }
            }
        });

        ccp.registerPhoneNumberTextView(phone);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (name.getText().length() == 0) {
                    name.setError("Enter Name");
                } else if (phone.getText().length() == 0) {
                    phone.setError("Enter Phone");
                } else if (phone.getText().length() < 8 || phone.getText().length() > 14) {
                    phone.setError("Enter valid phone number");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter Password");
                } else if (!checked) {
                    CommonUtils.showToast("Please accept terms and conditions");
                } else {
                    requestCode();
                }

            }
        });

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
        String ph = phone.getText().toString();
//        if (ph.startsWith("03")) {
//            ph=ph.substring(1);
//        }
        Intent i = new Intent(Register.this, VerifyPhone.class);
        i.putExtra("number", foneCode + ph);
        i.putExtra("name", name.getText().toString());
        i.putExtra("referralCode", referralCode.getText().toString());
        i.putExtra("password", password.getText().toString());
        startActivity(i);


    }

    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            referalIdFromLink = data.substring(data.lastIndexOf("/") + 1);
            referalId = referalIdFromLink.replace("refer?id=", "");
            referralCode.setText(referalId);
        }
    }



}