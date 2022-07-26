package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appsinventiv.noorenikah.Activities.SearchActivity;
import com.appsinventiv.noorenikah.R;

public class SearchFragment extends Fragment {
    private View rootView;
    private String selectedCast;
    private String selectedEducation;
    EditText minAge, maxAge, minHeight, maxHeight, city, minIncome, maxIncome;
    Button search;
    private String selectedHomeType;
    RadioButton jobRadio, businessRadio;
    private String jobOrBusiness;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_search, container, false);
        minAge = rootView.findViewById(R.id.minAge);
        maxAge = rootView.findViewById(R.id.maxAge);
        minIncome = rootView.findViewById(R.id.minIncome);
        maxIncome = rootView.findViewById(R.id.maxIncome);
        jobRadio = rootView.findViewById(R.id.jobRadio);
        businessRadio = rootView.findViewById(R.id.businessRadio);
        minHeight = rootView.findViewById(R.id.minHeight);
        maxHeight = rootView.findViewById(R.id.maxHeight);
        city = rootView.findViewById(R.id.city);
        search = rootView.findViewById(R.id.search);
        setCastSpinner();
        setEducationSpinner();
        setHomeSpinner();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), SearchActivity.class);
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
                i.putExtra("cast", selectedCast);
                startActivity(i);
            }
        });
        setupRadio();
        return rootView;
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
        Spinner homeSpinner = rootView.findViewById(R.id.homeSpinner);
        homeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHomeType = homeTypeList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, homeTypeList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        homeSpinner.setAdapter(aa);
    }

    private void setEducationSpinner() {
        String[] educationList = {"Select", "FA", "BA",
                "MA", "MPhil", "Phd"};
        Spinner educationSpinner = rootView.findViewById(R.id.qualificationSpinner);
        educationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEducation = educationList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, educationList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        educationSpinner.setAdapter(aa);
    }

    private void setCastSpinner() {
        String[] castList = {"Select", "Sheikh", "Butt", "Arain"};
        Spinner castSpinner = rootView.findViewById(R.id.castSpinner);
        castSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCast = castList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, castList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        castSpinner.setAdapter(aa);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}