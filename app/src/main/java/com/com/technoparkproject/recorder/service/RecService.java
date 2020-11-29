package com.com.technoparkproject.recorder.service;

import androidx.lifecycle.LiveData;

import java.util.UUID;

public interface RecService {
    void configureRecording();
    void startRecording();
    void resumeRecording();
    void pauseRecording();
    void stopRecording();
    int getRecordDuration();
    int getMaxRecDuration();
    LiveData<Void> getRecLimitEvent();
}
