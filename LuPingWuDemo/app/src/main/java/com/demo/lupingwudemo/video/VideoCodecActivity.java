package com.demo.lupingwudemo.video;

import android.Manifest;
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

import java.io.File;

/**
 * wuqingsen on 2020-06-03
 * Mailbox:1243411677@qq.com
 * annotation:仅录屏
 */
public class VideoCodecActivity extends AppCompatActivity {

    private Button mBtnStart;
    private Button mBtnStop;
    private TextView mTvResult;

    private MediaProjection mediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private EncoderVideoThread mEncoderThread;  //编码线程

    private boolean recordState = false;

    public static final int REQUEST_CODE = 111;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_codec);
        mBtnStart = (Button) findViewById(R.id.btn_start_record);
        mBtnStop = (Button) findViewById(R.id.btn_stop_record);
        mTvResult = (TextView) findViewById(R.id.tv_result);


        //通过系统服务获取MediaProjectionMananger
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

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
        checkPermission();
        //Android 5.0 后利用开放api开始屏幕录制
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE);
        Toast.makeText(this, "开始录制......", Toast.LENGTH_SHORT).show();
        mTvResult.setVisibility(View.VISIBLE);
        mTvResult.setText("正在录制中......");
    }

    public void stopRecord() {
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

    public void checkPermission() {
        //android 6.0后动态检测申请相关权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(VideoCodecActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VideoCodecActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
            }

            if (ContextCompat.checkSelfPermission(VideoCodecActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VideoCodecActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 12);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("wqs", "requestCode:" + requestCode + ";resultCode:" + resultCode + ";RESULT_OK:" + RESULT_OK);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);   //根据返回结果，获取MediaProjection--保存帧数据
            //开启编码线程进行编码
            mEncoderThread = new EncoderVideoThread(mediaProjection);
            mEncoderThread.start();
            recordState = true;
        }
    }


    public String getSavePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "手机录屏.mp4";

        return path;
    }
}
