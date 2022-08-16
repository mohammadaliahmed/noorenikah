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
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginScreen extends AppCompatActivity {

    EditText phone, password;
    Button login, register;

    DatabaseReference mDatabase;
    private HashMap<String, User> usersMap = new HashMap<>();

    TextView textt;
    CheckBox checkit;
    boolean checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        textt = findViewById(R.id.textt);
        checkit = findViewById(R.id.checkit);

        register = findViewById(R.id.register);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();

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
                } else {
                    loginNow(phone.getText().toString(), password.getText().toString());
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
        AlertsUtils.customTextView(LoginScreen.this, textt);

        checkit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    checked = true;
                }
            }
        });


    }

    private void loginNow(String phone, String pass) {
        phone = phone.substring(phone.length() - 10);

        if (usersMap.containsKey(phone)) {
            User user = usersMap.get(phone);

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

        } else {
            CommonUtils.showToast("Account does not exist. Please signup");

        }
    }


}