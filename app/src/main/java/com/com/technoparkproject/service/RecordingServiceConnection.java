package com.com.technoparkproject.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

public class RecordingServiceConnection {
    private volatile static RecordingServiceConnection CONNECTION_INSTANCE;
    private RecordingService.RecordBinder mRecServiceBinder;

    private MediatorLiveData<Integer> mTimeData = new MediatorLiveData<>();
    private MediatorLiveData<RecordingService.RecordState> mRecState = new MediatorLiveData<>();
    public MediatorLiveData<Integer> getTimeData() {
        return mTimeData;
    }
    public MediatorLiveData<RecordingService.RecordState> getRecState() {
        return mRecState;
    }


    public RecordingService.RecordBinder getRecordBinder(){
        return mRecServiceBinder;
    }

    public static synchronized RecordingServiceConnection getInstance(Context context) {
        if (CONNECTION_INSTANCE == null) {
            synchronized (RecordingServiceConnection.class) {
                if (CONNECTION_INSTANCE == null)
                    CONNECTION_INSTANCE = new RecordingServiceConnection(context);
            }
        }
        return CONNECTION_INSTANCE;
    }


    public LifecycleObserver getBinderObserver(){
        return mRecBinderObserver;
    }
    private final LifecycleObserver mRecBinderObserver;

    private RecordingServiceConnection(final Context context) {
        final Intent serviceIntent = new Intent(context, RecordingService.class);
        final ServiceConnection serviceConnection = new ServiceConnection() {


            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecServiceBinder = (RecordingService.RecordBinder) service;
                final RecordingService recService = mRecServiceBinder.getService();
                    mTimeData.addSource(recService.getRecTime(), new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            mTimeData.setValue(integer);
                            Log.d("test data", "val  = " + mTimeData.getValue());
                        }
                    });

                mRecState.addSource(recService.getRecordState(), new Observer<RecordingService.RecordState>() {
                    @Override
                    public void onChanged(RecordingService.RecordState recordState) {
                        mRecState.setValue(recordState);
                    }
                });

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                final RecordingService recService = mRecServiceBinder.getService();
                mTimeData.removeSource(recService.getRecTime());
                mTimeData = null;
                mRecState.removeSource(recService.getRecordState());
                mRecState = null;
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
                    mTimeData.removeSource(mRecServiceBinder.getService().getRecTime());
                    mRecState.removeSource(mRecServiceBinder.getService().getRecordState());
                }
            }
        };

    }
}