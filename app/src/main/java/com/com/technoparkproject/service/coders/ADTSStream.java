package com.com.technoparkproject.service.coders;

import android.util.Log;

import com.com.technoparkproject.service.AudioRecorder;
import com.com.technoparkproject.service.storage.RecordingProfile;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

//todo move encoder from this class possibly
public class ADTSStream implements PacketStream<ByteBuffer>{
    public final Encoder<ByteBuffer> mAACEncoder;


    private static final byte ADTS_HEADER_LENGTH = 7;
    private final RecordingProfile mRecProfile;

    //ADTS header parameters
    private int mSamplingFrequencyIndex = CD_FREQUENCY_INDEX;
    private int mChannelConfig = CHANNEL_CONFIG_MONO;
    private int mAACObjectType = AAC_LC_OBJECT_ID;

    private boolean mIsConfigured = false;

    //ADTS header constants
    private static final byte CD_FREQUENCY_INDEX = 4; //44100
    private static final byte AAC_LC_OBJECT_ID = 1;
    private static final byte CHANNEL_CONFIG_MONO = 1;


    public ADTSStream(RecordingProfile recProfile){
        mRecProfile = recProfile;
        mAACEncoder = new AACEncoder(mRecProfile);
    }


    //setup parameters for ADTS header from codec config info
    private void configureADTS(){
        if (!mIsConfigured){
            ByteBuffer asc = mAACEncoder.getCodecConfig();
            parseAACAudioSpecificConfig(asc);
            mIsConfigured = true;
        }
    }

    public void start() {
        mAACEncoder.start();
        configureADTS();
    }
    public void configure(){
        mAACEncoder.configure();
    }

    public void stop(){
        mAACEncoder.stop();
    }

    public void release(){
        mAACEncoder.release();
    }


    /**
     * Create 1 or more ADTS packets from PCM ByteBuffer.
     * Number of packets is determined by {@link #getMaxFrameLength()}.
     * 1 frame corresponds to 1 sample
     * 1 PCM sample representation = bitDepth*channelCount
     * @param pcmFrame: ByteBuffer containing PCM data
     * @return list of ADTS packets
     */
    //todo add this to interface also probably
    public List<ByteBuffer> getPackets(ByteBuffer pcmFrame){
        final int pcmLength = pcmFrame.remaining();

        final int bytesPerFrame = getMaxFrameLength()*mRecProfile.getFrameSize();

        List<ByteBuffer> packets = new ArrayList<>();
        for (int i = 0; i < pcmLength; i+=bytesPerFrame) {
            pcmFrame.position(i);
            pcmFrame.limit(Math.min(i + bytesPerFrame, pcmLength));
            ByteBuffer packet = getPacket(pcmFrame);
            if (packet.capacity() != 0)
                packets.add(packet);
        }
        return packets;
    }

    //returns all cached internal packets
    public List<ByteBuffer> flushPackets(){
        List<ByteBuffer> residualBuffers = mAACEncoder.drainEncoder();
        List<ByteBuffer> residualPackets = new ArrayList<>();
        for(ByteBuffer residualLoad : residualBuffers){
            ByteBuffer header = writeADTSFrameHeader(residualLoad);
            ByteBuffer packet =  createADTSPacket(header,residualLoad);
            residualPackets.add(packet);
        }
        return residualPackets;
    }

    @Override
    public int getMaxFrameLength() {
        return mAACEncoder.getMaxFrameLength();
    }

    public ByteBuffer getPacket(ByteBuffer pcmFrame){
            ByteBuffer aacLoad =  mAACEncoder.encode(pcmFrame);
            if (aacLoad.capacity() == 0)
                return aacLoad;
            else{
                //buffer contains aac info
                ByteBuffer header = writeADTSFrameHeader(aacLoad);
                return createADTSPacket(header,aacLoad);
            }
    }


    /**
     * Parser for AAC Audio Specific Config MPEG-4 block
     * It is sent before transmitting of any frames from MediaCodec
     * See: http://wiki.multimedia.cx/index.php?title=MPEG-4_Audio#Audio_Specific_Config
     * Part of the structure used in the implementation
     * (assuming object type < 32 and frequency index < 16)
     * AAAA ABBB BCCC C
     * Letter	Length (bits)	Description
     *   A	    5	        Audio Object Type (MPEG-4)
     *   B	    4	        MPEG-4 Sampling Frequency Index
     *   C	    4	        MPEG-4 Channel Configuration
     *  See most common values in {@link #writeADTSFrameHeader}
     */
    private void parseAACAudioSpecificConfig(ByteBuffer ascBuffer) {
        byte[] asc = new byte[ascBuffer.remaining()];
        ascBuffer.get(asc);

        mAACObjectType = ((asc[0] & 0xF4) >>> 3); // AAC object type (part : AAAAA)
        Log.d(this.getClass().getSimpleName(),"parseAacAudioSpecificConfig(): AAC profile set to: " + mAACObjectType);

        mSamplingFrequencyIndex = ((asc[0] & 0x07) << 1) | ((asc[1] & 0x80) >>> 7);// Sampling frequency index (part : BBBB)
        Log.d(this.getClass().getSimpleName(),"parseAacAudioSpecificConfig(): AAC sampling frequency index set to: " + mSamplingFrequencyIndex);

        mChannelConfig = ((asc[1] & 0x78) >>> 3); //channel configuration (part : CCCC)
        Log.d(this.getClass().getSimpleName(),"parseAacAudioSpecificConfig(): AAC channel configuration set to: " + mChannelConfig);
    }



    /**
     *  ADTS AAC Stream
     *  To create a valid .AAC file it's necessary to add
     *  ADTS header at the beginning of every AAC frame.
     *  MediaCodec encoder generates a packet of raw AAC data
     *  and we need to do it manually.
     *
     *  Structure
     *  (references: http://wiki.multimedia.cx/index.php?title=ADTS,
     *  ISO/IEC 13818-7 and ISO/IEC 14496-3)
     *  AAAAAAAA AAAABCCD EEFFFFGH HHIJ  KLMM MMMMMMMM MMMOOOOO OOOOOOPP  (QQQQQQQQ QQQQQQQQ)
     *  |--------adts_fixed_header-----||-----adts_variable_header------||--adts_error_check--|
     *  Letter	Length (bits)	Description
     *              adts_fixed_header
     *      A	    12	        syncword 0xFFF, all bits must be 1
     *      B	    1	        MPEG Version: 0 for MPEG-4, 1 for MPEG-2
     *      C	    2	        Layer: always 00
     *      D	    1	        protection absent, 1 = no CRC and 0 = CRC
     *      E	    2	        profile, the MPEG-4 Audio Object Type minus 1
     *                                  MPEG-4 object type (ID == 0)
     *                                      0 AAC Main
     *                                      1 AAC LC
     *                                      2 AAC SSR
     *                                      3 AAC LTP
     *      F	    4	        MPEG-4 Sampling Frequency Index (15 is forbidden)
     *                                  samplingFrequencyIndex Value
     *                                          0x0            96000
     *                                          0x1            88200
     *                                          0x2            64000
     *                                          0x3            48000
     *                                          0x4            44100
     *                                          0x5            32000
     *                                          0x6            24000
     *                                          0x7            22050
     *                                          0x8            16000
     *                                          0x9            12000
     *                                          0xa            11025
     *                                          0xb             8000
     *                                          0xc             7350
     *      G	    1	        private bit, 0 when encoding
     *      H	    3	        MPEG-4 Channel Configuration
     *                              0 - defined in AOT related SpecificConfig
     *                              1 - mono (center front speaker)
     *                              2 - stereo (left, right front speakers)
     *      I	    1	        originality, set to 0 when encoding
     *      J	    1	        home, set to 0 when encoding
     *              adts_variable_header
     *      K	    1	        copyrighted id bit, set to 0 when encoding
     *      L	    1	        copyright id start, set to 0 when encoding
     *      M	    13	        frame length, including this header, FrameLength = (ProtectionAbsent == 1 ? 7 : 9) + size(AACFrame)
     *      O	    11	        Buffer fullness
     *      P	    2	        Number of AAC frames (RDBs) in ADTS frame minus 1
     *              adts_error_check
     *      Q	    16	        CRC if protection absent is 0
     **/

    private ByteBuffer writeADTSFrameHeader(ByteBuffer aacLoad){
        final byte[] headerADTS = new byte[ADTS_HEADER_LENGTH];

        //adts_fixed_header
        //AAAAAAAA
        //11111111
        headerADTS[0] = (byte) 0xFF;

        //AAAABCCD
        //11110001
        headerADTS[1] = (byte) 0xF1;

        //EEFFFFGH
        headerADTS[2] |= (byte) ((mAACObjectType - 1) << 6); // AAC profile minus 1 (part: EE)
        headerADTS[2] |= (byte) ((0x0F & mSamplingFrequencyIndex) << 2); // Sampling freq index (part: FFFF)
        // part G is 0
        headerADTS[2] |= (byte) ((0x04 & mChannelConfig) >> 2); // 3d bit of channel config (part: H)

        //HHIJKLMM
        headerADTS[3] |= (byte) (mChannelConfig << 6); // 2 MSBs of channel config (part: HH)
        // parts I, J are 0

        //adts_variable_header
        //K and L are 0
        //frameLength = 7 + size(AACFrame), CRC not used
        final int frameLength = ADTS_HEADER_LENGTH + aacLoad.remaining();
        headerADTS[3] |= (byte) ((0x1FFF & frameLength) >> 11); // 2 MSBs of frame length (part: MM)

        //MMMMMMMM
        headerADTS[4] |= (byte) ((0x7FF & frameLength) >> 3); // 8 middle bits of frame length (part: MMMMMMMM)

        //MMMOOOOO
        headerADTS[5] |= (byte) ((0x07 & frameLength) << 5); // 3 LSBs of frame length (part: MMM)
        headerADTS[5] |= (byte) 0x1F; // Buffer fullness = all 1s in this config (part OOOOO)

        //OOOOOOPP
        headerADTS[6] |= (byte) 0xFC; // Buffer fullness (part OOOOOO)
        //PP left as 0, as it is the number of AAC frames in ADTS frame - 1 = 0

        //no CRC used => no adts_error_check


        ByteBuffer headerBuffer = ByteBuffer.wrap(headerADTS);
        return headerBuffer;

        /*//concatenate header and load into packet
        ByteBuffer packet = ByteBuffer.allocateDirect(frameLength).order(ByteOrder.nativeOrder());
        ByteBuffer headerBuffer = ByteBuffer.wrap(headerADTS);
        try {
            packet.put(headerBuffer);
            packet.put(aacLoad);
        }
        catch (BufferOverflowException e){
            Log.e(this.getClass().getSimpleName(),
                    "Buffer overflow when constructing packet", e);
            return null;
        }
        return packet;*/
    }


    //concatenate header and load into packet
    private ByteBuffer createADTSPacket(ByteBuffer header, ByteBuffer load){
        ByteBuffer packet = ByteBuffer.allocateDirect(header.remaining()+load.remaining()).order(ByteOrder.nativeOrder());
        try {
            packet.put(header);
            packet.put(load);
        }
        catch (BufferOverflowException e){
            Log.e(this.getClass().getSimpleName(),
                    "Buffer overflow when constructing packet", e);
            return null;
        }
        packet.rewind();
        return packet;

    }
}
