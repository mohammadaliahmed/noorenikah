package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.nouman.jazzcashlib.Constants;
import com.nouman.jazzcashlib.JazzCashResponse;

public class ResponseActivity extends AppCompatActivity {


    private TextView responseText;
    private JazzCashResponse jazzCashResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
        responseText = findViewById(R.id.responseText);

        if (getIntent() != null) {

            jazzCashResponse = (JazzCashResponse) getIntent().getSerializableExtra(Constants.jazzCashResponse);

            responseText.setText(jazzCashResponse.getPpResponseMessage());
        }




    }


}