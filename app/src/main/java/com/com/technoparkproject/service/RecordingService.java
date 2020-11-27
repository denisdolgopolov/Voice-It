package com.com.technoparkproject.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.repository.RecordRepo;
import com.com.technoparkproject.repository.RecordTopic;
import com.com.technoparkproject.service.storage.RecordingProfile;
import com.com.technoparkproject.service.storage.RecordingProfileStorage;
import com.com.technoparkproject.service.coders.ADTSStream;
import com.com.technoparkproject.service.coders.PacketStream;
import com.com.technoparkproject.service.tasks.DrainWriterTask;
import com.com.technoparkproject.service.tasks.RecorderTask;
import com.com.technoparkproject.service.tasks.StreamWriterTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.com.technoparkproject.service.storage.RecordingProfileStorage.AudioQuality;

public class RecordingService extends Service implements Recorder{

    private File mRecordFile;

    private static final int FOREGROUND_ID = 1111;

    private AudioRecorder mAudioRecorder;
    private RecTimeObserver mRecTimeObserver;

    public MutableLiveData<RecordState> getRecordState(){
        return mAudioRecorder.getRecordState();
    }

    public MutableLiveData<Integer> getRecTime() {
        return mAudioRecorder.getRecTime();
    }


    private final IBinder mBinder = new RecordBinder();


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
        mAudioRecorder = new AudioRecorder();
        mRecTimeObserver = new RecTimeObserver();
        mAudioRecorder.getRecTime().observeForever(mRecTimeObserver);

    }

    //start/stops the service in foreground mode
    private void runForeground(boolean isForeground){
        if (isForeground){
            RecorderNotification.createNotificationChannel(this);
            startForeground(FOREGROUND_ID,
                    RecorderNotification.buildForegroundNotification("",this));
        }
        else{
            stopForeground(true);
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
        //todo: remove const string somehow
        mRecordFile = RecordRepo.createTempFile(".aac",this);

        mAudioRecorder.prepare(mRecordFile);

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
    }

    public File getRecordFile(){
        return mRecordFile;
    }

    public int getRecordDuration(){
        return mAudioRecorder.getDuration();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //mAudioRecorder.getRecordState().removeObserver(mRecStateObserver);
        mAudioRecorder.getRecTime().removeObserver(mRecTimeObserver);
        mAudioRecorder.release();
        mAudioRecorder = null;
        mRecordFile = null;
    }

    private class RecTimeObserver implements Observer<Integer> {
        @Override
        public void onChanged(Integer seconds) {
            String notifyText = DateUtils.formatElapsedTime(seconds);
            RecorderNotification.updateNotification(notifyText, RecordingService.this,FOREGROUND_ID);
        }
    }
}
