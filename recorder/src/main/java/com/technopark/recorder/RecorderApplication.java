package com.technopark.recorder;

import android.app.Application;
import android.content.Context;


import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.repository.RecordTopicRepoImpl;

public class RecorderApplication extends Application {

    private AudioRecorder mAudioRecorder;
    private RecordTopicRepo mRecordTopicRepo;

    public AudioRecorder getRecorder(){
        return mAudioRecorder;
    }

    public static RecorderApplication from(Context context) {
        return (RecorderApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioRecorder = new AudioRecorder();
        mRecordTopicRepo = new RecordTopicRepoImpl(getApplicationContext());
    }

    public RecordTopicRepo getRecordTopicRepo() {
        return mRecordTopicRepo;
    }
}
