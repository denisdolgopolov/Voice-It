package com.com.technoparkproject.repository;

import java.io.File;

public final class Record {
    private File mRecFile;
    private String mName;
    private String mTopic;
    private long mDuration;

    public Record(File recFile,String name,String topic,long duration) {
        mRecFile = recFile;
        mName = name;
        mTopic = topic;
        mDuration = duration;

    }

    public Record(){}

    public String getName() {
        return mName;
    }

    public String getTopic() {
        return mTopic;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setTopic(String topic) {
        mTopic = topic;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public File getRecordFile() {
        return mRecFile;
    }

    public void setRecordFile(File recordFile) {
        mRecFile = recordFile;
    }
}
