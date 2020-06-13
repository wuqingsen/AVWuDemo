package com.demo.audiowudemo;

import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.audiowudemo.mixutils.AudioDecoder;
import com.demo.audiowudemo.mixutils.AudioEncoder;
import com.demo.audiowudemo.mixutils.MultiAudioMixer;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * wuqingsen on 2020-06-11
 * Mailbox:1243411677@qq.com
 * annotation:音频混音
 */
public class VoiceMixActivity extends AppCompatActivity {
    Button btnMP41, btnMP42, btnMIX, btnMP41Stop, btnMP42Stop, btnDecode;
    TextView tv_voice1, tv_voice2, tv_mix;
    private MediaRecorder mRecorder;
    private String filePathone = Environment.getExternalStorageDirectory() + "/" + "voiceMixOne.mp4";
    private String filePathtwo = Environment.getExternalStorageDirectory() + "/" + "voiceMixTwo.mp4";

    private String filePathonePcm = Environment.getExternalStorageDirectory() + "/" + "voiceMixOne.pcm";
    private String filePathtwoPcm = Environment.getExternalStorageDirectory() + "/" + "voiceMixTwo.pcm";

    //临时数据
    private String filePathLSPcm = Environment.getExternalStorageDirectory() + "/" + "voiceMixLS.pcm";
    //合成路径
    private String filePathMIX = Environment.getExternalStorageDirectory() + "/" + "voiceMixTwo.aac";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_mix);

        btnMP41 = findViewById(R.id.btnMP41);
        btnMP42 = findViewById(R.id.btnMP42);
        btnMIX = findViewById(R.id.btnMIX);
        btnMP41Stop = findViewById(R.id.btnMP41Stop);
        btnMP42Stop = findViewById(R.id.btnMP42Stop);
        tv_voice1 = findViewById(R.id.tv_voice1);
        tv_voice2 = findViewById(R.id.tv_voice2);
        tv_mix = findViewById(R.id.tv_mix);
        btnDecode = findViewById(R.id.btnDecode);

        //录制第一段音频
        btnMP41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord(filePathone);
            }
        });
        //停止录制第一段音频
        btnMP41Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
                tv_voice1.setText("路径:" + filePathone);
            }
        });

        //录制第二段音频
        btnMP42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord(filePathtwo);
            }
        });
        //停止录制第二段音频
        btnMP42Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
                tv_voice2.setText("路径:" + filePathtwo);
            }
        });

        //解码音频
        btnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List list = new ArrayList();
                list.add(filePathone);
                list.add(filePathtwo);
                for (int i = 0; i < list.size(); i++) {
                    new DecodeTask(list.get(i).toString(), i).execute();
                }
            }
        });

        //混音
        btnMIX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //有几个音频文件传数字几,这里写死,只做演示
                new MixTask(2).execute();
            }
        });
    }

    //开始录制
    private void startRecord(String filePath) {
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setAudioChannels(2);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setOutputFile(filePath);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }

    //停止
    private void stopRecord() {
        mRecorder.stop();
        mRecorder = null;
    }


    /**
     * 解码
     */
    class DecodeTask extends AsyncTask<Void, Double, Boolean> {

        private String fileUrl;
        private int position;

        DecodeTask(String url, int p) {
            fileUrl = url;
            position = p;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //解码
                AudioDecoder audioDec = AudioDecoder
                        .createDefualtDecoder(fileUrl);
                if (position == 0) {
                    fileUrl = filePathonePcm;
                    audioDec.decodeToFile(filePathonePcm);
                }
                if (position == 1) {
                    fileUrl = filePathtwoPcm;
                    audioDec.decodeToFile(filePathtwoPcm);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }


    /**
     * 混音
     */
    class MixTask extends AsyncTask<Void, Double, Boolean> {

        private int size;

        MixTask(int num) {
            size = num;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String rawAudioFile = null;

            // 将需要合音的音频解码后的文件放到数组里
            File[] rawAudioFiles = new File[size];
            StringBuilder sbMix = new StringBuilder();
            int index = 0;

            List list = new ArrayList();
            list.add(filePathonePcm);
            list.add(filePathtwoPcm);
            for (int i = 0; i < list.size(); i++) {
                rawAudioFiles[index++] = new File(list.get(i).toString());
                sbMix.append(i + "");
            }

            // 最终合音的路径
            final String mixFilePath = filePathLSPcm;

            // 下面的都是合音的代码
            try {
                MultiAudioMixer audioMixer = MultiAudioMixer.createAudioMixer();

                audioMixer.setOnAudioMixListener(new MultiAudioMixer.OnAudioMixListener() {

                    FileOutputStream fosRawMixAudio = new FileOutputStream(
                            mixFilePath);

                    @Override
                    public void onMixing(byte[] mixBytes) throws IOException {
                        fosRawMixAudio.write(mixBytes);
                    }

                    @Override
                    public void onMixError(int errorCode) {
                        try {
                            if (fosRawMixAudio != null)
                                fosRawMixAudio.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMixComplete() {
                        try {
                            if (fosRawMixAudio != null)
                                fosRawMixAudio.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });
                audioMixer.mixAudios(rawAudioFiles);
                rawAudioFile = mixFilePath;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            AudioEncoder accEncoder = AudioEncoder
                    .createAccEncoder(rawAudioFile);
            String finalMixPath = filePathMIX;
            accEncoder.encodeToFile(finalMixPath);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.e("wqs", "onPostExecute: 合音成功");
            Toast.makeText(getApplicationContext(), "合音成功", Toast.LENGTH_SHORT)
                    .show();
            tv_mix.setText("路径:" + filePathMIX);
        }
    }
}
