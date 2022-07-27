package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    RelativeLayout wholeLayout;
    Button picPicture;
    private Spinner maritalSpinner;
    private String selectedMaritalStatus;
    private String selectedEducation;
    private String selectedReligion;
    private String selectedCast;
    private String selectedHomeType;
    CircleImageView pickedPicture;
    private String livePicPath;
    RadioButton male, female, jobRadio, businessRadio;
    EditText age, height, income, belonging, houseSize, city, houseAddress, nationality,
            fatherName, motherName, brothers, sisters, name, phone;
    Button saveProfile;
    DatabaseReference mDatabase;
    private String genderSelected;
    private String jobOrBusiness;
    private String selectedSect;
    private User userModel;
    private ArrayAdapter maritalAdapter;
    private ArrayAdapter educationAdapter;
    private ArrayAdapter religionAdapter;
    private ArrayAdapter sectAdapter;
    private ArrayAdapter castAdapter;
    private ArrayAdapter homeAdapter;
    private Spinner educationSpinner;
    private Spinner religionSpinner;
    private Spinner sectSpinner;
    private Spinner castSpinner;
    private Spinner homeSpinner;

    private void setUpFindViewByIds() {
        age = findViewById(R.id.age);
        wholeLayout = findViewById(R.id.wholeLayout);
        height = findViewById(R.id.height);
        income = findViewById(R.id.income);
        belonging = findViewById(R.id.belonging);
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
        female = findViewById(R.id.female);
        jobRadio = findViewById(R.id.jobRadio);
        businessRadio = findViewById(R.id.businessRadio);
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
        setEducationSpinner();
        setReligionSpinner();
        setCastSpinner();
        setSectSpinner();
        setHomeSpinner();

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("livePicPath", livePicPath);
                map.put("age", Integer.parseInt(age.getText().toString()));
                map.put("height", Float.parseFloat(height.getText().toString()));
                map.put("income", Integer.parseInt(income.getText().toString()));
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
                map.put("education", selectedEducation);
                map.put("religion", selectedReligion);
                map.put("sect", selectedSect);
                map.put("cast", selectedCast);
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
                                    User user = dataSnapshot.getValue(User.class);
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
                userModel = dataSnapshot.getValue(User.class);
                if (userModel != null) {
                    if (userModel.getGender().equalsIgnoreCase("female")) {
                        female.setChecked(true);
                    } else {
                        male.setChecked(true);
                    }
                    if (userModel.getJobOrBusiness().equalsIgnoreCase("job")) {
                        jobRadio.setChecked(true);
                    } else {
                        businessRadio.setChecked(true);
                    }


                    maritalSpinner.setSelection(maritalAdapter.getPosition(userModel.getMaritalStatus()));
                    educationSpinner.setSelection(educationAdapter.getPosition(userModel.getEducation()));
                    religionSpinner.setSelection(religionAdapter.getPosition(userModel.getReligion()));
                    sectSpinner.setSelection(sectAdapter.getPosition(userModel.getSect()));
                    castSpinner.setSelection(castAdapter.getPosition(userModel.getCast()));
                    homeSpinner.setSelection(homeAdapter.getPosition(userModel.getHomeType()));
                    Glide.with(EditProfile.this).load(userModel.getLivePicPath()).into(pickedPicture);
                    livePicPath = "" + userModel.getLivePicPath();
                    name.setText("" + userModel.getName());
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
                    selectedEducation = "" + userModel.getEducation();
                    selectedReligion = "" + userModel.getReligion();
                    selectedSect = "" + userModel.getSect();
                    selectedCast = "" + userModel.getCast();
                    selectedHomeType = "" + userModel.getHomeType();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        maritalAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, maritalStatuses);
        maritalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        maritalSpinner.setAdapter(maritalAdapter);
    }

    private void setEducationSpinner() {
        String[] educationList = {"Qualification Level", "FA", "BA",
                "MA", "MPhil", "Phd"};
        educationSpinner = findViewById(R.id.educationSpinner);
        educationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEducation = educationList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        educationAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, educationList);
        educationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        educationSpinner.setAdapter(educationAdapter);
    }

    private void setReligionSpinner() {
        String[] religionList = {"Religion", "Islam"};
        religionSpinner = findViewById(R.id.religionSpinner);
        religionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedReligion = religionList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        religionAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, religionList);
        religionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        religionSpinner.setAdapter(religionAdapter);
    }

    private void setSectSpinner() {
        String[] sectList = {"Sect", "Suni", "Shia"};
        sectSpinner = findViewById(R.id.sectSpinner);
        sectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSect = sectList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sectList);
        sectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        sectSpinner.setAdapter(sectAdapter);
    }

    private void setCastSpinner() {
        String[] castList = {"Cast", "Sheikh", "Butt", "Arain"};
        castSpinner = findViewById(R.id.castSpinner);
        castSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCast = castList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        castAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, castList);
        castAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        castSpinner.setAdapter(castAdapter);
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
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            //TODO: action
            Uri uri = data.getData();
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(uri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                pickedPicture.setImageBitmap(selectedImage);
                uploadPicture();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
    }

    private void uploadPicture() {

        Bitmap bitmap = ((BitmapDrawable) pickedPicture.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] dataa = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference mountainsRef = storageRef.child(System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = mountainsRef.putBytes(dataa);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
//                CommonUtils.showToast(exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                CommonUtils.showToast("" + taskSnapshot.getMetadata());
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        livePicPath = "" + uri;

                    }
                });

            }
        });


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