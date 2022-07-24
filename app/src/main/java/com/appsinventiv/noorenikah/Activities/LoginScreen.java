package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Models.User;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
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

    }

    private void loginNow(String phone, String pass) {
        if (usersMap.containsKey(phone)) {
            User user = usersMap.get(phone);
            if (user.getPassword().equals(pass)) {
                SharedPrefs.setUser(user);
                if (user.getPictures() == null) {
                    startActivity(new Intent(LoginScreen.this, CompleteProfileScreen.class));

                } else {
                    startActivity(new Intent(LoginScreen.this, MainActivity.class));
                }
                finish();

            } else {
                CommonUtils.showToast("Wrong Password");
            }
        } else {
            CommonUtils.showToast("Account does not exist. Please signup");

        }
    }


}