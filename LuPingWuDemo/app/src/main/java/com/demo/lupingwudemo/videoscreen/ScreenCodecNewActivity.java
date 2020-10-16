package com.demo.lupingwudemo.videoscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.demo.lupingwudemo.R;
import com.demo.lupingwudemo.recordscreencodec.ScreenCodecActivity;

import java.io.File;

/**
 * wuqingsen on 2020-10-15
 * Mailbox:1243411677@qq.com
 * annotation:仅录屏
 */
public class ScreenCodecNewActivity extends AppCompatActivity {

    private Button mBtnStart;
    private Button mBtnStop;
    private TextView mTvResult;

    private MediaProjection mediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private EncoderVideoThread mEncoderThread;  //编码线程

    private boolean recordState = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_codec);
        mBtnStart = (Button) findViewById(R.id.btn_start_record);
        mBtnStop = (Button) findViewById(R.id.btn_stop_record);
        mTvResult = (TextView) findViewById(R.id.tv_result);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startRecord() {
        mediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, 1002);
    }

    public void stopRecord() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ScreenRecorderService.stop(ScreenCodecNewActivity.this);
        } else {
            //停止录制，阻塞线程,清空资源
            if (recordState) {
                recordState = false;
                if (mEncoderThread != null) {
                    mEncoderThread.interrupt();
                    mEncoderThread.quit();
                    mEncoderThread = null;
                }
            }
            Toast.makeText(this, "停止录制中......", Toast.LENGTH_SHORT).show();
            mTvResult.setText("完成录制，文件保存在" + getSavePath() + File.separator);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("wqs", "requestCode:" + requestCode + ";resultCode:" + resultCode + ";RESULT_OK:" + RESULT_OK);
        if (resultCode == RESULT_OK && requestCode == 1002) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ScreenRecorderService.start(this, mediaProjectionManager, data);
            } else {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);   //根据返回结果，获取MediaProjection--保存帧数据
                //开启编码线程进行编码
                mEncoderThread = new EncoderVideoThread(mediaProjection);
                mEncoderThread.start();
                recordState = true;
            }
        }
    }

    public String getSavePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "手机录屏.mp4";

        return path;
    }
}
