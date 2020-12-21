package com.demo.audiowudemo;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.audiowudemo.util.Mp3ToAacUtil;

/**
 * wuqingsen on 2020/12/21
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class Mp3ToAacActivity extends AppCompatActivity {

    Button btnStart;
    private String filePath1 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.mp3";
    private String filePath2 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.aac";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3toaac);

        btnStart = findViewById(R.id.btnStart);
        final Mp3ToAacUtil mp3ToAacUtil = new Mp3ToAacUtil(filePath1, filePath2);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp3ToAacUtil.start();
            }
        });
        mp3ToAacUtil.setListener(new Mp3ToAacUtil.OnProgressListener() {
            @Override
            public void onStart() {
                Log.w("wqs", "onStart: " );
            }

            @Override
            public void onProgress(int max, int progress) {
                Log.w("wqs", "onProgress: "+progress );
            }

            @Override
            public void onSuccess() {
                Log.w("wqs", "onSuccess: " );
            }

            @Override
            public void onFail() {
                Log.w("wqs", "onFail: " );
            }
        });
    }
}
