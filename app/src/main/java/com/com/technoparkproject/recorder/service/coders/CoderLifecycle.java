package com.com.technoparkproject.recorder.service.coders;

interface CoderLifecycle {
    void configure();
    void start();
    void stop();
    void release();
}
