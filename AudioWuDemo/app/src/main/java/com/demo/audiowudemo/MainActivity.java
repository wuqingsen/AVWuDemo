package com.demo.audiowudemo;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.audiowudemo.util.AACToPCM;
import com.demo.audiowudemo.util.PCMToAAC;
import com.demo.audiowudemo.util.PcmToWavUtil;
import com.demo.audiowudemo.util.PermissionsChecker;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private PermissionsChecker mPermissionsChecker;

    Button btnPcm, btnAac, btnPcmToAac, btnAacToPcm, btnPcmToWav;

    //定位权限,获取app内常用权限
    String[] permsLocation = {"android.permission.READ_PHONE_STATE"
            , "android.permission.ACCESS_COARSE_LOCATION"
            , "android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.READ_EXTERNAL_STORAGE"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.CAMERA"
            , "android.permission.RECORD_AUDIO"};
    private String filePathPcm = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.pcm";
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
    }
}