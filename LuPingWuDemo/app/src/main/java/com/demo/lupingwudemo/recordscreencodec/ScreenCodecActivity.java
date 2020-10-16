package com.demo.lupingwudemo.recordscreencodec;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.lupingwudemo.R;
import com.demo.lupingwudemo.recordscreen10.interfaces.MediaProjectionNotificationEngine;
import com.demo.lupingwudemo.recordscreen10.interfaces.MediaRecorderCallback;
import com.demo.lupingwudemo.recordscreen10.interfaces.ScreenCaptureCallback;
import com.demo.lupingwudemo.recordscreen10.utils.MediaProjectionHelper;
import com.demo.lupingwudemo.utils.LogUtil;
import com.demo.lupingwudemo.utils.NotificationHelper;
import com.mask.photo.interfaces.SaveBitmapCallback;
import com.mask.photo.utils.BitmapUtils;

import java.io.File;

/**
 * wuqingsen on 2020-06-03
 * Mailbox:1243411677@qq.com
 * annotation:仅录屏10.0
 */
public class ScreenCodecActivity extends AppCompatActivity {

    private Button mBtnStart;
    private Button mBtnStop;
    private TextView mTvResult;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecorder mRecorder;
    private MediaProjection mediaProjection;

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
                mMediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent, 1002);
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    ScreenRecorderService.stop(ScreenCodecActivity.this);
                } else {
                    mRecorder.quit();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002) {
            Log.e("VideoCallActivity", "父Activity, onActivityResult");
            receive(data);
        }
    }

    public void receive(Intent data) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            startForegroundService(new Intent(RemoteMeetingActivity.this, ScreenRecorderService.class));
            ScreenRecorderService.start(this, mMediaProjectionManager, data);
            Log.d("mmm", "android 10");
        } else {
            final int width = 1280;
            final int height = 720;
            final int dpi = 1;
            mediaProjection = mMediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data);

            mRecorder = new ScreenRecorder(width, height, dpi, mediaProjection);
            mRecorder.start();

        }
    }


}
