package com.com.technoparkproject.view_models;

import android.app.Application;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.com.technoparkproject.VoiceItApplication;
import com.example.player.PlayerServiceConnection;

public class PlayerViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    public LiveData<MediaMetadataCompat> currentMetadata;
    public LiveData<PlaybackStateCompat> currentState;
    public LiveData<Long> currentPosition;

    public void playButtonClicked() {
        if (playerServiceConnection != null) {
            playerServiceConnection.mediaController.getTransportControls().play();
        }
    }

    public void pauseButtonClicked() {
        if (playerServiceConnection != null) {
            playerServiceConnection.mediaController.getTransportControls().pause();
        }
    }

    public void nextButtonClicked() {
        if (playerServiceConnection != null) {
            playerServiceConnection.mediaController.getTransportControls().skipToNext();
        }
    }

    public void prevButtonClicked() {
        if (playerServiceConnection != null) {
            playerServiceConnection.mediaController.getTransportControls().skipToPrevious();
        }
    }

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        this.playerServiceConnection = ((VoiceItApplication) application).playerServiceConnection;
        this.currentMetadata = playerServiceConnection.nowPlayingMediaMetadata;
        this.currentState = playerServiceConnection.playbackState;
        this.currentPosition = playerServiceConnection.mediaPosition;

    }

    public void seekBarSeekTo(int progress) {
        playerServiceConnection.mediaController.getTransportControls().seekTo((long) progress);
        playerServiceConnection.mediaPosition.setValue((long) progress);
    }
}
