package com.com.technoparkproject.service;

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

public final class RecorderConnection {
    private volatile static RecorderConnection CONNECTION_INSTANCE;
    private RecordingService.RecordBinder mRecServiceBinder;

    private final RecordingLiveData mRecLiveData = new RecordingLiveData();

    public RecordingLiveData getRecLiveData(){
        return mRecLiveData;
    }

    public Recorder getRecorder(){
        if (mRecServiceBinder == null) {
            Log.e(this.getClass().getSimpleName(),
                    "Can't getRecorder, binder is not initialised!");
            return null;
        }
        return mRecServiceBinder.getRecorder();
    }

    public static class RecordingLiveData{
        private final MediatorLiveData<Integer> mTimeData;
        private final MediatorLiveData<RecordState> mRecState;

        public RecordingLiveData(){
            mTimeData = new MediatorLiveData<>();
            mRecState = new MediatorLiveData<>();
        }

        public MediatorLiveData<Integer> getTimeData() {
            return mTimeData;
        }

        public void addRecStateSource(final LiveData<RecordState> recState){
            mRecState.addSource(recState, new Observer<RecordState>() {
                @Override
                public void onChanged(RecordState recordState) {
                    mRecState.setValue(recordState);
                }
            });
        }

        public void removeRecStateSource(final LiveData<RecordState> recState) {
            mRecState.removeSource(recState);
        }

        public void addRecTimeSource(final LiveData<Integer> recTime){
            mTimeData.addSource(recTime, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    mTimeData.setValue(integer);
                }
            });
        }
        public void removeRecTimeSource(final LiveData<Integer> recTime) {
            mTimeData.removeSource(recTime);
        }


        public MediatorLiveData<RecordState> getRecState() {
            return mRecState;
        }
    }

    public static synchronized RecorderConnection getInstance(Context context) {
        if (CONNECTION_INSTANCE == null) {
            synchronized (RecorderConnection.class) {
                if (CONNECTION_INSTANCE == null)
                    CONNECTION_INSTANCE = new RecorderConnection(context);
            }
        }
        return CONNECTION_INSTANCE;
    }


    public LifecycleObserver getBinderObserver(){
        return mRecBinderObserver;
    }
    private final LifecycleObserver mRecBinderObserver;

    private RecorderConnection(final Context context) {
        final Intent serviceIntent = new Intent(context, RecordingService.class);
        final ServiceConnection serviceConnection = new ServiceConnection() {


            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecServiceBinder = (RecordingService.RecordBinder) service;
                final Recorder recorder = mRecServiceBinder.getRecorder();

                mRecLiveData.addRecTimeSource(recorder.getRecTime());
                mRecLiveData.addRecStateSource(recorder.getRecordState());

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                final Recorder recorder  = mRecServiceBinder.getRecorder();
                mRecLiveData.removeRecTimeSource(recorder.getRecTime());
                mRecLiveData.removeRecStateSource(recorder.getRecordState());
                mRecServiceBinder = null;
            }
        };
        //context.bindService(serviceIntent, serviceConnection,0);
        mRecBinderObserver = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onUiStart() {
                Log.d(getClass().getSimpleName(), "ON_START");
                context.bindService(serviceIntent, serviceConnection,Context.BIND_AUTO_CREATE);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onUiStop() {
                Log.d(getClass().getSimpleName(), "ON_STOP");
                context.unbindService(serviceConnection);
                if (mRecServiceBinder != null){
                    mRecLiveData.removeRecTimeSource(mRecServiceBinder.getRecorder().getRecTime());
                    mRecLiveData.removeRecStateSource(mRecServiceBinder.getRecorder().getRecordState());
                }
            }
        };

    }
}