package com.demo.camerawu.activity;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.camerawu.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * wuqingsen on 2020-06-12
 * Mailbox:1243411677@qq.com
 * annotation:从MP4文件中提取视频并生成新的视频文件
 */
@SuppressLint("NewApi")
public class ExtractorMuxerActivity extends AppCompatActivity {

    //原视频路径
    private String filePath = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.mp4";
    //将视频分离的h264路径
    private String ExtractorVideo = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemoExtractorVideo.h264";
    //将视频分离的aac路径
    private String ExtractorAudio = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemoExtractorAudio.aac";
    //新视频路径
    private String filePathNew = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemoNew.mp4";
    private MediaExtractor mMediaExtractor;//分离音视频
    private MediaMuxer mMediaMuxer;//合成音视频

    Button btnMessage, btnStart, btnStartExtractor;
    TextView tv_content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor_muxer);

        btnStart = findViewById(R.id.btnStart);
        btnMessage = findViewById(R.id.btnMessage);
        tv_content = findViewById(R.id.tv_content);
        btnStartExtractor = findViewById(R.id.btnStartExtractor);

        //获取视频详细信息
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getMp4Data();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //分离视频为h264和aac
        btnStartExtractor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mediaExtractorMp4();
                    }
                }).start();
            }
        });

        //开始分离视频合成新视频
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mediaExtractorMuxerMp4();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    //获取视频详细信息
    private void getMp4Data() throws IOException {
        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(filePath);
        tv_content.append("通道数:" + mMediaExtractor.getTrackCount());
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
            if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                tv_content.append("\n视频:" + mediaFormat);
            } else {
                tv_content.append("\n音频:" + mediaFormat);
            }
        }
    }

    //分离视频合成新视频
    private boolean mediaExtractorMuxerMp4() throws IOException {
        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(filePath);

        int mVideoTrackIndex = -1;
        int framerate = 0;
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat format = mMediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (!mime.startsWith("video/")) {
                continue;
            }
            framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            mMediaExtractor.selectTrack(i);
            mMediaMuxer = new MediaMuxer(filePathNew, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVideoTrackIndex = mMediaMuxer.addTrack(format);
            mMediaMuxer.start();
        }

        if (mMediaMuxer == null) {
            return false;
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        info.presentationTimeUs = 0;
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
        int sampleSize = 0;
        while ((sampleSize = mMediaExtractor.readSampleData(buffer, 0)) > 0) {

            info.offset = 0;
            info.size = sampleSize;
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            info.presentationTimeUs += 1000 * 1000 / framerate;
            mMediaMuxer.writeSampleData(mVideoTrackIndex, buffer, info);
            mMediaExtractor.advance();
        }

        mMediaExtractor.release();

        mMediaMuxer.stop();
        mMediaMuxer.release();

        return true;
    }

    //将一个视频文件分离视频与音频
    private void mediaExtractorMp4() {
        File mFile = new File(filePath);
        MediaExtractor extractor = new MediaExtractor();//实例一个MediaExtractor
        try {
            extractor.setDataSource(mFile.getAbsolutePath());//设置添加MP4文件路径
        } catch (IOException e) {
            e.printStackTrace();
        }
        int trackCount = extractor.getTrackCount();//获得通道数量
        int videoTrackIndex = 0;//视频轨道索引
        MediaFormat videoMediaFormat = null;//视频格式
        int audioTrackIndex = 0;//音频轨道索引
        MediaFormat audioMediaFormat = null;

        /**
         * 查找需要的视频轨道与音频轨道index
         */
        for (int i = 0; i < trackCount; i++) { //遍历所以轨道
            MediaFormat itemMediaFormat = extractor.getTrackFormat(i);
            String itemMime = itemMediaFormat.getString(MediaFormat.KEY_MIME);
            if (itemMime.startsWith("video")) { //获取视频轨道位置
                videoTrackIndex = i;
                videoMediaFormat = itemMediaFormat;
                continue;
            }
            if (itemMime.startsWith("audio")) { //获取音频轨道位置
                audioTrackIndex = i;
                audioMediaFormat = itemMediaFormat;
                continue;
            }
        }

        File videoFile = new File(ExtractorVideo);
        File audioFile = new File(ExtractorAudio);
        if (videoFile.exists()) {
            videoFile.delete();
        }
        if (audioFile.exists()) {
            audioFile.delete();
        }

        try {
            FileOutputStream videoOutputStream = new FileOutputStream(videoFile);
            FileOutputStream audioOutputStream = new FileOutputStream(audioFile);

            /**
             * 分离视频
             */
            int maxVideoBufferCount = videoMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);//获取视频的输出缓存的最大大小
            ByteBuffer videoByteBuffer = ByteBuffer.allocate(maxVideoBufferCount);
            extractor.selectTrack(videoTrackIndex);//选择到视频轨道
            int len = 0;
            while ((len = extractor.readSampleData(videoByteBuffer, 0)) != -1) {
                byte[] bytes = new byte[len];
                videoByteBuffer.get(bytes);//获取字节
                videoOutputStream.write(bytes);//写入字节
                videoByteBuffer.clear();
                extractor.advance();//预先加载后面的数据
            }
            videoOutputStream.flush();
            videoOutputStream.close();
            extractor.unselectTrack(videoTrackIndex);//取消选择视频轨道

            /**
             * 分离音频
             */
            int maxAudioBufferCount = audioMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);//获取音频的输出缓存的最大大小
            ByteBuffer audioByteBuffer = ByteBuffer.allocate(maxAudioBufferCount);
            extractor.selectTrack(audioTrackIndex);//选择音频轨道
            len = 0;
            while ((len = extractor.readSampleData(audioByteBuffer, 0)) != -1) {
                byte[] bytes = new byte[len];
                audioByteBuffer.get(bytes);


                /**
                 * 添加adts头
                 */
                byte[] adtsData = new byte[len + 7];
                addADTStoPacket(adtsData, len + 7);
                System.arraycopy(bytes, 0, adtsData, 7, len);

                audioOutputStream.write(bytes);
                audioByteBuffer.clear();
                extractor.advance();
            }

            audioOutputStream.flush();
            audioOutputStream.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        extractor.release();//释放资源
    }

    /**
     * 合成视频1的音频和视频2的图像
     *
     * @param audioVideoPath  提供音频的视频
     * @param audioStartTime  音频的开始时间
     * @param frameVideoPath  提供图像的视频
     * @param combinedVideoOutFile  合成后的文件
     */
    public static void combineTwoVideos(String audioVideoPath,
                                        long audioStartTime,
                                        String frameVideoPath,
                                        File combinedVideoOutFile) {
        MediaExtractor audioVideoExtractor = new MediaExtractor();
        int mainAudioExtractorTrackIndex = -1; //提供音频的视频的音频轨（有点拗口）
        int mainAudioMuxerTrackIndex = -1; //合成后的视频的音频轨
        int mainAudioMaxInputSize = 0; //能获取的音频的最大值

        MediaExtractor frameVideoExtractor = new MediaExtractor();
        int frameExtractorTrackIndex = -1; //视频轨
        int frameMuxerTrackIndex = -1; //合成后的视频的视频轨
        int frameMaxInputSize = 0; //能获取的视频的最大值
        int frameRate = 0; //视频的帧率
        long frameDuration = 0;

        MediaMuxer muxer = null; //用于合成音频与视频

        try {
            muxer = new MediaMuxer(combinedVideoOutFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            audioVideoExtractor.setDataSource(audioVideoPath); //设置视频源
            //音轨信息
            int audioTrackCount = audioVideoExtractor.getTrackCount(); //获取数据源的轨道数
            //在此循环轨道数，目的是找到我们想要的音频轨
            for (int i = 0; i < audioTrackCount; i++) {
                MediaFormat format = audioVideoExtractor.getTrackFormat(i); //得到指定索引的记录格式
                String mimeType = format.getString(MediaFormat.KEY_MIME); //主要描述mime类型的媒体格式
                if (mimeType.startsWith("audio/")) { //找到音轨
                    mainAudioExtractorTrackIndex = i;
                    mainAudioMuxerTrackIndex = muxer.addTrack(format); //将音轨添加到MediaMuxer，并返回新的轨道
                    mainAudioMaxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE); //得到能获取的有关音频的最大值
//                    mainAudioDuration = format.getLong(MediaFormat.KEY_DURATION);
                }
            }

            //图像信息
            frameVideoExtractor.setDataSource(frameVideoPath); //设置视频源
            int trackCount = frameVideoExtractor.getTrackCount(); //获取数据源的轨道数
            //在此循环轨道数，目的是找到我们想要的视频轨
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = frameVideoExtractor.getTrackFormat(i); //得到指定索引的媒体格式
                String mimeType = format.getString(MediaFormat.KEY_MIME); //主要描述mime类型的媒体格式
                if (mimeType.startsWith("video/")) { //找到视频轨
                    frameExtractorTrackIndex = i;
                    frameMuxerTrackIndex = muxer.addTrack(format); //将视频轨添加到MediaMuxer，并返回新的轨道
                    frameMaxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE); //得到能获取的有关视频的最大值
                    frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE); //获取视频的帧率
                    frameDuration = format.getLong(MediaFormat.KEY_DURATION); //获取视频时长
                }
            }

            muxer.start(); //开始合成

            audioVideoExtractor.selectTrack(mainAudioExtractorTrackIndex); //将提供音频的视频选择到音轨上
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer audioByteBuffer = ByteBuffer.allocate(mainAudioMaxInputSize);
            while (true) {
                int readSampleSize = audioVideoExtractor.readSampleData(audioByteBuffer, 0); //检索当前编码的样本并将其存储在字节缓冲区中
                if (readSampleSize < 0) { //如果没有可获取的样本则退出循环
                    audioVideoExtractor.unselectTrack(mainAudioExtractorTrackIndex);
                    break;
                }

                long sampleTime = audioVideoExtractor.getSampleTime(); //获取当前展示样本的时间（单位毫秒）

                if (sampleTime < audioStartTime) { //如果样本时间小于我们想要的开始时间就快进
                    audioVideoExtractor.advance(); //推进到下一个样本，类似快进
                    continue;
                }

                if (sampleTime > audioStartTime + frameDuration) { //如果样本时间大于开始时间+视频时长，就退出循环
                    break;
                }
                //设置样本编码信息
                audioBufferInfo.size = readSampleSize;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = audioVideoExtractor.getSampleFlags();
                audioBufferInfo.presentationTimeUs = sampleTime - audioStartTime;

                muxer.writeSampleData(mainAudioMuxerTrackIndex, audioByteBuffer, audioBufferInfo); //将样本写入
                audioVideoExtractor.advance(); //推进到下一个样本，类似快进
            }

            frameVideoExtractor.selectTrack(frameExtractorTrackIndex); //将提供视频图像的视频选择到视频轨上
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer videoByteBuffer = ByteBuffer.allocate(frameMaxInputSize);
            while (true) {
                int readSampleSize = frameVideoExtractor.readSampleData(videoByteBuffer, 0); //检索当前编码的样本并将其存储在字节缓冲区中
                if (readSampleSize < 0) { //如果没有可获取的样本则退出循环
                    frameVideoExtractor.unselectTrack(frameExtractorTrackIndex);
                    break;
                }
                //设置样本编码信息
                videoBufferInfo.size = readSampleSize;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = frameVideoExtractor.getSampleFlags();
                videoBufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;

                muxer.writeSampleData(frameMuxerTrackIndex, videoByteBuffer, videoBufferInfo); //将样本写入
                frameVideoExtractor.advance(); //推进到下一个样本，类似快进
            }
        } catch (IOException e) {
        } finally {
            //释放资源
            audioVideoExtractor.release();
            frameVideoExtractor.release();
            if (muxer != null) {
                muxer.release();
            }
        }
    }

    private static void addADTStoPacket(byte[] packet, int packetLen) {
        /*
        标识使用AAC级别 当前选择的是LC
        一共有1: AAC Main 2:AAC LC (Low Complexity) 3:AAC SSR (Scalable Sample Rate) 4:AAC LTP (Long Term Prediction)
        */
        int profile = 2;
        int frequencyIndex = 0x04; //设置采样率
        int channelConfiguration = 2; //设置频道,其实就是声道

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (frequencyIndex << 2) + (channelConfiguration >> 2));
        packet[3] = (byte) (((channelConfiguration & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
