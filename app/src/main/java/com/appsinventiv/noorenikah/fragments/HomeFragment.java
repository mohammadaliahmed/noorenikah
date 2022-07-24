package com.appsinventiv.noorenikah.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appsinventiv.noorenikah.LatestFrauds;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.ReportScreen;
import com.appsinventiv.noorenikah.VerifyScreen;

public class HomeFragment extends Fragment {


    private View rootView;
    LinearLayout verify,report, latestFraud;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        report = rootView.findViewById(R.id.report);
        verify = rootView.findViewById(R.id.verify);
        latestFraud = rootView.findViewById(R.id.latestFraud);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), VerifyScreen.class));
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ReportScreen.class));
            }
        });
        latestFraud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LatestFrauds.class));
            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}