package com.com.technoparkproject.viewmodels;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.model_converters.RecordConverter;
import com.com.technoparkproject.models.TopicTypes;
import com.technopark.recorder.RecordState;
import com.technopark.recorder.RecorderApplication;
import com.technopark.recorder.repository.RecordTopic;
import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.service.Recorder;
import com.technopark.recorder.service.RecordingService;
import com.technopark.recorder.utils.SingleLiveEvent;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.UUID;

import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseListener;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class RecorderViewModel extends AndroidViewModel {

    private final RecTimeLimitObserver mRecLimObserver;
    private final ServiceConnection serviceConnection;
    private Recorder mRecorder;

    private class RecTimeLimitObserver implements Observer<Boolean> {
        @Override
        public void onChanged(Boolean markerReached) {
            if (markerReached)
                //make stop progress LiveData, record time limit reached
                handleStop(false);
        }
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

    private static final int MAX_RECORD_LENGTH = 15; //max allowed recording in seconds

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        mRecLimObserver = new RecTimeLimitObserver();
        mRecState = new MediatorLiveData<>();
        mRecState.setValue(RecordState.INIT);
        mRecTime = new MediatorLiveData<>();
        mRecTime.setValue(0);
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRecorder = ((RecordingService.RecordBinder) service).getRecorder();
                mRecState.addSource(mRecorder.getRecordState(),
                        mRecState::setValue);
                mRecTime.addSource(mRecorder.getRecTime(),
                        mRecTime::setValue);
                mRecorder.getRecMarker().observeForever(mRecLimObserver);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRecState.removeSource(mRecorder.getRecordState());
                mRecTime.removeSource(mRecorder.getRecTime());
                mRecorder.getRecMarker().removeObserver(mRecLimObserver);
                mRecorder = null;
            }
        };
        final Intent serviceIntent = new Intent(application, RecordingService.class);
        application.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getApplication().unbindService(serviceConnection);
        mRecState.removeSource(mRecorder.getRecordState());
        mRecTime.removeSource(mRecorder.getRecTime());
        mRecorder.getRecMarker().removeObserver(mRecLimObserver);
    }


    public void OnRecPauseClick(){
        if (getRecState().getValue() == RecordState.INIT) {
            mRecorder.setMarkerPos(getMaxRecordLength());
            mRecorder.start();
        }
        else if (getRecState().getValue() == RecordState.PAUSE)
            mRecorder.resume();
        else if (getRecState().getValue() == RecordState.RECORDING)
            mRecorder.pause();

    }


    public MediatorLiveData<RecStopState> mRecStopState = new MediatorLiveData<>();

    public void onStopClick() {
        onStopClick(false);
    }

    private void onStopClick(final boolean isOnSave){
        mRecorder.stop();
        handleStop(isOnSave);
    }

    private void handleStop(final boolean isOnSave){
        mRecStopState.setValue(RecStopState.STOP_IN_PROGRESS);
        mRecStopState.addSource(getRecState(), new Observer<RecordState>() {
            @Override
            public void onChanged(RecordState recordState) {
                if (recordState == RecordState.STOP){
                    mRecStopState.setValue(RecStopState.STOP_COMPLETED);
                    mRecStopState.removeSource(getRecState());
                    if (isOnSave)
                        saveRec();
                }

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

    //todo upload recording
    private void loadFile(RecordTopic recTopic) {
        try {
            FileInputStream inputStream = new FileInputStream(recTopic.getRecordFile());
            final String recordUUID = UUID.randomUUID().toString();
            final String topicUUID = UUID.randomUUID().toString();

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
                            RecordConverter.toFirebaseModel(recTopic, recordUUID, topicUUID)
                    );
                    Log.d("save file", "saving record: " + recTopic.toString());
                }

                @Override
                public void onFailure(String error) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //private RecordTopic mRec;

    private void saveRec(){
        RecordTopicRepo repo = RecorderApplication.from(getApplication()).getRecordTopicRepo();
        repo.updateLastDuration(mRecorder.getDuration());

        //configure recorder for next recording
        mRecorder.configure();

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
