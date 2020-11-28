package com.com.technoparkproject.recorder.service.coders;

import java.util.List;

public interface PacketStream<E> extends CoderLifecycle{
    E getPacket(final E raw);
    List<E> getPackets(E rawFrame);
    List<E> flushPackets();
    int getMaxFrameLength();
}
