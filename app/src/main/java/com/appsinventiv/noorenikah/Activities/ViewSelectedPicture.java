package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

public class ViewSelectedPicture extends AppCompatActivity {

    ImageView image,remove,send;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_picture);
        image=findViewById(R.id.image);
        send=findViewById(R.id.send);
        remove=findViewById(R.id.remove);
        url=getIntent().getStringExtra("url");
        Glide.with(this).load(url).into(image);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("send","send");
                setResult(RESULT_OK, data);
                finish();


            }
        });



    }


}