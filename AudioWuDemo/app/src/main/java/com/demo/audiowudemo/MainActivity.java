package com.demo.audiowudemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.audiowudemo.util.PCMToAAC;
import com.demo.audiowudemo.util.PermissionsChecker;

public class MainActivity extends AppCompatActivity {
    private PermissionsChecker mPermissionsChecker;

    Button btnPcm,btnAac, btnPcmToAac;

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
                new PCMToAAC(filePathAac, filePathPcm);
            }
        });
    }
}
