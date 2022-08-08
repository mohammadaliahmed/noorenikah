package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appsinventiv.noorenikah.Activities.EditProfile;
import com.appsinventiv.noorenikah.Activities.InviteHistory;
import com.appsinventiv.noorenikah.Activities.ListOfFriends;
import com.appsinventiv.noorenikah.Activities.PaymentsHistory;
import com.appsinventiv.noorenikah.Activities.Splash;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;

public class MenuFragment extends Fragment {


    private View rootView;
    Button logout;

    TextView name;
    RelativeLayout editProfile, requestAccepted;
    RelativeLayout paymentsHistory;
    RelativeLayout invite;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        name = rootView.findViewById(R.id.name);
        paymentsHistory = rootView.findViewById(R.id.paymentsHistory);
        editProfile = rootView.findViewById(R.id.editProfile);
        requestAccepted = rootView.findViewById(R.id.requestAccepted);
        invite = rootView.findViewById(R.id.invite);
        logout = rootView.findViewById(R.id.logout);
        paymentsHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PaymentsHistory.class));
            }
        });
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), InviteHistory.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefs.logout();
                Intent i = new Intent(getActivity(), Splash.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                getActivity().finish();
            }
        });
        name.setText("Hi, " + SharedPrefs.getUser().getName());
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProfile.class));
            }
        });
        requestAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ListOfFriends.class));
            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}