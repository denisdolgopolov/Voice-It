package com.com.technoparkproject;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;
import com.com.technoparkproject.service.RecorderConnection;
import com.example.player.PlayerServiceConnection;

public class VoiceItApplication extends Application {

    public PlayerServiceConnection playerServiceConnection;

    public PlayerServiceConnection getPlayerServiceConnection() {
        return this.playerServiceConnection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RecorderConnection recServiceConn = RecorderConnection.getInstance(getApplicationContext());
        //TODO спросить - убирать или нет?
        this.playerServiceConnection = new PlayerServiceConnection(getApplicationContext());
        RecordingServiceConnection recServiceConn = RecordingServiceConnection.getInstance(getApplicationContext());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(recServiceConn.getBinderObserver());
    }
}
