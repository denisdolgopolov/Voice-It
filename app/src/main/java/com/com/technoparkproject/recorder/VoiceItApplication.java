package com.com.technoparkproject.recorder;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.com.technoparkproject.recorder.service.RecorderConnection;
import com.example.player.PlayerServiceConnection;

public class VoiceItApplication extends Application {

    private AudioRecorder mAudioRecorder;

    public AudioRecorder getRecorder(){
        return mAudioRecorder;
    }

    private RecorderConnection mRecConn;

    public RecorderConnection getRecordConnection(){
        return mRecConn;
    }

    public static VoiceItApplication from(Context context) {
        return (VoiceItApplication) context.getApplicationContext();
    }

    public PlayerServiceConnection playerServiceConnection;

    public PlayerServiceConnection getPlayerServiceConnection() {
        return this.playerServiceConnection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioRecorder = new AudioRecorder();
        mRecConn = new RecorderConnection(getApplicationContext());
        //TODO спросить - убирать или нет?
        this.playerServiceConnection = new PlayerServiceConnection(getApplicationContext());
        RecordingServiceConnection recServiceConn = RecordingServiceConnection.getInstance(getApplicationContext());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mRecConn.getBinderObserver());
    }
}
