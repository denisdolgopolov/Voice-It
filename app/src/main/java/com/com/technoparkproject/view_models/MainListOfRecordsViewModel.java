package com.com.technoparkproject.view_models;

import android.app.Application;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.model_converters.FirebaseConverter;
import com.com.technoparkproject.model_converters.FromRoomConverter;
import com.com.technoparkproject.model_converters.PlayerConverter;
import com.com.technoparkproject.model_converters.ToRoomConverter;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.repo.AppRepoImpl;
import com.example.player.PlayerServiceConnection;
import com.technopark.room.db.AppRoomDatabase;

import java.util.AbstractMap;
import java.util.List;

import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListListener;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class MainListOfRecordsViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    private final MediatorLiveData<List<Topic>> topics;
    private final MediatorLiveData<ArrayMap<Topic, List<Record>>> records;
    private final MutableLiveData<String> searchingValue = new MutableLiveData<>();

    public LiveData<List<Topic>> getTopics() {
        return topics;
    }

    public LiveData<ArrayMap<Topic, List<Record>>> getTopicRecords() {
        return records;
    }

    public void queryTopics() {
        LiveData<List<Topic>> repoTopics = AppRepoImpl.getAppRepo(getApplication()).queryTopics();
        topics.addSource(repoTopics, topicsList -> {
            topics.setValue(topicsList);
            topics.removeSource(repoTopics);
        });
    }

    public void clearRecordsList() {
        MainListOfRecordsViewModel.this.records.setValue(new ArrayMap<>());
    }

    public void queryRecord(final Topic topic) {
        LiveData<List<Record>> repoRecords = AppRepoImpl.getAppRepo(getApplication()).queryRecords(topic);
        records.addSource(repoRecords, recordsList -> {
            ArrayMap<Topic, List<Record>> topicRecords = records.getValue();
            topicRecords.put(topic, recordsList);
            records.setValue(topicRecords);
            records.removeSource(repoRecords);
        });
    }

    public void queryRecordTopics(){
        LiveData<ArrayMap<Topic, List<Record>>> repoRecords = AppRepoImpl.getAppRepo(getApplication()).queryAllTopicRecords();
        records.addSource(repoRecords, topicRecs -> {
            records.setValue(topicRecs);
            records.removeSource(repoRecords);
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

    public MainListOfRecordsViewModel(@NonNull Application application) {
        super(application);
        this.playerServiceConnection = ((VoiceItApplication) application).playerServiceConnection;
        records = new MediatorLiveData<>();
        records.setValue(new ArrayMap<>());
        topics = new MediatorLiveData<>();
        queryRecordTopics();
    }

}
