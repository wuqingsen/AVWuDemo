package com.demo.lupingwudemo.videoscreen;

import android.annotation.SuppressLint;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * wuqingsen on 2020-10-15
 * Mailbox:1243411677@qq.com
 * annotation:视频流合成视频
 */

@SuppressLint("NewApi")
public class EncoderVideoThread extends Thread {

    private MediaProjection mProjection; //帧数据，即编码的数据源
    private MediaCodec mEncoder;  //编码器
    private MediaMuxer mMuxer;   //将音视频数据合成多媒体文件
    private MediaCodec.BufferInfo mBufferInfo;
    private Surface mSurface;  //虚拟屏幕VirturalDiaplay的输出目的地
    private VirtualDisplay mVirtualDisplay;   //虚拟屏幕

    private int mWidth = 480;
    private int mHeight = 720;
    private int mDpi = 240;
    private int mBitRate = 5 * 1024 * 1024;   //编码位率，清晰度
    private int mFrameRate = 30;    //帧率，流畅度
    private int mIFrameInterval = 5; //帧与帧间的间隙

    private int mVideoTrackIndex = -1;   //视频轨索引

    private boolean mMuxerStarted = false;
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    private String mSavePath;

    private EncoderVideoThread.EncodeListener listener;


    public static final String MINE_TYPE = "video/avc";
    public static final String TAG = "screen record";

    public EncoderVideoThread(MediaProjection mediaProjection) {
        this.mProjection = mediaProjection;
        try {
            mEncoder = MediaCodec.createEncoderByType(MINE_TYPE);
            initEncoder();
            mSurface = mEncoder.createInputSurface();
            createVirturalDisplay();
            mSavePath = getSavePath();
            mBufferInfo =new  MediaCodec.BufferInfo();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initEncoder(){
        MediaFormat format = MediaFormat.createVideoFormat(MINE_TYPE,mWidth,mHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE,mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE,mFrameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,mIFrameInterval);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mEncoder.configure(format,null,null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public final void quit() {
        mQuit.set(true);
    }

    //传入MediaCodec生成的Surface,利用MediaCodec编码surface中的数据
    public void createVirturalDisplay(){
        if(mProjection != null){
            //这里有个坑：flags设DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mSurface这个才行，否则生成的文件无法播放
            mVirtualDisplay = mProjection.createVirtualDisplay(TAG, mWidth, mHeight,mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mSurface,null,null);
        }
    }

    private void recordVirtualDisplay() {
        while (!mQuit.get()) {
            //获取MediaCodec通过h264编码之后的数据
            int outputState = mEncoder.dequeueOutputBuffer(mBufferInfo, 1000);
            if (outputState == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            }else if (outputState == MediaCodec.INFO_TRY_AGAIN_LATER) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else if(outputState >= 0) {
                encodeToVideoTrack(outputState);
                mEncoder.releaseOutputBuffer(outputState, false);
            }
        }
    }

//    视频合成
    private void encodeToVideoTrack(int index) {
        ByteBuffer encodedData = mEncoder.getOutputBuffer(index);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            encodedData = null;
        }
        if (encodedData != null) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            //合成MP4文件
            mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);

            //将编码数据实时传给解码器进行解码,
            //但这里出现问题，导致解码不出画面
            int offset = encodedData.position();
            int len = mBufferInfo.size;
            int size = encodedData.limit();
            byte [] data = new byte[size];
            for(int i = offset;i < size ;i++){
                data[i] = encodedData.get(i);
            }
            if(listener != null){
                listener.onEncode(data,offset,len);
            }
        }
    }

    @Override
    public void run() {
        //实现编码
        mEncoder.start();
        try {
            //利用MediaMuxer将视频流合成为MP4文件
            mMuxer = new MediaMuxer(mSavePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            recordVirtualDisplay();
        }catch (IOException e){
            e.printStackTrace();
        } finally{
            release();
        }
    }


    private void resetOutputFormat() {
        if (mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        MediaFormat newFormat = mEncoder.getOutputFormat();
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        mMuxer.start();
        mMuxerStarted = true;
    }

    private void release() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mProjection != null) {
            mProjection.stop();
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }

    }

    public String getSavePath(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "手机录屏.mp4";

        return path;
    }

    public interface EncodeListener{
        void onEncode(byte[] data, int offset, int length);
    }

    public void setEncodeListener(EncodeListener listener){
        this.listener = listener;
    }


}
