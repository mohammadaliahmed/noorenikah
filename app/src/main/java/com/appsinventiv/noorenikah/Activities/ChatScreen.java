package com.appsinventiv.noorenikah.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.ChatAdapter;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.CompressImage;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.KeyboardUtils;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordPermissionHandler;
import com.devlomi.record_view.RecordView;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatScreen extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 23;
    String mFileName;
    EmojiEditText message;
    ImageView send;
    private DatabaseReference mDatabase;
    String msg = "";
    private User otherUser;
    TextView name;
    CircleImageView picture;
    private List<ChatModel> itemList = new ArrayList<>();
    ChatAdapter adapter;
    RecyclerView recyclerView;
    ImageView back;
    long recordingTime = 0L;

    private String otherUserPhone;
    boolean screenActive;
    private AdRequest adRequest;
    ImageView dropdown_menu;
    TextView blocked;
    RelativeLayout bottomArea;
    ImageView camera;
    private String imageUrl;
    private String livePicPath;
    private RecordView recordView;
    private RecordButton recordButton;
    RelativeLayout recordingArea;
    RelativeLayout messagingArea;
    private MediaRecorder mRecorder;
    private String recordingLocalUrl;
    ViewGroup rootView;

    ImageView emoji;
    EmojiPopup emojiPopup;
    TextView userStatus;
    private boolean emojiShowing;

    @Override
    protected void onResume() {
        super.onResume();
        screenActive = true;
        CommonUtils.sendCustomerStatus("Online");

    }

    @Override
    protected void onStop() {
        super.onStop();
        CommonUtils.sendCustomerStatus("" + System.currentTimeMillis());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);

        }
        getPermissions();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        rootView = findViewById(R.id.main_activity_root_view);

        emoji = findViewById(R.id.emoji);
        userStatus = findViewById(R.id.userStatus);
        messagingArea = findViewById(R.id.messagingArea);
        recordingArea = findViewById(R.id.recordingArea);
        recordView = (RecordView) findViewById(R.id.record_view);
        recordButton = (RecordButton) findViewById(R.id.record_button);
        SetupFileName();
        setUpRecord();


//IMPORTANT
        adRequest = new AdRequest.Builder().build();
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        otherUserPhone = getIntent().getStringExtra("phone");
//        otherUserPhone = "3097748424";
        adapter = new ChatAdapter(this, itemList);

        dropdown_menu = findViewById(R.id.dropdown_menu);
        camera = findViewById(R.id.camera);
        bottomArea = findViewById(R.id.bottomArea);
        back = findViewById(R.id.back);
        blocked = findViewById(R.id.blocked);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        name = findViewById(R.id.name);
        picture = findViewById(R.id.picture);
        send = findViewById(R.id.send);
        message = findViewById(R.id.message);
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    recordingArea.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);
                } else {
                    send.setVisibility(View.INVISIBLE);
                    recordingArea.setVisibility(View.VISIBLE);

                }
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Options options = Options.init()
                        .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
                        .setCount(1)
                        .setExcludeVideos(true)
                        .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                        ;                                       //Custom Path For media Storage

                Pix.start(ChatScreen.this, options);
            }
        });

        checkForBlockages();


        dropdown_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ChatScreen.this, dropdown_menu);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Toast message on menu item clicked
                        if (menuItem.getItemId() == R.id.clearChat) {
                            showClearAlert();
                        } else if (menuItem.getItemId() == R.id.blockUser) {
                            showBlockAlert();
                        }

                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChatScreen.this, ViewUserProfile.class);
                i.putExtra("phone", otherUserPhone);
                startActivity(i);
            }
        });
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChatScreen.this, ViewUserProfile.class);
                i.putExtra("phone", otherUserPhone);
                startActivity(i);
            }
        });
        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                if (isVisible) {
                    if (emojiShowing) {
                        recordButton.setVisibility(View.INVISIBLE);
                    } else {
                        recordButton.setVisibility(View.VISIBLE);

                    }
                } else {
                    params.setMargins(0, 0, 150, 0);
                    messagingArea.setLayoutParams(params);
                    emojiPopup.dismiss();
                    recordButton.setVisibility(View.VISIBLE);


                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (message.getText().length() == 0) {
                    message.setError("Cant send empty message");
                } else {
                    msg = message.getText().toString();
                    message.setText("");
                    sendMessageToDb(Constants.MESSAGE_TYPE_TEXT);
                }

            }
        });
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(message);
        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggles visibility of the Popup.


                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                    params.setMargins(0, 0, 150, 0);
                    messagingArea.setLayoutParams(params);
                    recordButton.setVisibility(View.VISIBLE);
                    emojiShowing = false;
                } else {
                    emojiPopup.toggle();

                    params.setMargins(0, 0, 150, 70);
                    messagingArea.setLayoutParams(params);
                    recordButton.setVisibility(View.INVISIBLE);
                    emojiShowing = true;


                }

            }
        });
        getOtherUserFromDb();

        itemList = SharedPrefs.getChatList(otherUserPhone);
        if (itemList == null) {
            itemList = new ArrayList<>();

        }
        adapter.setItemList(itemList);
        recyclerView.scrollToPosition(itemList.size() - 1);


        getDataFromDb();

    }

    private void setUpRecord() {
        recordButton.setRecordView(recordView);
        recordView.setRecordPermissionHandler(new RecordPermissionHandler() {
            private static final int PERMISSION_GRANTED = 0;

            @Override
            public boolean isPermissionGranted() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return true;
                }

                boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(ChatScreen.this,
                        Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
                if (recordPermissionAvailable) {
                    return true;
                }


                ActivityCompat.
                        requestPermissions(ChatScreen.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                0);

                return false;

            }
        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
                mRecorder = null;
                startRecording();
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");
                if (mRecorder != null) {
                    mRecorder.release();
                }
                mRecorder = null;

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //Stop Recording..
                //limitReached to determine if the Record was finished when time limit reached.
//                String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish");
                String time = CommonUtils.getFormattedDate(recordTime);
                Log.d("RecordView", "onFinish");

//                setMargins(recycler, 0, 0, 0, 0);

                Log.d("RecordTime", time);
                recordingTime = recordTime;
                messagingArea.setVisibility(View.VISIBLE);
                stopRecording();

            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
                messagingArea.setVisibility(View.VISIBLE);
                mRecorder = null;
            }
        });

        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
                messagingArea.setVisibility(View.VISIBLE);
                mRecorder = null;
            }
        });
    }

    private void checkForBlockages() {

        if (SharedPrefs.getUser().getBlockedMe() != null && SharedPrefs.getUser().getBlockedMe().containsKey(otherUserPhone)) {
            recordingArea.setVisibility(View.GONE);
            bottomArea.setVisibility(View.GONE);
            blocked.setVisibility(View.VISIBLE);
            blocked.setText("This user has blocked you");
        } else if (SharedPrefs.getUser().getiBlocked() != null && SharedPrefs.getUser().getiBlocked().containsKey(otherUserPhone)) {
            recordingArea.setVisibility(View.GONE);
            bottomArea.setVisibility(View.GONE);
            blocked.setVisibility(View.VISIBLE);
            blocked.setText("You have blocked this user");
        } else {
            recordingArea.setVisibility(View.VISIBLE);
            bottomArea.setVisibility(View.VISIBLE);
            blocked.setVisibility(View.GONE);
            blocked.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenActive = false;
    }

    private void getOtherUserFromDb() {
        mDatabase.child("Users").child(otherUserPhone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (screenActive) {
                        otherUser = dataSnapshot.getValue(User.class);
                        name.setText(otherUser.getName());
                        try {
                            Glide.with(ChatScreen.this).load(otherUser.getLivePicPath())
                                    .placeholder(R.drawable.picked).into(picture);
                            if (otherUser.getLastLoginTime() != null) {
                                if (otherUser.getLastLoginTime().equalsIgnoreCase("online")) {
                                    userStatus.setText("Online");
                                } else {
                                    long time = Long.parseLong(otherUser.getLastLoginTime());
                                    userStatus.setText("Last active: " + CommonUtils.getFormattedDate(time));
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataFromDb() {
        mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone())
                .child(otherUserPhone).limitToLast(150).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        itemList.clear();
                        if (dataSnapshot.getValue() != null) {

                            List<String> unreadList = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                ChatModel model = snapshot.getValue(ChatModel.class);
                                if (model != null && model.getMessage() != null) {
                                    itemList.add(model);
                                    if (!model.getSenderId().equalsIgnoreCase(SharedPrefs.getUser().getPhone())
                                            && model.getStatus() != null && model.getStatus().equalsIgnoreCase("sent")) {

                                        unreadList.add(model.getId());
                                    }
                                }
                            }

                            adapter.setItemList(itemList);
                            SharedPrefs.setChatList(itemList, otherUserPhone);
                            recyclerView.scrollToPosition(itemList.size() - 1);
                            if (screenActive) {
                                for (String item : unreadList) {
                                    mDatabase.child("Chats")
                                            .child(otherUserPhone)
                                            .child(SharedPrefs.getUser().getPhone())
                                            .child(item).child("status").setValue("seen");

                                }
                            }
                        } else {
                            SharedPrefs.setChatList(itemList, otherUserPhone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void sendMessageToDb(String type) {
        String key = "" + System.currentTimeMillis();
        ChatModel myModel = new ChatModel(key, msg, SharedPrefs.getUser().getPhone(), otherUserPhone,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath() == null ? "" : SharedPrefs.getUser().getLivePicPath(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                otherUser.getName() == null ? "Name=Unknown" : otherUser.getName(),
                otherUser.getPhone(),
                otherUser.getLivePicPath(),
                "sent",
                System.currentTimeMillis(), type,
                livePicPath, livePicPath, livePicPath,
                recordingTime
        );
        mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone()).child(otherUserPhone).child(key).setValue(
                myModel
        );
        if (type.equalsIgnoreCase(Constants.MESSAGE_TYPE_IMAGE) || type.equalsIgnoreCase(Constants.MESSAGE_TYPE_AUDIO)) {
            itemList.remove(itemList.size() - 1);
        }

        ChatModel hisModel = new ChatModel(key, msg, SharedPrefs.getUser().getPhone(), otherUserPhone,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath(),
                otherUser.getName(),
                otherUser.getPhone(),
                otherUser.getLivePicPath(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                "sent",
                System.currentTimeMillis(),
                type,
                livePicPath, livePicPath, livePicPath,
                recordingTime

        );

        mDatabase.child("Chats").child(otherUserPhone)
                .child(SharedPrefs.getUser().getPhone()).child(key).setValue(hisModel);
        if (type.equalsIgnoreCase(Constants.MESSAGE_TYPE_IMAGE)) {
            String notificationText = "\uD83D\uDCF7 Image";
            sendNotification(notificationText);

        } else if (type.equalsIgnoreCase(Constants.MESSAGE_TYPE_TEXT)) {
            String notificationText = msg;
            sendNotification(notificationText);
        } else if (type.equalsIgnoreCase(Constants.MESSAGE_TYPE_AUDIO)) {
            String notificationText = "\uD83C\uDFB5 Audio";
            sendNotification(notificationText);
        }
    }

    private void sendNotification(String msgText) {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New message from: " + SharedPrefs.getUser().getName();
        String NotificationMessage = msgText;
        notificationAsync.execute(
                "ali",
                otherUser.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "msg");
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


    private void showClearAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to clear whole chat? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone()).child(otherUserPhone).removeValue();
                itemList.clear();
                adapter.setItemList(itemList);
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showBlockAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to block this user? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("iBlocked")
                        .child(otherUserPhone).setValue(otherUserPhone);
                mDatabase.child("Users").child(otherUserPhone).child("blockedMe")
                        .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());

                CommonUtils.showToast("You have blocked this user");
                bottomArea.setVisibility(View.GONE);
                blocked.setVisibility(View.VISIBLE);
                blocked.setText("You have blocked this user");
                checkForBlockages();

            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            ArrayList<String> mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            CompressImage image = new CompressImage(ChatScreen.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Intent intent = new Intent(ChatScreen.this, ViewSelectedPicture.class);
            intent.putExtra("url", imageUrl);
            startActivityForResult(intent, 1);
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String send = data.getStringExtra("send");
                if (send.equalsIgnoreCase("send")) {
                    ChatModel myModel = new ChatModel("" + System.currentTimeMillis(), msg, SharedPrefs.getUser().getPhone(), otherUserPhone,
                            SharedPrefs.getUser().getName(),
                            SharedPrefs.getUser().getLivePicPath(),
                            SharedPrefs.getUser().getName(),
                            SharedPrefs.getUser().getPhone(),
                            SharedPrefs.getUser().getLivePicPath(),
                            otherUser.getName(),
                            otherUser.getPhone(),
                            otherUser.getLivePicPath(),
                            "sent",
                            System.currentTimeMillis(), Constants.MESSAGE_TYPE_IMAGE,
                            imageUrl, livePicPath, livePicPath,
                            recordingTime
                    );
                    itemList.add(myModel);
                    adapter.setItemList(itemList);
                    recyclerView.scrollToPosition(itemList.size() - 1);
                    uploadPicture();
                }
            }
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
                        riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        livePicPath = "" + uri;
                                        File file = new File(imageUrl);
                                        file.delete();
                                        sendMessageToDb(Constants.MESSAGE_TYPE_IMAGE);


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

    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,


        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {


                }
            }
        }
        return true;


    }

    private void startRecording() {
        messagingArea.setVisibility(View.GONE);

        recordingLocalUrl = Long.toHexString(Double.doubleToLongBits(Math.random()));
        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mRecorder.setOutputFile(mFileName + recordingLocalUrl + ".mp3");
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
//                    Log.e(LOG_TAG, "prepare() failed");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    messagingArea.setVisibility(View.VISIBLE);

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    messagingArea.setVisibility(View.VISIBLE);

                }

            }
        }, 100);


    }


    public void SetupFileName() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/media/"
                + ChatScreen.this.getPackageName()
                + "/Files/Compressed");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        String mImageName = "AUDIO_" + String.valueOf(System.currentTimeMillis()) + ".mp3";
        mFileName = (mediaStorageDir.getAbsolutePath() + "/");

    }

    private void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            String fileNam = mFileName + recordingLocalUrl + ".mp3";

            ChatModel myModel = new ChatModel("" + System.currentTimeMillis(), msg, SharedPrefs.getUser().getPhone(), otherUserPhone,
                    SharedPrefs.getUser().getName(),
                    SharedPrefs.getUser().getLivePicPath(),
                    SharedPrefs.getUser().getName(),
                    SharedPrefs.getUser().getPhone(),
                    SharedPrefs.getUser().getLivePicPath(),
                    otherUser.getName(),
                    otherUser.getPhone(),
                    otherUser.getLivePicPath(),
                    "sent",
                    System.currentTimeMillis(), Constants.MESSAGE_TYPE_AUDIO,
                    "", livePicPath, fileNam,
                    recordingTime
            );
            itemList.add(myModel);
            adapter.setItemList(itemList);
            recyclerView.scrollToPosition(itemList.size() - 1);
            uploadAudioToServer(fileNam);
        } catch (Exception e) {
//            CommonUtils.showToast("gere");

        } finally {
//            mRecorder.release();
//            mRecorder = null;
        }

    }

    private void uploadAudioToServer(String audioFile) {
        // create upload service client
        try {
            String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

            Uri file = Uri.fromFile(new File(audioFile));

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

            final StorageReference riversRef = mStorageRef.child("Audio").child(imgName);

            riversRef.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get a URL to the uploaded content
                        riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        livePicPath = "" + uri;
                                        File file = new File(audioFile);
                                        file.delete();
                                        sendMessageToDb(Constants.MESSAGE_TYPE_AUDIO);

                                    }
                                });


                            }
                        });


                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        // ...


                    });
        } catch (Exception e) {
//            mDatabase.child("Errors").child("mainError").child(mDatabase.push().getKey()).setValue(e.getMessage());
        }


    }

}