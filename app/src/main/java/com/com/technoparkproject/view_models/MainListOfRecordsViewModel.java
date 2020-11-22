package com.com.technoparkproject.view_models;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.repositories.TestRecordsRepository;
import com.example.player.PlayerServiceConnection;

import java.util.List;

public class MainListOfRecordsViewModel extends ViewModel {
    PlayerServiceConnection playerServiceConnection;
    private MutableLiveData<List<Topic>> topics;
    private MutableLiveData<String> searchingValue = new MutableLiveData<>();
    static final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    public LiveData<List<Topic>> getTopics() {
        if(topics == null) {
            topics = new MutableLiveData<>();
            queryTopics();
        }
        return topics;
    }

    private void queryTopics() {
        List<Topic> topics = TestRecordsRepository.getListTopics();
        this.topics.postValue(topics);
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
        playerServiceConnection.addToPlaylist(record.uuid);
    }

    public MainListOfRecordsViewModel(PlayerServiceConnection playerServiceConnection) {
        super();
        this.playerServiceConnection = playerServiceConnection;
    }
    public static MediaMetadataCompat recordToMediaMetadataCompat(Record record){
        /*metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), record.getBitmapResId()));*/
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, record.name);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, record.topicUUID);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, record.userUUID);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(record.duration));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, record.uuid);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, TestRecordsRepository.getUriFromRecordUUID(record.uuid));
        return metadataBuilder.build();
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
            return (T) new MainListOfRecordsViewModel(playerServiceConnection);
        }
    }

}
