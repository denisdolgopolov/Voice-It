package com.example.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;

public class PlayerServiceConnection {
    private static final Long POSITION_UPDATE_INTERVAL_MILLIS = 200L;
    public PlayerService playerService;
    private PlayerService.PlayerServiceBinder playerServiceBinder;
    public MediaControllerCompat mediaController;
    public MutableLiveData<MediaMetadataCompat> nowPlayingMediaMetadata = new MutableLiveData<>();
    public MutableLiveData<String> nowPlayingRecordUUID = new MutableLiveData<>();
    public MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    public MutableLiveData<Long> mediaPosition = new MutableLiveData<>();
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


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
        private ScheduledFuture timeFuture;

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            playbackState.postValue(state);
            if (state != null) {
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    timeFuture = scheduledExecutorService.scheduleAtFixedRate(checkPlaybackPosition,
                            0, POSITION_UPDATE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
                } else {
                    if (timeFuture != null) {
                        timeFuture.cancel(false);
                    }
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            nowPlayingMediaMetadata.postValue(metadata);
            if (metadata != null) {
                nowPlayingRecordUUID.postValue(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            }
            else {
                nowPlayingRecordUUID.postValue(null);
            }
            mediaPosition.postValue(0L);
        }

        final Runnable checkPlaybackPosition = new Runnable() {
            @Override
            public void run() {
                if (mediaController.getPlaybackState() != null) {
                    Long currPosition = mediaController.getPlaybackState().getPosition();
                    if (!currPosition.equals(mediaPosition.getValue())) {
                        mediaPosition.postValue(currPosition);
                    }
                }
            }
        };
    };


    public PlayerServiceConnection(Context context) {
        Intent serviceIntent = new Intent(context, PlayerService.class);
        context.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void setCurrentIndex(int index) {
        playerService.currentItemIndex = index;
    }

    public void addToPlaylist(Record record) {
        List<Record> temp = playerService.playlist.getValue();
        if (temp == null) {
            temp = new ArrayList<>();
        }
        temp.add(record);
        playerService.playlist.setValue(temp);
    }

    public void clearPlaylist() {
        playerService.playlist.setValue(new ArrayList<>());
    }

    /*
    public void setPlaylist(List<String> currentPlaylist) {
        // currentPlaylist - лист, состоящий из UUID
        if (playerService != null) {
            playerService.playlist.setValue(currentPlaylist);
        }
    }

     */

    public void setPlaylist(String topicUUID) {
        // TODO
    }

}

