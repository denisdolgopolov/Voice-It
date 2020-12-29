package com.com.technoparkproject.view_models;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.model_converters.PlayerConverter;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.repo.AppRepoImpl;
import com.example.player.PlayerServiceConnection;

import java.util.List;

public class AnotherAccountListOfRecordsViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    private final MediatorLiveData<ArrayMap<Topic, List<Record>>> topicRecords;
    public MutableLiveData<String> nowPlayingRecordUUID;
    private Bitmap profileImage = null;
    public String userUUID;

    public LiveData<ArrayMap<Topic, List<Record>>> getTopicRecords() {
        return topicRecords;
    }

    public void queryRecordTopics(String userUUID) {
        this.userUUID = userUUID;
        LiveData<ArrayMap<Topic, List<Record>>> repoRecords = AppRepoImpl
                .getAppRepo(getApplication())
                .queryAllTopicRecordsByUser(userUUID,false);
        System.out.println(userUUID + " query");
        topicRecords.addSource(repoRecords, topicRecs -> {
            topicRecords.setValue(topicRecs);
            topicRecords.removeSource(repoRecords);
        });
    }

    public void addToPlaylistClicked(Record record) {
        //TODO
        playerServiceConnection.addToPlaylist(PlayerConverter.toPlayerRecord(record));
    }


    public void itemClicked(Record record) {
        playerServiceConnection.clearPlaylist();
        int currentIndex = 0;
        ArrayMap<Topic, List<Record>> list = topicRecords.getValue();
        for (int i = 0; i < list.size(); i++) {
            for (int g = 0; g < list.valueAt(i).size(); g++) {
                Record currentRecord = list.valueAt(i).get(g);
                playerServiceConnection.addToPlaylist(PlayerConverter.toPlayerRecord(currentRecord));
                if (currentRecord.equals(record)) {
                    playerServiceConnection.setCurrentIndex(currentIndex);
                }
            }
            currentIndex++;
        }
        playerServiceConnection.mediaController.getTransportControls().play();
    }


    public AnotherAccountListOfRecordsViewModel(@NonNull Application application) {
        super(application);

        this.playerServiceConnection = ((VoiceItApplication) application).getPlayerServiceConnection();
        topicRecords = new MediatorLiveData<>();

        this.nowPlayingRecordUUID = this.playerServiceConnection.nowPlayingRecordUUID;
    }

    public void setProfileImage(Bitmap profileImage){
        this.profileImage = profileImage;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

}
