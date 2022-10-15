package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Activities.MatchMaker.MatchMakerProfile;
import com.appsinventiv.noorenikah.Activities.MatchMaker.RegisterMatchMaker;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.AlertsUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;

public class MenuActivity extends AppCompatActivity {

    Button logout;
    RelativeLayout  requestAccepted, privacy, terms, matchMaker, invite, myProfile;
    RelativeLayout paymentsHistory;
    TextView verified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Menu");
        paymentsHistory = findViewById(R.id.paymentsHistory);
        myProfile = findViewById(R.id.myProfile);
        requestAccepted = findViewById(R.id.requestAccepted);
        verified = findViewById(R.id.verified);
        matchMaker = findViewById(R.id.matchMaker);
        invite = findViewById(R.id.invite);
        privacy = findViewById(R.id.privacy);
        terms = findViewById(R.id.terms);
        logout = findViewById(R.id.logout);
        matchMaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPrefs.getUser().isMatchMakerProfile()) {
                    startActivity(new Intent(MenuActivity.this, MatchMakerProfile.class));
                } else {
                    startActivity(new Intent(MenuActivity.this, RegisterMatchMaker.class));

                }
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertsUtils.showTermsAlert(MenuActivity.this);
            }
        });
        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, ViewMyProfile.class));
            }
        });
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, InviteActivity.class));

            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertsUtils.showPrivacyAlert(MenuActivity.this);
            }
        });
        paymentsHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, PaymentsHistory.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefs.logout();
                Intent i = new Intent(MenuActivity.this, Splash.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

        requestAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, ListOfFriends.class));
            }
        });

        if (SharedPrefs.getUser().isPhoneVerified()) {
            verified.setVisibility(View.GONE);
        } else {
            verified.setVisibility(View.VISIBLE);
        }
        verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, VerifyPhone.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}