package com.com.technoparkproject.view_models;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.model_converters.RecordConverter;
import com.com.technoparkproject.models.TopicTypes;
import com.com.technoparkproject.view.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.technopark.recorder.RecordState;
import com.technopark.recorder.RecorderApplication;
import com.technopark.recorder.repository.RecordTopic;
import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.utils.SingleLiveEvent;
import com.technopark.recorder.viewmodels.RecorderViewModel;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.UUID;

import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseListener;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class RecordViewModel extends RecorderViewModel {

    private String currentUserId;

    private final RecTimeLimitObserver mRecLimObserver;

    private class RecTimeLimitObserver implements Observer<Boolean> {
        @Override
        public void onChanged(Boolean markerReached) {
            if (markerReached)
                //make stop progress LiveData, record time limit reached
                handleStop(false);
        }
    }

    public void setCurrentUserId(String userId) {
        currentUserId = userId;
    }

    private final MediatorLiveData<RecordState> mRecState;

    public LiveData<RecordState> getRecState(){
        return mRecState;
    }

    private final MediatorLiveData<Integer> mRecTime;

    public LiveData<Integer> getRecTime(){
        return mRecTime;
    }

    public int getMaxRecordLength(){
        return MAX_RECORD_LENGTH;
    }

    private static final int MAX_RECORD_LENGTH = 45; //max allowed recording in seconds

    public RecordViewModel(@NonNull Application application) {
        super(application);
        mRecLimObserver = new RecTimeLimitObserver();
        mRecState = new MediatorLiveData<>();
        mRecState.setValue(RecordState.INIT);
        mRecTime = new MediatorLiveData<>();
        mRecTime.setValue(0);
    }

    @Override
    protected void onRecorderDisconnected() {
        mRecState.removeSource(getRecorder().getRecordState());
        mRecTime.removeSource(getRecorder().getRecTime());
        getRecorder().getRecMarker().removeObserver(mRecLimObserver);
    }

    @Override
    protected void onRecorderConnected() {
        mRecState.addSource(getRecorder().getRecordState(),
                mRecState::setValue);
        mRecTime.addSource(getRecorder().getRecTime(),
                mRecTime::setValue);
        getRecorder().getRecMarker().observeForever(mRecLimObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRecState.removeSource(getRecorder().getRecordState());
        mRecTime.removeSource(getRecorder().getRecTime());
        getRecorder().getRecMarker().removeObserver(mRecLimObserver);
    }


    public void OnRecPauseClick(){
        if (getRecState().getValue() == RecordState.INIT) {
            getRecorder().setMarkerPos(getMaxRecordLength());
            getRecorder().start();
        }
        else if (getRecState().getValue() == RecordState.PAUSE)
            getRecorder().resume();
        else if (getRecState().getValue() == RecordState.RECORDING)
            getRecorder().pause();

    }

    public MediatorLiveData<RecStopState> mRecStopState = new MediatorLiveData<>();

    public void onStopClick() {
        onStopClick(false);
    }

    private void onStopClick(final boolean isOnSave){
        getRecorder().stop();
        handleStop(isOnSave);
    }

    private void handleStop(final boolean isOnSave){
        mRecStopState.setValue(RecStopState.STOP_IN_PROGRESS);
        mRecStopState.addSource(getRecState(), recordState -> {
            if (recordState == RecordState.STOP){
                mRecStopState.setValue(RecStopState.STOP_COMPLETED);
                mRecStopState.removeSource(getRecState());
                if (isOnSave)
                    saveRec();
            }

        });
    }

    public void dismissRecording(){
        RecordTopicRepo repo = RecorderApplication.from(getApplication()).getRecordTopicRepo();
        repo.deleteLastRecord();
    }

    public void saveRecording() {
        RecordTopicRepo repo = RecorderApplication.from(getApplication()).getRecordTopicRepo();
        loadFile(repo.getLastRecord());
    }

    private void loadFile(RecordTopic recTopic) {
        try {
            FileInputStream inputStream = new FileInputStream(recTopic.getRecordFile());
            final String recordUUID = UUID.randomUUID().toString();
            final String topicUUID = UUID.randomUUID().toString();
            final String userUUID = currentUserId;

            FirebaseTopic topic = new FirebaseTopic(recTopic.getName(),
                    "randomUUID",
                    Collections.singletonList(recordUUID),
                    TopicTypes.TOPIC_THEMATIC.toString(),
                    topicUUID);

            new FirebaseLoader().add(topic, new FirebaseListener() {
                @Override
                public void onSuccess() {
                    new FirebaseFileLoader(getApplication()).uploadFile(
                            inputStream,
                            FirebaseFileTypes.RECORDS,
                            recTopic.getRecordFile().length(),
                            RecordConverter.toFirebaseModel(recTopic, recordUUID, topicUUID, userUUID),
                            new FirebaseListener() {
                                @Override
                                public void onSuccess() {
                                    dismissRecording();
                                }

                                @Override
                                public void onFailure(String error) {
                                }
                            }
                    );
                }

                @Override
                public void onFailure(String error) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRec(){
        RecordTopicRepo repo = RecorderApplication.from(getApplication()).getRecordTopicRepo();
        repo.updateLastDuration(getRecorder().getDuration());

        //configure recorder for next recording
        getRecorder().configure();

        mSaveEvent.call(); //recording is ready to be saved to repo/other storage
    }

    private final SingleLiveEvent<Void> mSaveEvent = new SingleLiveEvent<>();

    public SingleLiveEvent<Void> getSaveEvent() {
        return mSaveEvent;
    }

    public void onSaveClick(String recName, String recTopic) {
        if (!isRecTextValid(recName,recTopic)) {
            if (getRecState().getValue() != RecordState.STOP){
                onStopClick(false);
            }
            return;
        }

        RecordTopicRepo repo = RecorderApplication.from(getApplication()).getRecordTopicRepo();
        repo.updateLastName(recName);
        repo.updateLastTopic(recTopic);

        if (getRecState().getValue() != RecordState.STOP){
            onStopClick(true);
        }
        else{
            saveRec();
        }
    }

    private final MutableLiveData<RecTextState> mNameState = new MutableLiveData<>(RecTextState.VALID);

    public LiveData<RecTextState> getNameState() {
        return mNameState;
    }

    private final MutableLiveData<RecTextState> mTopicState = new MutableLiveData<>(RecTextState.VALID);

    public LiveData<RecTextState> getTopicState() {
        return mTopicState;
    }

    public enum RecTextState{
        INVALID,
        VALID
    }

    private boolean isRecTextValid(String recName, String recTopic){
        boolean isNameValid = isInputTextValid(recName,mNameState);
        boolean isTopicValid = isInputTextValid(recTopic,mTopicState);
        return isNameValid && isTopicValid;
    }

    private boolean isInputTextValid(String text, MutableLiveData<RecTextState> textState){
        if (TextUtils.isEmpty(text)) {
            textState.setValue(RecTextState.INVALID);
            return false;
        }
        textState.setValue(RecTextState.VALID);
        return true;
    }

    public enum RecStopState{
        STOP_IN_PROGRESS,
        STOP_COMPLETED
    }
}
