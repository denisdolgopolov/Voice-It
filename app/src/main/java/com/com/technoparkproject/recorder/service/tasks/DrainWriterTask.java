package com.com.technoparkproject.recorder.service.tasks;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class DrainWriterTask extends AbstractWriterTask{

    public DrainWriterTask(FileChannel recChannel, BlockingDeque<ByteBuffer> packetsQ) {
        super(recChannel, packetsQ);
    }

    @Override
    public void write(BlockingDeque<ByteBuffer> packetsQ) {
        List<ByteBuffer> packets = new ArrayList<>();
        packetsQ.drainTo(packets);
        for (ByteBuffer packet : packets){
            writeBuffer(packet);
        }
    }
}
