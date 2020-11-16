package com.com.technoparkproject.service.coders;

import java.util.List;

public interface PacketStream<E> extends CoderLifecycle{
    E getPacket(final E raw);
    List<E> flushPackets();
    int getMaxFrameLength(); //todo probably remove it, if encoder is used separately
}
