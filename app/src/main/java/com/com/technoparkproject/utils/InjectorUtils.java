package com.com.technoparkproject.utils;

import android.content.Context;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.service.AudioRecorder;
import com.com.technoparkproject.service.Recorder;
import com.com.technoparkproject.service.RecorderConnection;

public final class InjectorUtils {

    private InjectorUtils(){}

    public static Recorder provideRecorder(Context context) {
        RecorderConnection recServiceConnection = provideRecordingServiceConn(context);
        return recServiceConnection.getRecorder();
    }

    //todo remove this method
    public static RecorderConnection provideRecordingServiceConn(Context context) {
        return VoiceItApplication.from(context).getRecordConnection();
    }

    public static AudioRecorder provideAudioRecorder(Context context){
        return VoiceItApplication.from(context).getRecorder();
    }
}
