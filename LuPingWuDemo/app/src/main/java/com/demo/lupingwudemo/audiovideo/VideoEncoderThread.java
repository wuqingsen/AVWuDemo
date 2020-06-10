package com.demo.lupingwudemo.audiovideo;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * wuqingsen on 2020-06-03
 * Mailbox:1243411677@qq.com
 * annotation:视频编码线程
 */
@SuppressLint("NewApi")
public class VideoEncoderThread extends Thread {
    EncoderThread mEncoderThread;
    // 存储每一帧的数据 Vector 自增数组
//    private Vector<Integer> frameBytes;
    private final Object lock = new Object();

    private WeakReference<MediaMuxerThread> mediaMuxer; // 音视频混合器

    private volatile boolean isStart = false;
    private volatile boolean isExit = false;
    private volatile boolean isMuxerReady = false;


    public VideoEncoderThread(WeakReference<MediaMuxerThread> mediaMuxer) {
        // 初始化相关对象和参数
        this.mediaMuxer = mediaMuxer;
//        frameBytes = new Vector<Integer>();

    }

    /**
     * 开始视频编码
     */
    private void startMediaCodec() throws IOException {
        mEncoderThread = new EncoderThread(AudioVideoCodecActivity.mediaProjection);
        mEncoderThread.start();
        isStart = true;
    }

    //混合器已经初始化，等待添加轨道
    public void setMuxerReady(boolean muxerReady) {
        synchronized (lock) {
            Log.e("=====视频录制", Thread.currentThread().getId() + " video -- setMuxerReady..." + muxerReady);
            isMuxerReady = muxerReady;
            lock.notifyAll();
        }
    }

    @Override
    public void run() {
        super.run();
        while (!isExit) {
            if (!isStart) {
                stopMediaCodec();
                if (!isMuxerReady) {
                    synchronized (lock) {
                        try {
                            Log.e("=====视频录制", "video -- 等待混合器准备...");
                            lock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                if (isMuxerReady) {
                    try {
                        Log.e("=====视频录制", "video -- startMediaCodec...");
                        startMediaCodec();
                    } catch (IOException e) {
                        isStart = false;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                        }
                    }
                }

            } else {
                try {
                    encodeFrame(1);
                } catch (Exception e) {
                    Log.e("=====视频录制", "解码视频(Video)数据 失败" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        Log.e("=====视频录制", "Video 录制线程 退出...");
    }

    public void exit() {
        isExit = true;
    }

    /**
     * 编码每一帧的数据
     *
     * @param input 每一帧的数据
     */
    private void encodeFrame(int input) {
        int outputState = EncoderThread.mEncoder.dequeueOutputBuffer(EncoderThread.mBufferInfo, 1000);

        Log.e("=====视频录制", "解码视频数据:" + outputState);

        ByteBuffer[] outputBuffers = EncoderThread.mEncoder.getOutputBuffers();

        while (outputState >= 0 || outputState == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            if (outputState == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e("=====视频录制", "添加轨道");
                //添加轨道的好时机，只有一次
                MediaFormat newFormat = EncoderThread.mEncoder.getOutputFormat();
                MediaMuxerThread mediaMuxerRunnable = this.mediaMuxer.get();
                if (mediaMuxerRunnable != null) {
                    Log.e("=====视频录制", "添加轨道成功");
                    mediaMuxerRunnable.addTrackIndex(MediaMuxerThread.TRACK_VIDEO, newFormat);
                }
            } else {
                ByteBuffer outputBuffer = outputBuffers[outputState];
                if ((EncoderThread.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    EncoderThread.mBufferInfo.size = 0;
                }
                Log.e("=====视频录制", "EncoderThread.mBufferInfo.size:" + EncoderThread.mBufferInfo.size);
                if (EncoderThread.mBufferInfo.size != 0) {
                    MediaMuxerThread mediaMuxer = this.mediaMuxer.get();
                    if (mediaMuxer != null && mediaMuxer.isMuxerStart()) {
                        Log.e("=====视频录制", "添加视频数据111");
                        mediaMuxer.addMuxerData(new MediaMuxerThread.MuxerData(MediaMuxerThread.TRACK_VIDEO, outputBuffer, EncoderThread.mBufferInfo));
                    }
                }
                EncoderThread.mEncoder.releaseOutputBuffer(outputState, false);
            }
            outputState = EncoderThread.mEncoder.dequeueOutputBuffer(EncoderThread.mBufferInfo, 1000);
        }
    }

    /**
     * 停止视频编码
     */
    private void stopMediaCodec() {
        if (EncoderThread.mEncoder != null) {
            mEncoderThread.interrupt();
            mEncoderThread.quit();
            EncoderThread.mEncoder = null;
        }
        isStart = false;
        Log.e("=====视频录制", "stop video 录制...");
    }

}
