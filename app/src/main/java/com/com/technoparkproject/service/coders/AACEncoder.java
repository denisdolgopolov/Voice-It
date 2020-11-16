package com.com.technoparkproject.service.coders;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.com.technoparkproject.RecordingProfile;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AACEncoder implements Encoder<ByteBuffer> {

    private static final int MAX_INPUT_POLL_COUNT = 10;
    //private final int mBytesPerSample;
   // private final int mChannelCount;
    private final RecordingProfile mRecProfile;
    // The encoder instance
    private MediaCodec mEncoder;

    private static final long TIMEOUT_US = 100; //timeout value for working with codec buffers


    private static final int MAX_AAC_FRAME_LENGTH = 1024; //one aac frame can't contain more than 1024 PCM samples

    private static final String DEFAULT_AAC_ENCODER_NAME = "OMX.google.aac.encoder";


    private MediaFormat mMediaFormat;

    /*public AACEncoder(int sampleRate, int channelCount, int bitrate, int bytesPerSample) {
        mBytesPerSample = bytesPerSample;
        mChannelCount = channelCount;
        setup(sampleRate, channelCount, bitrate);
    }*/
    public AACEncoder(RecordingProfile recProfile) {
        mRecProfile = recProfile;
        setup();
    }

    //private void setup(int sampleRate, int channelCount, int bitrate) {
    private void setup(){
        //setting up AAC-LC format info for codec
        mMediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                mRecProfile.getSamplingRate(), mRecProfile.getChannelsCount());
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mRecProfile.getBitRate());


        //get encoder name corresponding to AAL-LC
        MediaCodecList regularCodecs = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String encoderName = regularCodecs.findEncoderForFormat(mMediaFormat);

        if (encoderName == null) {
            encoderName = DEFAULT_AAC_ENCODER_NAME; //try to create encoder with default name
        }

        try {
            mEncoder = MediaCodec.createByCodecName(encoderName);
        } catch (IllegalArgumentException e) {
            Log.e("MediaCodec", "can't create MediaCodec with name: " + encoderName, e);
        } catch (IOException e) {
            Log.e("MediaCodec", "I/O errors occurred while creating encoder", e);
        }
        //mEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //mOutputFormat = mEncoder.getOutputFormat();

    }

    public int getMaxFrameLength(){
        return AACEncoder.MAX_AAC_FRAME_LENGTH;
    }

    public void configure(){
        mEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void start() {
        mEncoder.start();
    }

    public void stop() {
        mEncoder.stop();
    }

    public void release() {
        mEncoder.release();
    }


    //fill encoder inputBuffer with valid data
    private void putDataToEncode(final ByteBuffer pcmFrame, final ByteBuffer inputBuffer) {
        //length of PCM frame is based on position and limit set by the callee
        int pcmLength = pcmFrame.remaining();
        try {
            inputBuffer.put(pcmFrame);
        } catch (BufferOverflowException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Can't put" + pcmFrame +
                            " to buffer with a length " + inputBuffer.capacity(), e);
        }
        //reset position back to the original state
        pcmFrame.position(pcmFrame.limit() - pcmLength);
    }


    //fill encoder inputBuffer with valid data
    private ByteBuffer getEncodedData(final ByteBuffer outputBuffer) {
        //Create outFrame with capacity = length of encoded data
        //Set direction and order based on encoder's buffer
        final ByteBuffer outFrame = (outputBuffer.isDirect()) ?
                ByteBuffer.allocateDirect(outputBuffer.remaining()) :
                ByteBuffer.allocate(outputBuffer.remaining());
        outFrame.order(ByteOrder.nativeOrder());
        try {
            outFrame.put(outputBuffer);
        } catch (BufferOverflowException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Can't put" + outFrame.remaining() +
                            " to buffer with a capacity " + outFrame.capacity(), e);
            return null;
        }
        outFrame.rewind();
        return outFrame;
    }


    //pass input ByteBuffer to Encoder
    private void enqueueEncodeData(final ByteBuffer inputFrame) {
        try {
            int inputPollCount = 0; //try to get input buffer MAX_INPUT_POLL_COUNT
            int inputBufferId = MediaCodec.INFO_TRY_AGAIN_LATER;
            while (inputBufferId < 0 && inputPollCount < MAX_INPUT_POLL_COUNT) {
                inputBufferId = mEncoder.dequeueInputBuffer(TIMEOUT_US);
                if (inputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    //failed to retrieve bufferId
                    inputPollCount++;
                } else if (inputBufferId >= 0){
                    Log.d("Encoding", "Data put to buffer #" + inputBufferId);
                    final ByteBuffer inputBuffer = mEncoder.getInputBuffer(inputBufferId);
                    putDataToEncode(inputFrame, inputBuffer);
                    mEncoder.queueInputBuffer(inputBufferId, 0, inputFrame.remaining(), 0, 0);
                }
            }
            if (inputPollCount >= MAX_INPUT_POLL_COUNT){
                Log.e(this.getClass().getSimpleName(),
                        "No available input buffers for Encoding after " +
                                MAX_INPUT_POLL_COUNT +" polls with time "+ TIMEOUT_US + "Us");
            }

        } catch (MediaCodec.CodecException e) {
            handleCodecEx(e);
        } catch (IllegalStateException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Encoder is not in running state, can't work with input buffers", e);
        }
    }

    //retrieve encoded ByteBuffer from Encoder
    private ByteBuffer dequeueEncodedData(MediaCodec.BufferInfo bufferInfo) {
        try {
            MediaCodec.BufferInfo localBufferInfo = bufferInfo;
            if (localBufferInfo == null) {
                localBufferInfo = new MediaCodec.BufferInfo();
            }
            int outputBufferId = mEncoder.dequeueOutputBuffer(localBufferInfo, TIMEOUT_US);
            if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.e(this.getClass().getSimpleName(),
                        "No available Encoder output buffers after " + TIMEOUT_US + "Us");
                return null;
            } else if (outputBufferId >= 0) {
                Log.d("Receiving", "Output data in buffer #" + outputBufferId);
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferId);
                final ByteBuffer outFrame = getEncodedData(outputBuffer);
                mEncoder.releaseOutputBuffer(outputBufferId, false);
                return outFrame;
            }
        } catch (MediaCodec.CodecException e) {
            handleCodecEx(e);
        } catch (IllegalStateException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Encoder is not in running state, can't work with output buffers", e);
        }
        return null;
    }

    private void handleCodecEx(MediaCodec.CodecException e){
        if (e.isRecoverable()){
            //restart encoder to recover
            stop();
            configure();
            start();
            getCodecConfig();
        }
        else if (e.isTransient()) {
            Log.d(this.getClass().getSimpleName(),
                    "Retry to encode/decode later");
        }
        else
            Log.e(this.getClass().getSimpleName(),
                    "Internal codec error during working with output buffers", e);
    }

    private ByteBuffer dequeueEncodedData() {
        return dequeueEncodedData(null);
    }


    //AAC encoding for one pcmFrame
    //bufferInfo will store flags and other data describing output buffer
    //NOTE: pcmFrame should have valid position and limit before invoking this method
    //      may return null buffer
    public ByteBuffer encode(final ByteBuffer pcmFrame) {
        if (pcmFrame.remaining() > getMaxFrameLength() * mRecProfile.getFrameSize())
            Log.e(this.getClass().getSimpleName(), "Can't encode PCM frame, length " + pcmFrame.remaining() + " exceeds " + MAX_AAC_FRAME_LENGTH);
            //throw new IllegalArgumentException
             //       ("Can't encode PCM frame, length " + pcmFrame.remaining() + " exceeds " + MAX_AAC_FRAME_LENGTH);
        enqueueEncodeData(pcmFrame);
        final ByteBuffer outFrame = dequeueEncodedData();
        if (outFrame != null)
            Log.d(this.getClass().getSimpleName(), "encoded buffer length" + outFrame.capacity());
        return outFrame;
    }

    public List<ByteBuffer> drainEncoder(){
        List<ByteBuffer> residualBuffers = new ArrayList<>();
        ByteBuffer residual;
        while ((residual = dequeueEncodedData()) != null ){
            residualBuffers.add(residual);
        }
        return residualBuffers;
    }


    public ByteBuffer getCodecConfig() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer codecConfig = null;
        //test if indicates this buffer contains codec specific data
        while ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
            codecConfig = dequeueEncodedData(bufferInfo);
        }
        assert codecConfig != null;
        Log.d(this.getClass().getSimpleName(), "ASC detected");
        return codecConfig;
    }

}
