package com.com.technoparkproject.recorder.service;

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
