package com.com.technoparkproject.recorder.utils;

import android.content.Context;

import com.com.technoparkproject.recorder.VoiceItApplication;
import com.com.technoparkproject.recorder.AudioRecorder;
import com.com.technoparkproject.recorder.service.RecService;
import com.com.technoparkproject.recorder.service.RecorderConnection;

public final class InjectorUtils {

    private InjectorUtils(){}

    public static RecService provideRecService(Context context) {
        RecorderConnection recServiceConnection = VoiceItApplication.from(context).getRecordConnection();
        return recServiceConnection.getRecorder();
    }

    public static AudioRecorder provideRecorder(Context context){
        return VoiceItApplication.from(context).getRecorder();
    }
}
