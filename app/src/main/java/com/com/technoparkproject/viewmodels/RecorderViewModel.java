package com.com.technoparkproject.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.SingleLiveEvent;
import com.com.technoparkproject.repository.RecordRepo;
import com.com.technoparkproject.repository.RecordTopic;
import com.com.technoparkproject.service.AudioRecorder;
import com.com.technoparkproject.service.RecordState;
import com.com.technoparkproject.service.RecService;
import com.com.technoparkproject.utils.InjectorUtils;

import java.io.File;

public class RecorderViewModel extends AndroidViewModel {

    public LiveData<RecordState> getRecState(){
        return mRecState;
    }

    public LiveData<Integer> getRecTime() {
       return mRecTime;
    }

    private final MediatorLiveData<RecordState> mRecState;

    private final MediatorLiveData<Integer> mRecTime;

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        mRecState = new MediatorLiveData<>();
        mRecTime = new MediatorLiveData<>();
        mRecTime.setValue(0);
        AudioRecorder recorder = InjectorUtils.provideRecorder(application);
        mRecState.addSource(recorder.getRecordState(), new Observer<RecordState>() {
            @Override
            public void onChanged(RecordState recordState) {
                mRecState.setValue(recordState);
            }
        });
        mRecTime.addSource(recorder.getRecTime(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer seconds) {
                mRecTime.setValue(seconds);
            }
        });

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

    @Override
    protected void onCleared() {
        super.onCleared();
        AudioRecorder recorder = InjectorUtils.provideRecorder(getApplication());
        mRecState.removeSource(recorder.getRecordState());
        mRecTime.removeSource(recorder.getRecTime());
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

    private String mRecName = "Новая запись";
    private String mRecTopic = "Топик записи";


    public void dismissRecording(){
        File recFile = mRec.getRecordFile();
        RecordRepo.deleteTempFile(recFile);
        mRec = null;
    }

    public void saveRecording(){
        if (mRec!=null) {
            loadFile(mRec);
        }
    }

    //todo upload recording
    private void loadFile(RecordTopic recTopic){
        Log.d("save file","saving record: "+recTopic.toString());
    }

    private RecordTopic mRec;

    private void saveRec(){
        RecService recService = InjectorUtils.provideRecService(getApplication());

        if (getRecState().getValue() != RecordState.STOP){
            onStopClick(true);
            return;
        }
        RecordTopic rec = new RecordTopic();
        rec.setRecordFile(recService.getRecordFile());
        rec.setDuration(recService.getRecordDuration());
        rec.setName(mRecName);
        rec.setTopic(mRecTopic);
        mRec = rec;

        //configure recorder for next recording
        recService.configureRecording();
        mRecTime.setValue(0);

        mSaveEvent.call(); //recording is ready to be saved to repo/other storage
    }

    private final SingleLiveEvent<Void> mSaveEvent = new SingleLiveEvent<>();

    public SingleLiveEvent<Void> getSaveEvent() {
        return mSaveEvent;
    }
    public void onSaveClick(String recName, String recTopic) {
        if (recName != null && !recName.isEmpty())
            mRecName = recName;
        if (recTopic != null && !recTopic.isEmpty())
            mRecTopic = recTopic;
        saveRec();
    }

    public enum RecStopState{
        STOP_IN_PROGRESS,
        STOP_COMPLETED
    }
}
