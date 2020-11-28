package com.com.technoparkproject.utils;

import android.content.Context;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.service.AudioRecorder;
import com.com.technoparkproject.service.RecService;
import com.com.technoparkproject.service.RecorderConnection;

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
