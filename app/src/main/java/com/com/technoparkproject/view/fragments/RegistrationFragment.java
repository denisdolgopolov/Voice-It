package com.com.technoparkproject.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.activities.MainActivity;

public class RegistrationFragment extends Fragment {

    private static final int FRAGMENT_REGISTRATION_NAME = R.string.fragment_registration_name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(FRAGMENT_REGISTRATION_NAME));
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }
}
