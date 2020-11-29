package com.com.technoparkproject;

import android.app.Application;
import android.content.Context;

import com.example.player.PlayerServiceConnection;

public class VoiceItApplication extends Application {

    public PlayerServiceConnection playerServiceConnection;

    public static VoiceItApplication from(Context context) {
        return (VoiceItApplication) context.getApplicationContext();
    }

    public PlayerServiceConnection getPlayerServiceConnection() {
        return this.playerServiceConnection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.playerServiceConnection = new PlayerServiceConnection(this.getApplicationContext());
    }
}
