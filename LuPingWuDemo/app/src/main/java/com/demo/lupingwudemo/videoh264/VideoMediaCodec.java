package com.demo.lupingwudemo.videoh264;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * wuqingsen on 2020-06-15
 * Mailbox:1243411677@qq.com
 * annotation:
 */

@SuppressLint("NewApi")
public class VideoMediaCodec extends MediaCodecBase {
    private Surface mSurface;
    private int TIMEOUT_USEC = 12000;
    public byte[] configbyte;

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/吴庆森录屏H264.h264";
    private BufferedOutputStream outputStream;

    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VideoMediaCodec() {
        createfile();
        prepare();
    }

    public Surface getSurface() {
        return mSurface;
    }

    public void isRun(boolean isR) {
        this.isRun = isR;
    }


    @Override
    public void prepare() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 480, 720);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 5 * 1024 * 1024);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mEncoder = MediaCodec.createEncoderByType("video/avc");
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mEncoder.createInputSurface();
            mEncoder.start();
        } catch (IOException e) {

        }
    }

    @Override
    public void release() {
        this.isRun = false;

    }


    /**
     * 获取h264数据
     **/
    public void getBuffer() {
        try {
            while (isRun) {
                if (mEncoder == null)
                    break;

                MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = mEncoder.getOutputBuffers()[outputBufferIndex];
                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);
                    if (mBufferInfo.flags == 2) {
                        configbyte = new byte[mBufferInfo.size];
                        configbyte = outData;
                    }
//                    else{
//                        MainActivity.putData(outData,2,mBufferInfo.presentationTimeUs*1000L);
//                    }

                    else if (mBufferInfo.flags == 1) {
                        byte[] keyframe = new byte[mBufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                        if(outputStream != null){
                            outputStream.write(keyframe, 0, keyframe.length);
                        }
                    } else {
                        if(outputStream != null){
                            outputStream.write(outData, 0, outData.length);
                        }
                    }
                    mEncoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                }
            }
        } catch (Exception e) {

        }
        try {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
