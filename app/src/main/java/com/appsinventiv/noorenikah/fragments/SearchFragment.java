package com.appsinventiv.noorenikah.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appsinventiv.noorenikah.R;

public class SearchFragment extends Fragment {
    private View rootView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_search, container, false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}