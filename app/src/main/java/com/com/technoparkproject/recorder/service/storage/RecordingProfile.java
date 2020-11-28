package com.com.technoparkproject.recorder.service.storage;

import android.media.AudioFormat;


public class RecordingProfile {

    private final int mSamplingRate;
    private final int mConfigChannels;
    private final int mAudioFormat;
    private final int mBitRate;
    private final String mFileFormat;

    public RecordingProfile(int samplingRate, int configChannels,
                            int audioFormat, int bitRate,
                            String fileFormat) {
        mSamplingRate = samplingRate;
        mConfigChannels = configChannels;
        mAudioFormat = audioFormat;
        mBitRate = bitRate;
        mFileFormat = fileFormat;
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

    public String getFileFormat() {
        return mFileFormat;
    }

    //Helper methods for conversion of AudioFormat constants
    //to meaningful numerical values
    private static final class AudioFormatUtils {

        private AudioFormatUtils(){}

        //convert AudioFormat bit depth
        public static int getBytesPerSample(int audioFormat)
        {
            switch (audioFormat) {
                case AudioFormat.ENCODING_PCM_8BIT:
                    return 1;
                case AudioFormat.ENCODING_PCM_16BIT:
                    return 2;
                //non-pcm encoding aren't used in this implementation
                default:
                    throw new IllegalArgumentException("Unrecognized audio format " + audioFormat);
            }
        }

        //convert AudioFormat channel config
        public static int getChannelCount(int channelConfig){
            switch (channelConfig) {
                case AudioFormat.CHANNEL_IN_MONO:
                    return 1;
                case AudioFormat.CHANNEL_IN_STEREO:
                    return 2;
                default:
                    throw new IllegalArgumentException("Unrecognized channel config "+channelConfig);
            }
        }

    }
}
