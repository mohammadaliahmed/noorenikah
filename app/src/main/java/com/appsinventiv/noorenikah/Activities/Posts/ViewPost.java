package com.appsinventiv.noorenikah.Activities.Posts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.appsinventiv.noorenikah.Activities.ViewUserProfile;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPost extends AppCompatActivity {

    String postId;
    DatabaseReference mDatabase;
    private PostModel postModel;
    TextView name, text, commentCount, likesCount, time;
    ImageView postImage, likeUnlike, comment, share, removePost;
    CircleImageView userImage;
    RelativeLayout userView;
    LinearLayout commentCountView,likeCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("View Post");
        text = findViewById(R.id.text);
        likeCountView = findViewById(R.id.likeCountView);
        commentCountView = findViewById(R.id.commentCountView);
        name = findViewById(R.id.name);
        postImage = findViewById(R.id.postImage);
        likeUnlike = findViewById(R.id.likeUnlike);
        comment = findViewById(R.id.comment);
        share = findViewById(R.id.share);
        likesCount = findViewById(R.id.likesCount);
        time = findViewById(R.id.time);
        commentCount = findViewById(R.id.commentCount);
        userView = findViewById(R.id.userView);
        userImage = findViewById(R.id.userImage);
        removePost = findViewById(R.id.removePost);
        mDatabase = Constants.M_DATABASE;
        postId = getIntent().getStringExtra("postId");


    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataFromDB();
    }

    private void getDataFromDB() {
        mDatabase.child("Posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    postModel = dataSnapshot.getValue(PostModel.class);
                    setUpdata();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpdata() {
        if (postModel.getType().equalsIgnoreCase("text")) {
            postImage.setVisibility(View.GONE);
        } else if (postModel.getType().equalsIgnoreCase("image")) {
            postImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(postModel.getImageUrl()).into(postImage);

        }
        if (postModel.getUserId().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
            removePost.setVisibility(View.VISIBLE);
        } else {
            removePost.setVisibility(View.GONE);

        }


        HashMap<String, String> map = SharedPrefs.getPostLikedMap();
        if (map != null) {
            if (map.containsKey(postModel.getId())) {
                postModel.setLiked(true);
            } else {
                postModel.setLiked(false);
            }
        }
        if (postModel.isLiked()) {
            likeUnlike.setImageResource(R.drawable.ic_like_filled);
        } else {
            likeUnlike.setImageResource(R.drawable.ic_like_empty);

        }

        likeUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postModel.isLiked()) {
                    HashMap<String, String> map = SharedPrefs.getPostLikedMap();
                    if (map != null) {
                        if (map.containsKey(postModel.getId())) {
                            map.remove(postModel.getId());
                            postModel.setLiked(false);
                            SharedPrefs.setPostLikedMap(map);
                        }
                    }
                    likeUnlike.setImageResource(R.drawable.ic_like_empty);
                    postModel.setLikeCount(postModel.getLikeCount() - 1);
                    likesCount.setText((postModel.getLikeCount()) + " likes");

                    onLiked(postModel, false);

                } else {
                    likeUnlike.setImageResource(R.drawable.ic_like_filled);
                    HashMap<String, String> map = SharedPrefs.getPostLikedMap();
                    if (map != null) {
                        map.put(postModel.getId(), postModel.getId());
                    } else {
                        map = new HashMap<>();
                        map.put(postModel.getId(), postModel.getId());
                    }
                    SharedPrefs.setPostLikedMap(map);
                    postModel.setLiked(true);
                    postModel.setLikeCount(postModel.getLikeCount() + 1);

                    likesCount.setText((postModel.getLikeCount()) + " likes");
                    onLiked(postModel, true);
                }
            }
        });

        Glide.with(this).load(postModel.getUserPicUrl()).into(userImage);
        name.setText(postModel.getUserName());
        text.setText(postModel.getText());
        commentCount.setText(postModel.getCommentCount() + " comments");
        likesCount.setText(postModel.getLikeCount() + " likes");
        time.setText(CommonUtils.getFormattedDate(postModel.getTime()));
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewPost.this, PostComments.class);
                i.putExtra("postId", postModel.getId());
                startActivity(i);
            }
        });
        commentCountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewPost.this, PostComments.class);
                i.putExtra("postId", postModel.getId());
                ViewPost.this.startActivity(i);
            }
        });
        likeCountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewPost.this, PostLikes.class);
                i.putExtra("postId", postModel.getId());
                startActivity(i);
            }
        });
        userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewPost.this, ViewUserProfile.class);
                i.putExtra("phone", postModel.getUserId());
                ViewPost.this.startActivity(i);
            }
        });
        removePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRemovePost(postModel);
            }
        });

    }

    private void onRemovePost(PostModel postModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to delete this? ");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child("PostsByUsers").child(SharedPrefs.getUser().getPhone()).child(postModel.getId()).removeValue();
                mDatabase.child("Posts").child(postModel.getId()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                       finish();
                    }
                });


            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onLiked(PostModel model, boolean liked) {
        if (liked) {
            mDatabase.child("PostLikes").child(model.getId())
                    .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
            if (!model.getUserId().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                sendLikeNotification(model);
            }
        } else {
            mDatabase.child("PostLikes").child(model.getId())
                    .child(SharedPrefs.getUser().getPhone()).removeValue();
        }
        mDatabase.child("Posts").child(model.getId()).child("likeCount").setValue(model.getLikeCount());
    }

    private void sendLikeNotification(PostModel postModel) {

        mDatabase.child("Users").child(postModel.getUserId()).child("fcmKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fcmKey = dataSnapshot.getValue(String.class);
                NotificationAsync notificationAsync = new NotificationAsync(ViewPost.this);
                String NotificationTitle = "New Like";
                String NotificationMessage = SharedPrefs.getUser().getName() + " liked your post";
                notificationAsync.execute(
                        "ali",
                        fcmKey,
                        NotificationTitle,
                        NotificationMessage,
                        postModel.getId(),
                        "postlike");
                String key = "" + System.currentTimeMillis();
                NotificationModel model = new NotificationModel(key, NotificationTitle,
                        NotificationMessage, "postlike", SharedPrefs.getUser().getLivePicPath(),
                        postModel.getId(), System.currentTimeMillis());
                mDatabase.child("Notifications").child(postModel.getUserId()).child(key).setValue(model);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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