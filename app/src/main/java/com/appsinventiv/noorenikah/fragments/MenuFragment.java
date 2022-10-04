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
import com.appsinventiv.noorenikah.Activities.MatchMaker.MatchMakerProfile;
import com.appsinventiv.noorenikah.Activities.PaymentsHistory;
import com.appsinventiv.noorenikah.Activities.MatchMaker.RegisterMatchMaker;
import com.appsinventiv.noorenikah.Activities.Splash;
import com.appsinventiv.noorenikah.Activities.VerifyPhone;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.AlertsUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;

public class MenuFragment extends Fragment {

    private View rootView;
    Button logout;
    RelativeLayout editProfile, requestAccepted, privacy, terms, matchMaker;
    RelativeLayout paymentsHistory;
    RelativeLayout invite;
    TextView verified;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        paymentsHistory = rootView.findViewById(R.id.paymentsHistory);
        editProfile = rootView.findViewById(R.id.editProfile);
        requestAccepted = rootView.findViewById(R.id.requestAccepted);
        verified = rootView.findViewById(R.id.verified);
        matchMaker = rootView.findViewById(R.id.matchMaker);
        invite = rootView.findViewById(R.id.invite);
        privacy = rootView.findViewById(R.id.privacy);
        terms = rootView.findViewById(R.id.terms);
        logout = rootView.findViewById(R.id.logout);
        matchMaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPrefs.getUser().isMatchMakerProfile()) {
                    startActivity(new Intent(getContext(), MatchMakerProfile.class));
                } else {
                    startActivity(new Intent(getContext(), RegisterMatchMaker.class));

                }
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertsUtils.showTermsAlert(getContext());
            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertsUtils.showPrivacyAlert(getContext());
            }
        });
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

        if (SharedPrefs.getUser().isPhoneVerified()) {
            verified.setVisibility(View.GONE);
        } else {
            verified.setVisibility(View.VISIBLE);
        }
        verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), VerifyPhone.class));
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}