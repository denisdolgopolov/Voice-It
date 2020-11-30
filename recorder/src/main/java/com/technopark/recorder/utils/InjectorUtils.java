package com.technopark.recorder.utils;

import android.content.Context;

import com.technopark.recorder.RecorderApplication;
import com.technopark.recorder.AudioRecorder;
import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.service.RecService;
import com.technopark.recorder.service.RecorderConnection;

public final class InjectorUtils {

    private InjectorUtils(){}

    public static RecordTopicRepo provideRecordTopicRepo(Context context){
        return RecorderApplication.from(context).getRecordTopicRepo();
    }

    public static RecService provideRecService(Context context) {
        RecorderConnection recServiceConnection = provideRecServiceConn(context);
        return recServiceConnection.getRecorder();
    }

    private static RecorderConnection provideRecServiceConn(Context context) {
        return RecorderApplication.from(context).getRecordConnection();
    }

    public static RecorderConnection.RecServiceLiveData provideRecServiceData(Context context) {
        RecorderConnection recConn = provideRecServiceConn(context);
        return recConn.getRecServiceLiveData();
    }

    public static AudioRecorder provideRecorder(Context context){
        return RecorderApplication.from(context).getRecorder();
    }
}
