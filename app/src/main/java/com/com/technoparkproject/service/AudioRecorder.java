package com.com.technoparkproject.service;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.com.technoparkproject.service.coders.ADTSStream;
import com.com.technoparkproject.service.coders.PacketStream;
import com.com.technoparkproject.service.storage.RecordingProfile;
import com.com.technoparkproject.service.storage.RecordingProfileStorage;
import com.com.technoparkproject.service.tasks.DrainWriterTask;
import com.com.technoparkproject.service.tasks.RecorderTask;
import com.com.technoparkproject.service.tasks.StreamWriterTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioRecorder {
    private static final int QUEUE_CAPACITY = 100;
    private PacketStream<ByteBuffer> mADTSStream;
    private final BlockingDeque<ByteBuffer> mBuffersQ;

    private final AtomicBoolean mIsRecTasksCancel = new AtomicBoolean();
    private ScheduledFuture<?> mTimeFuture;
    private final RecordingProfile mRecProfile;


    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private final ExecutorService mRecordingExecutor;
    private final ExecutorService mWriterExecutor;
    private final ScheduledExecutorService mScheduledExecutor;
    private AudioRecord mAudioRecord;
    private FileOutputStream mRecordBOS;


    private final AtomicInteger mRecRawSize = new AtomicInteger();

    private int mRecordTimeInMills; //internal record time in millis

    public LiveData<RecordState> getRecordState(){
        return mRecordState;
    }

    private final MutableLiveData<RecordState> mRecordState;

    private final MutableLiveData<Integer> mRecTime;

    public LiveData<Integer> getRecTime() {
        return mRecTime;
    }

    private int mBufferSizeInBytes;

    public AudioRecorder(){
        mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        mRecordingExecutor = Executors.newSingleThreadExecutor();
        mWriterExecutor = Executors.newSingleThreadExecutor();

        mBuffersQ = new LinkedBlockingDeque<>(QUEUE_CAPACITY);

        mRecProfile = RecordingProfileStorage.getRecordingProfile(RecordingProfileStorage.AudioQuality.STANDARD);


        mRecTime = new MutableLiveData<>();
        mRecordState = new MutableLiveData<>();

        mRecordingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                configureSelf();
            }
        });
    }

    public RecordingProfile getRecordingProfile(){
        return mRecProfile;
    }

    private static int roundUp(int value, int multiplier) {
        return (value + multiplier - 1) / multiplier * multiplier;
    }

    private void getBufferSize() {
        //minimum buffer size in bytes
        int minBufferSize = AudioRecord.getMinBufferSize(
                mRecProfile.getSamplingRate(),
                mRecProfile.getConfigChannels(),
                mRecProfile.getAudioFormat());
        mBufferSizeInBytes = minBufferSize * 4;
        //round buffer size up to nearest multiple of encoder's max frame length
        mBufferSizeInBytes = roundUp(mBufferSizeInBytes,
                mADTSStream.getMaxFrameLength()* mRecProfile.getFrameSize());
    }

    public void configure(){
        //may use some special client reconfig logic in future here
        if (mRecordState.getValue() != RecordState.STOP)
            throw new IllegalStateException("configure call wrong state");
        mRecordState.setValue(RecordState.INIT);
    }

    private void configureSelf(){
        //mRecordTimeInMills = 0;
        mADTSStream = new ADTSStream(mRecProfile);

        getBufferSize();

        mAudioRecord = new AudioRecord(AUDIO_SOURCE,
                mRecProfile.getSamplingRate(), mRecProfile.getConfigChannels(),
                mRecProfile.getAudioFormat(), mBufferSizeInBytes);

        mRecordState.postValue(RecordState.INIT);
    }


    public void prepare(File outFile){
        if (mRecordState.getValue() != RecordState.INIT
                && mRecordState.getValue() != RecordState.STOP)
            throw new IllegalStateException("prepare call wrong state");
        setOutputFile(outFile);
        reset();
        mRecordState.setValue(RecordState.READY);
    }

    private void reset(){
        //mRecTime.setValue(0);
        mRecRawSize.set(0);
        mRecordTimeInMills = 0;
    }

    private void setOutputFile(File outFile){
        try {
            mRecordBOS = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            Log.e("FileOutputStream", "File not found for recording ", e);
        }
    }

    public void start() {
        resume();
    }


    public void resume(){
        if (mRecordState.getValue()!=RecordState.READY
                && mRecordState.getValue()!=RecordState.PAUSE)
            throw new IllegalStateException("resume call wrong state");

        //todo: move it to end of method???
        mRecordState.setValue(RecordState.RECORDING);

        FileChannel recordFileChannel = mRecordBOS.getChannel();

        mIsRecTasksCancel.set(false);

        RecorderTask recordTask = new RecorderTask(mAudioRecord, mADTSStream,
                mBuffersQ, mBufferSizeInBytes,
                mRecRawSize, mIsRecTasksCancel);

        mRecordingExecutor.execute(recordTask);

        StreamWriterTask writeTask = new StreamWriterTask(recordFileChannel, mBuffersQ, mIsRecTasksCancel);
        mWriterExecutor.execute(writeTask);


        Runnable timer = new Runnable() {
            @Override
            public void run() {
                //send first LiveData with 0 seconds
                if (mRecordTimeInMills == 0){
                    mRecTime.postValue(0);
                }
                mRecordTimeInMills+=100;
                //LiveData updates every second
                if (mRecordTimeInMills % 1000 == 0) {
                    int seconds = mRecordTimeInMills/1000;
                    mRecTime.postValue(seconds);
                }
            }
        };
        mTimeFuture = mScheduledExecutor.scheduleAtFixedRate(timer,0,100, TimeUnit.MILLISECONDS);

    }

    public void pause(){
        if (mRecordState.getValue()!=RecordState.RECORDING)
            throw new IllegalStateException("pause call wrong state");

        //todo: remove???????
        if (mAudioRecord == null) {
            Log.e("pauseRecording", "AudioRecord is null!");
            return;
        }

        mIsRecTasksCancel.set(true);
        mTimeFuture.cancel(false);

        mRecordState.setValue(RecordState.PAUSE);
    }


    private void closeFile(){
        try {
            mRecordBOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecordState.postValue(RecordState.STOP);
    }


    public void stop(){
        if (mRecordState.getValue()!=RecordState.RECORDING
            && mRecordState.getValue()!=RecordState.PAUSE)
            throw new IllegalStateException("stop call wrong state");

        if (mRecordState.getValue() == RecordState.RECORDING)
            pause();

        FileChannel recordFileChannel = mRecordBOS.getChannel();
        DrainWriterTask writeTask = new DrainWriterTask(recordFileChannel, mBuffersQ);

        //using recording executor to make sure
        //recording task is finished and we can start to drain encoder
        mRecordingExecutor.execute(writeTask);
        mRecordingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                closeFile();
            }
        });

    }


    public void release(){
        mBuffersQ.clear();
        mScheduledExecutor.shutdownNow();
        mRecordingExecutor.shutdownNow();
        mWriterExecutor.shutdownNow();

        mAudioRecord.release();
        mAudioRecord = null;
        mADTSStream.release();
        mADTSStream = null;
    }



    public int getDuration(){
        int bytesPerSecond = mRecProfile.getFrameSize()*mRecProfile.getSamplingRate();
        double recSeconds = (double)mRecRawSize.get() / bytesPerSecond;
        int recMillis = (int)(recSeconds*1000);
        return recMillis;
    }


    //DEBUG: measuring call times
    private static long getMillis(long startTime, long endTime){
        return (endTime-startTime)/1000000;
    }
}
