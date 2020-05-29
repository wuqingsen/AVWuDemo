package com.demo.audiowudemo.util;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
/**
 * wuqingsen on 2020-05-28
 * Mailbox:1243411677@qq.com
 * annotation:aac转pcm
 */
@SuppressLint("NewApi")
public class AACToPCM {
    //用于分离出音频轨道
    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaDecode;
    private File targetFile;
    //类型
    private String mime = "audio/mp4a-latm";
    //输入缓存组
    private ByteBuffer[] inputBuffers;
    //输出缓存组
    private ByteBuffer[] outputBuffers;
    private MediaCodec.BufferInfo bufferInfo;
    private File pcmFile;
    private FileOutputStream fileOutputStream;
    private int totalSize = 0;

    public AACToPCM(String aacPath, String pcmPath)
    {
        targetFile = new File(aacPath);
        pcmFile = new File(pcmPath);
        if (!pcmFile.exists()) {
            try {
                pcmFile.getParentFile().mkdirs();
                pcmFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            fileOutputStream = new FileOutputStream(pcmFile.getAbsoluteFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaExtractor = new MediaExtractor();
        try {
            //设置资源
            mMediaExtractor.setDataSource(targetFile.getAbsolutePath());
            //获取含有音频的MediaFormat
            MediaFormat mediaFormat = createMediaFormat();
            mMediaDecode = MediaCodec.createDecoderByType(mime);
            mMediaDecode.configure(mediaFormat, null, null, 0);//当解压的时候最后一个参数为0
            mMediaDecode.start();//开始，进入runnable状态
            //只有MediaCodec进入到Runnable状态后，才能过去缓存组
            inputBuffers = mMediaDecode.getInputBuffers();
            outputBuffers = mMediaDecode.getOutputBuffers();
            bufferInfo = new MediaCodec.BufferInfo();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private MediaFormat createMediaFormat() {
        //获取文件的轨道数，做循环得到含有音频的mediaFormat
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
            //MediaFormat键值对应
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.contains("audio/")) {
                mMediaExtractor.selectTrack(i);
                return mediaFormat;
            }
        }
        return null;
    }


    public void decode() {
        boolean inputSawEos = false;
        boolean outputSawEos = false;
        long kTimes = 5000;//循环时间
        while (!outputSawEos) {
            if (!inputSawEos) {
                //每5000毫秒查询一次
                int inputBufferIndex = mMediaDecode.dequeueInputBuffer(kTimes);
                //输入缓存index可用
                if (inputBufferIndex >= 0) {
                    //获取可用的输入缓存
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    //从MediaExtractor读取数据到输入缓存中，返回读取长度
                    int bufferSize = mMediaExtractor.readSampleData(inputBuffer, 0);
                    if (bufferSize <= 0) {//已经读取完
                        //标志输入完毕
                        inputSawEos = true;
                        //做标识
                        mMediaDecode.queueInputBuffer(inputBufferIndex, 0, 0, kTimes, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        long time = mMediaExtractor.getSampleTime();
                        //将输入缓存放入MediaCodec中
                        mMediaDecode.queueInputBuffer(inputBufferIndex, 0, bufferSize, time, 0);
                        //指向下一帧
                        mMediaExtractor.advance();
                    }
                }
            }
            //获取输出缓存，需要传入MediaCodec.BufferInfo 用于存储ByteBuffer信息
            int outputBufferIndex = mMediaDecode.dequeueOutputBuffer(bufferInfo, kTimes);
            if (outputBufferIndex >= 0) {
                int id = outputBufferIndex;
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mMediaDecode.releaseOutputBuffer(id, false);
                    continue;
                }
                //有输出数据
                if (bufferInfo.size > 0) {
                    //获取输出缓存
                    ByteBuffer outputBuffer = outputBuffers[id];
                    //设置ByteBuffer的position位置
                    outputBuffer.position(bufferInfo.offset);
                    //设置ByteBuffer访问的结点
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    byte[] targetData = new byte[bufferInfo.size];
                    //将数据填充到数组中
                    outputBuffer.get(targetData);
                    try {
                        fileOutputStream.write(targetData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //释放输出缓存
                mMediaDecode.releaseOutputBuffer(id, false);
                //判断缓存是否完结
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputSawEos = true;
                }
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaDecode.getOutputBuffers();

            }else if(outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat  mediaFormat = mMediaDecode.getOutputFormat();
            }
        }
        //释放资源
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
            mMediaDecode.stop();
            mMediaDecode.release();
            mMediaExtractor.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
