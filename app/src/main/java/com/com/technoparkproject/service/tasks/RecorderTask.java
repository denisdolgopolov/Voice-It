package com.com.technoparkproject.service.tasks;

import android.media.AudioRecord;
import android.util.Log;

import com.com.technoparkproject.service.coders.PacketStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RecorderTask implements Runnable {

    private final AudioRecord mAudioRecord;
    private final BlockingDeque<ByteBuffer> mPacketsQ;
    private final PacketStream<ByteBuffer> mADTSStream;
    private final int mBufferSize;
    private final AtomicBoolean mIsCancelled;
    private final AtomicInteger mRecRawSize;

    public RecorderTask(final AudioRecord audioRecord, final PacketStream<ByteBuffer> adtsStream,
                      final BlockingDeque<ByteBuffer> packetsQ, final int bufferSize,
                      AtomicInteger recRawSize,
                      AtomicBoolean isCancelled) {
        mAudioRecord = audioRecord;
        mADTSStream = adtsStream;
        mPacketsQ = packetsQ;
        mBufferSize = bufferSize;
        mRecRawSize = recRawSize;
        mIsCancelled = isCancelled;
    }


    private void enqueuePacket(ByteBuffer packet){
        try {
            //Log.d("QUEUE","size of q while recording = "
              //      +mPacketsQ.size());
            mPacketsQ.add(packet);
        }
        catch (IllegalStateException e){
            Log.e(this.getClass().getSimpleName(),
                    "can't enqueue buffer,no space in queue", e);

        }
    }


    @Override
    public void run() {
        //Log.d("RECORD TASK","START");

        int samplesCount = 0;
        //long startTime = System.nanoTime();
        mADTSStream.configure();
        /*long endTime = System.nanoTime();
        Log.d("Timing ADTSStream", "configure took"
                + getMillis(startTime, endTime));*/

        mADTSStream.start();

        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(mBufferSize).order(ByteOrder.nativeOrder());


        mAudioRecord.startRecording();
        while (!mIsCancelled.get()) {
            //Log.d("Task", "new call");
            //startTime = System.nanoTime();
            int bytesRead = mAudioRecord.read(audioBuffer,
                    mBufferSize);
           // endTime = System.nanoTime();
            //Log.d("Timing AudioRecord","reading buffer took "
            //        +getMillis(startTime,endTime));
            //int bytesRead = mAudioRecord.read(test,0,test.length );
            //Log.d("BytesRead", String.valueOf(bytesRead));

            //bytesRead indicates number of bytes OR error status;
            if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION ||
                    bytesRead == AudioRecord.ERROR_BAD_VALUE) {
                Log.e("AudioRecord", "Error reading audio data!");
                return;
            }
            audioBuffer.limit(bytesRead); //manually set limit as it's not clear if it's set correctly

            ByteBuffer packet = mADTSStream.getPacket(audioBuffer);

            if (packet != null) {
                enqueuePacket(packet);
            }

            samplesCount += bytesRead;
            //Log.d("Task", "new finish");
            audioBuffer.clear();
        }
        //Log.d("RECORD","INTERRUPTION");
        //get any cached packets and put the to file
        List<ByteBuffer> flushedPackets = mADTSStream.flushPackets();
        for (ByteBuffer packet : flushedPackets){
            enqueuePacket(packet);
        }

        mRecRawSize.getAndAdd(samplesCount);

        mAudioRecord.stop();

        //startTime = System.nanoTime();
        mADTSStream.stop();
        /*endTime = System.nanoTime();
        Log.d("Timing ADTSStream","stop took"
                +getMillis(startTime,endTime));

        Log.d("RECORD TASK","END");*/
    }
}
