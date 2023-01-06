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
    private String maritalStatus;
    EditText minAge, maxAge,  city;
    Button search;
    RadioButton male, female;
    private String gender="male";

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

        female = findViewById(R.id.female);
        male = findViewById(R.id.male);

        city = findViewById(R.id.city);
        search = findViewById(R.id.search);
        setHomeSpinner();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SearchScreen.this, SearchActivity.class);
                i.putExtra("minAge", Integer.parseInt(minAge.getText().toString()));
                i.putExtra("maxAge", Integer.parseInt(maxAge.getText().toString()));
                i.putExtra("city", city.getText().toString());
                i.putExtra("gender", gender);
                i.putExtra("maritalStatus", maritalStatus);
                startActivity(i);
            }
        });
        setupRadio();
    }

    private void setupRadio() {
        female.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                gender = "female";
            }
        });
        male.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                gender = "male";
            }
        });
    }

    private void setHomeSpinner() {
        String[] maritalStatuses = {"Single", "Married", "Windowed", "Separated", "Khula",
                "Divorced"};
        Spinner maritalSpinner = findViewById(R.id.maritalSpinner);
        maritalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                maritalStatus = maritalStatuses[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, maritalStatuses);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        maritalSpinner.setAdapter(aa);
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