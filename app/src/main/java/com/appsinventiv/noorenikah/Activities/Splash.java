package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;

public class Splash extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                if (SharedPrefs.getUser() != null) {
                    if(SharedPrefs.getUser().getLivePicPath()==null) {
                        startActivity(new Intent(Splash.this,CompleteProfileScreen.class));
                    }else{
                        startActivity(new Intent(Splash.this,MainActivity.class));

                    }
                } else {
                    Intent i = new Intent(Splash.this, LoginScreen.class);
                    startActivity(i);
                }

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
        //alias:amrozanew
        //key:amrozanew
        //pass:amrozanew

    }


}