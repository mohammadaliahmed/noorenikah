package com.appsinventiv.noorenikah.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.ChatScreen;
import com.appsinventiv.noorenikah.Activities.PaymentsHistory;
import com.appsinventiv.noorenikah.Adapters.CallLogsAdapter;
import com.appsinventiv.noorenikah.Models.CallModel;
import com.appsinventiv.noorenikah.Models.CommentReplyModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.appsinventiv.noorenikah.call.CallManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CallsLogsFragment extends Fragment {

    private View rootView;
    DatabaseReference mDatabase;
    CallLogsAdapter adapter;
    private List<CallModel> logsList = new ArrayList<>();
    RecyclerView recycler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.calllogs_fragment, container, false);
        mDatabase = Constants.M_DATABASE;
        recycler = rootView.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new CallLogsAdapter(getContext(), logsList, new CallLogsAdapter.CallLogsCallback() {
            @Override
            public void DialCall(String phone, boolean video) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Alert");
                builder.setMessage("Dial call?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("Users").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                UserModel userModel = snapshot.getValue(UserModel.class);
                                ArrayList<UserModel> userList = new ArrayList<>();
                                userList.add(userModel);
                                CallManager callManager = new CallManager(userModel.getFcmKey(),
                                        getContext(), "test", 1L, Long.valueOf(userModel.getId()), userList);
                                if (video) {
                                    callManager.callHandler(CallManager.CallType.INDIVIDUAL_VIDEO);
                                } else {
                                    callManager.callHandler(CallManager.CallType.INDIVIDUAL_AUDIO);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancel", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });
        recycler.setAdapter(adapter);

        getLogsFromDB();
        return rootView;
    }

    private void getLogsFromDB() {
        mDatabase.child("Calls").child(SharedPrefs.getUser().getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logsList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        CallModel model = snapshot2.getValue(CallModel.class);
                        if (model != null) {
                            logsList.add(model);
                        }
                    }

                }
                Collections.sort(logsList, new Comparator<CallModel>() {
                    @Override
                    public int compare(CallModel listData, CallModel t1) {
                        Long ob1 = listData.getStartTime();
                        Long ob2 = t1.getStartTime();
                        return ob2.compareTo(ob1);

                    }
                });
                adapter.setItemList(logsList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
