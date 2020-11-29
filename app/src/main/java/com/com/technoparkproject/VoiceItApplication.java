package com.com.technoparkproject;

import android.app.Application;

import com.example.player.PlayerServiceConnection;

public class VoiceItApplication extends Application {

    public PlayerServiceConnection playerServiceConnection;

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
