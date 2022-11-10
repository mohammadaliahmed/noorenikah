package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.BuildConfig;
import com.appsinventiv.noorenikah.Models.ReferralCodePaidModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.AlertsUtils;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rilixtech.CountryCodePicker;

import java.util.HashMap;

public class SocialRegister extends AppCompatActivity {
    EditText  name;
    Button register;
    DatabaseReference mDatabase;
    TextView textt;
    CheckBox checkit;
    boolean checked = false;
    RadioButton male, female;
    String gender;
    private Spinner maritalSpinner;
    private String selectedMaritalStatus;
    EditText city, sect, education;
    String userId;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_register);

        city = findViewById(R.id.city);
        sect = findViewById(R.id.sect);
        education = findViewById(R.id.education);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        textt = findViewById(R.id.textt);
        register = findViewById(R.id.register);
        username=getIntent().getStringExtra("name");
        userId=getIntent().getStringExtra("userId");
        checkit = findViewById(R.id.checkit);
        name = findViewById(R.id.name);
        mDatabase = Constants.M_DATABASE;
        AlertsUtils.customTextView(SocialRegister.this, textt);
        setMaritalSpinner();

        name.setText(username);
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

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().length() == 0) {
                    name.setError("Enter Name");
                } else if (gender == null) {
                    CommonUtils.showToast("Please select gender");
                } else if (city.getText().length() == 0) {
                    city.setError("Enter city");
                } else if (sect.getText().length() == 0) {
                    sect.setError("Enter sect");
                } else if (education.getText().length() == 0) {
                    education.setError("Enter education");
                } else if (!checked) {
                    CommonUtils.showToast("Please accept terms and conditions");
                } else {
                    requestCode();
                }

            }
        });


    }

    private void requestCode() {

        String myReferralCode = CommonUtils.getRandomCode(7);
        User user = new User(
                name.getText().toString(),
                userId,
                userId, "", myReferralCode, gender,
                city.getText().toString(),
                sect.getText().toString(),
                education.getText().toString(),
                selectedMaritalStatus, System.currentTimeMillis(),
                BuildConfig.VERSION_CODE
        );


        mDatabase.child("Users").child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                CommonUtils.showToast("Successfully registered");
                SharedPrefs.setUser(user);
                Intent i = new Intent(SocialRegister.this, UploadProfilePicture.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });
    }


    private void setMaritalSpinner() {
        String[] maritalStatuses = {"Single", "Married", "Windowed", "Separated", "Khula",
                "Divorced"};
        maritalSpinner = findViewById(R.id.maritalSpinner);
        maritalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMaritalStatus = maritalStatuses[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, maritalStatuses);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        maritalSpinner.setAdapter(aa);
    }


}