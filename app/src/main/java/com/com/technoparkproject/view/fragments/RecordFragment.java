package com.com.technoparkproject.view.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.activities.MainActivity;
import com.com.technoparkproject.service.RecordingService;
import com.com.technoparkproject.viewmodels.RecorderViewModel;

import org.jetbrains.annotations.NotNull;


public class RecordFragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1000;
    private Button mRecPauseButton;
    private Button mStopButton;
    private TextView mTimeTextView;
    private Button mDoneButton;


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkRecordPermissions() {
        if (requireActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_RECORD_AUDIO
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {


            //TODO: imitate exiting fragment or other behaviour
            TextView textView = requireActivity().findViewById(R.id.record_state_text);
            textView.setText(R.string.text_record_permission_denied);
        }
    }


    private static final int FRAGMENT_RECORD_NAME = R.string.fragment_record_name;

    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkRecordPermissions();
        }

        mRecPauseButton = view.findViewById(R.id.record_pause_button);
        mRecPauseButton.setText("Запись");
        mStopButton = view.findViewById(R.id.stop_button);
        mStopButton.setText("Стоп");
        mDoneButton = view.findViewById(R.id.done_button);
        mDoneButton.setText("Готово");
        mTimeTextView = view.findViewById(R.id.record_time_text);

        final RecorderViewModel recViewModel = new ViewModelProvider(requireActivity()).get(RecorderViewModel.class);
        mRecPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recViewModel.OnRecPauseClick();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recViewModel.onStopClick();
            }
        });


        final EditText recNameEdit = view.findViewById(R.id.record_name_edit_text);
        final EditText recTopicEdit = view.findViewById(R.id.topic_edit_text);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recName = recNameEdit.getText().toString();
                String topicName = recTopicEdit.getText().toString();
                recViewModel.onSaveClick(recName, topicName);
            }
        });


        recViewModel.getRecState()
                .observe(getViewLifecycleOwner(), new MyObserver());

        recViewModel.getRecTime().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer sec) {
                mTimeTextView.setText(DateUtils.formatElapsedTime(sec));
            }
        });

        final ProgressBar stopProgress = view.findViewById(R.id.record_stop_progress);
        stopProgress.setVisibility(View.GONE);
        recViewModel.mRecStopState.observe(getViewLifecycleOwner(), new Observer<RecorderViewModel.RecStopState>() {
            @Override
            public void onChanged(RecorderViewModel.RecStopState recStopState) {
                if (recStopState == RecorderViewModel.RecStopState.STOP_IN_PROGRESS)
                    stopProgress.setVisibility(View.VISIBLE);
                else if (recStopState == RecorderViewModel.RecStopState.STOP_COMPLETED)
                    stopProgress.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(FRAGMENT_RECORD_NAME));
        return inflater.inflate(R.layout.fragment_record, container, false);
    }


    private class MyObserver implements Observer<RecordingService.RecordState> {
        @Override
        public void onChanged(RecordingService.RecordState recordState) {
            switch (recordState){
                case READY:{
                    mStopButton.setEnabled(false);
                    mDoneButton.setEnabled(false);
                    break;
                }
                case RECORDING:{
                    mRecPauseButton.setText("Пауза");
                    mStopButton.setEnabled(true);
                    mDoneButton.setEnabled(true);
                    break;
                }
                case PAUSE:
                    mRecPauseButton.setText("Запись");
                    break;
                case STOP:{
                    mRecPauseButton.setText("Запись");
                    mStopButton.setEnabled(false);
                    mRecPauseButton.setEnabled(false);
                    break;
                }
            }
        }
    }
}