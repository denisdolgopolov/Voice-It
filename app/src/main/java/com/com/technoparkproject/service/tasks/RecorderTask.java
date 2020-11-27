package com.com.technoparkproject.service.tasks;

import android.media.AudioRecord;
import android.util.Log;

import com.com.technoparkproject.service.AudioRecorder;
import com.com.technoparkproject.service.RecordingService;
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

        int samplesCount = 0;
        mADTSStream.configure();

        mADTSStream.start();

        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(mBufferSize).order(ByteOrder.nativeOrder());


        mAudioRecord.startRecording();
        while (!mIsCancelled.get()) {
            long startTime = System.nanoTime();
            int bytesRead = mAudioRecord.read(audioBuffer,
                    mBufferSize);
            long endTime = System.nanoTime();
            Log.d("Timing AudioRecord","reading buffer took "
                    + AudioRecorder.getMillis(startTime,endTime));
            //bytesRead indicates number of bytes OR error status;
            if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION
                    || bytesRead == AudioRecord.ERROR_BAD_VALUE) {
                Log.e("AudioRecord", "Error reading audio data!");
                return;
            }

            Log.d("BytesRead", String.valueOf(bytesRead));

            audioBuffer.limit(bytesRead); //manually set limit as it's not clear if it's set correctly


            startTime = System.nanoTime();
            List<ByteBuffer> packets = mADTSStream.getPackets(audioBuffer);
            endTime = System.nanoTime();
            Log.d("Timing getPackets","encoding took "
                    + AudioRecorder.getMillis(startTime,endTime));
            for (ByteBuffer packet : packets){
                enqueuePacket(packet);
            }
            /*ByteBuffer packet = mADTSStream.getPacket(audioBuffer);

            if (packet.capacity() != 0) {
                enqueuePacket(packet);
            }*/

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
        Log.d("record samplesCount","millis "+mRecRawSize.get());

        mAudioRecord.stop();

        mADTSStream.stop();

        //Log.d("RECORD TASK","END");
    }
}
