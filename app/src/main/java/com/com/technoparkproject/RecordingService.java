package com.com.technoparkproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.com.technoparkproject.view.activities.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RecordingService extends Service {

    private ADTSStream mADTSStream;

    public interface OnRecordTimeListener{
        void OnRecordTick(final long seconds);
    }

    private OnRecordTimeListener mRecordTimeListener;

    public void setOnRecordTimeListener(OnRecordTimeListener onRecordTimeListener){
        mRecordTimeListener = onRecordTimeListener;
    }

    private static final String CHANNEL_AUDIO_APP = "Voice-it channel";
    private static final int FOREGROUND_ID = 1111;
    public static int SAMPLING_RATE = 44100;
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //public static int BYTE_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT); //minimum buffer size in bytes for AudioRecord
    //public static final int SHORT_BUFFER_SIZE = BYTE_BUFFER_SIZE/2; //minimum buffer size in shorts for AudioRecord
    private int mSampleSizeInBytes;
    private int mFrameSizeInBytes;

    ExecutorService mRecordingExecutor;
    ScheduledExecutorService mScheduledExecutor;
    private ScheduledFuture<?> mRecordFuture;
    private Future<?> mRecordResult;
    private AudioRecord mAudioRecord;
    private FileOutputStream mRecordBOS;

    private long mRecordTime; //record time in seconds


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
            // Return this instance of LocalService so clients can call public methods
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


    @Deprecated
    @Override
    public void onCreate() {
        mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        mRecordingExecutor = Executors.newSingleThreadExecutor();
        getBufferSize();
        mRecordStatus = RECORD_STATUS_READY;
    }

    private void getBufferSize() {
        mFrameSizeInBytes = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT); //minimum buffer size in bytes
        int channelsCount = CHANNEL_IN_CONFIG == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        int bitDepthInBytes = AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1;
        mSampleSizeInBytes = channelsCount * bitDepthInBytes;
        mBufferSizeInBytes = mFrameSizeInBytes*5; //for smoother recording


        //TESTTTTTT
        //mBufferSizeInBytes = mFrameSizeInBytes;


        /*int minFrameSize = bufferSizeInBytes / mSampleSizeInBytes;
        int minFramesInSecond = SAMPLING_RATE / minFrameSize;
        int framesInSecond = (minFramesInSecond / 10) * 10; //align min frame size to lower multiple of 10
        mFrameSizeInBytes = SAMPLING_RATE / framesInSecond * mSampleSizeInBytes;*/
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


    public void startRecording() {
        File recordFile = createTempFile();

        //buffer size can store 5x  buffer size
        //for smoother recording than with the minBufferSize value
        //int bufferSizeInBytes = BYTE_BUFFER_SIZE * 4;

        mRecordTime = 0;
        long startTime = System.nanoTime();
        mADTSStream = new ADTSStream(SAMPLING_RATE,128000,CHANNEL_IN_CONFIG, AUDIO_FORMAT);

        long endTime = System.nanoTime();
        Log.d("Timing ADTSStream","construction of stream and encoder took"
                +getMillis(startTime,endTime));

        mAudioRecord = new AudioRecord(AUDIO_SOURCE,
                SAMPLING_RATE, CHANNEL_IN_CONFIG,
                AUDIO_FORMAT, mBufferSizeInBytes);

        //notification period is 1 second
        //so clients may use it as a timer callback
        mAudioRecord.setPositionNotificationPeriod(SAMPLING_RATE);

        mAudioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {
                //not using marker callback for now
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                if (mRecordTimeListener == null){
                    Log.e("OnRecordTimeListener","OnRecordTimeListener is not set on RecordingService");
                    return;
                }
                mRecordTime++;
                updateNotification("Имя записи..."+ DateUtils.formatElapsedTime(mRecordTime));
                mRecordTimeListener.OnRecordTick(mRecordTime);
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
        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(mBufferSizeInBytes).order(ByteOrder.nativeOrder());
        //short[] testBuf = new short[mBufferSizeInBytes];

        mAudioRecord.startRecording();

        mADTSStream.start();

        RecordTask recordTask = new RecordTask(mAudioRecord, recordFileChannel, audioBuffer);

        //int samplesInFrame = mFrameSizeInBytes / mSampleSizeInBytes;
        /*mRecordFuture = mScheduledExecutor.scheduleAtFixedRate(recordTask, 0,
                SAMPLING_RATE/mFrameSizeInBytes
                ,TimeUnit.MILLISECONDS);*/
        mRecordResult = mRecordingExecutor.submit(recordTask);
    }

    public void pauseRecording(){
        mRecordStatus = RECORD_STATUS_PAUSED;
        if (mAudioRecord == null) {
            Log.e("pauseRecording", "AudioRecord is null!");
            return;
        }
        try {
            //wait till record task is finished
            if (mRecordResult.get() == null)
                mAudioRecord.stop();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        mADTSStream.stop();
        mADTSStream.configure();
    }

    public void stopRecording(){
        pauseRecording();
        mRecordStatus = RECORD_STATUS_STOPPED;
        mAudioRecord.release();
        mAudioRecord = null;

        mADTSStream.release();
        try {
            mRecordBOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecordStatus = RECORD_STATUS_READY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScheduledExecutor.shutdown();
        mRecordingExecutor.shutdown();
        if (mRecordTimeListener != null)
            mRecordTimeListener = null;
        /*
        //TODO: Don't know if needed??
        try {
            mScheduledExecutor.awaitTermination(1,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */
    }


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
    }

    private long getMillis(long startTime, long endTime){
        return (endTime-startTime)/1000000;
    }

    public class RecordTask implements Runnable {

        private final ByteBuffer mAudioBuffer;
        private final AudioRecord mAudioRecord;
        private final FileChannel mRecordFileChannel;

        public RecordTask(final AudioRecord audioRecord, final FileChannel recBOS, final ByteBuffer audioBuffer) {
            mAudioRecord = audioRecord;
            mRecordFileChannel = recBOS;
            mAudioBuffer = audioBuffer;
        }



        @Override
        public void run() {
           while (isRecording()) {
                Log.d("Task", "new call");
                long startTime = System.nanoTime();
                int bytesRead = mAudioRecord.read(mAudioBuffer, mBufferSizeInBytes);
                long endTime = System.nanoTime();
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

                try {
                    mAudioBuffer.limit(bytesRead); //manually set limit as it's not clear if it's set correctly

                    startTime = System.nanoTime();
                    List<ByteBuffer> packets = mADTSStream.getADTSPackets(mAudioBuffer);
                    endTime = System.nanoTime();
                    Log.d("Timing Encoder", "encoding buffer took "
                            + getMillis(startTime, endTime));
                    for (ByteBuffer packet : packets) {
                        //write in loop in case not full buffer is written immediately
                        startTime = System.nanoTime();
                        while (packet.hasRemaining())
                            mRecordFileChannel.write(packet);
                        endTime = System.nanoTime();
                        Log.d("Timing FileChannel", "writing buffer took "
                                + getMillis(startTime, endTime));
                        //mRecordBOS.write(shortArrToByteArr(mAudioBuffer));
                    }
                }
                catch (IOException e) {
                    Log.e("RecordFileChannel", "Error writing to the channel ", e);
                    return;
                }
                Log.d("Task", "new finish");
                mAudioBuffer.clear();
           }
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
