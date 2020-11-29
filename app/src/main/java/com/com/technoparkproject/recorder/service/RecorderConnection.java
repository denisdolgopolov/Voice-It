package com.com.technoparkproject.recorder.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.com.technoparkproject.recorder.AudioRecorder;
import com.com.technoparkproject.recorder.RecordState;
import com.com.technoparkproject.recorder.utils.InjectorUtils;

public final class RecorderConnection {
    private final RecServiceLiveData mRecServiceLiveData;

    public RecServiceLiveData getRecServiceLiveData() {
        return mRecServiceLiveData;
    }

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

    public static class RecServiceLiveData{
        private final MediatorLiveData<RecordState> mRecState;
        private final MediatorLiveData<Integer> mRecTime;
        private final MediatorLiveData<Void> mRecLimit;
        public RecServiceLiveData(){
            mRecState = new MediatorLiveData<>();
            mRecTime = new MediatorLiveData<>();
            mRecTime.setValue(0);
            mRecLimit = new MediatorLiveData<>();
        }

        public LiveData<RecordState> getRecState() {
            return mRecState;
        }

        public LiveData<Integer> getRecTime() {
            return mRecTime;
        }

        public LiveData<Void> getRecLimit() {
            return mRecLimit;
        }

        private void addSources(RecService recService, AudioRecorder recorder){
            mRecState.addSource(recorder.getRecordState(), new Observer<RecordState>() {
                @Override
                public void onChanged(RecordState recordState) {
                    mRecState.setValue(recordState);
                }
            });
            mRecTime.addSource(recorder.getRecTime(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer seconds) {
                    mRecTime.setValue(seconds);
                }
            });
            mRecLimit.addSource(recService.getRecLimitEvent(), new Observer<Void>() {
                @Override
                public void onChanged(Void aVoid) {
                    mRecLimit.setValue(aVoid);
                }
            });
        }

        private void removeSources(RecService recService, AudioRecorder recorder){
            mRecState.removeSource(recorder.getRecordState());
            mRecTime.removeSource(recorder.getRecTime());
            mRecLimit.removeSource(recService.getRecLimitEvent());
        }
    }


    public RecorderConnection(final Context context) {
        final Intent serviceIntent = new Intent(context, RecordingService.class);
        mRecServiceLiveData = new RecServiceLiveData();

        final ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecServiceBinder = (RecordingService.RecordBinder) service;
                AudioRecorder recorder = InjectorUtils.provideRecorder(context);
                mRecServiceLiveData.addSources(mRecServiceBinder.getRecorder(),recorder);
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
                AudioRecorder recorder = InjectorUtils.provideRecorder(context);
                mRecServiceLiveData.removeSources(mRecServiceBinder.getRecorder(),recorder);
            }
        };

    }
}