package com.com.technoparkproject.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public final class RecorderConnection {
    private RecordingService.RecordBinder mRecServiceBinder;

    public RecService getRecorder(){
        if (mRecServiceBinder == null) {
            Log.e(this.getClass().getSimpleName(),
                    "Can't getRecorder, binder is not initialised!");
            return null;
        }
        return mRecServiceBinder.getRecorder();
    }

    public LifecycleObserver getBinderObserver(){
        return mRecBinderObserver;
    }
    private final LifecycleObserver mRecBinderObserver;

    public RecorderConnection(final Context context) {
        final Intent serviceIntent = new Intent(context, RecordingService.class);
        final ServiceConnection serviceConnection = new ServiceConnection() {


            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecServiceBinder = (RecordingService.RecordBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRecServiceBinder = null;
            }
        };
        mRecBinderObserver = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onUiStart() {
                context.bindService(serviceIntent, serviceConnection,Context.BIND_AUTO_CREATE);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onUiStop() {
                context.unbindService(serviceConnection);
            }
        };

    }
}