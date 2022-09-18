package com.appsinventiv.noorenikah.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


import com.appsinventiv.noorenikah.R;
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;

public class ViewPictures extends AppCompatActivity {
    //    ViewPager mViewPager;
//    PhotosAdapter adapter;
    ImageView image;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pictures);

        image = findViewById(R.id.image);
        url = getIntent().getStringExtra("url");
        Glide.with(this).load(url).into(image);
        image.setOnTouchListener(new ImageMatrixTouchHandler(this));

    }


}
