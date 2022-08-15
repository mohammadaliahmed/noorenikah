package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;

public class SearchScreen extends AppCompatActivity {
    private String selectedEducation;
    EditText minAge, maxAge, minHeight, maxHeight, city, minIncome, maxIncome, cast;
    Button search;
    private String selectedHomeType;
    RadioButton jobRadio, businessRadio;
    private String jobOrBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
            this.setTitle("Search");
        }

        minAge = findViewById(R.id.minAge);
        maxAge = findViewById(R.id.maxAge);
        minIncome = findViewById(R.id.minIncome);
        maxIncome = findViewById(R.id.maxIncome);
        jobRadio = findViewById(R.id.jobRadio);
        businessRadio = findViewById(R.id.businessRadio);
        minHeight = findViewById(R.id.minHeight);
        maxHeight = findViewById(R.id.maxHeight);
        city = findViewById(R.id.city);
        search = findViewById(R.id.search);
        cast = findViewById(R.id.cast);
        setEducationSpinner();
        setHomeSpinner();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SearchScreen.this, SearchActivity.class);
                i.putExtra("minAge", Integer.parseInt(minAge.getText().toString()));
                i.putExtra("maxAge", Integer.parseInt(maxAge.getText().toString()));
                i.putExtra("minHeight", Float.parseFloat(minHeight.getText().toString()));
                i.putExtra("maxHeight", Float.parseFloat(maxHeight.getText().toString()));
                i.putExtra("maxIncome", Integer.parseInt(maxIncome.getText().toString()));
                i.putExtra("minIncome", Integer.parseInt(minIncome.getText().toString()));
                i.putExtra("city", city.getText().toString());
                i.putExtra("selectedHomeType", selectedHomeType);
                i.putExtra("jobOrBusiness", jobOrBusiness);
                i.putExtra("education", selectedEducation);
                i.putExtra("cast", cast.getText().toString());
                startActivity(i);
            }
        });
        setupRadio();
    }

    private void setupRadio() {
        jobRadio.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                jobOrBusiness = "job";
            }
        });
        businessRadio.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                jobOrBusiness = "business";
            }
        });
    }

    private void setHomeSpinner() {
        String[] homeTypeList = {"Home type", "Own", "Rental", "Flat", "Apartment"};
        Spinner homeSpinner = findViewById(R.id.homeSpinner);
        homeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHomeType = homeTypeList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(SearchScreen.this, android.R.layout.simple_spinner_item, homeTypeList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        homeSpinner.setAdapter(aa);
    }

    private void setEducationSpinner() {
        String[] educationList = {"Select", "FA", "BA",
                "MA", "MPhil", "Phd"};
        Spinner educationSpinner = findViewById(R.id.qualificationSpinner);
        educationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEducation = educationList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(SearchScreen.this, android.R.layout.simple_spinner_item, educationList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        educationSpinner.setAdapter(aa);
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