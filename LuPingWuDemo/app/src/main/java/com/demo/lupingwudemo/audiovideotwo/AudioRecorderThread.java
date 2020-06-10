package com.demo.lupingwudemo.audiovideotwo;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * wuqingsen on 2020-06-09
 * Mailbox:1243411677@qq.com
 * annotation:录制音频
 */
@SuppressLint("NewApi")
public class AudioRecorderThread extends Thread {


    private static final String LOG_TAG = "wqs+AudioRecorderThread";

    private long mPresentationTimeStamp = 0;

    private AudioRecord mAudioRecord;

    private static long mAudioBytesReceived = 0; // 接收到的音频数据 用来设置录音起始时间的

    private long mAudioStartTime = 0;

    private boolean mIsEncoding = false;

    private boolean mRecording = false;

    //退出录制的标志位
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    private MediaFormat mAudioMediaFormat;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private MediaCodec mMediaCodec;

    private boolean mMuxerStart = false;

    private MediaMuxer mMediaMuxer;

    private int mTackIndex = -1;

    private long prevOutputPTSUs = 0;

    protected MuxerListener mMuxerListener;

    public AudioRecorderThread() {

        //初始化AudioRecord
        mAudioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT));
    }

    public void startRecord() {
        Log.e("wqs", "AudioRecorderThread: 开始录制音频");
        mRecording = true;
        start();
    }

    public void stopRecord() {
        Log.e("wqs", "AudioRecorderThread: 停止录制音频");
        mQuit.set(true);
        mRecording = false;
    }

    @Override
    public void run() {
        super.run();
        mAudioRecord.startRecording();
        byte buffer[] = new byte[AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT) / 4];

        while (!mQuit.get()) {
            mAudioRecord.read(buffer, 0,
                    AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT) / 4);
            mPresentationTimeStamp = System.nanoTime();
            //将编码过后的数据写入MediaMuxer
            encodeToTrack(-1);
            //将录制的音频源数据送入MediaCodec编码
            encodeToMediaCodec(buffer.clone(), mPresentationTimeStamp);
        }
        mAudioRecord.stop();

        release();
    }

    public void config(MediaFormat mediaFormat) throws IOException {
        if (mediaFormat != null) {
            this.mAudioMediaFormat = mediaFormat;
        }
        prepareEncoder();

    }


    private void prepareEncoder() throws IOException {

        mAudioBytesReceived = 0;
        mBufferInfo = new MediaCodec.BufferInfo();
        mMediaCodec = MediaCodec.createEncoderByType(mAudioMediaFormat.getString(MediaFormat.KEY_MIME));
        mMediaCodec.configure(mAudioMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        mIsEncoding = true;
    }

    /**
     * 将录制的音频源数据送入MediaCodec编码
     */
    private void encodeToMediaCodec(byte[] buffer, long presentationTimeStamp) {
        if (mAudioBytesReceived == 0) {

            mAudioStartTime = presentationTimeStamp;

        }
        mAudioBytesReceived += buffer.length;

        if (mMediaCodec != null) {
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buffer);
                long presentationTimeUs = (presentationTimeStamp - mAudioStartTime) / 1000;
                // Log.d("hsk","presentationTimeUs--"+presentationTimeUs);
                if (!mIsEncoding) {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
                            buffer.length, presentationTimeUs,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    // closeEncoder(mAudioCodec, mAudioBufferInfo,
                    // mAudioTrackIndex);
                    // closeMuxer();
                    // encodingService.shutdown();
                    //closeEncode();
                } else {

                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
                            buffer.length, presentationTimeUs, 0);
                    //   Log.d(LOG_TAG, "audio data is encoding...");
                }
                /*
                 * mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
                 * this_buffer.length, presentationTimeStamp, 0);
                 */

            }

        }

    }

    public void startMuxer(boolean muxerStart) {
        mMuxerStart = muxerStart;
    }

    public void setMediaMuxer(MediaMuxer mediaMuxer) {
        mMediaMuxer = mediaMuxer;
    }

    public void setMuxerListener(MuxerListener listener) {
        mMuxerListener = listener;
    }

    public void release() {
        // mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
        //       this_buffer.length, presentationTimeUs,
        //       MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mMediaMuxer != null){
            mMediaMuxer = null;
        }
        if(mMuxerListener!=null){
            mMuxerListener.stopMuxer(2);
            mMediaMuxer = null;
        }
    }

    /**
     * 将编码过后的数据写入MediaMuxer
     */
    public void encodeToTrack(int index) {

        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        while (mRecording) {
            int encoderIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo,
                    100);
            if (encoderIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                if (mTackIndex == -1) {
                    mTackIndex = mMediaMuxer.addTrack(mMediaCodec.getOutputFormat());
                    Log.d(LOG_TAG, "Audio tack_index is " + mTackIndex);
                    if (mMuxerListener != null) {
                        mMuxerStart = mMuxerListener.startMuxer(2);
                    }
                }


            } else if (encoderIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encoderIndex > 0) {
                if (mMuxerStart) {
                    ByteBuffer enData = encoderOutputBuffers[encoderIndex];
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mBufferInfo.size = 0;
                    }
                    if (mBufferInfo.size == 0) {
                        enData = null;
                    }
                    if (enData != null) {
                        enData.position(mBufferInfo.offset);
                        enData.limit(mBufferInfo.size + mBufferInfo.offset);
                        mBufferInfo.presentationTimeUs = getPTSUs();
                        prevOutputPTSUs = mBufferInfo.presentationTimeUs;
                        mMediaMuxer.writeSampleData(mTackIndex, enData, mBufferInfo);
                        Log.d(LOG_TAG, "audio data is writing to mediamuxer...");
                    }

                    mMediaCodec.releaseOutputBuffer(encoderIndex, false);
                    // 退出循环
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

                        break;

                    }
                }
            }
        }
    }

    private long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;

    }

}
