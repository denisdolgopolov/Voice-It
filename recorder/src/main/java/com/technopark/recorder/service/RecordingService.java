package com.technopark.recorder.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.DateUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.technopark.recorder.AudioRecorder;
import com.technopark.recorder.RecordState;
import com.technopark.recorder.RecorderApplication;
import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.service.storage.RecordingProfile;

import java.io.File;

public class RecordingService extends Service implements Recorder {


    private static final int FOREGROUND_ID = 1111;

    private AudioRecorder mAudioRecorder;
    private RecTimeObserver mRecTimeObserver;


    private final IBinder mBinder = new RecordBinder();
    private RecTimeLimitObserver mRecLimObserver;


    public class RecordBinder extends Binder {
        public Recorder getRecorder() {
            return RecordingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mAudioRecorder = RecorderApplication.from(this).getRecorder();
        mRecTimeObserver = new RecTimeObserver();
        mAudioRecorder.getRecTime().observeForever(mRecTimeObserver);

        mRecLimObserver = new RecTimeLimitObserver();
        mAudioRecorder.getRecMarker().observeForever(mRecLimObserver);
    }

    private boolean mIsForeground = false;

    //start/stops the service in foreground mode
    private void runForeground(boolean isForeground){
        if (isForeground){
            RecorderNotification.createNotificationChannel(this);
            startForeground(FOREGROUND_ID,
                    RecorderNotification.buildForegroundNotification("",this));
            mIsForeground = true;
        }
        else{
            stopForeground(true);
            mIsForeground = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }



    @Override
    public void configure() {
        mAudioRecorder.configure();
    }


    @Override
    public void start() {
        String fileFormat = mAudioRecorder.getRecProfile().getFileFormat();
        RecordTopicRepo recTopicRepo = RecorderApplication.from(getApplicationContext()).getRecordTopicRepo();
        recTopicRepo.createRecord(fileFormat);
        File recordFile = recTopicRepo.getLastRecord().getRecordFile();
        mAudioRecorder.prepare(recordFile);

        mAudioRecorder.start();
        final Intent serviceIntent = new Intent(getApplicationContext(), RecordingService.class);
        startService(serviceIntent);
        runForeground(true);
    }


    @Override
    public void resume(){
        mAudioRecorder.resume();
    }


    @Override
    public void pause(){
        mAudioRecorder.pause();
        RecorderNotification.updateNotification("Пауза...", RecordingService.this,FOREGROUND_ID);
    }


    @Override
    public void stop(){
        mAudioRecorder.stop();
        runForeground(false);
        stopSelf();
    }

    @Override
    public int getDuration(){
        return mAudioRecorder.getDuration();
    }

    @Override
    public int getMarkerPos() {
        return mAudioRecorder.getMarkerPos();
    }

    @Override
    public void setMarkerPos(int seconds) {
        mAudioRecorder.setMarkerPos(seconds);
    }

    @Override
    public LiveData<RecordState> getRecordState() {
        return mAudioRecorder.getRecordState();
    }

    @Override
    public LiveData<Integer> getRecTime() {
        return mAudioRecorder.getRecTime();
    }

    @Override
    public RecordingProfile getRecProfile() {
        return mAudioRecorder.getRecProfile();
    }

    @Override
    public LiveData<Boolean> getRecMarker() {
        return mAudioRecorder.getRecMarker();
    }

    @Override
    public void release() {
        mAudioRecorder.release();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioRecorder.getRecTime().removeObserver(mRecTimeObserver);
        mAudioRecorder.getRecMarker().removeObserver(mRecLimObserver);
        mAudioRecorder = null;
    }

    private class RecTimeLimitObserver implements Observer<Boolean> {
        @Override
        public void onChanged(Boolean markerReached) {
            if (markerReached)
                stop();
        }
    }

    private class RecTimeObserver implements Observer<Integer> {
        @Override
        public void onChanged(Integer seconds) {
            if (!mIsForeground)
                return;
            String notifyText = DateUtils.formatElapsedTime(seconds);
            RecorderNotification.updateNotification(notifyText, RecordingService.this,FOREGROUND_ID);
        }
    }
}
