package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.ReferralCodePaidModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.AlertsUtils;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    RadioButton male, female;
    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
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
        AlertsUtils.customTextView(Register.this, textt);

        male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    gender = "male";
                }
            }
        });
        female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    gender = "female";
                }
            }
        });

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
                } else if (gender==null) {
                    CommonUtils.showToast("Please select gender");
                } else if (!checked) {
                    CommonUtils.showToast("Please accept terms and conditions");
                } else {
                    requestCode();
                }

            }
        });


    }

    private void requestCode() {
        String phoneNumber = foneCode + phone.getText().toString();
        SharedPrefs.setPhone(phoneNumber);
        String ph = phoneNumber.substring(phoneNumber.length() - 10);
        ph = ph.replaceAll("\\s+","");


        String myReferralCode = CommonUtils.getRandomCode(7);
        User user = new User(
                name.getText().toString(),
                ph,
                password.getText().toString(), referralCode.getText().toString(), myReferralCode,gender);
        if (referralCode.getText().length() > 0) {
            ReferralCodePaidModel codePaid = new ReferralCodePaidModel(ph, referralCode.getText().toString(), false);
            mDatabase.child("ReferralCodesHistory")
                    .child(referralCode.getText().toString())
                    .child(ph)
                    .setValue(codePaid);
        }

        mDatabase.child("Users").child(ph).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                CommonUtils.showToast("Successfully registered");
                SharedPrefs.setUser(user);
                Intent i = new Intent(Register.this, UploadProfilePicture.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });
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