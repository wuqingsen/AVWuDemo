package com.demo.camerawu;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.camerawu.util.AvcEncoder;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * wuqingsen on 2020-05-26
 * Mailbox:1243411677@qq.com
 * annotation:h264录制
 */
public class H264RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    SurfaceHolder surfaceHolder;
    SurfaceView surfaceView;
    private int mCameraId;//前置1还是后置0
    Button button, button1;
    private static int yuvqueuesize = 10;
    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<>(yuvqueuesize);
    private int width = 640;
    private int height = 480;
    private AvcEncoder avcCodec;
    private boolean isStart = false;//是否开始录制视频:默认未开始

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h264_record);

        surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);

        avcCodec = new AvcEncoder(width, height, 15);
        initCamera();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStart) {
                    //正在录制则停止录制
                    button1.setVisibility(View.VISIBLE);
                    isStart = false;
                    button.setText("开始");
                    avcCodec.StopThread();
                } else {
                    //开始录制
                    button1.setVisibility(View.GONE);
                    isStart = true;
                    button.setText("停止");
                    avcCodec.StartEncoderThread();
                }
            }
        });
        //切换摄像头
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCameraDir();
            }
        });
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        putYUVData(data);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void putYUVData(byte[] buffer) {
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }


    private void initCamera() {
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;//默认后置
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            Log.e("wqsCameraShowActivity", "摄像头被占用");

            e.printStackTrace();
        }
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

    }

    private void startCamera() {
        setCameraDisplayOrientation();
        // 获取当前相机参数
        Camera.Parameters parameters = mCamera.getParameters();
        // 设置预览大小
        parameters.setPreviewSize(width, height);
        try {
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
    }

    /**
     * 得到摄像头默认旋转角度后，旋转回来  注意是逆时针旋转
     */
    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;//旋转角度
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.e("摄像头被旋转的角度;", "" + result);
        mCamera.setDisplayOrientation(result);
    }

    //改变摄像头方向
    public void changeCameraDir() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

        }
        startCamera();
    }

    private void stopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
