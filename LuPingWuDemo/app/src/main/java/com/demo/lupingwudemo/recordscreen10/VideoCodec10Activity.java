package com.demo.lupingwudemo.recordscreen10;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.demo.lupingwudemo.R;
import com.demo.lupingwudemo.recordscreen10.interfaces.MediaProjectionNotificationEngine;
import com.demo.lupingwudemo.recordscreen10.interfaces.MediaRecorderCallback;
import com.demo.lupingwudemo.recordscreen10.interfaces.ScreenCaptureCallback;
import com.demo.lupingwudemo.recordscreen10.utils.MediaProjectionHelper;
import com.demo.lupingwudemo.utils.LogUtil;
import com.demo.lupingwudemo.utils.NotificationHelper;
import com.demo.lupingwudemo.video.EncoderVideoThread;
import com.mask.photo.interfaces.SaveBitmapCallback;
import com.mask.photo.utils.BitmapUtils;

import java.io.File;

/**
 * wuqingsen on 2020-06-03
 * Mailbox:1243411677@qq.com
 * annotation:仅录屏10.0
 */
public class VideoCodec10Activity extends AppCompatActivity {

    private Button mBtnStart;
    private Button mBtnStop;
    private TextView mTvResult;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_codec);
        mBtnStart = (Button) findViewById(R.id.btn_start_record);
        mBtnStop = (Button) findViewById(R.id.btn_stop_record);
        mTvResult = (TextView) findViewById(R.id.tv_result);

        initData();

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doServiceStart();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doServiceStop();
                doMediaRecorderStop();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MediaProjectionHelper.getInstance().createVirtualDisplay(requestCode, resultCode, data, true, true);
        doMediaRecorderStart();
    }

    private void initData() {
        MediaProjectionHelper.getInstance().setNotificationEngine(new MediaProjectionNotificationEngine() {
            @Override
            public Notification getNotification() {
                String title = getString(R.string.service_start);
                return NotificationHelper.getInstance().createSystem()
                        .setOngoing(true)// 常驻通知栏
                        .setTicker(title)
                        .setContentText(title)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LogUtil.i("Environment.isExternalStorageLegacy: " + Environment.isExternalStorageLegacy());
        }
    }

    /**
     * 启动媒体投影服务
     */
    private void doServiceStart() {
        MediaProjectionHelper.getInstance().startService(this);
    }

    /**
     * 停止媒体投影服务
     */
    private void doServiceStop() {
        MediaProjectionHelper.getInstance().stopService(this);
    }

    /**
     * 开始屏幕录制
     */
    private void doMediaRecorderStart() {
        MediaProjectionHelper.getInstance().startMediaRecorder(new MediaRecorderCallback() {
            @Override
            public void onSuccess(File file) {
                super.onSuccess(file);

                LogUtil.i("MediaRecorder onSuccess: " + file.getAbsolutePath());

                Toast.makeText(getApplication(), getString(R.string.content_media_recorder_result, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFail() {
                super.onFail();

                LogUtil.e("MediaRecorder onFail");
            }
        });
    }

    /**
     * 停止屏幕录制
     */
    private void doMediaRecorderStop() {
        MediaProjectionHelper.getInstance().stopMediaRecorder();
    }

    @Override
    protected void onDestroy() {
        MediaProjectionHelper.getInstance().stopService(this);
        super.onDestroy();
    }

}
