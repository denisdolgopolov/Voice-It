package com.example.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.IMediaSession;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class PlayerServiceConnection {
    private PlayerService playerService;
    private PlayerService.PlayerServiceBinder playerServiceBinder;
    public MediaControllerCompat mediaController;
    public MutableLiveData<MediaMetadataCompat> nowPlayingMediaMetadata = new MutableLiveData<>();
    public MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    public List<String> playlist = new ArrayList<>();

    /*public List<MediaMetadataCompat> currentPlaylist = new ArrayList<>();*/
    public MutableLiveData<List<String>> currentPlaylist = new MutableLiveData<>();
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                playerServiceBinder = (PlayerService.PlayerServiceBinder) service;
                playerService = playerServiceBinder.getServiceInstance();
                mediaController = new MediaControllerCompat(playerService.getApplicationContext(), playerServiceBinder.getMediaSessionToken());
                mediaController.registerCallback(mediaControllerCallback);
                mediaControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
                mediaControllerCallback.onMetadataChanged(mediaController.getMetadata());
                playerService.playlist = playlist;
                playerService.maxIndex = playlist.size()-1;
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerServiceBinder = null;
            if (mediaController != null) {
                mediaController.unregisterCallback(mediaControllerCallback);
                mediaController = null;
                playerService = null;
            }
        }
    };

    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {

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

    private PlayerServiceConnection(Context context) {
        Intent serviceIntent = new Intent(context, PlayerService.class);
        context.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void setCurrentIndex(int index) {
        playerService.currentItemIndex = index;
    }

    List<String> getPlaylist() {
        return playlist;
    }

    public void addToPlaylist(String UUID) {
        playlist.add(UUID);
    }

    public void setPlaylist(List<String> newPlaylist) {
        this.playlist = newPlaylist;
    }

    private volatile static PlayerServiceConnection INSTANCE;

    public static synchronized PlayerServiceConnection getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PlayerServiceConnection.class) {
                if (INSTANCE == null)
                    INSTANCE = new PlayerServiceConnection(context);
            }
        }
        return INSTANCE;
    }

}

