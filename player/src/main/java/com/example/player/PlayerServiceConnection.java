package com.example.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class PlayerServiceConnection {
    private PlayerService playerService;
    private PlayerService.PlayerServiceBinder playerServiceBinder;
    public MediaControllerCompat mediaController;
    public MutableLiveData<MediaMetadataCompat> nowPlayingMediaMetadata = new MutableLiveData<>();
    public MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            playbackState.postValue(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            nowPlayingMediaMetadata.postValue(metadata);
        }
    };

    private PlayerServiceConnection(final Context context, ComponentName serviceComponent) {
        Intent serviceIntent = new Intent(context, PlayerService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerService.PlayerServiceBinder) service;
                playerService = playerServiceBinder.getServiceInstance();
                try {
                    mediaController = new MediaControllerCompat(context, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                    callback.onMetadataChanged(mediaController.getMetadata());
                } catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                    playerService = null;
                }
            }
        };
        context.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }


    private volatile static PlayerServiceConnection INSTANCE;
    public static synchronized PlayerServiceConnection getInstance(Context context, ComponentName serviceComponent) {
        if (INSTANCE == null) {
            synchronized (PlayerServiceConnection.class) {
                if (INSTANCE == null)
                    INSTANCE = new PlayerServiceConnection(context, serviceComponent);
            }
        }
        return INSTANCE;
    }
}

