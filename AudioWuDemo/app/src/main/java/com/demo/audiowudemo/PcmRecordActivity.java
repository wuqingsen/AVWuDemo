package com.demo.audiowudemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * wuqingsen on 2020-05-28
 * Mailbox:1243411677@qq.com
 * annotation:Pcm音频录制
 */
public class PcmRecordActivity extends AppCompatActivity {
    private boolean isRecord = false;//是否在录制，默认没在录制
    private AudioRecordThread audioRecordThread;
    private Button btnPcm;
    private String filePath = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.pcm";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcm_record);
        btnPcm = findViewById(R.id.btnPcm);

        btnPcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecord){
                    //停止录制
                    isRecord = false;
                    btnPcm.setText("开始");
                    stopRecord();
                }else {
                    isRecord = true;
                    btnPcm.setText("停止");
                    //开始录制
                    startRecord();
                }
            }
        });
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

    private class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;
        private int bufferSize;

        AudioRecordThread() {

            /**
             * 1.设置缓冲区大小
             * 参数:采样率 16k; 通道数 单通道; 采样位数
             */
            bufferSize = AudioRecord.getMinBufferSize(16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT * 1);

            /**
             * 2.初始化AudioRecord
             * 参数:录音来源 麦克风; 采样率 16k; 通道数 单通道 ;采样位数/数据格式 pcm; 缓冲区大小
             */
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }

        @Override
        public void run() {
            super.run();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath);
                audioRecord.startRecording();
                byte[] byteBuffer = new byte[bufferSize];

                while (isRecord) {
                    //3.不断读取录音数据并保存至文件中
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                    fos.write(byteBuffer, 0, end);
                    fos.flush();
                }
                //4.当执行stop()方法后state != RecordState.RECORDING，终止循环，停止录音
                audioRecord.stop();
            } catch (Exception e) {

            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {

                }
            }
        }
    }
}
