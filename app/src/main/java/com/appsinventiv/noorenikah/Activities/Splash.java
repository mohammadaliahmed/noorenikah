package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;

import org.w3c.dom.Text;

public class Splash extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 2500;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView male = (ImageView)findViewById(R.id.male);
         text = findViewById(R.id.text);
        ImageView female = (ImageView)findViewById(R.id.female);
        Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_slide_in_right);
        male.startAnimation(aniSlide);
        Animation aniSlidef = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_slide_in_left);
        female.startAnimation(aniSlidef);
        Animation aniSlideu = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_slide_in_up);
        text.startAnimation(aniSlideu);

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
        //alias:noorenikah
        //key:noorenikah
        //pass:noorenikah

    }


}