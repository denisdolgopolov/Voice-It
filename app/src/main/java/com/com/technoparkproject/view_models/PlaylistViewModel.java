package com.com.technoparkproject.view_models;

import android.app.Application;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.com.technoparkproject.VoiceItApplication;
import com.example.player.PlayerServiceConnection;
import com.example.repo.Record;
import com.example.repo.TestRecordsRepository;

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
            listOfRecords.add(TestRecordsRepository.getRecordByUUID(UUID));
        }
        return listOfRecords;
    }

}
