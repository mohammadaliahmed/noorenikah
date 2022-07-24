package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.R;


public class CompleteProfileScreen extends AppCompatActivity {


    Button picPicture;
    private Spinner maritalSpinner;
    private String selectedMaritalStatus;
    private String selectedEducation;
    private String selectedReligion;
    private String selectedCast;
    private String selectedHomeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        picPicture = findViewById(R.id.picPicture);
        picPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });
        setMaritalSpinner();
        setEducationSpinner();
        setReligionSpinner();
        setCastSpinner();
        setSectSpinner();
        setHomeSpinner();


    }

    private void setMaritalSpinner() {
        String[] maritalStatuses = {"Marital Status", "Single", "Windowed",
                "Divorced"};
        maritalSpinner = findViewById(R.id.maritalSpinner);
        maritalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMaritalStatus = maritalStatuses[i];
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

    private void setEducationSpinner() {
        String[] educationList = {"Qualification Level", "FA", "BA",
                "MA", "MPhil", "Phd"};
        Spinner educationSpinner = findViewById(R.id.educationSpinner);
        educationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEducation = educationList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, educationList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        educationSpinner.setAdapter(aa);
    }

    private void setReligionSpinner() {
        String[] religionList = {"Religion", "Islam"};
        Spinner religionSpinner = findViewById(R.id.religionSpinner);
        religionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedReligion = religionList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, religionList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        religionSpinner.setAdapter(aa);
    }

    private void setSectSpinner() {
        String[] sectList = {"Religion", "Suni", "Shia"};
        Spinner sectSpinner = findViewById(R.id.sectSpinner);
        sectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedReligion = sectList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sectList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        sectSpinner.setAdapter(aa);
    }

    private void setCastSpinner() {
        String[] castList = {"Cast", "Sheikh", "Butt","Arain"};
        Spinner castSpinner = findViewById(R.id.castSpinner);
        castSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCast= castList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, castList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        castSpinner.setAdapter(aa);
    }
    private void setHomeSpinner() {
        String[] homeTypeList = {"Home type", "Own", "Rental"};
        Spinner homeSpinner = findViewById(R.id.homeSpinner);
        homeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHomeType= homeTypeList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, homeTypeList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        homeSpinner.setAdapter(aa);
    }

    private void initMatisse() {
//        PixPicker.Companion.getPic();

//        Options options = Options.app()
//                .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
//                .setCount(8)
//                .setMode(Options.Mode.Picture)//Number of images to restict selection count
//                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
//                ;                                       //Custom Path For media Storage
//
//        Pix.start(this, options);
    }

}