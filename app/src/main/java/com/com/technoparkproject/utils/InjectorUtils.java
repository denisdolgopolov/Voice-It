package com.com.technoparkproject.utils;

import android.content.Context;

import com.com.technoparkproject.service.RecordingService;
import com.com.technoparkproject.service.RecordingServiceConnection;

public class InjectorUtils {
    public static RecordingService provideRecordingService(Context context) {
        RecordingServiceConnection recServiceConnection= RecordingServiceConnection.getInstance(context);
        if (recServiceConnection.getRecordBinder()!= null)
            return recServiceConnection.getRecordBinder().getService();
        return null;
    }
    public static RecordingServiceConnection provideRecordingServiceConn(Context context) {
        return RecordingServiceConnection.getInstance(context);
    }
}
