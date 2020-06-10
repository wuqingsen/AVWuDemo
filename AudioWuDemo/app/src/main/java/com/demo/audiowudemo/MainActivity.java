package com.demo.audiowudemo;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.audiowudemo.ceshi.AudioEncoder;
import com.demo.audiowudemo.ceshi.MultiAudioMixer;
import com.demo.audiowudemo.ceshi.PCMAnalyser;
import com.demo.audiowudemo.util.AACToPCM;
import com.demo.audiowudemo.util.PCMToAAC;
import com.demo.audiowudemo.util.PcmAndPcm;
import com.demo.audiowudemo.util.PcmToWavUtil;
import com.demo.audiowudemo.util.PermissionsChecker;
import com.demo.audiowudemo.util.PlayPcmUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private PermissionsChecker mPermissionsChecker;

    Button btnPcm, btnAac, btnPcmToAac, btnAacToPcm, btnPcmToWav, btnPlayPcm;

    //定位权限,获取app内常用权限
    String[] permsLocation = {"android.permission.READ_PHONE_STATE"
            , "android.permission.ACCESS_COARSE_LOCATION"
            , "android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.READ_EXTERNAL_STORAGE"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.CAMERA"
            , "android.permission.RECORD_AUDIO"};
    private String filePathPcm = Environment.getExternalStorageDirectory() + "/" + "1234.pcm";
    private String filePathAac = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.aac";
    private String filePathWav = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.wav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        PcmAndPcm.meargeAudio();
        new ExpertThread().start();

        mPermissionsChecker = new PermissionsChecker(MainActivity.this);
        if (mPermissionsChecker.lacksPermissions(permsLocation)) {
            ActivityCompat.requestPermissions(MainActivity.this, permsLocation, 1);
        }
        btnPcm = findViewById(R.id.btnPcm);
        btnAac = findViewById(R.id.btnAac);
        btnPcmToAac = findViewById(R.id.btnPcmToAac);
        btnAacToPcm = findViewById(R.id.btnAacToPcm);
        btnPcmToWav = findViewById(R.id.btnPcmToWav);
        btnPlayPcm = findViewById(R.id.btnPlayPcm);

        //pcm录制
        btnPcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PcmRecordActivity.class));
            }
        });
        //aac录制
        btnAac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AacRecordActivity.class));
            }
        });
        //pcm转aac
        btnPcmToAac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PCMToAAC pcmToAAC = new PCMToAAC(filePathAac, filePathPcm);
                pcmToAAC.readInputStream(new File(filePathPcm));
            }
        });
        //aac转pcm
        btnAacToPcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AACToPCM aacToPCM = new AACToPCM(filePathAac, filePathPcm);
                aacToPCM.decode();
            }
        });
        //pcm转wav
        btnPcmToWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(16000,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT * 1);
                pcmToWavUtil.pcmToWav(filePathPcm, filePathWav);
            }
        });
        //播放pcm音频流
        btnPlayPcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayPcmUtils playPcmUtils = new PlayPcmUtils(filePathPcm);
                playPcmUtils.playPcm();
            }
        });
    }

    private class ExpertThread extends Thread {
        private MultiAudioMixer audioMixer = MultiAudioMixer.createAudioMixer();
        private PCMAnalyser recordPcmAudioFile = PCMAnalyser.createPCMAnalyser();
        @Override
        public void run() {

            String pcm1 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.pcm";
            String pcm2 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo1.pcm";
            File[] audioFiles = new File[]{new File(pcm1), new File(pcm2)};
//            for (int i = 0, size = audioFiles.length; i != size; i++) {
//                if (i == 0){
//                    audioFiles[i] = new File(pcm1);
//                }else{
//                    audioFiles[1] = new File(pcm2);
//                }
//
//            }


            try {
                String filePath1 = Environment.getExternalStorageDirectory() + "/" + "1234.pcm";
                File tempMixAudioFile = new File(filePath1);
                final FileOutputStream mixTempOutStream = new FileOutputStream(tempMixAudioFile);
                audioMixer.setOnAudioMixListener(new MultiAudioMixer.OnAudioMixListener() {

                    @Override
                    public void onMixing(byte[] mixBytes) throws IOException {
                        mixTempOutStream.write(mixBytes);
                    }

                    @Override
                    public void onMixError(int errorCode) {

                    }

                    @Override
                    public void onMixComplete() {

                    }
                });
                audioMixer.mixAudios(audioFiles, recordPcmAudioFile.bytesPerSample());
                mixTempOutStream.close();
                 String filePath = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo1.mp3";
                File outputFile = new File(filePath);
                int channelCount = audioFiles.length;
                AudioEncoder accEncoder = AudioEncoder.createAccEncoder(tempMixAudioFile, channelCount);
                accEncoder.encodeToFile(outputFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
