package com.com.technoparkproject.view_models;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.player.PlayerServiceConnection;

public class PlayerViewModel extends ViewModel{
    PlayerServiceConnection playerServiceConnection;
    public LiveData<MediaMetadataCompat> currentMetadata;
    public LiveData<PlaybackStateCompat> currentState;

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


    private PlayerViewModel(PlayerServiceConnection playerServiceConnection) {
        this.playerServiceConnection = playerServiceConnection;
        this.currentMetadata = playerServiceConnection.nowPlayingMediaMetadata;
        this.currentState = playerServiceConnection.playbackState;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory{
        PlayerServiceConnection playerServiceConnection;
        public Factory(Context context) {
            this.playerServiceConnection = PlayerServiceConnection.getInstance(context.getApplicationContext()
            );
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PlayerViewModel(playerServiceConnection);
        }
    }
}
