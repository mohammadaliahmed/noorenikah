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
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
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
        adapter = new PostsAdapter(getContext(), itemList);
        recycler.setAdapter(adapter);

        getDataFromDB();
        return rootView;
    }

    private void getDataFromDB() {
        mDatabase.child("Posts").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel model = snapshot.getValue(PostModel.class);
                    if (model != null) {
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
