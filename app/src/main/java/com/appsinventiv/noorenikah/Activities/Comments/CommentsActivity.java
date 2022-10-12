package com.appsinventiv.noorenikah.Activities.Comments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.CommentsAdapter;
import com.appsinventiv.noorenikah.Models.CommentReplyModel;
import com.appsinventiv.noorenikah.Models.CommentsModel;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.KeyboardUtils;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    RecyclerView recycler;
    EditText comment;
    ImageView send;
    AdView adView;
    DatabaseReference mDatabase;
    String id;
    private String cmnt;
    private List<CommentsModel> itemList = new ArrayList<>();
    CommentsAdapter adapter;
    private User otherUser;
    private AdView mAdView;
    private AdRequest adRequest;
    boolean replying;
    String commentId;
    RelativeLayout replyingToView;
    TextView replyingToName;
    ImageView closeReply;
    private String replyingToId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Comments");
        adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.adView);
        replyingToView = findViewById(R.id.replyingToView);
        closeReply = findViewById(R.id.closeReply);
        replyingToName = findViewById(R.id.replyingToName);
        recycler = findViewById(R.id.recycler);
        send = findViewById(R.id.send);
        comment = findViewById(R.id.comment);
        mDatabase = Constants.M_DATABASE;
        closeReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeReplyingView();
            }
        });
        adapter = new CommentsAdapter(this, itemList, new CommentsAdapter.CommentsAdapterCallbacks() {
            @Override
            public void onReplyClicked(CommentsModel model) {
                replying = true;
                commentId = model.getId();
                replyingToId = model.getPhone();
                replyingToView.setVisibility(View.VISIBLE);
                replyingToName.setText("Replying to: " + model.getCommentByName());
            }

            @Override
            public void onShowOptions(CommentsModel model) {
                showAlert(model);
            }

            @Override
            public void onShowReplyOptions(CommentReplyModel model) {
                showReplyAlert(model);
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recycler.setAdapter(adapter);
        id = getIntent().getStringExtra("id");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                recycler.scrollToPosition(itemList.size() - 1);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getText().length() == 0) {
                    comment.setError("Enter comment");
                } else {
                    cmnt = comment.getText().toString();
                    comment.setText("");
                    if (replying) {
                        sendCommentReply();
                    } else {
                        sendCommentToDb();
                    }
                }
            }
        });
        getDataFromDB();
        getUserFromDB();
    }

    private void removeReplyingView() {
        replying = false;
        commentId = null;
        replyingToId = null;
        replyingToView.setVisibility(View.GONE);
    }

    private void sendCommentReply() {

        String replyKey = "" + System.currentTimeMillis();
        CommentReplyModel model = new CommentReplyModel(
                replyKey,
                cmnt,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                commentId,
                System.currentTimeMillis()
        );
        if(commentId!=null) {
            mDatabase.child("Comments").child(id).child(commentId).child("replies").child(replyKey).setValue(model);
            if (!replyingToId.equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                mDatabase.child("Users").child(replyingToId).child("fcmKey").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String fcm = dataSnapshot.getValue(String.class);
                        NotificationAsync notificationAsync = new NotificationAsync(CommentsActivity.this);
                        String NotificationTitle = SharedPrefs.getUser().getName() + " replied to your comment";
                        String NotificationMessage = "Comment: " + cmnt;
                        notificationAsync.execute(
                                "ali",
                                fcm,
                                NotificationTitle,
                                NotificationMessage,
                                id,
                                "comment");
                        String key = "" + System.currentTimeMillis();
                        NotificationModel model = new NotificationModel(key, NotificationTitle,
                                NotificationMessage, "comment", SharedPrefs.getUser().getLivePicPath(), SharedPrefs.getUser().getPhone(), System.currentTimeMillis());
                        mDatabase.child("Notifications").child(id).child(key).setValue(model);
                        removeReplyingView();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void showReplyAlert(CommentReplyModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Delete comment?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatabase.child("Comments").child(id).child(model.getParentId()).child("replies").child(model.getId())
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                CommonUtils.showToast("Comment deleted");
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showAlert(CommentsModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Delete comment?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDatabase.child("Comments").child(id).child(model.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        CommonUtils.showToast("Comment deleted");
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getUserFromDB() {
        mDatabase.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherUser = dataSnapshot.getValue(User.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataFromDB() {
        mDatabase.child("Comments").child(id).limitToLast(150).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentsModel model = snapshot.getValue(CommentsModel.class);
                    itemList.add(model);
                }
                adapter.setItemList(itemList);
                recycler.scrollToPosition(itemList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendCommentToDb() {
        String key = "" + System.currentTimeMillis();
        CommentsModel model = new CommentsModel(
                key,
                cmnt,
                SharedPrefs.getUser().getName(),
                SharedPrefs.getUser().getPhone(),
                SharedPrefs.getUser().getLivePicPath(),
                System.currentTimeMillis()
        );
        mDatabase.child("Comments").child(id).child(key).setValue(model);
        if (!SharedPrefs.getUser().getPhone().equalsIgnoreCase(id)) {
            if (otherUser != null) {
                sendNotification();
            }
        }
    }

    private void sendNotification() {
        NotificationAsync notificationAsync = new NotificationAsync(this);
        String NotificationTitle = "New comment posted by " + SharedPrefs.getUser().getName();
        String NotificationMessage = "Comment: " + cmnt;
        notificationAsync.execute(
                "ali",
                otherUser.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                id,
                "comment");
        String key = "" + System.currentTimeMillis();
        NotificationModel model = new NotificationModel(key, NotificationTitle,
                NotificationMessage, "comment", SharedPrefs.getUser().getLivePicPath(), SharedPrefs.getUser().getPhone(), System.currentTimeMillis());
        mDatabase.child("Notifications").child(id).child(key).setValue(model);
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