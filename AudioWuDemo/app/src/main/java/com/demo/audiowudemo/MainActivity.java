package com.demo.audiowudemo;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.audiowudemo.util.AACToPCM;
import com.demo.audiowudemo.util.PCMToAAC;
import com.demo.audiowudemo.util.PcmMixer;
import com.demo.audiowudemo.util.PcmToWavUtil;
import com.demo.audiowudemo.util.PermissionsChecker;
import com.demo.audiowudemo.util.PlayPcmUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private PermissionsChecker mPermissionsChecker;

    Button btnPcm, btnAac, btnPcmToAac, btnAacToPcm, btnPcmToWav,
            btnPlayPcm, btnVoiceMix, btnVoiceMix1, btnVoiceMix16, btnMp3ToAac;

    //定位权限,获取app内常用权限
    String[] permsLocation = {"android.permission.READ_PHONE_STATE"
            , "android.permission.ACCESS_COARSE_LOCATION"
            , "android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.READ_EXTERNAL_STORAGE"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.CAMERA"
            , "android.permission.RECORD_AUDIO"};
    private String filePathPcm = Environment.getExternalStorageDirectory() + "/" + "voiceMixOne.pcm";
    private String filePathAac = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.aac";
    private String filePathWav = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.wav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        btnVoiceMix = findViewById(R.id.btnVoiceMix);
        btnVoiceMix1 = findViewById(R.id.btnVoiceMix1);
        btnVoiceMix16 = findViewById(R.id.btnVoiceMix16);
        btnMp3ToAac = findViewById(R.id.btnMp3ToAac);

//        startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));

        //判断是否获取MANAGE_EXTERNAL_STORAGE权限：
//        boolean isHasStoragePermission= Environment.isExternalStorageManager();

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
        //音频混音，音频长度参数要一样
        btnVoiceMix1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String audiopath1 = Environment.getExternalStorageDirectory() + "/" + "111.pcm";
                String audiopath2 = Environment.getExternalStorageDirectory() + "/" + "222.pcm";
                String audioout = Environment.getExternalStorageDirectory() + "/" + "合并之后.wav";
                PcmMixer.startMix(audiopath1, audiopath2, audioout);
            }
        });
        //音频混音mp4提取音频混音 44.1k
        btnVoiceMix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VoiceMix441Activity.class));
            }
        });
        //音频混音mp4提取音频混音 16k
        btnVoiceMix16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VoiceMix16Activity.class));
            }
        });
        //mp3转aac
        btnMp3ToAac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Mp3ToAacActivity.class));
            }
        });

    }
}
