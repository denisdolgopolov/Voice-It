package com.com.technoparkproject.view_models;

import android.app.Application;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.widget.AutoCompleteTextView;

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
import com.example.player.PlayerService;
import com.example.player.PlayerServiceConnection;

import java.util.ArrayList;
import java.util.List;

public class MainListOfRecordsViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    private final MediatorLiveData<ArrayMap<Topic, List<Record>>> topicRecords;
    private final MutableLiveData<String> searchingValue = new MutableLiveData<>();
    public MutableLiveData<String> nowPlayingRecordUUID;

    public LiveData<ArrayMap<Topic, List<Record>>> getTopicRecords() {
        return topicRecords;
    }

    public void queryRecordTopics(){
        LiveData<ArrayMap<Topic, List<Record>>> repoRecords = AppRepoImpl.getAppRepo(getApplication()).queryAllTopicRecords();
        topicRecords.addSource(repoRecords, topicRecs -> {
            topicRecords.setValue(topicRecs);
            topicRecords.removeSource(repoRecords);
        });
    }

    public LiveData<String> getSearchingValue() {
        return searchingValue;
    }

    public void setSearchingInput(AutoCompleteTextView editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchingValue.postValue(s.toString());
            }
        });
    }

    public void addToPlaylistClicked(Record record) {
        //TODO
        playerServiceConnection.addToPlaylist(PlayerConverter.toPlayerRecord(record));
    }


    public void itemClicked(Record record) {
        playerServiceConnection.clearPlaylist();
        List<Record> tempPlaylist = new ArrayList<>();
        ArrayMap<Topic, List<Record>> list = topicRecords.getValue();
        for (int i = 0; i < list.size(); i++) {
            tempPlaylist.addAll(list.valueAt(i));
        }
        for (int i = 0; i < tempPlaylist.size(); i++) {
            playerServiceConnection.addToPlaylist(PlayerConverter.toPlayerRecord(tempPlaylist.get(i)));
        }
        playerServiceConnection.setCurrentIndex(tempPlaylist.indexOf(record));
        playerServiceConnection.mediaController.getTransportControls().play();
    }

    public MainListOfRecordsViewModel(@NonNull Application application) {
        super(application);

        this.playerServiceConnection = ((VoiceItApplication) application).getPlayerServiceConnection();
        topicRecords = new MediatorLiveData<>();
        topicRecords.setValue(new ArrayMap<>());
        queryRecordTopics();

        this.nowPlayingRecordUUID = this.playerServiceConnection.nowPlayingRecordUUID;
    }

}
