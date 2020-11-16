package com.com.technoparkproject;

import com.com.technoparkproject.service.utils.AudioFormatUtils;



public class RecordingProfile {

    private final int mSamplingRate;
    private final int mConfigChannels;
    private final int mAudioFormat;
    private final int mBitRate;

    public RecordingProfile(int samplingRate, int configChannels, int audioFormat, int bitRate) {
        mSamplingRate = samplingRate;
        mConfigChannels = configChannels;
        mAudioFormat = audioFormat;
        mBitRate = bitRate;
    }


    public int getAudioFormat() {
        return mAudioFormat;
    }

    public int getConfigChannels() {
        return mConfigChannels;
    }

    public int getSamplingRate() {
        return mSamplingRate;
    }

    public int getChannelsCount(){
        return AudioFormatUtils.getChannelCount(mConfigChannels);
    }

    public int getBytesPerSample(){
        return AudioFormatUtils.getBytesPerSample(mAudioFormat);
    }

    public int getBitRate() {
        return mBitRate;
    }

    public int getFrameSize(){
        return getChannelsCount()*getBytesPerSample();
    }
}
