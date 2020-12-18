package com.com.technoparkproject;

import com.example.player.PlayerServiceConnection;
import com.technopark.recorder.RecorderApplication;

public class VoiceItApplication extends RecorderApplication {

    private PlayerServiceConnection playerServiceConnection;

    public PlayerServiceConnection getPlayerServiceConnection() {
        return this.playerServiceConnection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO спросить - убирать или нет?
        this.playerServiceConnection = new PlayerServiceConnection(getApplicationContext());
    }
}
