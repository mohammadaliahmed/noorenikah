package com.appsinventiv.noorenikah.Activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.ChatAdapter;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.KeyboardUtils;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatScreen extends AppCompatActivity {
    EditText message;
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
    private String otherUserPhone;
    boolean screenActive;

    @Override
    protected void onResume() {
        super.onResume();
        screenActive = true;
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
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        otherUserPhone = getIntent().getStringExtra("phone");
        adapter = new ChatAdapter(this, itemList);

        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        name = findViewById(R.id.name);
        picture = findViewById(R.id.picture);
        send = findViewById(R.id.send);
        message = findViewById(R.id.message);
        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {


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
                    sendMessageToDb();
                }
            }
        });
        getOtherUserFromDb();
        getDataFromDb();

    }

    @Override
    protected void onPause() {
        super.onPause();
        screenActive = false;
    }

    private void getOtherUserFromDb() {
        mDatabase.child("Users").child(otherUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    otherUser = dataSnapshot.getValue(User.class);
                    name.setText(otherUser.getName());
                    Glide.with(ChatScreen.this).load(otherUser.getLivePicPath()).into(picture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataFromDb() {

        mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone())
                .child(otherUserPhone).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            itemList.clear();
                            if (screenActive) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    ChatModel model = snapshot.getValue(ChatModel.class);
                                    if (model != null && model.getMessage() != null) {
                                        itemList.add(model);
                                    }
                                }
                                adapter.setItemList(itemList);
                                recyclerView.scrollToPosition(itemList.size() - 1);
                                mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone())
                                        .child(otherUserPhone).child(itemList.get(itemList.size() - 1).getId()).child("read").setValue(true);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void sendMessageToDb() {
        msg = message.getText().toString();
        message.setText("");
        String key = "" + System.currentTimeMillis();
        ChatModel myModel = new ChatModel(key, msg, SharedPrefs.getUser().getPhone(), otherUserPhone,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                otherUser.getName(),
                otherUser.getPhone(),
                otherUser.getLivePicPath(),
                true,
                System.currentTimeMillis());
        mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone()).child(otherUserPhone).child(key).setValue(
                myModel
        );
        ChatModel hisModel = new ChatModel(key, msg, SharedPrefs.getUser().getPhone(), otherUserPhone,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getLivePicPath(),
                otherUser.getName(),
                otherUser.getPhone(),
                otherUser.getLivePicPath(),
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                false,
                System.currentTimeMillis());

        mDatabase.child("Chats").child(otherUserPhone)
                .child(SharedPrefs.getUser().getPhone()).child(key).setValue(hisModel);
        sendNotification();
    }

    private void sendNotification() {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New message from: " + SharedPrefs.getUser().getName();
        String NotificationMessage = msg;
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


}