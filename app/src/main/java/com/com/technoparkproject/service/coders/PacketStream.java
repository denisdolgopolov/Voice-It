package com.com.technoparkproject.service.coders;

import java.nio.ByteBuffer;
import java.util.List;

public interface PacketStream<E> extends CoderLifecycle{
    E getPacket(final E raw);
    List<E> getPackets(E rawFrame);
    List<E> flushPackets();
    int getMaxFrameLength();
}
