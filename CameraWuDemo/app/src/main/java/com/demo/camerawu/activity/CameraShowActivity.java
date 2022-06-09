package com.demo.camerawu.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.camerawu.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * wuqingsen on 2020-05-26
 * Mailbox:1243411677@qq.com
 * annotation:摄像头预览数据
 */
public class CameraShowActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    SurfaceHolder surfaceHolder;
    SurfaceView surfaceView,surfaceView1;
    private int mCameraId;//前置1还是后置0
    Button button;
    private HandlerThread thread = new HandlerThread("TAG");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_show);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView1 = findViewById(R.id.surfaceView1);
        button = findViewById(R.id.button);

        if (checkCameraHardware()) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;//默认前置
            try {
                mCamera = Camera.open(mCameraId);
            } catch (Exception e) {
                Log.e("wqsCameraShowActivity", "摄像头被占用");

                e.printStackTrace();
            }
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);
        }

        surfaceView1.getHolder().setKeepScreenOn(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCameraDir();
            }
        });
        thread.start();
        threadHandler = new Handler(thread.getLooper());
    }

    //判断手机是否有摄像头
    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    //判断手机是否有摄像头
    private void startCamera() {
        setCameraDisplayOrientation();
        Camera.Parameters parameters = mCamera.getParameters();

        // 这个宽高的设置必须和后面编解码的设置一样，否则不能正常处理
        parameters.setPreviewSize(1920, 1080);
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
    public void onPreviewFrame(byte[] data, Camera camera) {

        if (data == null) {
            return;
        }
        updateBitmap(data);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (checkCameraHardware()) {
            startCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
    }

    private void stopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Object bufferLock = new Object();
    byte[] buffer;
    private ExecutorService service = Executors.newCachedThreadPool();
    private ArrayBlockingQueue<Bitmap> queue = new ArrayBlockingQueue<Bitmap>(16);
    private void updateBitmap(byte[] data) {
        synchronized (bufferLock) {
            if (buffer == null) {
                buffer = new byte[data.length];
            }
            System.arraycopy(data, 0, buffer, 0, data.length);
        }
        data = null;
        service.submit(new Runnable() {
            @Override
            public void run() {
                Camera.Size size = mCamera.getParameters().getPreviewSize(); //获取预览大小
                final int w = size.width;  //宽度
                final int h = size.height;
                final YuvImage image;
                byte[] tmp;
                synchronized (bufferLock) {
                    image = new YuvImage(buffer, ImageFormat.NV21, w, h, null);
                    final int len = buffer.length;
                    ByteArrayOutputStream os = new ByteArrayOutputStream(len);
                    if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
                        return;
                    }
                    tmp = os.toByteArray();
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                if (bmp == null) {
                    return;
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                Bitmap rotate = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
                try {
                    queue.put(rotate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateView();
            }
        });
    }

    private Handler threadHandler;
    private void updateView() {
        threadHandler.post(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = surfaceView1.getHolder().lockCanvas();
                if (canvas == null) {
                    return;
                }
                Bitmap bitmap = queue.poll();
                if (bitmap == null) {
                    return;
                }
                Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                Rect dest = new Rect(0, 0, surfaceView1.getWidth(), surfaceView1.getHeight());
                canvas.drawBitmap(bitmap, src, dest, null);
                surfaceView1.getHolder().unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
