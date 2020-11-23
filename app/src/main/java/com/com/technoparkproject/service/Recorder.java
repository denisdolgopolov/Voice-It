package com.com.technoparkproject.service;
import androidx.lifecycle.LiveData;

import com.com.technoparkproject.repository.Record;

public interface Recorder {
    void startRecording();
    void resumeRecording();
    void pauseRecording();
    void stopRecording();
    Record saveRecording();
    LiveData<RecordState> getRecordState();
    LiveData<Integer> getRecTime();
}
