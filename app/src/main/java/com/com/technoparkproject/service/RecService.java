package com.com.technoparkproject.service;
import androidx.lifecycle.LiveData;

import com.com.technoparkproject.repository.RecordTopic;

import java.io.File;

public interface RecService {
    void configureRecording();
    void startRecording();
    void resumeRecording();
    void pauseRecording();
    void stopRecording();
    File getRecordFile();
    int getRecordDuration();
}
