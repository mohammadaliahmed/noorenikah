package com.appsinventiv.noorenikah.Activities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ViewRequestProfile extends AppCompatActivity {

    CircleImageView image;
    TextView name,city,maritalStatus,education,cast,jobOrBusiness,about;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("View Profile");
        about=findViewById(R.id.about);
        image=findViewById(R.id.image);
        name=findViewById(R.id.name);
        city=findViewById(R.id.city);
        maritalStatus=findViewById(R.id.maritalStatus);
        education=findViewById(R.id.education);
        cast=findViewById(R.id.cast);
        jobOrBusiness=findViewById(R.id.jobOrBusiness);
        user = (User) getIntent().getSerializableExtra("user");

        Glide.with(this)
                .load(user.getLivePicPath())
                .apply(bitmapTransform(new BlurTransformation(50)))
                .into(image);

        name.setText(""+user.getName());
        city.setText(""+user.getCity());
        maritalStatus.setText(""+user.getMaritalStatus());
        education.setText(""+user.getEducation());
        cast.setText(""+user.getCast());
        jobOrBusiness.setText(""+user.getJobOrBusiness());



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