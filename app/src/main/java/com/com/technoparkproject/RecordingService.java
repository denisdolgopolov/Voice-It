package com.com.technoparkproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.com.technoparkproject.view.activities.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RecordingService extends Service {

    private static final int QUEUE_CAPACITY = 20;
    private ADTSStream mADTSStream;
    private BlockingDeque<ByteBuffer> mBuffersQ;
    private Future<?> mWriteFuture;

    private final AtomicBoolean mIsTaskCancel = new AtomicBoolean();

    public interface RecordCallbacks{
        void OnRecordTick(final long seconds);
        void OnConfigure();
        void OnRecordStop();
    }

    private RecordCallbacks mRecordCallbacks;

    public void setRecordCallbacks(RecordCallbacks recordCallbacks){
        mRecordCallbacks = recordCallbacks;
    }

    private static final String CHANNEL_AUDIO_APP = "Voice-it channel";
    private static final int FOREGROUND_ID = 1111;
    public static int SAMPLING_RATE = 44100;
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //public static int BYTE_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT); //minimum buffer size in bytes for AudioRecord
    //public static final int SHORT_BUFFER_SIZE = BYTE_BUFFER_SIZE/2; //minimum buffer size in shorts for AudioRecord
    private int mFrameSize;
    private int mMinBufferSize;

    private ExecutorService mRecordingExecutor;
    private ExecutorService mWriterExecutor;
    //private ScheduledExecutorService mScheduledExecutor;
    private ScheduledFuture<?> mRecordFuture;
    private Future<?> mRecordResult;
    private AudioRecord mAudioRecord;
    private FileOutputStream mRecordBOS;

    private long mRecordTimeInMills; //record time in seconds


    public static String START_FOREGROUND = "START_FOREGROUND";
    public static String STOP_FOREGROUND = "STOP_FOREGROUND";



    private volatile int mRecordStatus; //current status of the service, one of the constants

    public static final int RECORD_STATUS_READY = 100;
    public static final int RECORD_STATUS_RECORDING = 200;
    public static final int RECORD_STATUS_PAUSED = 300;
    public static final int RECORD_STATUS_STOPPED = 400;


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


    /* // remove this method and move it to onCreateCommand
    @Deprecated*/
    @Override
    public void onCreate() {
        //mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        mRecordingExecutor = Executors.newSingleThreadExecutor();
        mWriterExecutor = Executors.newSingleThreadExecutor();
        getBufferSize();
        mRecordStatus = RECORD_STATUS_READY;
        //TODO test max queue capacity
        mBuffersQ = new LinkedBlockingDeque<>(QUEUE_CAPACITY);
    }

    private static int roundUp(int value, int multiplier) {
        return (value + multiplier - 1) / multiplier * multiplier;
    }

    private void getBufferSize() {
        mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT); //minimum buffer size in bytes
        //int channelsCount = CHANNEL_IN_CONFIG == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        //int bitDepthInBytes = AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1;
        int channelsCount = AudioFormatUtils.getChannelCount(CHANNEL_IN_CONFIG);
        int bitDepthInBytes = AudioFormatUtils.getBytesPerSample(AUDIO_FORMAT);
        mFrameSize = channelsCount * bitDepthInBytes;
        //
        mBufferSizeInBytes = mMinBufferSize * 2;

        //round buffer size up to nearest multiple of encoder's max frame length
        mBufferSizeInBytes = roundUp(mBufferSizeInBytes,AACEncoder.MAX_AAC_FRAME_LENGTH*mFrameSize);


        //TESTTTTTT
        //mBufferSizeInBytes = mFrameSizeInBytes;

    }


    private void createNotificationChannel() {
        //notification channel is needed for API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager.getNotificationChannel(CHANNEL_AUDIO_APP)!=null)
                return;
            CharSequence name = getString(R.string.channel_audio_app);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_AUDIO_APP, name, importance);
            channel.setDescription(description);
            channel.setSound(null,null);//disable sounds for this channel
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateNotification(String text){
        Notification recordNotification = buildForegroundNotification(text);
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(FOREGROUND_ID,recordNotification);

    }

    private Notification buildForegroundNotification(String text) {
        NotificationCompat.Builder builder=
                new NotificationCompat.Builder(this, CHANNEL_AUDIO_APP);

        //disable sound using builder if API < 26
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            builder.setSound(null);
        }

        builder.setOngoing(true)
                .setContentTitle(getString(R.string.audio_record))
                .setContentText(text)
                .setSmallIcon(android.R.drawable.presence_audio_busy);

        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("showFragment", "RecordFragment");
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return(builder.build());
    }


    //start/stops the service in foreground mode
    public void runForeground(boolean isForeground){
        if (isForeground){
            createNotificationChannel();
            startForeground(FOREGROUND_ID,
                    buildForegroundNotification("Имя записи..."));
        }
        else{
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_STICKY;



        return START_STICKY;
    }


    private void configureAsync(){
        Runnable configureRunner = new Runnable() {
            @Override
            public void run() {
                configure();
            }
        };
        Runnable configureCallback = new Runnable() {
            @Override
            public void run() {
                resumeRecording();
                if (mRecordCallbacks!=null)
                    mRecordCallbacks.OnConfigure();
            }
        };
        CompletionRunner completionRunner = new CompletionRunner(configureRunner,
                configureCallback,null);
        mRecordingExecutor.execute(completionRunner);
    }


    private void configure(){
        File recordFile = createTempFile();

        mRecordTimeInMills = 0;
        long startTime = System.nanoTime();
        mADTSStream = new ADTSStream(SAMPLING_RATE,128000,CHANNEL_IN_CONFIG, AUDIO_FORMAT);

        long endTime = System.nanoTime();
        Log.d("Timing ADTSStream","construction of stream and encoder took"
                +getMillis(startTime,endTime));

        mAudioRecord = new AudioRecord(AUDIO_SOURCE,
                SAMPLING_RATE, CHANNEL_IN_CONFIG,
                AUDIO_FORMAT, mBufferSizeInBytes);

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
                    updateNotification("Имя записи..." + DateUtils.formatElapsedTime(mRecordTimeInMills/1000));
                    mRecordCallbacks.OnRecordTick(mRecordTimeInMills/1000);
                }
            }
        });

        if (mAudioRecord.getState() ==
                AudioRecord.STATE_UNINITIALIZED)
            Log.e("AudioRecord init error", "AudioRecord state is uninitialized after constructor called");

        mRecordBOS = null;
        try {
            mRecordBOS = new FileOutputStream(recordFile);
            //mRecordBOS = new BufferedOutputStream(recordFOS);
        } catch (FileNotFoundException e) {
            Log.e("FileOutputStream", "File not found for recording ", e);
        }
    }

    public void startRecording() {
        configureAsync();
        //resumeRecording();
    }


    public void startRecordingOld() {
        File recordFile = createTempFile();

        //buffer size can store 5x  buffer size
        //for smoother recording than with the minBufferSize value
        //int bufferSizeInBytes = BYTE_BUFFER_SIZE * 4;

        mRecordTimeInMills = 0;
        long startTime = System.nanoTime();
        mADTSStream = new ADTSStream(SAMPLING_RATE,128000,CHANNEL_IN_CONFIG, AUDIO_FORMAT);

        long endTime = System.nanoTime();
        Log.d("Timing ADTSStream","construction of stream and encoder took"
                +getMillis(startTime,endTime));

        mAudioRecord = new AudioRecord(AUDIO_SOURCE,
                SAMPLING_RATE, CHANNEL_IN_CONFIG,
                AUDIO_FORMAT, mBufferSizeInBytes);

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
                    updateNotification("Имя записи..." + DateUtils.formatElapsedTime(mRecordTimeInMills/1000));
                    mRecordCallbacks.OnRecordTick(mRecordTimeInMills/1000);
                }
            }
        });

        if (mAudioRecord.getState() ==
                AudioRecord.STATE_UNINITIALIZED)
            Log.e("AudioRecord init error", "AudioRecord state is uninitialized after constructor called");

        mRecordBOS = null;
        try {
            mRecordBOS = new FileOutputStream(recordFile);
            //mRecordBOS = new BufferedOutputStream(recordFOS);
        } catch (FileNotFoundException e) {
            Log.e("FileOutputStream", "File not found for recording ", e);
        }
        resumeRecording();
    }


    public void resumeRecording(){
        mRecordStatus = RECORD_STATUS_RECORDING;

        FileChannel recordFileChannel = mRecordBOS.getChannel();


        //Create Direct ByteBuffer with native endianness that should be Little Endian for most devices
        //ByteBuffer audioBuffer = ByteBuffer.allocateDirect(mBufferSizeInBytes).order(ByteOrder.nativeOrder());
        //short[] testBuf = new short[mBufferSizeInBytes];

        /*
        mAudioRecord.startRecording();

        long startTime = System.nanoTime();
        mADTSStream.configure();
        long endTime = System.nanoTime();
        Log.d("Timing ADTSStream","configure took"
                +getMillis(startTime,endTime));

        mADTSStream.start();
         */

        mIsTaskCancel.set(false);

        RecordTask recordTask = new RecordTask(mAudioRecord, mADTSStream,
                mBuffersQ, mFrameSize*AACEncoder.MAX_AAC_FRAME_LENGTH,
                mIsTaskCancel);

        //int samplesInFrame = mFrameSizeInBytes / mSampleSizeInBytes;
        /*mRecordFuture = mScheduledExecutor.scheduleAtFixedRate(recordTask, 0,
                SAMPLING_RATE/mFrameSizeInBytes
                ,TimeUnit.MILLISECONDS);*/
        mRecordResult = mRecordingExecutor.submit(recordTask);

        /*
        //stop writing with interruption if is still running
        if (mWriteFuture != null && !mWriteFuture.isDone()) {
            mWriteFuture.cancel(true);
        }*/
        WriteTask writeTask = new WriteTask(recordFileChannel, mBuffersQ,false,
                mIsTaskCancel);
        mWriteFuture = mWriterExecutor.submit(writeTask);

    }

    public void pauseRecording(){
        mRecordStatus = RECORD_STATUS_PAUSED;
        if (mAudioRecord == null) {
            Log.e("pauseRecording", "AudioRecord is null!");
            return;
        }

        mIsTaskCancel.set(true);
        //stop record task
        //mRecordResult.cancel(true);
        //stop write task
        //mWriteFuture.cancel(true);
        /*try {
            //stop record task and wait till it's finished
            mRecordResult.cancel(true);
            if (mRecordResult.get() == null)
                mAudioRecord.stop();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        long startTime = System.nanoTime();
        mADTSStream.stop();
        long endTime = System.nanoTime();
        Log.d("Timing ADTSStream","stop took"
                +getMillis(startTime,endTime));*/

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
        releaseRecording();
    }

    //clean and close resources associated with recording
    private void releaseRecording(){
        try {
            mRecordBOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioRecord.release();
        mAudioRecord = null;
        mADTSStream.release();
        mBuffersQ.clear();
        Log.e("RECORD RELEASE", "release called");
        //notify fragment about recording stop
        if (mRecordCallbacks !=null)
            mRecordCallbacks.OnRecordStop();

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
        WriteTask writeTask = new WriteTask(recordFileChannel, mBuffersQ,
                                        true,mIsTaskCancel);

        CompletionRunner completionRunner = new CompletionRunner(writeTask, writeComplete,null);
        mWriteFuture = mRecordingExecutor.submit(completionRunner);
        mRecordStatus = RECORD_STATUS_STOPPED;
        //wait till all data is written to file
        // todo waiting takes a lot of time should make it async somehow

        /*try {
            if (mWriteFuture.get() == null) {
                //all recording and writing is completed to this point
                try {
                    mRecordBOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAudioRecord.release();
                mAudioRecord = null;
                mADTSStream.release();
                mBuffersQ.clear();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }*/

       // mRecordStatus = RECORD_STATUS_READY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mScheduledExecutor.shutdown();
        mRecordingExecutor.shutdown();
        if (mRecordCallbacks != null)
            mRecordCallbacks = null;
        /*
        //TODO: Don't know if needed?? maybe shutdown now only
        try {
            mScheduledExecutor.awaitTermination(1,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */
    }

    /*
    private static byte[] shortToByteLE(short x){
        byte[] ret = new byte[2];
        ret[0] = (byte)(x & 0xff);
        ret[1] = (byte)((x >> 8) & 0xff);
        return ret;
    }

    private static byte[] shortArrToByteArr(short[] shorts){
        byte[] bytes = new byte[shorts.length*2];
        for (int i = 0; i< shorts.length ; i++){
            bytes[2*i] = (byte)(shorts[i] & 0xff);
            bytes[2*i+1] = (byte)((shorts[i] >> 8) & 0xff);
        }
        return bytes;
    } */

    private static long getMillis(long startTime, long endTime){
        return (endTime-startTime)/1000000;
    }

    public static class RecordTask implements Runnable {

        private final AudioRecord mAudioRecord;
        private final BlockingDeque<ByteBuffer> mPacketsQ;
        private final ADTSStream mADTSStream;
        private final int mBufferSize;
        private final AtomicBoolean mIsCancelled;

        public RecordTask(final AudioRecord audioRecord, final ADTSStream adtsStream,
                          final BlockingDeque<ByteBuffer> packetsQ, final int bufferSize,
                          AtomicBoolean isCancelled) {
            mAudioRecord = audioRecord;
            mADTSStream = adtsStream;
            mPacketsQ = packetsQ;
            mBufferSize = bufferSize;
            mIsCancelled = isCancelled;
        }


        private void enqueuePacket(ByteBuffer packet){
            try {
                Log.d("QUEUE","size of q while recording = "
                        +mPacketsQ.size());
                mPacketsQ.add(packet);
            }
            catch (IllegalStateException e){
                Log.e(this.getClass().getSimpleName(),
                        "can't enqueue buffer,no space in queue", e);

            }
        }


        @Override
        public void run() {
            Log.d("RECORD TASK","START");

            long startTime = System.nanoTime();
            mADTSStream.configure();
            long endTime = System.nanoTime();
            Log.d("Timing ADTSStream", "configure took"
                    + getMillis(startTime, endTime));

            mADTSStream.start();

            ByteBuffer audioBuffer = ByteBuffer.allocateDirect(mBufferSize).order(ByteOrder.nativeOrder());


            mAudioRecord.startRecording();
           while (!mIsCancelled.get()) {
                Log.d("Task", "new call");
                startTime = System.nanoTime();
                int bytesRead = mAudioRecord.read(audioBuffer,
                        mBufferSize);
                endTime = System.nanoTime();
                Log.d("Timing AudioRecord","reading buffer took "
                        +getMillis(startTime,endTime));
                //int bytesRead = mAudioRecord.read(test,0,test.length );
                Log.d("BytesRead", String.valueOf(bytesRead));
                //bytesRead indicates number of bytes OR error status;
                if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION ||
                        bytesRead == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e("AudioRecord", "Error reading audio data!");
                    return;
                }
                    audioBuffer.limit(bytesRead); //manually set limit as it's not clear if it's set correctly

                    /*startTime = System.nanoTime();
                    List<ByteBuffer> packets = mADTSStream.getADTSPackets(mAudioBuffer);
                    endTime = System.nanoTime();
                    Log.d("Timing Encoder", "encoding buffer took "
                            + getMillis(startTime, endTime));
                    for (ByteBuffer packet : packets) {
                        //write in loop in case not full buffer is written immediately
                        startTime = System.nanoTime();
                        writeBuffer(packet);
                        endTime = System.nanoTime();
                        Log.d("Timing FileChannel", "writing buffer took "
                                + getMillis(startTime, endTime));
                    }*/
                    ByteBuffer packet = mADTSStream.getADTSPacket(audioBuffer);

                    if (packet != null) {
                        enqueuePacket(packet);
                    }

                Log.d("Task", "new finish");
                audioBuffer.clear();
           }
           Log.d("RECORD","INTERRUPTION");
           //get any cached packets and put the to file
           List<ByteBuffer> flushedPackets = mADTSStream.flushPackets();
           for (ByteBuffer packet : flushedPackets){
               enqueuePacket(packet);
           }
           mAudioRecord.stop();

           startTime = System.nanoTime();
           mADTSStream.stop();
           endTime = System.nanoTime();
           Log.d("Timing ADTSStream","stop took"
                    +getMillis(startTime,endTime));

           Log.d("RECORD TASK","END");
        }
    }



    public static class WriteTask implements Runnable {

        private final FileChannel mRecordFileChannel;
        private final BlockingDeque<ByteBuffer> mPacketsQ;
        private static final long POLL_TIMEOUT = 500;
        private final AtomicBoolean mIsCancelled;
        private final boolean mIsDrain;

        public WriteTask(final FileChannel recChannel, BlockingDeque<ByteBuffer> packetsQ,
                         boolean isDrain, AtomicBoolean isCancelled) {
            mRecordFileChannel = recChannel;
            mPacketsQ = packetsQ;
            mIsDrain = isDrain;
            mIsCancelled = isCancelled;
        }

        private void writeBuffer(ByteBuffer buffer){
            try{
                long startTime = System.nanoTime();
                while (buffer.hasRemaining())
                    mRecordFileChannel.write(buffer);
                long endTime = System.nanoTime();
                Log.d("Timing FileChannel", "writing buffer took "
                        + getMillis(startTime, endTime));
            }
            catch (ClosedByInterruptException e){
                Thread.currentThread().interrupt(); //restore interrupt status
                //it is not possible to write element
                //channel is closed by interruption
                //=> return buffer back to head of deque
                mPacketsQ.addFirst(buffer);
                Log.d(this.getClass().getSimpleName(),
                        "File Channel is closed due to thread interruption");
            }
            catch (IOException e) {
                Log.e("RecordFileChannel", "Error writing to the channel ", e);
            }

        }

        @Override
        public void run() {
            Log.d("WRITE TASK","START");
            if (!mIsDrain) {
                try {
                    while (!mIsCancelled.get()) {
                        ByteBuffer packet = mPacketsQ.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
                        //finish task if there are no available packets after timeout
                        if (packet == null) {
                            Log.e("WRITE TASK", "NO DATA");
                            break;
                        }
                        writeBuffer(packet);
                    }
                    Log.d("WRITE END", "sizeof q = " + mPacketsQ.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); //restore interrupt status
                    Log.d(this.getClass().getSimpleName(),
                            "Thread was interrupted " +
                                    "while polling the queue or writing to file"
                                    + "or due to outer interruption", e);
                }
            }
            else{
                List<ByteBuffer> packets = new ArrayList<>();
                mPacketsQ.drainTo(packets);
                for (ByteBuffer packet : packets){
                    writeBuffer(packet);
                }
            }
            Log.d("WRITE TASK","END");
        }
    }


    File createTempFile() {
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
    }
}
