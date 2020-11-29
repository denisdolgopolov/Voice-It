package com.com.technoparkproject.view_models;

import android.app.Application;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.models.Record;
import com.example.player.PlayerServiceConnection;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    public LiveData<PlaybackStateCompat> currentState;
    public LiveData<List<String>> currentPlaylist;
    public LiveData<MediaMetadataCompat> currentMetadata;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        this.playerServiceConnection = ((VoiceItApplication) application).playerServiceConnection;
        this.currentPlaylist = playerServiceConnection.playerService.playlist;
        this.currentState = playerServiceConnection.playbackState;
        this.currentMetadata = playerServiceConnection.nowPlayingMediaMetadata;
    }

    public void itemClicked(int position) {
        playerServiceConnection.setCurrentIndex(position);
        playerServiceConnection.mediaController.getTransportControls().play();
    }

    public List<Record> getListOfRecordsFromUUIDs(List<String> ListOfUUIDs){
        List<Record> listOfRecords = new ArrayList<>();
        for(String UUID: ListOfUUIDs){
            //listOfRecords.add(TestRecordsRepository.getRecordByUUID(UUID));
        }
        return listOfRecords;
    }
}
