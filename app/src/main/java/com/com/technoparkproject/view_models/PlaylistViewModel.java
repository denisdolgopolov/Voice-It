package com.com.technoparkproject.view_models;

import android.app.Application;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.model_converters.PlayerConverter;
import com.com.technoparkproject.models.Record;
import com.example.player.PlayerServiceConnection;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    public LiveData<PlaybackStateCompat> currentState;
    public LiveData<List<Record>> currentPlaylist;
    public LiveData<MediaMetadataCompat> currentMetadata;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        this.playerServiceConnection = ((VoiceItApplication) application).playerServiceConnection;
        this.currentPlaylist = Transformations.map(playerServiceConnection.playerService.playlist, new Function<List<com.example.player.Record>, List<Record>>() {
            @Override
            public List<Record> apply(List<com.example.player.Record> input) {
                ArrayList<Record> records = new ArrayList<>();
                for(com.example.player.Record record: input)
                    records.add(PlayerConverter.toMainRecord(record));
                return records;
            }
        });
        this.currentState = playerServiceConnection.playbackState;
        this.currentMetadata = playerServiceConnection.nowPlayingMediaMetadata;
    }

    public void itemClicked(int position) {
        playerServiceConnection.setCurrentIndex(position);
        playerServiceConnection.mediaController.getTransportControls().play();
    }
}
