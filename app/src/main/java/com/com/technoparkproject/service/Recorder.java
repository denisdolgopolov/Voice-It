package com.com.technoparkproject.service;
import androidx.lifecycle.LiveData;

import com.com.technoparkproject.repository.RecordTopic;

public interface Recorder {
    void startRecording();
    void resumeRecording();
    void pauseRecording();
    void stopRecording();
    //returns null if recording isn't finished yet
    RecordTopic saveRecording();
    LiveData<RecordState> getRecordState();
    LiveData<Integer> getRecTime();
}
