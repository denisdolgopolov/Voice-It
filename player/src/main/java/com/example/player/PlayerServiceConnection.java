package com.example.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.lifecycle.MutableLiveData;


import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class PlayerServiceConnection {
    private static final Long POSITION_UPDATE_INTERVAL_MILLIS = 1000L;
    public PlayerService playerService;
    private PlayerService.PlayerServiceBinder playerServiceBinder;
    public MediaControllerCompat mediaController;
    public MutableLiveData<MediaMetadataCompat> nowPlayingMediaMetadata = new MutableLiveData<>();
    public MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    Handler handler = new Handler(Looper.getMainLooper());
    public MutableLiveData<Long> mediaPosition = new MutableLiveData<>();

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerServiceBinder = (PlayerService.PlayerServiceBinder) service;
            playerService = playerServiceBinder.getServiceInstance();
            mediaController = playerService.mediaSession.getController();
            mediaController.registerCallback(mediaControllerCallback);
            mediaControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
            mediaControllerCallback.onMetadataChanged(mediaController.getMetadata());

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

    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            playbackState.postValue(state);
            if (state != null) {
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    updatePosition = true;
                } else {
                    updatePosition = false;
                }
                checkPlaybackPosition();
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            nowPlayingMediaMetadata.postValue(metadata);
            mediaPosition.postValue(0L);
        }
    };

    private boolean updatePosition = false;

    private PlayerServiceConnection(Context context) {
        Intent serviceIntent = new Intent(context, PlayerService.class);
        context.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void setCurrentIndex(int index) {
        playerService.currentItemIndex = index;
    }

    public void addToPlaylist(String UUID) {
        // не уверен, что это хорошее решение
        List<String> temp = playerService.playlist.getValue();
        if (temp == null) {
            temp = new ArrayList<>();
        }
        temp.add(UUID);
        playerService.playlist.setValue(temp);
    }

    public void setPlaylist(List<String> currentPlaylist) {
        // currentPlaylist - лист, состоящий из UUID
        if (playerService != null) {
            playerService.playlist.setValue(currentPlaylist);
        }
    }


    public void setPlaylist(String topicUUID) {
        // TODO
    }

    // Анти-паттерн)
    private volatile static PlayerServiceConnection INSTANCE;

    public static PlayerServiceConnection getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PlayerServiceConnection.class) {
                if (INSTANCE == null)
                    INSTANCE = new PlayerServiceConnection(context);
            }
        }
        return INSTANCE;
    }

    private void checkPlaybackPosition() {
        handler.postDelayed(() -> {
            if (playerService.exoPlayer != null) {
                Long currPosition = playerService.exoPlayer.getCurrentPosition();
                if (currPosition != mediaPosition.getValue()) {
                    mediaPosition.postValue(currPosition);
                    if (updatePosition) {
                        checkPlaybackPosition();
                    }
                }
            }
        }, POSITION_UPDATE_INTERVAL_MILLIS);
    }

}

