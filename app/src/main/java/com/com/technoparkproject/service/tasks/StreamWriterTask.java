package com.com.technoparkproject.service.tasks;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamWriterTask extends AbstractWriterTask{


    private final AtomicBoolean mIsCancelled;

    public StreamWriterTask(FileChannel recChannel, BlockingDeque<ByteBuffer> packetsQ, AtomicBoolean isCancelled) {
        super(recChannel, packetsQ);
        mIsCancelled = isCancelled;

    }

    private static final long POLL_TIMEOUT = 500;

    @Override
    public void write(BlockingDeque<ByteBuffer> packetsQ) {
        try {
            while (!mIsCancelled.get()) {
                ByteBuffer packet = packetsQ.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
                //finish task if there are no available packets after timeout
                if (packet == null) {
                    Log.e("WRITE TASK", "NO DATA");
                    break;
                }
                writeBuffer(packet);
            }
            //Log.d("WRITE END", "sizeof q = " + packetsQ.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //restore interrupt status
            Log.e(this.getClass().getSimpleName(),
                    "Thread was interrupted while writing", e);
        }
    }
}
