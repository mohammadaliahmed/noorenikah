package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.PaymentProof;
import com.appsinventiv.noorenikah.Activities.ViewFriendProfile;
import com.appsinventiv.noorenikah.Adapters.RequestsAdapter;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {


    private View rootView;
    RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private List<User> userList = new ArrayList<>();
    RequestsAdapter adapter;
    TextView noRequests;
    ProgressBar progress;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_request_received, container, false);
        progress=rootView.findViewById(R.id.progress);
        noRequests=rootView.findViewById(R.id.noRequests);
        adapter = new RequestsAdapter(getActivity(), userList, new RequestsAdapter.RequestsAdapterCallbacks() {
            @Override
            public void onAcceptClicked(User user) {
                if(SharedPrefs.getUser().isPaid()) {
                    mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone())
                            .child("received").child(user.getPhone()).removeValue();
                    mDatabase.child("Requests").child(user.getPhone())
                            .child("sent").child(SharedPrefs.getUser().getPhone()).removeValue();

                    mDatabase.child("Users").child(SharedPrefs.getUser().getPhone()).child("friends")
                            .child(user.getPhone()).setValue(user.getPhone());
                    mDatabase.child("Users").child(user.getPhone()).child("friends")
                            .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
                    CommonUtils.showToast("Accepted");
                    sendNotification(user);
                    Intent i = new Intent(getActivity(), ViewFriendProfile.class);
                    i.putExtra("phone", user.getPhone());
                    startActivity(i);
                }else{
                    Intent i = new Intent(getActivity(), PaymentProof.class);
                    startActivity(i);
                }

            }

            @Override
            public void onRejectClicked(User user) {
                mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone())
                        .child("received").child(user.getPhone()).removeValue();
                mDatabase.child("Requests").child(user.getPhone())
                        .child("sent").child(SharedPrefs.getUser().getPhone()).removeValue();
                CommonUtils.showToast("Rejected");


            }
        });
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        mDatabase = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
        getDataFromDB();
        return rootView;
    }

    private void sendNotification(User user) {

        NotificationAsync notificationAsync = new NotificationAsync(getActivity());
        String NotificationTitle = "Request Accepted by: "+SharedPrefs.getUser().getName();
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                user.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "accepted");

        String key=""+System.currentTimeMillis();
        NotificationModel model=new NotificationModel(key,NotificationTitle,
                NotificationMessage,"accepted",user.getLivePicPath(),SharedPrefs.getUser().getPhone(),System.currentTimeMillis());
        mDatabase.child("Notifications").child(user.getPhone()).child(key).setValue(model);
    }

    private void getDataFromDB() {
        mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone()).child("received")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList = new ArrayList<>();
                        progress.setVisibility(View.GONE);

                        if (dataSnapshot.getValue() != null) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userId = snapshot.getValue(String.class);
                                getUserData(userId);
                            }
                        } else {
//                            adapter.setUserList(userList);
                            noRequests.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getUserData(String userId) {
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null && user.getName() != null) {
                    userList.add(user);
                    adapter.setUserList(userList);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}