package com.technopark.recorder.service;

import androidx.lifecycle.LiveData;

import com.technopark.recorder.RecordState;
import com.technopark.recorder.service.storage.RecordingProfile;

public interface Recorder {
    void configure();
    void start();
    void resume();
    void pause();
    void stop();
    void release();
    int getDuration();
    int getMarkerPos();
    void setMarkerPos(int seconds);
    LiveData<RecordState> getRecordState();
    LiveData<Integer> getRecTime();
    RecordingProfile getRecProfile();
    LiveData<Boolean> getRecMarker();
}
