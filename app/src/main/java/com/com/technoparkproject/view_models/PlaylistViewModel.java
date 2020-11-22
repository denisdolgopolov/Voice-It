package com.com.technoparkproject.view_models;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.repositories.TestRecordsRepository;
import com.example.player.PlayerServiceConnection;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends ViewModel{
    PlayerServiceConnection playerServiceConnection;
    public LiveData<PlaybackStateCompat> currentState;
    public List<Record> recordList = new ArrayList<>();

    public PlaylistViewModel(PlayerServiceConnection playerServiceConnection) {
        this.playerServiceConnection = playerServiceConnection;
        for (String uuid: playerServiceConnection.playlist){
            this.recordList.add(TestRecordsRepository.getRecordByUUID(uuid));
        }
    }

    public void itemClicked(int position) {
        playerServiceConnection.setCurrentIndex(position);
        playerServiceConnection.mediaController.getTransportControls().play();
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory{
        PlayerServiceConnection playerServiceConnection;
        public Factory(Context context) {
            super();
            this.playerServiceConnection = PlayerServiceConnection.getInstance(context.getApplicationContext()
            );
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PlaylistViewModel(playerServiceConnection);
        }
    }
}
