package com.demo.camerawu.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.camerawu.R;
import com.demo.camerawu.util.CameraUtils;
import com.demo.camerawu.util.MySurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * wuqingsen on 2020-05-26
 * Mailbox:1243411677@qq.com
 * annotation:摄像头预览数据偏好设置
 */
@SuppressLint("NewApi")
public class CameraShowSPActivity extends AppCompatActivity {
    private MySurfaceView mSurfaceView;
    private Button button;
    private RelativeLayout rl_all;
    private Camera mCamera;
    private Camera.Size mSupportSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_show_sp);

        mSurfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.button);
        rl_all = findViewById(R.id.rl_all);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mCamera = mSurfaceView.getCameraInstance();
        initSurfaceView();
    }

    private void initSurfaceView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("CameraFragment", "surfaceCreated");
                mSupportSize = CameraUtils.getSupportSize(rl_all, 0, 0, mSurfaceView.getCameraInstance().getParameters().getSupportedPreviewSizes());
                mSurfaceView.setAspectRatio(mSupportSize.width, mSupportSize.height);
                rl_all.post(new Runnable() {
                    @Override
                    public void run() {
                        initCamera(mSurfaceView);
                    }
                });

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("CameraFragment", "surfaceDestroyed");
            }
        });

    }

    private void initCamera(MySurfaceView surfaceView) {
        try {
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                @Override
                public void onAutoFocusMoving(boolean start, Camera camera) {
                    Log.d("CameraFragment", "onAutoFocusMoving start:" + start);
                }
            });

            mCamera.setDisplayOrientation(90);
            Camera.Parameters parameters = mCamera.getParameters();
            // 拿到支持的输出分辨率    这个分辨率好像是横屏*竖屏 比如 1920*1080
            List supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            //设置输出的分辨率
            parameters.setPreviewSize(mSupportSize.width, mSupportSize.height);
            //持续对焦模式  这种模式下不能调用autoFocus
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setJpegQuality(100);
            //设置拍照的格式
            parameters.setPictureFormat(ImageFormat.JPEG);
            //设置拍照的分辨率
            parameters.setPictureSize(mSupportSize.width, mSupportSize.height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSurfaceView.onStop();
    }

}
