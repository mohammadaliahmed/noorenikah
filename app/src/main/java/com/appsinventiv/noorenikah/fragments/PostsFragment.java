package com.appsinventiv.noorenikah.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.Posts.AddPost;
import com.appsinventiv.noorenikah.Adapters.PostsAdapter;
import com.appsinventiv.noorenikah.Models.LikesModel;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PostsFragment extends Fragment {
    private View rootView;
    Button addPost;
    DatabaseReference mDatabase;
    private List<PostModel> itemList = new ArrayList<>();
    RecyclerView recycler;
    PostsAdapter adapter;
    ImageView promotionBanner;

    private AdView mAdView;
    private AdRequest adRequest;

    ProgressBar progress;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder().build();
        progress = rootView.findViewById(R.id.progress);
        mAdView = rootView.findViewById(R.id.adView);
        mAdView.loadAd(adRequest);
        mDatabase = Constants.M_DATABASE;
        promotionBanner = rootView.findViewById(R.id.promotionBanner);

        if (SharedPrefs.getPromotionalBanner("postsScreen") != null) {
            Glide.with(getContext()).load(SharedPrefs.getPromotionalBanner("postsScreen").getImgUrl())
                    .into(promotionBanner);
        }
        addPost = rootView.findViewById(R.id.addPost);
        recycler = rootView.findViewById(R.id.recycler);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddPost.class));
            }
        });
        promotionBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(SharedPrefs.getPromotionalBanner("postsScreen").getUrl()));
                getContext().startActivity(i);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new PostsAdapter(getContext(), itemList, new PostsAdapter.PostsAdapterCallbacks() {
            @Override
            public void onLiked(PostModel model, boolean liked) {
                if (liked) {
                    LikesModel likesModel = new LikesModel(
                            SharedPrefs.getUser().getPhone(), SharedPrefs.getUser().getPhone(), SharedPrefs.getUser().getName(),
                            SharedPrefs.getUser().getLivePicPath());
                    mDatabase.child("PostLikes").child(model.getId()).child(SharedPrefs.getUser().getPhone())
                            .setValue(likesModel);
                    if (!model.getUserId().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                        sendLikeNotification(model);
                    }
                } else {
                    mDatabase.child("PostLikes").child(model.getId())
                            .child(SharedPrefs.getUser().getPhone()).removeValue();
                }
                mDatabase.child("Posts").child(model.getId()).child("likeCount").setValue(model.getLikeCount());

            }

            @Override
            public void onComment(PostModel model) {

            }

            @Override
            public void onShare(PostModel model) {

            }

            @Override
            public void onRemovePost(PostModel model) {
                showAlert(model);
            }

            @Override
            public void onReportPost(PostModel model, int pos) {
                ShowReportAlert(model, pos);
            }
        });
        recycler.setAdapter(adapter);

        getDataFromDB();

        return rootView;
    }

    private void ShowReportAlert(PostModel model, int pos) {
        final String[] reason = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose a reason for report");

// add a radio button list
        String[] reasonList = {
                "It's Spam",
                "Hate Speech",
                "I don't like it",
                "False information",
                "Bully or harassment",
                "Scam or fraud",
                "Violence or Dangerous",
                "Scam or fraud",
                "Sexual or nudity",
                "Something else",

        };
        builder.setSingleChoiceItems(reasonList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                reason[0] = reasonList[which];
            }
        });

// add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                HashMap<String, String> map = new HashMap<>();
                map.put("reason", reason[0]);
                map.put("phone", SharedPrefs.getUser().getPhone());
                mDatabase.child("PostReports").child(model.getId())
                        .child(SharedPrefs.getUser().getPhone()).setValue(map);
                adapter.notifyItemRemoved(pos);
                mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("iBlocked")
                        .child(model.getUserId()).setValue(model.getUserId());
                mDatabase.child("Users").child(model.getUserId()).child("blockedMe")
                        .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
                CommonUtils.showToast("Post reported\nThanks for feedback\nWe will take action");
            }
        });
        builder.setNegativeButton("Cancel", null);

// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showAlert(PostModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Alert");
        builder.setMessage("Do you want to delete this? ");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child("Posts").child(model.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        CommonUtils.showToast("Post deleted");
                        getDataFromDB();
                    }
                });


            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendLikeNotification(PostModel postModel) {

        mDatabase.child("Users").child(postModel.getUserId()).child("fcmKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fcmKey = dataSnapshot.getValue(String.class);
                NotificationAsync notificationAsync = new NotificationAsync(getContext());
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


    private void getDataFromDB() {
        itemList.clear();
        progress.setVisibility(View.VISIBLE);
        mDatabase.child("Posts").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel model = snapshot.getValue(PostModel.class);
                    if (model != null && model.getType() != null) {
                        if (SharedPrefs.getUser().getiBlocked() != null) {
                            if (!SharedPrefs.getUser().getiBlocked().containsKey(model.getUserId())) {
                                itemList.add(model);
                            }
                        } else {
                            itemList.add(model);
                        }


                    }
                }
                Collections.reverse(itemList);
                adapter.setItemList(itemList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
