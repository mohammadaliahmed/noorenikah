package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.EditProfile;
import com.appsinventiv.noorenikah.Activities.ListOfFriends;
import com.appsinventiv.noorenikah.Activities.Splash;
import com.appsinventiv.noorenikah.Adapters.ChatListAdapter;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment {
    private View rootView;
    HashMap<String, ChatModel> itemMap=new HashMap<>();
    RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    ChatListAdapter adapter;
    private List<ChatModel> itemList=new ArrayList<>();
    ImageView promotionBanner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        mDatabase = Constants.M_DATABASE;
        promotionBanner=rootView.findViewById(R.id.promotionBanner);
        recyclerView=rootView.findViewById(R.id.recyclerView);
        adapter=new ChatListAdapter(getContext(),itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);


        itemMap=SharedPrefs.getChatMap();
        if(itemMap==null){
            itemMap=new HashMap<>();
            itemList=new ArrayList<>(itemMap.values());
            adapter.setItemList(itemList);
        }else{
            itemList=new ArrayList<>(itemMap.values());
            Collections.sort(itemList, new Comparator<ChatModel>() {
                @Override
                public int compare(ChatModel listData, ChatModel t1) {
                    Long ob1 = listData.getTime();
                    Long ob2 = t1.getTime();
                    return ob2.compareTo(ob1);
                }
            });
            adapter.setItemList(itemList);
        }
        getDataFromDB();
        if (SharedPrefs.getPromotionalBanner("chatScreen") != null) {
            Glide.with(getContext()).load(SharedPrefs.getPromotionalBanner("chatScreen").getImgUrl())
                    .into(promotionBanner);

        }
        promotionBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(SharedPrefs.getPromotionalBanner("chatScreen").getUrl()));
                getContext().startActivity(i);
            }
        });

        return rootView;
    }

    private void getDataFromDB() {

        mDatabase.child("Chats").child(SharedPrefs.getUser().getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                itemMap=new HashMap<>();
                if(dataSnapshot.getValue()!=null){
                    for (DataSnapshot phoneNumbers:dataSnapshot.getChildren()) {
                        for (DataSnapshot msgs:phoneNumbers.getChildren()){
                            ChatModel model=msgs.getValue(ChatModel.class);
                            itemMap.put(phoneNumbers.getKey(),model);
                        }
                    }
                    itemList=new ArrayList<>(itemMap.values());
                    Collections.sort(itemList, new Comparator<ChatModel>() {
                        @Override
                        public int compare(ChatModel listData, ChatModel t1) {
                            Long ob1 = listData.getTime();
                            Long ob2 = t1.getTime();
                            return ob2.compareTo(ob1);

                        }
                    });
                    adapter.setItemList(itemList);
                    SharedPrefs.setChatMap(itemMap);


                }else{
                    SharedPrefs.setChatMap(itemMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}