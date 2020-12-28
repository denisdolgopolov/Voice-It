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
import com.com.technoparkproject.repo.LoadStatus;
import com.example.player.PlayerServiceConnection;
import com.google.firebase.auth.FirebaseAuth;

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
        String userUUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LiveData<ArrayMap<Topic, List<Record>>> repoRecords = AppRepoImpl
                .getAppRepo(getApplication())
                .queryAllTopicRecordsByUser(userUUID,false);
        topicRecords.addSource(repoRecords, topicRecs -> {
            topicRecords.setValue(topicRecs);
            topicRecords.removeSource(repoRecords);
        });
    }

    public LiveData<LoadStatus> getLoadStatus(){
        return AppRepoImpl
                .getAppRepo(getApplication()).getLoadStatus();
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

    public MainListOfRecordsViewModel(@NonNull Application application) {
        super(application);

        this.playerServiceConnection = ((VoiceItApplication) application).getPlayerServiceConnection();
        topicRecords = new MediatorLiveData<>();
        queryRecordTopics();

        this.nowPlayingRecordUUID = this.playerServiceConnection.nowPlayingRecordUUID;
    }

}
