package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.nouman.jazzcashlib.JazzCash;

public class PaymentActivity extends AppCompatActivity {


    private WebView webView;
    private JazzCash jazzCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        webView = findViewById(R.id.activity_payment_webview);
//
//        Intent intentData = getIntent();
//        String price = intentData.getStringExtra("price");
//        try {
//            jazzCash = new JazzCash(this, this, ResponseActivity.class, webView,
//                    "MC46377",
//                    "9a03ye858s",
//                    "5a8x221228",
//                    "localhost.com", price);
//
//            jazzCash.integrateNow();
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }


}