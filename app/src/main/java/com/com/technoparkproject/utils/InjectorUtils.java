package com.com.technoparkproject.utils;

import android.content.Context;

import com.com.technoparkproject.service.Recorder;
import com.com.technoparkproject.service.RecorderConnection;

public class InjectorUtils {
    public static Recorder provideRecorder(Context context) {
        RecorderConnection recServiceConnection = provideRecordingServiceConn(context);
        return recServiceConnection.getRecorder();
    }
    public static RecorderConnection provideRecordingServiceConn(Context context) {
        return RecorderConnection.getInstance(context);
    }

    public static RecorderConnection.RecordingLiveData provideRecordingLiveData(Context context) {
        return RecorderConnection.getInstance(context).getRecLiveData();
    }
}
