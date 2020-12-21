package com.technopark.recorder.viewmodels;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.technopark.recorder.service.Recorder;
import com.technopark.recorder.service.RecordingService;

public abstract class RecorderViewModel extends AndroidViewModel {

    private final ServiceConnection mServiceConn;
    private Recorder mRecorder;

    protected Recorder getRecorder(){
        return mRecorder;
    }

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        mServiceConn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecorder = ((RecordingService.RecordBinder) service).getRecorder();
                onRecorderConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                onRecorderDisconnected();
                mRecorder = null;
            }
        };
        final Intent serviceIntent = new Intent(application, RecordingService.class);
        application.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    protected abstract void onRecorderDisconnected();

    protected abstract void onRecorderConnected();

    @Override
    protected void onCleared() {
        super.onCleared();
        getApplication().unbindService(mServiceConn);
    }
}
