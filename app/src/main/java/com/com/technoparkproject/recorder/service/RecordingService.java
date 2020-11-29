package com.com.technoparkproject.recorder.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.recorder.AudioRecorder;
import com.com.technoparkproject.recorder.VoiceItApplication;
import com.com.technoparkproject.recorder.repository.RecordTopic;
import com.com.technoparkproject.recorder.repository.RecordTopicRepo;
import com.com.technoparkproject.recorder.utils.InjectorUtils;
import com.com.technoparkproject.recorder.utils.SingleLiveEvent;

import java.io.File;
import java.util.UUID;

public class RecordingService extends Service implements RecService {

    //private File mRecordFile;

    private static final int FOREGROUND_ID = 1111;

    private AudioRecorder mAudioRecorder;
    private RecTimeObserver mRecTimeObserver;


    private final IBinder mBinder = new RecordBinder();


    public class RecordBinder extends Binder {
        public RecService getRecorder() {
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
        mAudioRecorder = VoiceItApplication.from(this).getRecorder();
        mRecTimeObserver = new RecTimeObserver();
        mAudioRecorder.getRecTime().observeForever(mRecTimeObserver);
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


    public void configureRecording() {
        mAudioRecorder.configure();
    }

    public void startRecording() {
        String fileFormat = mAudioRecorder.getRecordingProfile().getFileFormat();
        RecordTopicRepo recTopicRepo = InjectorUtils.provideRecordTopicRepo(this);
        UUID recTopicID = recTopicRepo.createRecord(fileFormat);
        File recordFile = recTopicRepo.getRecord(recTopicID).getRecordFile();
        mAudioRecorder.prepare(recordFile);

        mAudioRecorder.start();
        final Intent serviceIntent = new Intent(getApplicationContext(), RecordingService.class);
        startService(serviceIntent);
        runForeground(true);
    }


    public void resumeRecording(){
        mAudioRecorder.resume();
    }

    public void pauseRecording(){
        mAudioRecorder.pause();
    }

    public void stopRecording(){
        mAudioRecorder.stop();
        runForeground(false);
        stopSelf();
    }

    public int getRecordDuration(){
        return mAudioRecorder.getDuration();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVICE","destroy()");
        mAudioRecorder.getRecTime().removeObserver(mRecTimeObserver);
        mAudioRecorder = null;
    }

    private static final int MAX_RECORD_LENGTH = 10*60; //max allowed recording in seconds

    @Override
    public int getMaxRecDuration() {
        return MAX_RECORD_LENGTH;
    }

    private final SingleLiveEvent<Void> mRecLimitEvent = new SingleLiveEvent<>();

    public LiveData<Void> getRecLimitEvent(){
        return mRecLimitEvent;
    }

    private class RecTimeObserver implements Observer<Integer> {
        @Override
        public void onChanged(Integer seconds) {
            if (!mIsForeground)
                return;
            if (seconds == getMaxRecDuration()){
                mRecLimitEvent.call();
                stopRecording();
                return;
            }
            String notifyText = DateUtils.formatElapsedTime(seconds);
            RecorderNotification.updateNotification(notifyText, RecordingService.this,FOREGROUND_ID);
        }
    }
}
