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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    EditText phone, password, name;
    Button login, register;

    DatabaseReference mDatabase;
    private HashMap<String, User> usersMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();


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
                } else if (password.getText().length() == 0) {
                    password.setError("Enter Password");
                } else {
                    if (usersMap.containsKey(phone.getText().toString())) {
                        CommonUtils.showToast("Phone number taken");
                    } else {
                        User user = new User(
                                name.getText().toString(),
                                phone.getText().toString(),
                                password.getText().toString());
                        mDatabase.child("Users").child(phone.getText().toString()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                CommonUtils.showToast("Successfully registered");
                                SharedPrefs.setUser(user);
                                startActivity(new Intent(Register.this,CompleteProfileScreen.class));
                                finish();
                            }
                        });

                    }

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



}