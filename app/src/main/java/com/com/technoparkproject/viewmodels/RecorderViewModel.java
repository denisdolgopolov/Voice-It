package com.com.technoparkproject.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.com.technoparkproject.repository.Record;
import com.com.technoparkproject.repository.RecordRepo;
import com.com.technoparkproject.service.RecordingService;
import com.com.technoparkproject.utils.InjectorUtils;

public class RecorderViewModel extends AndroidViewModel {


    public enum RecordState{
        READY,
        RECORDING,
        PAUSE,
        STOP
    }

    private MutableLiveData<RecordState> mRecState2;

    public MutableLiveData<RecordState> getRecState2(){
        return mRecState2;
    }


    private MediatorLiveData<RecordingService.RecordState> mRecState;

    public MediatorLiveData<RecordingService.RecordState> getRecState(){
        return mRecState;
    }

    private MediatorLiveData<Integer> mRecTime = new MediatorLiveData<>();

    //todo remove variables and return service livedata straight from injector
    public MutableLiveData<Integer> getRecTime() {

        return mRecTime;
    }

    public RecorderViewModel(@NonNull Application application) {
        super(application);
        RecordingService recordingService = InjectorUtils.provideRecordingService(getApplication());

        //todo clean restore state: after user relaunch app
        //this is the case when app is closed by user, but recording may still be on
        if (recordingService==null){
            mRecState2 = new MutableLiveData<>(RecordState.READY);
            //mRecTime.setValue(0);
        }
        else if (recordingService.isRecording()) {
            mRecState2 = new MutableLiveData<>(RecordState.RECORDING);
            /*mRecTime.addSource(recordingService.getRecTime(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer timeInSec) {
                    mRecTime.setValue(timeInSec);
                }
            });*/
        }
        mRecTime = InjectorUtils.provideRecordingServiceConn(getApplication()).testData;
        mRecState = InjectorUtils.provideRecordingServiceConn(getApplication()).recState;
    }


    public void OnRecPauseClick(){
        RecordingService recService = InjectorUtils.provideRecordingService(getApplication());

        //if (mRecState.getValue() == RecordState.READY){
                /*RecTime.addSource(recService.getRecTime(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer timeInSec) {
                    mRecTime.setValue(timeInSec);
                }
            });*/
        //}

        //OLD way
        /*if (mRecState2.getValue() == RecordState.READY){
            recService.startRecording();
            mRecState2.setValue(RecordState.RECORDING);
        }
        else if ( mRecState2.getValue() == RecordState.PAUSE){
            recService.resumeRecording();
            mRecState2.setValue(RecordState.RECORDING);
        }
        else if (mRecState2.getValue() == RecordState.RECORDING){
            recService.pauseRecording();
            mRecState2.setValue(RecordState.PAUSE);
        }*/



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
    private String mRecTopic = "";

    public void onSaveClickOld() {
        RecordingService recService = InjectorUtils.provideRecordingService(getApplication());
        Record rec = recService.saveRecording();
        if (rec == null) {
            onStopClick(true);
            return;
        }
        Log.d("Record object",rec.toString());
        //mRecState2.setValue(RecordState.READY);
    }

    private void saveRec(){
        RecordingService recService = InjectorUtils.provideRecordingService(getApplication());
        Record rec = recService.saveRecording();
        if (rec == null) {
            onStopClick(true);
            return;
        }
        rec.setName(mRecName);
        rec.setTopic(mRecTopic);

        RecordRepo.getInstance(getApplication()).addRecord(rec);
        Log.d("RECORD OBJ","name "+rec.getName()+
                ",topic "+rec.getTopic()+
                ",file "+rec.getRecordFile());
    }

    public void onSaveClick(String recName, String recTopic) {
        this.mRecName = recName;
        this.mRecTopic = recTopic;
        saveRec();
    }

    public enum RecStopState{
        STOP_IN_PROGRESS,
        STOP_COMPLETED
    }
}
