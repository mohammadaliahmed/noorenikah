package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.Posts.AddPost;
import com.appsinventiv.noorenikah.Adapters.PostsAdapter;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostsFragment extends Fragment {
    private View rootView;
    Button addPost;
    DatabaseReference mDatabase;
    private List<PostModel> itemList = new ArrayList<>();
    RecyclerView recycler;
    PostsAdapter adapter;
    ImageView promotionBanner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        mDatabase = Constants.M_DATABASE;
        promotionBanner = rootView.findViewById(R.id.promotionBanner);

        if (SharedPrefs.getPromotionalBanner() != null) {
            Glide.with(getContext()).load(SharedPrefs.getPromotionalBanner().getImgUrl())
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

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new PostsAdapter(getContext(), itemList, new PostsAdapter.PostsAdapterCallbacks() {
            @Override
            public void onLiked(PostModel model, boolean liked) {
                if (liked) {
                    mDatabase.child("PostLikes").child(model.getId())
                            .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
                    if(!model.getUserId().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
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
        });
        recycler.setAdapter(adapter);


        return rootView;
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


    @Override
    public void onResume() {
        super.onResume();
        getDataFromDB();

    }

    private void getDataFromDB() {
        itemList.clear();
        mDatabase.child("Posts").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel model = snapshot.getValue(PostModel.class);
                    if (model != null && model.isApproved()) {
                        itemList.add(model);
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
