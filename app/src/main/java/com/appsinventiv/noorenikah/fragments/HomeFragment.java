package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Adapters.UsersRecyclerAdapter;
import com.appsinventiv.noorenikah.LatestFrauds;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.ReportScreen;
import com.appsinventiv.noorenikah.Utils.NotificationAsync;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.VerifyScreen;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {


    private View rootView;
    RecyclerView recycler;
    DatabaseReference mDatabase;
    private List<User> usersList=new ArrayList<>();
    UsersRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recycler=rootView.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        adapter=new UsersRecyclerAdapter(getContext(), usersList, new UsersRecyclerAdapter.UsersAdapterCallbacks() {
            @Override
            public void onLikeClicked(User user) {

            }

            @Override
            public void onRequestClicked(User user) {
                sendNotification(user);

            }
        });
        recycler.setAdapter(adapter);


        mDatabase= FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();

        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    usersList.clear();
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                        User user=snapshot.getValue(User.class);
                        if(user!=null && user.getName()!=null){
                            String myGender = SharedPrefs.getUser().getGender();
                            if(!myGender.equals(user.getGender())){
                                usersList.add(user);
                            }
                        }
                    }
                    adapter.setUserList(usersList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    private void sendNotification(User user) {
        NotificationAsync notificationAsync = new NotificationAsync(getContext());
        String NotificationTitle = "New request";
        String NotificationMessage = "Click to view";
        notificationAsync.execute(
                "ali",
                SharedPrefs.getFcmKey(),
                NotificationTitle,
                NotificationMessage,
                SharedPrefs.getUser().getPhone(),
                "");
        mDatabase.child("Requests").child(SharedPrefs.getUser().getPhone())
                .child("sent").child(user.getPhone()).setValue(user.getPhone());
        mDatabase.child("Requests").child(user.getPhone()).child("received")
                .child(SharedPrefs.getUser().getPhone()).setValue(SharedPrefs.getUser().getPhone());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}