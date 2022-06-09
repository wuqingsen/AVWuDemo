package com.demo.lupingwudemo.audiovideotwo;

import android.annotation.SuppressLint;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * wuqingsen on 2020-06-09
 * Mailbox:1243411677@qq.com
 * annotation:
 */
@SuppressLint("NewApi")
public class RecordScreenUtils implements MuxerListener {
    private MediaProjection mediaProjection;
    private MediaMuxer mMediaMuxer;
    private String mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/吴庆森录屏2.mp4";
    private ScreenRecorderThread screenRecorderThread;
    private AudioRecorderThread audioRecorderThread;
    private MediaFormat mVideoMediaFormat = DefaultMediaFormat.getDefaultVideoFormat();//获取视频默认的MediaFormat
    private MediaFormat mAudioMediaFormat = DefaultMediaFormat.getDefaultAudioFormat();//获取音频默认的MediaFormat

    protected volatile boolean mScreenRecordMuxerStartReady = false;//ScreenRecord是否准备好开启MediaMuxer;

    protected volatile boolean mAudioRecordMuxerStartReady = false;//AudioRecord是否准备好开启MediaMuxer;

    //volatile后加
    protected volatile boolean mScreenRecordMuxerStopReady = false;//ScreenRecord是否准备好停止MediaMuxer;
    //volatile后加
    protected volatile boolean mAudioRecordMuxerStopReady = false;//AudioRecord是否准备好停止MediaMuxer;

    public RecordScreenUtils(MediaProjection mediaProjection, VideoDataListener videoDataListener) {
        this.mediaProjection = mediaProjection;

        screenRecorderThread = new ScreenRecorderThread(videoDataListener);
        audioRecorderThread = new AudioRecorderThread();

        screenRecorderThread.setMediaProjection(this.mediaProjection);

        screenRecorderThread.setMuxerListener(this);
        audioRecorderThread.setMuxerListener(this);
    }

    public void config() {
        //初始化MediaMuxer
        try {
            //初始化MediaMuxer,设置输出格式为mp4
            mMediaMuxer = new MediaMuxer(mSavePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            screenRecorderThread.setMediaMuxer(mMediaMuxer);
            //配置ScreenRecorder
            screenRecorderThread.config(mVideoMediaFormat);

            audioRecorderThread.setMediaMuxer(mMediaMuxer);
            //配置AudioRecorder
            audioRecorderThread.config(mAudioMediaFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecord() {
        //启动音频录制
        audioRecorderThread.startRecord();
    }

    public void stopRecord() {
        audioRecorderThread.stopRecord();
        screenRecorderThread.stopRecord();
    }

    @Override
    public boolean startMuxer(int type) {
        if (type == 1) {
            mScreenRecordMuxerStartReady = true;
        } else if (type == 2) {
            mAudioRecordMuxerStartReady = true;
            screenRecorderThread.startRecord();
        }
        if (mScreenRecordMuxerStartReady && mAudioRecordMuxerStartReady) {
            mMediaMuxer.start();
            screenRecorderThread.startMuxer(true);
            audioRecorderThread.startMuxer(true);
            Log.e("wqs+Utils", "muxer is starting...");
            return true;
        }
        return false;

    }

    @Override
    public boolean stopMuxer(int type) {

        if (type == 1) {
            mScreenRecordMuxerStopReady = true;
        } else if (type == 2) {
            mAudioRecordMuxerStopReady = true;
        }
        if (mAudioRecordMuxerStopReady && mScreenRecordMuxerStopReady) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            Log.d("wqs", "muxer is stop...");
        }

        return true;
    }
}
