package com.com.technoparkproject;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.com.technoparkproject.repository.RecordRepo;
import com.com.technoparkproject.service.RecordingServiceConnection;
import com.example.player.PlayerServiceConnection;

public class VoiceItApplication extends Application {

    private RecordRepo mRecordRepo;
    public PlayerServiceConnection playerServiceConnection;

    public PlayerServiceConnection getPlayerServiceConnection() {
        return this.playerServiceConnection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO спросить - убирать или нет?
        this.playerServiceConnection = new PlayerServiceConnection(getApplicationContext());
        RecordingServiceConnection recServiceConn = RecordingServiceConnection.getInstance(getApplicationContext());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(recServiceConn.getBinderObserver());
        mRecordRepo = new RecordRepo();
    }

    public static VoiceItApplication from(Context context) {
        return (VoiceItApplication) context.getApplicationContext();
    }

    public RecordRepo getRecordRepo() {
        return mRecordRepo;
    }
}
