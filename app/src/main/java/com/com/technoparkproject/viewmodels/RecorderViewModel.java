package com.com.technoparkproject.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.SingleLiveEvent;
import com.com.technoparkproject.repository.Record;
import com.com.technoparkproject.repository.RecordRepoImpl;
import com.com.technoparkproject.service.RecordState;
import com.com.technoparkproject.service.Recorder;
import com.com.technoparkproject.service.RecorderConnection;
import com.com.technoparkproject.utils.InjectorUtils;

import java.io.File;

public class RecorderViewModel extends AndroidViewModel {

    public MediatorLiveData<RecordState> getRecState(){
        return mRecLiveData.getRecState();
    }

    private final RecorderConnection.RecordingLiveData mRecLiveData;


    public MediatorLiveData<Integer> getRecTime() {
       return mRecLiveData.getTimeData();
    }

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        mRecLiveData = InjectorUtils.provideRecordingLiveData(getApplication());
    }


    public void OnRecPauseClick(){
        Recorder recorder = InjectorUtils.provideRecorder(getApplication());

        if (getRecState().getValue() == RecordState.READY)
            recorder.startRecording();
        else if (getRecState().getValue() == RecordState.PAUSE)
            recorder.resumeRecording();
        else if (getRecState().getValue() == RecordState.RECORDING)
            recorder.pauseRecording();

    }


    public MediatorLiveData<RecStopState> mRecStopState = new MediatorLiveData<>();

    public void onStopClick() {
        onStopClick(false);
    }

    private void onStopClick(final boolean isOnSave){
        Recorder recorder = InjectorUtils.provideRecorder(getApplication());

        //handle async recording stop
        mRecStopState.setValue(RecStopState.STOP_IN_PROGRESS);
        recorder.stopRecording();
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
        RecordRepoImpl.getInstance(getApplication()).deleteTempFile(recFile);
        mRec = null;
    }

    public void saveRecording(){
        if (mRec!=null) {
            RecordRepoImpl.getInstance(getApplication()).addRecord(mRec);
        }
    }

    private Record mRec;

    private void saveRec(){
        Recorder recorder = InjectorUtils.provideRecorder(getApplication());

        Record rec = recorder.saveRecording();
        if (rec == null) {
            onStopClick(true);
            return;
        }
        rec.setName(mRecName);
        rec.setTopic(mRecTopic);
        mRec = rec;
        mSaveEvent.call(); //recording is ready to be saved to repo/other storage
    }

    private final SingleLiveEvent<Void> mSaveEvent = new SingleLiveEvent<>();

    public SingleLiveEvent<Void> getSaveEvent() {
        return mSaveEvent;
    }
    public void onSaveClick(String recName, String recTopic) {
        mRecName = recName;
        mRecTopic = recTopic;
        saveRec();
    }

    public enum RecStopState{
        STOP_IN_PROGRESS,
        STOP_COMPLETED
    }
}
