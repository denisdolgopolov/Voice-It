package com.com.technoparkproject.viewmodels;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.technopark.recorder.repository.RecordTopicRepo;
import com.technopark.recorder.service.RecorderConnection;
import com.technopark.recorder.utils.SingleLiveEvent;
import com.technopark.recorder.repository.RecordTopic;
import com.technopark.recorder.RecordState;
import com.technopark.recorder.service.RecService;
import com.technopark.recorder.utils.InjectorUtils;

public class RecorderViewModel extends AndroidViewModel {

    private final RecTimeLimitObserver mRecLimObserver;

    private class RecTimeLimitObserver implements Observer<Void> {
        @Override
        public void onChanged(Void aVoid) {
            //make stop progress LiveData, record time limit reached
            mRecStopState.setValue(RecStopState.STOP_IN_PROGRESS);
            mRecStopState.addSource(getRecState(), new Observer<RecordState>() {
                @Override
                public void onChanged(RecordState recordState) {
                    if (recordState == RecordState.STOP){
                        mRecStopState.setValue(RecStopState.STOP_COMPLETED);
                        mRecStopState.removeSource(getRecState());
                    }

                }
            });
        }
    }

    public LiveData<RecordState> getRecState(){
        return mRecServiceData.getRecState();
    }

    public LiveData<Integer> getRecTime() {
        return mRecServiceData.getRecTime();
    }

    private final RecorderConnection.RecServiceLiveData mRecServiceData;

    public int getMaxRecordLength(){
        return InjectorUtils.provideRecService(getApplication()).getMaxRecDuration();
    }

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        mRecServiceData = InjectorUtils.provideRecServiceData(getApplication());
        mRecLimObserver = new RecTimeLimitObserver();
        mRecServiceData.getRecLimit().observeForever(mRecLimObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRecServiceData.getRecLimit().removeObserver(mRecLimObserver);
    }


    public void OnRecPauseClick(){
        RecService recService = InjectorUtils.provideRecService(getApplication());
        if (getRecState().getValue() == RecordState.INIT)
            recService.startRecording();
        else if (getRecState().getValue() == RecordState.PAUSE)
            recService.resumeRecording();
        else if (getRecState().getValue() == RecordState.RECORDING)
            recService.pauseRecording();

    }


    public MediatorLiveData<RecStopState> mRecStopState = new MediatorLiveData<>();

    public void onStopClick() {
        onStopClick(false);
    }

    private void onStopClick(final boolean isOnSave){
        RecService recService = InjectorUtils.provideRecService(getApplication());

        //handle async recording stop
        mRecStopState.setValue(RecStopState.STOP_IN_PROGRESS);
        recService.stopRecording();
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
        RecordTopicRepo repo = InjectorUtils.provideRecordTopicRepo(getApplication());
        repo.deleteLastRecord();
    }

    public void saveRecording() {
        RecordTopicRepo repo = InjectorUtils.provideRecordTopicRepo(getApplication());
        loadFile(repo.getLastRecord());
    }

    //todo upload recording
    private void loadFile(RecordTopic recTopic){
        Log.d("save file","saving record: "+recTopic.toString());
    }

    //private RecordTopic mRec;

    private void saveRec(){
        RecService recService = InjectorUtils.provideRecService(getApplication());
        RecordTopicRepo repo = InjectorUtils.provideRecordTopicRepo(getApplication());
        repo.updateLastDuration(recService.getRecordDuration());

        //configure recorder for next recording
        recService.configureRecording();

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

        RecordTopicRepo repo = InjectorUtils.provideRecordTopicRepo(getApplication());
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
