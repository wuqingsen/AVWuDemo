package com.demo.lupingwudemo.audiovideotwo;

import android.annotation.SuppressLint;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * wuqingsen on 2020-06-09
 * Mailbox:1243411677@qq.com
 * annotation:录制屏幕
 */
@SuppressLint("NewApi")
public class ScreenRecorderThread extends Thread {

    private Surface mSurface;

    private VirtualDisplay mVirtualDisplay;

    private MediaProjection mMediaProjection;

    private MediaCodec mMediaCodec;

    private volatile boolean mMuxerStart = false;

    private MediaFormat mVideoMediaFormat;

    private MediaMuxer mMediaMuxer;

    //退出录制的标志位
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private int mTackIndex = -1;

    private long prevOutputPTSUs = 0;

    protected MuxerListener mMuxerListener;
    private VideoDataListener videoDataListener;

    public ScreenRecorderThread(VideoDataListener videoDataListener) {
        this.videoDataListener = videoDataListener;
    }

    @Override
    public void run() {
        super.run();
        while (!mQuit.get()) {
            //  int input_index = mMediaCodec.dequeueInputBuffer(-1);
            //   Log.d(LOG_TAG,"input_index "+input_index);
            //获取MediaCodec的Output缓冲池的数据
            int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);

            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //为MediaMuxer添加VideoTrack
                resetOutputFormat();
//                Log.e("wqs", "ScreenRecorderThread: 开始录制屏幕 VideoTrack");
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //没有拿到数据，10ms后再去获取
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e("wqs", "ScreenRecorderThread: 开始录制屏幕 10ms后再去获取");
            } else if (index > 0) {
                if (!mMuxerStart) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                }
                //将数据写入MediaMuxer
                encodeToTrack(index);
//                Log.e("wqs", "ScreenRecorderThread: 开始录制屏幕 encodeToTrack");
                //释放MediaCodec的OutputBuffer
                mMediaCodec.releaseOutputBuffer(index, false);
            }
        }
        release();
    }

    public void startRecord() {
        Log.e("wqs", "ScreenRecorderThread 视频录制开始");
        start();
    }

    public void stopRecord() {
        mQuit.set(true);
    }

    public void config(MediaFormat mediaFormat) throws IOException {

        if (mediaFormat == null) {
            throw new NullPointerException("video MediaFormat is null");
        }

        mVideoMediaFormat = mediaFormat;
        //初始化编码器
        prepareEncoder(mediaFormat);
        //初始化VirtualDisplay
        prepareVirtualDisPlay();


    }

    /**
     * 初始化VirtualDisPlay
     */
    private void prepareVirtualDisPlay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen_record",
                mVideoMediaFormat.getInteger(MediaFormat.KEY_WIDTH), mVideoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT),
                1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mSurface, null, null);
    }

    /**
     * 初始化MediaCodec
     */
    private void prepareEncoder(MediaFormat mediaFormat) throws IOException {
        String mimeType = mVideoMediaFormat.getString(MediaFormat.KEY_MIME);
        mMediaCodec = MediaCodec.createEncoderByType(mimeType);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
    }

    public void startMuxer(boolean muxerStart) {
        mMuxerStart = muxerStart;
    }

    public void setMuxerListener(MuxerListener listener) {
        mMuxerListener = listener;
    }

    public void setMediaMuxer(MediaMuxer mediaMuxer) {

        if (mediaMuxer == null) {
            throw new NullPointerException("MediaMuxer is null");
        }

        mMediaMuxer = mediaMuxer;
    }

    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if (mVirtualDisplay != null) {

            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if (mMediaProjection != null) {
            mMediaProjection = null;
        }
        if (mMuxerListener != null) {
            mMuxerListener.stopMuxer(1);
            mMediaMuxer = null;
        }
    }

    public void resetOutputFormat() {
        if (mMuxerStart) {

            throw new IllegalStateException("output format already changed");

        }

        MediaFormat format = mMediaCodec.getOutputFormat();
        mTackIndex = mMediaMuxer.addTrack(format);
        // Log.d(LOG_TAG,"screen tack_index is "+mTackIndex);
        mMuxerStart = mMuxerListener.startMuxer(1);//1为视频

    }

    /**
     * 将MediaCodec编码过后的byte流写入MediaMuxer
     */
    public void encodeToTrack(int index) {
        ByteBuffer enData = mMediaCodec.getOutputBuffer(index);
        //  Log.d(LOG_TAG, "BufferInfo flag is "+mBufferInfo.flags+" size is "+mBufferInfo.size);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mBufferInfo.size = 0;
        }

        if (mBufferInfo.size == 0) {
            enData = null;
        }

        if (enData != null) {
            //mBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
            enData.position(mBufferInfo.offset);
            enData.limit(mBufferInfo.size + mBufferInfo.offset);
            mBufferInfo.presentationTimeUs = getPTSUs();
            prevOutputPTSUs = mBufferInfo.presentationTimeUs;
            synchronized (this) {
                mMediaMuxer.writeSampleData(mTackIndex, enData, mBufferInfo);
            }
            // Log.d(LOG_TAG, "mTackIndex is "+mTackIndex+" screen data is writing to mediamuxer...");


            //将编码数据实时传给解码器进行解码,
            //但这里出现问题，导致解码不出画面
            int offset = enData.position();
            int len = mBufferInfo.size;
            int size = enData.limit();
            byte[] data = new byte[size];
            for (int i = offset; i < size; i++) {
                data[i] = enData.get(i);
            }
            if (videoDataListener != null) {
                videoDataListener.onEncode(data, offset, len);
            }
        }

    }

    public void setMediaProjection(MediaProjection _mediaProjection) {
        if (_mediaProjection == null) {
            throw new NullPointerException("mediaProjection is null");
        }
        this.mMediaProjection = _mediaProjection;
    }

    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;

    }
}
