package com.technopark.recorder.service.tasks;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingDeque;


public abstract class AbstractWriterTask implements Runnable {

    private final FileChannel mRecordFileChannel;
    private final BlockingDeque<ByteBuffer> mPacketsQ;

    public AbstractWriterTask(final FileChannel recChannel, BlockingDeque<ByteBuffer> packetsQ) {
        mRecordFileChannel = recChannel;
        mPacketsQ = packetsQ;
    }

    public void writeBuffer(ByteBuffer buffer){
        try{
            while (buffer.hasRemaining())
                mRecordFileChannel.write(buffer);
        }
        catch (ClosedByInterruptException e){
            Thread.currentThread().interrupt(); //restore interrupt status
            //it is not possible to write element
            //channel is closed by interruption
            //=> return buffer back to head of deque
            mPacketsQ.addFirst(buffer);
            Log.e(this.getClass().getSimpleName(),
                    "File Channel is closed due to thread interruption",e);
        }
        catch (IOException e) {
            Log.e("RecordFileChannel", "Error writing to the channel ", e);
        }

    }

    public abstract void write(BlockingDeque<ByteBuffer> packetsQ);

    @Override
    public void run() {
        write(mPacketsQ);
    }
}