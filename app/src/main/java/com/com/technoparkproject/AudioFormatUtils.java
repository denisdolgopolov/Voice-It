package com.com.technoparkproject;

import android.media.AudioFormat;

//Helper methods for conversion of AudioFormat constants
//to meaningful numerical values
public final class AudioFormatUtils {

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
