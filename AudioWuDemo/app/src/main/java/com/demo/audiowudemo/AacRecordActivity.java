package com.demo.audiowudemo;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;

/**
 * wuqingsen on 2020-05-29
 * Mailbox:1243411677@qq.com
 * annotation:录制aac音频
 */
@SuppressLint("NewApi")
public class AacRecordActivity extends AppCompatActivity {
    private boolean isRecord = false;//是否在录制，默认没在录制
    private Button btnPcm;
    private String filePath = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.aac";
    private MediaCodec mediaCodec;
    private int samples_per_frame = 2048;
    private MediaCodec.BufferInfo encodeBufferInfo;
    private ByteBuffer[] encodeInputBuffers;
    private ByteBuffer[] encodeOutputBuffers;
    private byte[] chunkAudio = new byte[0];
    private BufferedOutputStream out;
    private AudioRecordThread audioRecordThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aac_record);
        btnPcm = findViewById(R.id.btnPcm);

        btnPcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecord) {
                    //停止录制
                    isRecord = false;
                    btnPcm.setText("开始");
                    stopRecord();
                } else {
                    isRecord = true;
                    btnPcm.setText("停止");
                    //开始录制
                    startRecord();
                }
            }
        });
        isFile();
    }

    private void isFile() {
        String LVideo = Environment.getExternalStorageDirectory() + "/Android/data/com.sinosoft.chinalife/files//VideoRecorder/5597d09c-3fc0-416d-8af6-626d5d9c25c7/5597d09c-3fc0-416d-8af6-626d5d9c25c7.mp4";
        String LSVideo = Environment.getExternalStorageDirectory() + "/Android/data/com.sinosoft.chinalife/files//VideoRecorder/5597d09c-3fc0-416d-8af6-626d5d9c25c7/5597d09c-3fc0-416d-8af6-626d5d9c25c71.mp4";
        File file = new File(LVideo);
        if (!file.exists()) {
            Log.w("wqs", "文件不存在;" + LVideo);
        } else {
            Log.w("wqs", "文件存在;" + LVideo);
        }
    }

    //开始录制
    public void startRecord() {
        isRecord = true;
        //1.开启录音线程并准备录音
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    //停止录制
    public void stopRecord() {
        isRecord = false;
    }

    public class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;

        AudioRecordThread() {
            /**
             * 1.设置缓冲区大小
             * 参数:采样率 16k; 通道数 单通道; 采样位数
             */
            int bufferSize = AudioRecord.getMinBufferSize(16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT * 1);
            /**
             * 2.初始化AudioRecord
             * 参数:录音来源 麦克风; 采样率 16k; 通道数 单通道 ;采样位数/数据格式 pcm; 缓冲区大小
             */
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            try {
                out = new BufferedOutputStream(new FileOutputStream(new File(filePath), false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            initAACMediaEncode();
        }

        @Override
        public void run() {
            super.run();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath);
                audioRecord.startRecording();

                while (isRecord) {
                    byte[] byteBuffer = new byte[samples_per_frame];
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);

                    if (end == android.media.AudioRecord.ERROR_BAD_VALUE || end == android.media.AudioRecord.ERROR_INVALID_OPERATION)
                        Log.e("wqs", "Read error");

                    if (audioRecord != null && end > 0) {
                        dstAudioFormatFromPCM(byteBuffer);
                    }
                }
            } catch (FileNotFoundException e) {
                Log.e("wqs+AacRecordActivity", "run: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    /**
     * 初始化AAC编码器
     */
    private void initAACMediaEncode() {
        try {
            //参数对应-> mime type、采样率、声道数
            MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 16000, 1);
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);//比特率
            encodeFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
            encodeFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, samples_per_frame);//作用于inputBuffer的大小
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mediaCodec == null) {
            Log.e("wqs", "create mediaEncode failed");
            return;
        }
        mediaCodec.start();
        encodeInputBuffers = mediaCodec.getInputBuffers();
        encodeOutputBuffers = mediaCodec.getOutputBuffers();
        encodeBufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * 编码PCM数据 得到AAC格式的音频文件
     */
    private void dstAudioFormatFromPCM(byte[] pcmData) {

        int inputIndex;
        ByteBuffer inputBuffer;
        int outputIndex;
        ByteBuffer outputBuffer;

        int outBitSize;
        int outPacketSize;
        byte[] PCMAudio;
        PCMAudio = pcmData;

        encodeInputBuffers = mediaCodec.getInputBuffers();
        encodeOutputBuffers = mediaCodec.getOutputBuffers();
        encodeBufferInfo = new MediaCodec.BufferInfo();


        inputIndex = mediaCodec.dequeueInputBuffer(0);
        inputBuffer = encodeInputBuffers[inputIndex];
        inputBuffer.clear();
        inputBuffer.limit(PCMAudio.length);
        inputBuffer.put(PCMAudio);//PCM数据填充给inputBuffer
        mediaCodec.queueInputBuffer(inputIndex, 0, PCMAudio.length, 0, 0);//通知编码器 编码


        outputIndex = mediaCodec.dequeueOutputBuffer(encodeBufferInfo, 0);
        while (outputIndex > 0) {

            outBitSize = encodeBufferInfo.size;
            outPacketSize = outBitSize + 7;//7为ADT头部的大小
            outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
            outputBuffer.position(encodeBufferInfo.offset);
            outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
            chunkAudio = new byte[outPacketSize];
            addADTStoPacket(chunkAudio, outPacketSize);//添加ADTS
            outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中

            try {
                //录制aac音频文件，保存在手机内存中
                out.write(chunkAudio, 0, chunkAudio.length);
                out.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            outputBuffer.position(encodeBufferInfo.offset);
            mediaCodec.releaseOutputBuffer(outputIndex, false);
            outputIndex = mediaCodec.dequeueOutputBuffer(encodeBufferInfo, 0);

        }

    }


    /**
     * 添加ADTS头
     *
     * @param packet
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 8; // 16KHz
        int chanCfg = 1; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;

    }

}
