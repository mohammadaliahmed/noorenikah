package com.appsinventiv.noorenikah.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.ChatListAdapter;
import com.appsinventiv.noorenikah.Adapters.NotificationAdapter;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class NotificationFragment extends Fragment {


    private View rootView;
    private DatabaseReference mDatabase;
    private List<NotificationModel> itemList = new ArrayList<>();
    NotificationAdapter adapter;
    RecyclerView recycler;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_notifications, container, false);

        recycler = rootView.findViewById(R.id.recycler);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        adapter = new NotificationAdapter(getContext(), itemList, new NotificationAdapter.NotificationAdapterCallbacks() {
            @Override
            public void onDeleteClicked(NotificationModel model) {
                mDatabase.child("Notifications").child(SharedPrefs.getUser().getPhone()).child(model.getId()).removeValue();
                CommonUtils.showToast("Cleared");
//                showAlert(model);
            }
        });
        recycler.setAdapter(adapter);
        if (SharedPrefs.getUser() != null) {
            mDatabase.child("Notifications").child(SharedPrefs.getUser().getPhone()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    itemList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        NotificationModel model = snapshot.getValue(NotificationModel.class);
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


        return rootView;
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}