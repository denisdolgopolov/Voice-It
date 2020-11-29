package com.com.technoparkproject.recorder.service;

import androidx.lifecycle.LiveData;

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
