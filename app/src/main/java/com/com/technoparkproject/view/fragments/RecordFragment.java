package com.com.technoparkproject.view.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.activities.MainActivity;
import com.com.technoparkproject.RecordingService;

import org.jetbrains.annotations.NotNull;


public class RecordFragment extends Fragment implements ServiceConnection, RecordingService.RecordCallbacks {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1000;
    RecordingService mRecService;
    boolean mBound = false;
    private Button mRecPauseButton;
    private Button mStopButton;
    private TextView mTimeTextView;
    private TextView mStateTextView;

    // Requesting permission to RECORD_AUDIO
    //private boolean permissionToRecordAccepted = false;
    //private String[] permissions = {Manifest.permission.RECORD_AUDIO};


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
            TextView textView = requireActivity().findViewById(R.id.sample_record_text);
            textView.setText(R.string.text_record_permission_denied);
        }
    }


    private static final int FRAGMENT_RECORD_NAME = R.string.fragment_record_name;

    public void onStart() {
        super.onStart();
        Intent recServiceIntent = new Intent(getActivity(), RecordingService.class);
        // start service before binding
        // to handle its lifecycle in an unbound manner
        requireActivity().startService(recServiceIntent);
        requireActivity().bindService(recServiceIntent, this, Context.BIND_DEBUG_UNBIND);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            requireActivity().unbindService(this);
            mBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //get Recording Service instance
        RecordingService.RecordBinder binder = (RecordingService.RecordBinder) service;
        mRecService = binder.getService();
        mBound = true;
        mRecService.setRecordCallbacks(this);
        activateButtons();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
        mRecService = null;
    }


    private void setButtonsEnabled(){
        mRecPauseButton.setEnabled(mBound);
        mRecPauseButton.setEnabled(mBound);
    }

    private void setButtonsState() {
        if (mRecService != null){
            switch (mRecService.getStatus()){
                case RecordingService.RECORD_STATUS_PAUSED:{
                    mRecPauseButton.setText("Запись");
                    break;
                }
                case RecordingService.RECORD_STATUS_RECORDING:{
                    mRecPauseButton.setText("Пауза");
                    break;
                }
                default:{
                    mRecPauseButton.setText("Запись");
                }

            }
        }
    }


    private void activateButtons() {
        if (mRecService == null)
            throw new NullPointerException("mRecService is null, can't setup listeners");
        setButtonsEnabled();
        setButtonsState();

        mRecPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecService.isReady()) {
                    //mRecPauseButton.setText("Пауза");
                    mRecService.startRecording();

                    //make service run foreground during recording
                    mRecService.runForeground(true);
                }
                else
                    if (mRecService.isRecording()) {
                       // Log.d("cvxc", "xcvxcvx");
                        //mRecPauseButton.setText("Запись");
                        mRecService.pauseRecording();
                    }
                    else
                        if (mRecService.isPaused()) {
                            //mRecPauseButton.setText("Пауза");
                            mRecService.resumeRecording();
                        }
                setButtonsState();
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecService.isPaused() || mRecService.isRecording()) {
                    //mRecPauseButton.setText("Запись");
                    mRecService.stopRecording();
                    //remove service from foreground, recording is stopped
                    mRecService.runForeground(false);
                }
                setButtonsState();
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(FRAGMENT_RECORD_NAME));
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        mRecPauseButton = view.findViewById(R.id.record_pause_button);
        //mRecPauseButton.setText("Запись");
        mStopButton = view.findViewById(R.id.stop_button);
        setButtonsEnabled();
        mStopButton.setText("Стоп");
        mTimeTextView = view.findViewById(R.id.record_time_text);
        mStateTextView = view.findViewById(R.id.record_state_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkRecordPermissions();
        }

        return view;
    }

    @Override
    public void OnRecordTick(long seconds) {
        if (mTimeTextView!= null)
            mTimeTextView.setText(DateUtils.formatElapsedTime(seconds));
    }

    @Override
    public void OnConfigure() {
        mStateTextView.setText("Record configured");
    }

    @Override
    public void OnRecordStop() {
        mStateTextView.setText("Record stopped");
    }
}