package com.demo.lupingwudemo.audiovideothree;

import android.annotation.SuppressLint;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * wuqingsen on 2020-06-03
 * Mailbox:1243411677@qq.com
 * annotation:
 */

@SuppressLint("NewApi")
public class EncoderThread extends Thread {

    private MediaProjection mProjection; //帧数据，即编码的数据源
    public static MediaCodec mEncoder;  //编码器
    //    private MediaMuxer mMuxer;   //将音视频数据合成多媒体文件
    public static MediaCodec.BufferInfo mBufferInfo;
    private Surface mSurface;  //虚拟屏幕VirturalDiaplay的输出目的地
    private VirtualDisplay mVirtualDisplay;   //虚拟屏幕

    private int mWidth = 480;
    private int mHeight = 720;
    private int mDpi = 240;
    private int mBitRate = 5 * 1024 * 1024;   //编码位率，清晰度
    private int mFrameRate = 30;    //帧率，流畅度
    private int mIFrameInterval = 5; //帧与帧间的间隙

    private AtomicBoolean mQuit = new AtomicBoolean(false);

//    private String mSavePath;

    private EncodeListener listener;


    public static final String MINE_TYPE = "video/avc";
    public static final String TAG = "screen record";

    public EncoderThread(MediaProjection mediaProjection) {
        this.mProjection = mediaProjection;
        try {
            mEncoder = MediaCodec.createEncoderByType(MINE_TYPE);
            initEncoder();
            mSurface = mEncoder.createInputSurface();
            createVirturalDisplay();
//            mSavePath = getSavePath();
            mBufferInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initEncoder() {
        MediaFormat format = MediaFormat.createVideoFormat(MINE_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mIFrameInterval);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }


    public final void quit() {
        mQuit.set(true);
    }

    //传入MediaCodec生成的Surface,利用MediaCodec编码surface中的数据
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void createVirturalDisplay() {
        if (mProjection != null) {
            //这里有个坑：flags设DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mSurface这个才行，否则生成的文件无法播放
            mVirtualDisplay = mProjection.createVirtualDisplay(TAG, mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);
        }
    }

//    private void recordVirtualDisplay() {
//        while (!mQuit.get()) {
//            //获取MediaCodec通过h264编码之后的数据
//            MediaMuxerThread.addVideoFrameData(1);
//        }
//    }

    @Override
    public void run() {
        //实现编码
        mEncoder.start();
//        recordVirtualDisplay();
    }

//    public String getSavePath() {
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "手机录屏.mp4";
//        File file = new File(path);
//        try {
//            file.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return path;
//    }


    public interface EncodeListener {
        void onEncode(byte[] data, int offset, int length);
    }

    public void setEncodeListener(EncodeListener listener) {
        this.listener = listener;
    }


}
