package com.com.technoparkproject.recorder.service;

import androidx.lifecycle.LiveData;

import java.io.File;

public interface RecService {
    void configureRecording();
    void startRecording();
    void resumeRecording();
    void pauseRecording();
    void stopRecording();
    File getRecordFile();
    int getRecordDuration();
    int getMaxRecDuration();
    LiveData<Void> getRecLimitEvent();
}
