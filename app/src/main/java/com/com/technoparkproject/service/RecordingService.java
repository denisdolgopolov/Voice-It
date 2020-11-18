package com.com.technoparkproject.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.com.technoparkproject.repository.Record;
import com.com.technoparkproject.repository.RecordRepo;
import com.com.technoparkproject.service.storage.RecordingProfile;
import com.com.technoparkproject.service.storage.RecordingProfileStorage;
import com.com.technoparkproject.service.coders.ADTSStream;
import com.com.technoparkproject.service.coders.PacketStream;
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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.com.technoparkproject.service.storage.RecordingProfileStorage.AudioQuality;

public class RecordingService extends Service {

    private static final int QUEUE_CAPACITY = 20;
    private PacketStream<ByteBuffer> mADTSStream;
    private BlockingDeque<ByteBuffer> mBuffersQ;
    private Future<?> mWriteFuture;

    private final AtomicBoolean mIsTaskCancel = new AtomicBoolean();
    private ScheduledFuture<?> mTimeFuture;
    private RecordingProfile mRecProfile;
    private File mRecordFile;


    private static final String CHANNEL_AUDIO_APP = "Voice-it channel";
    private static final int FOREGROUND_ID = 1111;
    //public static int SAMPLING_RATE = 44100;
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private ExecutorService mRecordingExecutor;
    private ExecutorService mWriterExecutor;
    private ScheduledExecutorService mScheduledExecutor;
    //private ScheduledFuture<?> mRecordFuture;
    private Future<?> mRecordResult;
    private AudioRecord mAudioRecord;
    private FileOutputStream mRecordBOS;

    private final AtomicInteger mRecRawSize = new AtomicInteger();

    private int mRecordTimeInMills; //record time in seconds


    private volatile int mRecordStatus; //current status of the service, one of the constants

    public static final int RECORD_STATUS_READY = 100;
    public static final int RECORD_STATUS_RECORDING = 200;
    public static final int RECORD_STATUS_PAUSED = 300;
    public static final int RECORD_STATUS_STOPPED = 400;

    public enum RecordState{
        READY,
        RECORDING,
        PAUSE,
        STOP
    }

    public MutableLiveData<RecordState> getRecordState(){
        return mRecordState;
    }

    private MutableLiveData<RecordState> mRecordState;


    private final IBinder mBinder = new RecordBinder();
    private int mBufferSizeInBytes;

    public int getStatus() {
        return mRecordStatus;
    }



    public class RecordBinder extends Binder {
        public RecordingService getService() {
            return RecordingService.this;
        }
    }

    public boolean isReady(){
        return mRecordStatus == RECORD_STATUS_READY;
    }

    public boolean isRecording(){
        return mRecordStatus == RECORD_STATUS_RECORDING;
    }

    public boolean isPaused(){
        return mRecordStatus == RECORD_STATUS_PAUSED;
    }
    public boolean isStopped(){
        return mRecordStatus == RECORD_STATUS_STOPPED;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    @Override
    public void onCreate() {
        mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        mRecordingExecutor = Executors.newSingleThreadExecutor();
        mWriterExecutor = Executors.newSingleThreadExecutor();
        //getBufferSize();
        mRecordStatus = RECORD_STATUS_READY;
        //TODO test max queue capacity
        mBuffersQ = new LinkedBlockingDeque<>(QUEUE_CAPACITY);

        mRecProfile = RecordingProfileStorage.getRecordingProfile(AudioQuality.STANDARD);


        mRecTime = new MutableLiveData<>(0);
        mRecordState = new MutableLiveData<>();

        configureAsync();
    }

    private static int roundUp(int value, int multiplier) {
        return (value + multiplier - 1) / multiplier * multiplier;
    }

    private void getBufferSize() {
        int minBufferSize = AudioRecord.getMinBufferSize(
                mRecProfile.getSamplingRate(),
                mRecProfile.getConfigChannels(),
                mRecProfile.getAudioFormat()); //minimum buffer size in bytes
        mBufferSizeInBytes = minBufferSize * 2;
        //round buffer size up to nearest multiple of encoder's max frame length
        mBufferSizeInBytes = roundUp(mBufferSizeInBytes,
                mADTSStream.getMaxFrameLength()* mRecProfile.getFrameSize());
    }




    //start/stops the service in foreground mode
    public void runForeground(boolean isForeground){
        if (isForeground){
            RecorderNotification.createNotificationChannel(this);
            startForeground(FOREGROUND_ID,
                    RecorderNotification.buildForegroundNotification("Имя записи...",this));
        }
        else{
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;


        return START_NOT_STICKY;
    }


    private void configureAsync(){
        Runnable configureRunner = new Runnable() {
            @Override
            public void run() {
                configure();
            }
        };
        /*Runnable configureCallback = new Runnable() {
            @Override
            public void run() {
                //resumeRecording();
                if (mRecordCallbacks!=null)
                    mRecordCallbacks.OnConfigure();
            }
        };*/
        /*CompletionRunner completionRunner = new CompletionRunner(configureRunner,
                configureCallback,null);
        mRecordingExecutor.execute(completionRunner);*/
        mRecordingExecutor.execute(configureRunner);
    }


    private void configure(){

        mRecordTimeInMills = 0;
        long startTime = System.nanoTime();
        mADTSStream = new ADTSStream(mRecProfile);

        long endTime = System.nanoTime();
        Log.d("Timing ADTSStream","construction of stream and encoder took"
                +getMillis(startTime,endTime));

        getBufferSize();

        mAudioRecord = new AudioRecord(AUDIO_SOURCE,
                mRecProfile.getSamplingRate(), mRecProfile.getConfigChannels(),
                mRecProfile.getAudioFormat(), mBufferSizeInBytes);

        /*
        //notification period is 100 msec
        //so clients may use it as a timer callback
        mAudioRecord.setPositionNotificationPeriod(SAMPLING_RATE/10);

        mAudioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {
                //not using marker callback for now
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                if (mRecordCallbacks == null){
                    Log.e("RecordCallbacks","RecordCallbacks is not set on RecordingService");
                    return;
                }
                mRecordTimeInMills+=100; //callback is called 10 times a second
                if (mRecordTimeInMills % 1000 == 0) {
                    String notifyText = "Имя записи..." + DateUtils.formatElapsedTime(mRecordTimeInMills/1000);
                    RecorderNotification.updateNotification(notifyText,RecordingService.this,FOREGROUND_ID);
                    mRecordCallbacks.OnRecordTick(mRecordTimeInMills/1000);
                }
            }
        });*/

        /*if (mAudioRecord.getState() ==
                AudioRecord.STATE_UNINITIALIZED)
            Log.e("AudioRecord init error", "AudioRecord state is uninitialized after constructor called");


        File recordFile = RecordRepo.getInstance(this)
                .createTempFile(mRecProfile.getFileFormat(),this);

        mRecordBOS = null;
        try {
            mRecordBOS = new FileOutputStream(recordFile);
            //mRecordBOS = new BufferedOutputStream(recordFOS);
        } catch (FileNotFoundException e) {
            Log.e("FileOutputStream", "File not found for recording ", e);
        }*/

        mRecordState.postValue(RecordState.READY);
    }

    public void startRecording() {

        mRecordFile = RecordRepo.getInstance(this)
                .createTempFile(mRecProfile.getFileFormat(),this);

        mRecordBOS = null;
        try {
            mRecordBOS = new FileOutputStream(mRecordFile);
            //mRecordBOS = new BufferedOutputStream(recordFOS);
        } catch (FileNotFoundException e) {
            Log.e("FileOutputStream", "File not found for recording ", e);
        }

        mRecRawSize.set(0);
        mRecordTimeInMills = 0;

        resumeRecording();
    }


    private MutableLiveData<Integer> mRecTime;

    public MutableLiveData<Integer> getRecTime() {
        return mRecTime;
    }

    public void resumeRecording(){
        final Intent serviceIntent = new Intent(getApplicationContext(), RecordingService.class);
        startService(serviceIntent);
        mRecordStatus = RECORD_STATUS_RECORDING;
        mRecordState.setValue(RecordState.RECORDING);
        runForeground(true);

        FileChannel recordFileChannel = mRecordBOS.getChannel();


        mIsTaskCancel.set(false);

        RecorderTask recordTask = new RecorderTask(mAudioRecord, mADTSStream,
                mBuffersQ, mRecProfile.getFrameSize()*mADTSStream.getMaxFrameLength(),
                mRecRawSize,mIsTaskCancel);

        mRecordResult = mRecordingExecutor.submit(recordTask);

        StreamWriterTask writeTask = new StreamWriterTask(recordFileChannel, mBuffersQ,mIsTaskCancel);
        mWriteFuture = mWriterExecutor.submit(writeTask);

        /*Runnable timeRunner = new Runnable() {
            @Override
            public void run() {
                mRecordTimeInMills+=100; //callback is called 10 times a second
            }
        };
        Runnable timeCallback = new Runnable() {
            @Override
            public void run() {
                if (mRecordTimeInMills % 1000 == 0) {
                    String notifyText = "Имя записи..." + DateUtils.formatElapsedTime(mRecordTimeInMills/1000);
                    RecorderNotification.updateNotification(notifyText,RecordingService.this,FOREGROUND_ID);
                    //mRecordCallbacks.OnRecordTick(mRecordTimeInMills/1000);
                }
            }
        };*/
        //CompletionRunner completionRunner = new CompletionRunner(timeRunner,timeCallback,null);
        Runnable timer = new Runnable() {
            @Override
            public void run() {
                mRecordTimeInMills+=100; //internal time in millis
                //callback to ui elements every second
                if (mRecordTimeInMills % 1000 == 0) {
                    int seconds = mRecordTimeInMills/1000;
                    String notifyText = "Имя записи..." + DateUtils.formatElapsedTime(seconds);
                    RecorderNotification.updateNotification(notifyText,RecordingService.this,FOREGROUND_ID);
                    mRecTime.postValue(seconds);
                }
            }
        };
        mTimeFuture = mScheduledExecutor.scheduleAtFixedRate(timer,0,100, TimeUnit.MILLISECONDS);

    }

    public void pauseRecording(){
        mRecordStatus = RECORD_STATUS_PAUSED;

        runForeground(false);
        if (mAudioRecord == null) {
            Log.e("pauseRecording", "AudioRecord is null!");
            return;
        }

        mIsTaskCancel.set(true);
        mTimeFuture.cancel(false);


        mRecordState.setValue(RecordState.PAUSE);
    }


    //class that runs specified runnable
    //and post callback runnable to a thread associated with handler
    //if handler is null => run on UI thread
    public static class CompletionRunner implements Runnable {


        private final Runnable mRunner;
        private final Runnable mCallback;
        private final Handler mHandler;

        public CompletionRunner(Runnable runner, Runnable callback, Handler callbackHandler){
            mRunner = runner;
            mCallback = callback;
            if (callbackHandler == null)
                mHandler = new Handler(Looper.getMainLooper());
            else
                mHandler = callbackHandler;
        }

        @Override
        public void run() {
            mRunner.run();
            mHandler.post(mCallback);
        }
    }


    private void onWriteComplete(){
        Log.d("ONWRITECOMPLETE","called");
        try {
            mRecordBOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecordState.setValue(RecordState.STOP);
        stopSelf();
    }

    //clean and close resources associated with recording
    private void releaseRecording(){
        mAudioRecord.release();
        mAudioRecord = null;
        mADTSStream.release();
        mBuffersQ.clear();
        Log.e("RECORD RELEASE", "release called");
        //notify fragment about recording stop
        //if (mRecordCallbacks !=null)
        //    mRecordCallbacks.OnRecordStop();

        mRecordStatus = RECORD_STATUS_READY;
    }

    public void stopRecording(){
        pauseRecording();

        Runnable writeComplete = new Runnable() {
            @Override
            public void run() {
                onWriteComplete();
            }
        };

        FileChannel recordFileChannel = mRecordBOS.getChannel();
        DrainWriterTask writeTask = new DrainWriterTask(recordFileChannel, mBuffersQ);

        CompletionRunner completionRunner = new CompletionRunner(writeTask, writeComplete,null);
        mWriteFuture = mRecordingExecutor.submit(completionRunner);

        mRecordStatus = RECORD_STATUS_STOPPED;

    }



    private int getCurrentRecDuration(){
        int bytesPerSecond = mRecProfile.getFrameSize()*mRecProfile.getSamplingRate();
        double recSeconds = (double)mRecRawSize.get() / bytesPerSecond;
        int recMillis = (int)(recSeconds*1000);
        return recMillis;
    }

    //returns null if recording isn't finished yet
    public Record saveRecording(){
        if (mRecordState.getValue() != RecordState.STOP){
            return null;
        }
        Record rec = new Record();
        rec.setRecordFile(mRecordFile);
        int recDuration = getCurrentRecDuration();
        rec.setDuration(recDuration);
        mRecTime.setValue(0);
        mRecordState.setValue(RecordState.READY);
        return rec;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRecording();
        mScheduledExecutor.shutdownNow();
        mRecordingExecutor.shutdownNow();
        mWriterExecutor.shutdownNow();
    }


    private static long getMillis(long startTime, long endTime){
        return (endTime-startTime)/1000000;
    }



    /*File createTempFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        String fileName = sdf.format(new Date());
        try {
            File tempFileDir = this.getCacheDir();
            File tempRecordFile = File.createTempFile(fileName, ".aac", tempFileDir);
            return tempRecordFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/
}
