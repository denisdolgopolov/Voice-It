package com.com.technoparkproject.service.coders;

interface CoderLifecycle {
    void configure();
    void start();
    void stop();
    void release();
}
