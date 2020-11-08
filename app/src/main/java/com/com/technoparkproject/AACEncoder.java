package com.com.technoparkproject;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AACEncoder {

    private final int mBytesPerSample;
    private final int mChannelCount;
    // The encoder instance
    private MediaCodec mEncoder;

    private static final long mTimeoutUs = 100; //timeout value for working with codec buffers


    public static final int MAX_AAC_FRAME_LENGTH = 1024; //one aac frame can't contain more than 1024 PCM samples

    private static final String DEFAULT_AAC_ENCODER_NAME = "OMX.google.aac.encoder";


    private MediaFormat mOutputFormat;

    public AACEncoder(int sampleRate, int channelCount, int bitrate, int bytesPerSample) {
        mBytesPerSample = bytesPerSample;
        mChannelCount = channelCount;
        configure(sampleRate, channelCount, bitrate);
    }

    public void configure(int sampleRate, int channelCount, int bitrate) {

        //setting up AAC-LC format info for codec
        MediaFormat mediaFormat =
                MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);


        //get encoder name corresponding to AAL-LC
        MediaCodecList regularCodecs = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String encoderName = regularCodecs.findEncoderForFormat(mediaFormat);

        if (encoderName == null){
            encoderName = DEFAULT_AAC_ENCODER_NAME; //try to create encoder with default name
        }

        try {
            mEncoder = MediaCodec.createByCodecName(encoderName);
        }catch (IllegalArgumentException e){
            Log.e("MediaCodec", "can't create MediaCodec with name: " + encoderName, e);
            return;
        }
        catch (IOException e) {
            Log.e("MediaCodec", "I/O errors occurred while creating encoder", e);
            return;
        }

        mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mOutputFormat = mEncoder.getOutputFormat();

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
        pcmFrame.position(pcmFrame.limit()-pcmLength);
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
            int inputBufferId = mEncoder.dequeueInputBuffer(mTimeoutUs);
            if (inputBufferId < 0) {
                Log.e(this.getClass().getSimpleName(),
                        "No available input buffers for Encoding after " + mTimeoutUs + "Us");
            } else {
                Log.d("Encoding","Data put to buffer #"+inputBufferId);
                final ByteBuffer inputBuffer = mEncoder.getInputBuffer(inputBufferId);
                putDataToEncode(inputFrame, inputBuffer);
                mEncoder.queueInputBuffer(inputBufferId, 0, inputFrame.remaining(), 0, 0);
            }
        } catch (MediaCodec.CodecException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Internal codec error during working with input buffers", e);
        } catch (IllegalStateException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Encoder is not in running state, can't work with input buffers", e);
        }
    }

    //retrieve encoded ByteBuffer from Encoder
    private ByteBuffer dequeueEncodedData(MediaCodec.BufferInfo bufferInfo) {
        try {
            //bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mEncoder.dequeueOutputBuffer(bufferInfo, mTimeoutUs);
            if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.e(this.getClass().getSimpleName(),
                        "No available Encoder output buffers after " + mTimeoutUs + "Us");
                return null;
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mOutputFormat = mEncoder.getOutputFormat(); //update format of buffers
            } else if (outputBufferId >= 0) {
                Log.d("Receiving","Output data in buffer #"+outputBufferId);
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferId);
                final ByteBuffer outFrame = getEncodedData(outputBuffer);
                mEncoder.releaseOutputBuffer(outputBufferId, false);
                return outFrame;
            }
        } catch (MediaCodec.CodecException e) {
            if (e.isRecoverable() | e.isTransient()) {
                //TODO: try to recover from errors
            }
            Log.e(this.getClass().getSimpleName(),
                    "Internal codec error during working with output buffers", e);
            //return null;
        } catch (IllegalStateException e) {
            Log.e(this.getClass().getSimpleName(),
                    "Encoder is not in running state, can't work with output buffers", e);
            //return null;
        }
        return null;
    }


    //AAC encoding for one pcmFrame
    //bufferInfo will store flags and other data describing output buffer
    //NOTE: pcmFrame should have valid position and limit before invoking this method
    //      may return null buffer
    public ByteBuffer encode(final ByteBuffer pcmFrame, MediaCodec.BufferInfo bufferInfo) {
        if (pcmFrame.remaining() > MAX_AAC_FRAME_LENGTH*mBytesPerSample*mChannelCount)
            throw new IllegalArgumentException
                    ("Can't encode PCM frame, length " + pcmFrame.remaining() + " exceeds " + MAX_AAC_FRAME_LENGTH);
        enqueueEncodeData(pcmFrame);
        final ByteBuffer outFrame = dequeueEncodedData(bufferInfo);
        if (outFrame != null)
            Log.d(this.getClass().getSimpleName(), "encoded buffer length" + outFrame.capacity());
        return outFrame;
    }

}
