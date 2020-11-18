package com.com.technoparkproject.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.repository.Record;
import com.com.technoparkproject.repository.RecordRepoImpl;
import com.com.technoparkproject.service.RecordingService;
import com.com.technoparkproject.service.RecordingServiceConnection;
import com.com.technoparkproject.utils.InjectorUtils;

public class RecorderViewModel extends AndroidViewModel {

    private final MediatorLiveData<RecordingService.RecordState> mRecState;

    public MediatorLiveData<RecordingService.RecordState> getRecState(){
        return mRecState;
    }

    private final MediatorLiveData<Integer> mRecTime;

    //todo remove variables and return service livedata straight from injector
    public MediatorLiveData<Integer> getRecTime() {

        return mRecTime;
    }

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        RecordingServiceConnection recServiceConn = InjectorUtils.provideRecordingServiceConn(getApplication());
        mRecTime = recServiceConn.getTimeData();
        mRecState = recServiceConn.getRecState();
    }


    public void OnRecPauseClick(){
        RecordingService recService = InjectorUtils.provideRecordingService(getApplication());

        if (mRecState.getValue() == RecordingService.RecordState.READY)
            recService.startRecording();
        else if (mRecState.getValue() == RecordingService.RecordState.PAUSE)
            recService.resumeRecording();
        else if (mRecState.getValue() == RecordingService.RecordState.RECORDING)
            recService.pauseRecording();

    }


    public MediatorLiveData<RecStopState> mRecStopState = new MediatorLiveData<>();

    public void onStopClick() {
        onStopClick(false);
    }

    private void onStopClick(final boolean isOnSave){
        RecordingService recService = InjectorUtils.provideRecordingService(getApplication());

        //handle async recording stop
        mRecStopState.setValue(RecStopState.STOP_IN_PROGRESS);
        recService.stopRecording();
        mRecStopState.addSource(mRecState, new Observer<RecordingService.RecordState>() {
            @Override
            public void onChanged(RecordingService.RecordState recordState) {
                if (recordState == RecordingService.RecordState.STOP){
                    mRecStopState.setValue(RecStopState.STOP_COMPLETED);
                    mRecStopState.removeSource(mRecState);
                    if (isOnSave)
                        saveRec();
                }

            }
        });
    }

    private String mRecName = "Новая запись";
    private String mRecTopic = "Топик записи";

    private void saveRec(){
        RecordingService recService = InjectorUtils.provideRecordingService(getApplication());
        Record rec = recService.saveRecording();
        if (rec == null) {
            onStopClick(true);
            return;
        }
        rec.setName(mRecName);
        rec.setTopic(mRecTopic);

        RecordRepoImpl.getInstance(getApplication()).addRecord(rec);
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
