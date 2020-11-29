package com.technopark.recorder.repository;

import androidx.annotation.NonNull;

import java.io.File;

public final class RecordTopic {
    private File mRecFile;
    private String mName;
    private String mTopic;
    private long mDuration;

    public RecordTopic(File recFile, String name, String topic, long duration) {
        mRecFile = recFile;
        mName = name;
        mTopic = topic;
        mDuration = duration;

    }

    public RecordTopic(RecordTopic recTopic){
        this.mDuration = recTopic.mDuration;
        this.mName = recTopic.mName;
        this.mTopic = recTopic.mTopic;
        this.mRecFile = new File(recTopic.mRecFile.getPath());
    }

    public RecordTopic(){}

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


    //for debugging
    @NonNull
    @Override
    public String toString() {
        return "name "+getName()
        + ",topic "+getTopic()
        + ",file "+getRecordFile()
        + ",duration "+getDuration();
    }
}
