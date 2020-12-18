package com.com.technoparkproject.view_models;

import android.app.Application;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.model_converters.FirebaseConverter;
import com.com.technoparkproject.model_converters.PlayerConverter;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.example.player.PlayerServiceConnection;

import java.util.List;

import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListListener;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class MainListOfRecordsViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    private MutableLiveData<List<Topic>> topics;
    private MutableLiveData<ArrayMap<Topic, List<Record>>> records;
    private final MutableLiveData<String> searchingValue = new MutableLiveData<>();
    public MutableLiveData<String> nowPlayingRecordUUID;

    public LiveData<List<Topic>> getTopics() {
        if (topics == null) {
            topics = new MutableLiveData<>();
            queryTopics();
        }
        return topics;
    }

    public LiveData<ArrayMap<Topic, List<Record>>> getRecords() {
        if (records == null) {
            records = new MutableLiveData<>();
            records.setValue(new ArrayMap<>());
        }
        return records;
    }

    public void queryTopics() {
        new FirebaseLoader().getAll(FirebaseCollections.Topics, new FirebaseGetListListener<FirebaseTopic>() {
            @Override
            public void onFailure(String error) {
            }

            @Override
            public void onGet(List<FirebaseTopic> item) {
                List<Topic> topics = new FirebaseConverter().toTopicList(item);
                MainListOfRecordsViewModel.this.topics.setValue(topics);
            }
        });
    }

    public void clearRecordsList() {
        MainListOfRecordsViewModel.this.records.setValue(new ArrayMap<>());
    }

    public void queryRecord(final Topic topic) {
        new FirebaseLoader().getAll(FirebaseCollections.Topics, topic.uuid,
                new FirebaseGetListListener<FirebaseRecord>() {
                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onGet(List<FirebaseRecord> item) {
                        List<Record> records = new FirebaseConverter().toRecordList(item);
                        MainListOfRecordsViewModel.this.records.getValue().put(topic, records);
                        MainListOfRecordsViewModel.this.records.setValue(MainListOfRecordsViewModel.this.records.getValue());
                    }
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
        int currentIndex = 0;
        ArrayMap<Topic, List<Record>> list = records.getValue();
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
        /*for (Topic topic : topics.getValue()) {
            for (Record recordInTopic : records.getValue().get(topic)) {
                playerServiceConnection.addToPlaylist(PlayerConverter.toPlayerRecord(recordInTopic));
                if (recordInTopic.equals(record)) {
                    playerServiceConnection.setCurrentIndex(currentIndex);
                }
            }
            currentIndex++;
        }*/
        playerServiceConnection.mediaController.getTransportControls().play();
    }

    public MainListOfRecordsViewModel(@NonNull Application application) {
        super(application);
        this.playerServiceConnection = ((VoiceItApplication) application).getPlayerServiceConnection();
        this.nowPlayingRecordUUID = playerServiceConnection.nowPlayingRecordUUID;
    }

}
