package com.com.technoparkproject.view_models;

import android.app.Application;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.com.technoparkproject.VoiceItApplication;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.example.player.PlayerServiceConnection;

import java.util.List;

public class MainListOfRecordsViewModel extends AndroidViewModel {
    PlayerServiceConnection playerServiceConnection;
    private MutableLiveData<List<Topic>> topics;
    private MutableLiveData<String> searchingValue = new MutableLiveData<>();

    public LiveData<List<Topic>> getTopics() {
        if(topics == null) {
            topics = new MutableLiveData<>();
            queryTopics();
        }
        return topics;
    }

    private void queryTopics() {
        //List<Topic> topics = TestRecordsRepository.getListTopics();
        //this.topics.postValue(topics);
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
        playerServiceConnection.addToPlaylist(record.uuid);
    }

    public MainListOfRecordsViewModel(@NonNull Application application) {
        super(application);
        this.playerServiceConnection = ((VoiceItApplication) application).playerServiceConnection;
    }

}
