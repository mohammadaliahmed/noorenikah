package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.CompressImage;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    private static final int REQUEST_CODE_CHOOSE = 23;
    RelativeLayout wholeLayout;
    Button picPicture;
    private Spinner maritalSpinner;
    private String selectedMaritalStatus;
    private String selectedHomeType;
    CircleImageView pickedPicture;
    private String livePicPath;
    RadioButton male, female, jobRadio, joblessRadio, businessRadio;
    EditText age, height, income, belonging, houseSize, city, houseAddress, nationality,
            fatherName, motherName, brothers, sisters, name, phone, cast;
    Button saveProfile;
    DatabaseReference mDatabase;
    private String genderSelected;
    private String jobOrBusiness;
    private UserModel userModel;
    private ArrayAdapter maritalAdapter;
    private ArrayAdapter homeAdapter;

    private Spinner homeSpinner;
    private ArrayList<String> mSelected = new ArrayList<>();
    private String imageUrl;
    EditText about;
    EditText religion, sect;
    EditText companyName, fatherOccupation, motherOccupation,education;

    private void setUpFindViewByIds() {
        about = findViewById(R.id.about);
        religion = findViewById(R.id.religion);
        sect = findViewById(R.id.sect);
        companyName = findViewById(R.id.companyName);
        education = findViewById(R.id.education);
        fatherOccupation = findViewById(R.id.fatherOccupation);
        motherOccupation = findViewById(R.id.motherOccupation);
        wholeLayout = findViewById(R.id.wholeLayout);
        joblessRadio = findViewById(R.id.joblessRadio);
        height = findViewById(R.id.height);
        income = findViewById(R.id.income);
        belonging = findViewById(R.id.belonging);
        age = findViewById(R.id.age);
        jobRadio = findViewById(R.id.jobRadio);
        houseSize = findViewById(R.id.houseSize);
        city = findViewById(R.id.city);
        houseAddress = findViewById(R.id.houseAddress);
        nationality = findViewById(R.id.nationality);
        fatherName = findViewById(R.id.fatherName);
        motherName = findViewById(R.id.motherName);
        brothers = findViewById(R.id.brothers);
        sisters = findViewById(R.id.sisters);
        saveProfile = findViewById(R.id.saveProfile);
        male = findViewById(R.id.male);
        cast = findViewById(R.id.cast);
        female = findViewById(R.id.female);
        jobRadio = findViewById(R.id.jobRadio);
        businessRadio = findViewById(R.id.businessRadio);
        jobRadio.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                jobOrBusiness = "job";
            }
        });
        jobRadio.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                jobOrBusiness = "jobless";
            }
        });
        businessRadio.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                jobOrBusiness = "business";
            }
        });
        male.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                genderSelected = "male";
            }
        });
        female.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                genderSelected = "female";
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Edit Profile");
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        pickedPicture = findViewById(R.id.pickedPicture);
        picPicture = findViewById(R.id.picPicture);
        picPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });
        setUpFindViewByIds();
        setMaritalSpinner();

        setHomeSpinner();

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("livePicPath", livePicPath);
                map.put("age", Integer.parseInt(age.getText().toString()));
                map.put("height", Float.parseFloat(height.getText().toString()));
                map.put("income", Integer.parseInt(income.getText().toString().equals("")?"0":income.getText().toString()));
                map.put("belonging", belonging.getText().toString());
                map.put("houseSize", houseSize.getText().toString());
                map.put("name", name.getText().toString());
                map.put("city", city.getText().toString());
                map.put("houseAddress", houseAddress.getText().toString());
                map.put("nationality", nationality.getText().toString());
                map.put("fatherName", fatherName.getText().toString());
                map.put("motherName", motherName.getText().toString());
                map.put("brothers", Integer.parseInt(brothers.getText().toString()));
                map.put("sisters", Integer.parseInt(sisters.getText().toString()));
                map.put("gender", genderSelected);
                map.put("jobOrBusiness", jobOrBusiness);
                map.put("maritalStatus", selectedMaritalStatus);
                map.put("education", education.getText().toString());
                map.put("fatherOccupation", fatherOccupation.getText().toString());
                map.put("motherOccupation", motherOccupation.getText().toString());
                map.put("about", about.getText().toString());
                map.put("company", companyName.getText().toString());
                map.put("religion", religion.getText().toString());
                map.put("sect", sect.getText().toString());
                map.put("cast", cast.getText().toString());
                map.put("homeType", selectedHomeType);
                wholeLayout.setVisibility(View.VISIBLE);
                mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        wholeLayout.setVisibility(View.GONE);
                        CommonUtils.showToast("Profile updated");
                        mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    UserModel user = dataSnapshot.getValue(UserModel.class);
                                    SharedPrefs.setUser(user);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
//

                    }
                });
            }
        });

        getDataFromServer();
    }
    private void getDataFromServer() {
        mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel.class);
                if (userModel != null) {
                    if(userModel.getGender()!=null) {
                        if (userModel.getGender().equalsIgnoreCase("female")) {
                            female.setChecked(true);
                        } else {
                            male.setChecked(true);
                        }
                    }
                    if(userModel.getJobOrBusiness()!=null) {
                        if (userModel.getJobOrBusiness().equalsIgnoreCase("job")) {
                            jobRadio.setChecked(true);
                        } else if (userModel.getJobOrBusiness().equalsIgnoreCase("jobless")) {
                            joblessRadio.setChecked(true);
                        } else {
                            businessRadio.setChecked(true);
                        }
                    }


                    maritalSpinner.setSelection(maritalAdapter.getPosition(userModel.getMaritalStatus()));
                    religion.setText(userModel.getReligion());
                    sect.setText(userModel.getSect());
                    fatherOccupation.setText(userModel.getFatherOccupation());
                    motherOccupation.setText(userModel.getMotherOccupation());
                    cast.setText(userModel.getCast());
                    homeSpinner.setSelection(homeAdapter.getPosition(userModel.getHomeType()));
                    try {
                        Glide.with(EditProfile.this).load(userModel.getLivePicPath())
                                .placeholder(R.drawable.picked).into(pickedPicture);
                    }catch (Exception e){

                    }
                    livePicPath = "" + userModel.getLivePicPath();
                    name.setText("" + userModel.getName());
                    about.setText("" + userModel.getAbout());
                    phone.setText("" + userModel.getPhone());
                    age.setText("" + userModel.getAge());
                    height.setText("" + userModel.getHeight());
                    income.setText("" + userModel.getIncome());
                    belonging.setText("" + userModel.getBelonging());
                    houseSize.setText("" + userModel.getHouseSize());
                    city.setText("" + userModel.getCity());
                    houseAddress.setText("" + userModel.getHouseAddress());
                    nationality.setText("" + userModel.getNationality());
                    fatherName.setText("" + userModel.getFatherName());
                    motherName.setText("" + userModel.getMotherName());
                    brothers.setText("" + userModel.getBrothers());
                    sisters.setText("" + userModel.getSisters());
                    genderSelected = "" + userModel.getGender();
                    jobOrBusiness = "" + userModel.getJobOrBusiness();
                    selectedMaritalStatus = "" + userModel.getMaritalStatus();
                    education .setText(userModel.getEducation());
                    selectedHomeType = "" + userModel.getHomeType();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setMaritalSpinner() {
        String[] maritalStatuses = {"Marital Status", "Single", "Windowed","Separated",
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
        maritalAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, maritalStatuses);
        maritalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        maritalSpinner.setAdapter(maritalAdapter);
    }



    private void setHomeSpinner() {
        String[] homeTypeList = {"Home type", "Own", "Rental", "Flat", "Apartment"};
        homeSpinner = findViewById(R.id.homeSpinner);
        homeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHomeType = homeTypeList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        homeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, homeTypeList);
        homeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        homeSpinner.setAdapter(homeAdapter);
    }

    private void initMatisse() {
        Options options = Options.init()
                .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
                .setCount(1)
                .setExcludeVideos(true)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                ;                                       //Custom Path For media Storage

        Pix.start(this, options);
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

            CompressImage image = new CompressImage(EditProfile.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(EditProfile.this).load(mSelected.get(0)).into(pickedPicture);
            uploadPicture();

        }
    }

    private void uploadPicture() {
        try {
            String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

            Uri file = Uri.fromFile(new File(imageUrl));

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

            final StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

            riversRef.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get a URL to the uploaded content

                        String downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        livePicPath = "" + uri;

                                    }
                                });


                            }
                        });


                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        // ...

                        CommonUtils.showToast("There was some error uploading pic");


                    });
        } catch (Exception e) {
            mDatabase.child("Errors").child("mainError").child(mDatabase.push().getKey()).setValue(e.getMessage());
        }


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