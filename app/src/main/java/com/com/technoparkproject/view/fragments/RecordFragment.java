package com.com.technoparkproject.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.activities.MainActivity;
import com.technopark.recorder.RecordState;
import com.com.technoparkproject.viewmodels.RecorderViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.technopark.recorder.views.RecorderFragment;


public class RecordFragment extends RecorderFragment {
    private MaterialButton mRecPauseButton;
    private MaterialButton mStopButton;
    private TextView mTimeTextView;
    private MaterialButton mDoneButton;

    @Override
    public void setPermissionRequestLayout() {
        LinearLayout recLayout = requireActivity().findViewById(R.id.record_layout);
        LinearLayout recButtonsLayout = requireActivity().findViewById(R.id.buttons_layout);
        recLayout.setVisibility(View.GONE);
        recButtonsLayout.setVisibility(View.GONE);
    }

    @Override
    protected void setPermissionDeniedLayout() {
        TextView recDenyTextView = requireActivity().findViewById(R.id.record_deny_text);
        recDenyTextView.setText(R.string.text_record_permission_denied);
        recDenyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setPermissionGrantedLayout() {
        LinearLayout recLayout = requireActivity().findViewById(R.id.record_layout);
        LinearLayout recButtonsLayout = requireActivity().findViewById(R.id.buttons_layout);
        recLayout.setVisibility(View.VISIBLE);
        recButtonsLayout.setVisibility(View.VISIBLE);
    }


    private static final int FRAGMENT_RECORD_NAME = R.string.fragment_record_name;

    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecPauseButton = view.findViewById(R.id.record_pause_button);
        mRecPauseButton.setIcon(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_round_fiber_manual_record_24));
        mStopButton = view.findViewById(R.id.stop_button);
        mDoneButton = view.findViewById(R.id.done_button);
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
        final TextInputLayout recNameInput = view.findViewById(R.id.record_name_input_text);
        final TextInputLayout recTopicInput = view.findViewById(R.id.topic_input_text);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recName = recNameEdit.getText().toString();
                String topicName = recTopicEdit.getText().toString();
                recViewModel.onSaveClick(recName, topicName);
            }
        });
        recViewModel.getNameState().observe(getViewLifecycleOwner(), new Observer<RecorderViewModel.RecTextState>() {
            @Override
            public void onChanged(RecorderViewModel.RecTextState recTextState) {
                switch (recTextState){
                    case INVALID:
                        recNameInput.setErrorEnabled(true);
                        recNameInput.setError("Введите название");
                        break;
                    case VALID:
                        recNameInput.setError(null);
                        recNameInput.setErrorEnabled(false);
                        break;
                }
            }
        });
        recViewModel.getTopicState().observe(getViewLifecycleOwner(), new Observer<RecorderViewModel.RecTextState>() {
            @Override
            public void onChanged(RecorderViewModel.RecTextState recTextState) {
                switch (recTextState){
                    case INVALID:
                        recTopicInput.setErrorEnabled(true);
                        recTopicInput.setError("Введите топик");
                        break;
                    case VALID:
                        recTopicInput.setError(null);
                        recTopicInput.setErrorEnabled(false);
                        break;
                }
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


        final Snackbar saveSnack = Snackbar
                .make(view, "Отменить сохранение аудио", BaseTransientBottomBar.LENGTH_LONG)
                .setAction("ОТМЕНА", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recViewModel.dismissRecording();
                    }
                });
        saveSnack.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event != DISMISS_EVENT_ACTION)
                    recViewModel.saveRecording();
            }
        });

        recViewModel.getSaveEvent().observe(getViewLifecycleOwner(), new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                saveSnack.show();
            }
        });


        final ProgressBar recLimitProgress = view.findViewById(R.id.record_limit_progress);
        recLimitProgress.setMax(recViewModel.getMaxRecordLength());
        recViewModel.getRecTime().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer seconds) {
                recLimitProgress.setProgress(seconds);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(FRAGMENT_RECORD_NAME));
        return inflater.inflate(R.layout.fragment_record, container, false);
    }


    private class MyObserver implements Observer<RecordState> {
        @Override
        public void onChanged(RecordState recordState) {
            switch (recordState){
                case INIT:{
                    mStopButton.setEnabled(false);
                    mRecPauseButton.setEnabled(true);
                    mDoneButton.setEnabled(false);
                    break;
                }
                case RECORDING:{
                    mRecPauseButton.setIcon(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_round_pause_24));
                    mStopButton.setEnabled(true);
                    mDoneButton.setEnabled(true);
                    break;
                }
                case PAUSE: {
                    mRecPauseButton.setIcon(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_round_fiber_manual_record_24));
                    break;
                }
                case STOP:{
                    mRecPauseButton.setIcon(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_round_fiber_manual_record_24));
                    mStopButton.setEnabled(false);
                    mRecPauseButton.setEnabled(false);
                    break;
                }
            }
        }
    }
}