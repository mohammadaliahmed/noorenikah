package com.appsinventiv.noorenikah.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Models.User;
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


public class CompleteProfileScreen extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    private static final int REQUEST_CODE_CHOOSE = 23;

    RelativeLayout wholeLayout;
    TextView skip;

    CheckBox consent;
    Button picPicture;
    private Spinner maritalSpinner;
    private String selectedMaritalStatus;
    private String selectedHomeType;
    CircleImageView pickedPicture;
    private String livePicPath;
    RadioButton male, female, jobRadio, joblessRadio, businessRadio;
    EditText age, height, income, belonging, houseSize, city, houseAddress, nationality,
            fatherName, motherName, brothers, sisters, cast, education;
    Button saveProfile;
    DatabaseReference mDatabase;
    private String genderSelected;
    private String jobOrBusiness;
    private ArrayList<String> mSelected = new ArrayList<>();
    private String imageUrl;
    private boolean consentGiven;
    EditText about;
    EditText religion, sect;
    EditText companyName, fatherOccupation, motherOccupation;


    private void setUpFindViewByIds() {
        age = findViewById(R.id.age);
        wholeLayout = findViewById(R.id.wholeLayout);
        height = findViewById(R.id.height);
        income = findViewById(R.id.income);
        belonging = findViewById(R.id.belonging);
        cast = findViewById(R.id.cast);
        houseSize = findViewById(R.id.houseSize);
        joblessRadio = findViewById(R.id.joblessRadio);
        city = findViewById(R.id.city);
        education = findViewById(R.id.education);
        houseAddress = findViewById(R.id.houseAddress);
        consent = findViewById(R.id.consent);
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
        joblessRadio.setOnCheckedChangeListener((compoundButton, b) -> {
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
        setContentView(R.layout.activity_complete_profile);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        pickedPicture = findViewById(R.id.pickedPicture);
        companyName = findViewById(R.id.companyName);
        skip = findViewById(R.id.skip);

        about = findViewById(R.id.about);
        motherOccupation = findViewById(R.id.motherOccupation);
        fatherOccupation = findViewById(R.id.fatherOccupation);
        religion = findViewById(R.id.religion);
        sect = findViewById(R.id.sect);
        picPicture = findViewById(R.id.picPicture);
        picPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CompleteProfileScreen.this,MainActivity.class));

            }
        });

        setUpFindViewByIds();
        setMaritalSpinner();

//        setEducationSpinner();
        setHomeSpinner();
        consent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    consentGiven = true;
                }
            }


        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (genderSelected == null) {

                    CommonUtils.showToast("Please select gender");
                } else if (age.getText().length() == 0) {
                    age.setError("Enter age");
                    age.requestFocus();

                } else if (height.getText().length() == 0) {
                    height.setError("Enter height");
                    height.requestFocus();

                } else if (selectedMaritalStatus == null) {

                    CommonUtils.showToast("Please select marital status");
                } else if (education.getText().length() == 0) {
                    education.setError("Enter education");
                    education.requestFocus();

                } else if (jobOrBusiness == null) {

                    CommonUtils.showToast("Please select job status");
                } else if (religion.getText().length() == 0) {
                    religion.setError("Enter religion");
                    religion.requestFocus();

                } else if (sect.getText().length() == 0) {
                    sect.setError("Enter sect");
                    sect.requestFocus();

                } else if (cast.getText().length() == 0) {
                    cast.setError("Enter cast");
                    cast.requestFocus();
                } else if (city.getText().length() == 0) {
                    city.setError("Enter city");
                    city.requestFocus();
                }  else if (about.getText().length() == 0) {
                    about.setError("Enter some lines about yourself");
                    about.requestFocus();

                } else if (livePicPath == null) {

                    CommonUtils.showToast("Please upload picture");
                } else if (!consentGiven) {
                    CommonUtils.showToast("Please accept the consent form");
                } else {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("livePicPath", livePicPath);
                    map.put("age", Integer.parseInt(age.getText().toString()));
                    map.put("height", Float.parseFloat(height.getText().toString()));
                    map.put("income", Integer.parseInt(income.getText().toString()));
                    map.put("belonging", belonging.getText().toString());
                    map.put("houseSize", houseSize.getText().toString());
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
                    map.put("religion", religion.getText().toString());
                    map.put("about", about.getText().toString());
                    map.put("sect", sect.getText().toString());
                    map.put("fatherOccupation", fatherOccupation.getText().toString());
                    map.put("motherOccupation", motherOccupation.getText().toString());
                    map.put("companyName", companyName.getText().toString());
                    map.put("cast", cast.getText().toString());
                    map.put("homeType", selectedHomeType);
                    wholeLayout.setVisibility(View.VISIBLE);
                    mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        User user = dataSnapshot.getValue(User.class);
                                        SharedPrefs.setUser(user);
                                        startActivity(new Intent(CompleteProfileScreen.this, MainActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                }
            }
        });


    }


    private void setMaritalSpinner() {
        String[] maritalStatuses = { "Single","Married", "Windowed","Separated",
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
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, homeTypeList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        homeSpinner.setAdapter(aa);
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

    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE) {
//            //TODO: action
//            Uri uri = data.getData();
//            final InputStream imageStream;
//            try {
//                imageStream = getContentResolver().openInputStream(uri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                pickedPicture.setImageBitmap(selectedImage);
//                uploadPicture();
//
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            CompressImage image = new CompressImage(CompleteProfileScreen.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(CompleteProfileScreen.this).load(mSelected.get(0)).into(pickedPicture);
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
                        mDatabase.child("Errors").child("picUploadError").child(mDatabase.push().getKey()).setValue(exception.getMessage());

                        CommonUtils.showToast("There was some error uploading pic");


                    });
        } catch (Exception e) {
            mDatabase.child("Errors").child("mainError").child(mDatabase.push().getKey()).setValue(e.getMessage());
        }


    }


//    private void uploadPicture() {
//
//        Bitmap bitmap = ((BitmapDrawable) pickedPicture.getDrawable()).getBitmap();
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
//        byte[] dataa = baos.toByteArray();
//
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//        StorageReference mountainsRef = storageRef.child(System.currentTimeMillis() + ".jpg");
//
//        UploadTask uploadTask = mountainsRef.putBytes(dataa);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
////                CommonUtils.showToast(exception.getMessage());
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                CommonUtils.showToast("" + taskSnapshot.getMetadata());
//                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
//                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        livePicPath = "" + uri;
//
//                    }
//                });
//
//            }
//        });
//
//
//    }


}