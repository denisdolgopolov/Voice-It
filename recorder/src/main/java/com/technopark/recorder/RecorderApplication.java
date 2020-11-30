package com.technopark.recorder;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.repository.RecordTopicRepoImpl;
import com.technopark.recorder.service.RecorderConnection;

public class RecorderApplication extends Application {

    private AudioRecorder mAudioRecorder;
    private RecordTopicRepo mRecordTopicRepo;



    public AudioRecorder getRecorder(){
        return mAudioRecorder;
    }

    private RecorderConnection mRecConn;

    public RecorderConnection getRecordConnection(){
        return mRecConn;
    }

    public static RecorderApplication from(Context context) {
        return (RecorderApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioRecorder = new AudioRecorder();
        mRecConn = new RecorderConnection(getApplicationContext());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mRecConn.getBinderObserver());
        mRecordTopicRepo = new RecordTopicRepoImpl(getApplicationContext());
    }

    public RecordTopicRepo getRecordTopicRepo() {
        return mRecordTopicRepo;
    }
}
