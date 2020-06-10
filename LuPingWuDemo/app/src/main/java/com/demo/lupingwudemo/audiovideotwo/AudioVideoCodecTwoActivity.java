package com.demo.lupingwudemo.audiovideotwo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.lupingwudemo.R;

/**
 * wuqingsen on 2020-06-09
 * Mailbox:1243411677@qq.com
 * annotation:录音录屏封装
 */
@SuppressLint("NewApi")
public class AudioVideoCodecTwoActivity extends AppCompatActivity {
    private Button mBtnStart;
    private Button mBtnStop;
    private TextView tv_result;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 15;
    private RecordScreenUtils recordScreenUtils;
    private String mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/吴庆森录屏2.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiovideo_codec_two);

        mBtnStart = (Button) findViewById(R.id.btn_start_record);
        mBtnStop = (Button) findViewById(R.id.btn_stop_record);
        tv_result = findViewById(R.id.tv_result);

        //开始
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_result.setText("录制中...");
                tv_result.setVisibility(View.VISIBLE);
                startRecord();
            }
        });
        //结束
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_result.setText("录制完成,路径为:" + mSavePath);
                recordScreenUtils.stopRecord();
            }
        });
    }

    private void startRecord() {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, SCREEN_CAPTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SCREEN_CAPTURE_REQUEST_CODE) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        if (mediaProjection == null) {
            return;
        }

        //开始屏幕录制
        recordScreenUtils = new RecordScreenUtils(mediaProjection);
        recordScreenUtils.config();
        recordScreenUtils.startRecord();
    }

}
