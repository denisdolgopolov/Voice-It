package com.com.technoparkproject.service.coders;

import java.util.List;

public interface Encoder<E> extends CoderLifecycle{
    E encode(final E in);
    List<E> drainEncoder();
    E getCodecConfig();
    int getMaxFrameLength();
}
