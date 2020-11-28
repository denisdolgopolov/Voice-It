package com.com.technoparkproject.service;
import androidx.lifecycle.LiveData;

import com.com.technoparkproject.repository.RecordTopic;

import java.io.File;

public interface Recorder {
    void configureRecording();
    void startRecording();
    void resumeRecording();
    void pauseRecording();
    void stopRecording();
    /*//returns null if recording isn't finished yet
    RecordTopic saveRecording();*/
    File getRecordFile();
    int getRecordDuration();
    //LiveData<RecordState> getRecordState();
    //LiveData<Integer> getRecTime();
}
