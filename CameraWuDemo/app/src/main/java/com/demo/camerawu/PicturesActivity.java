package com.demo.camerawu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * wuqingsen on 2020-05-26
 * Mailbox:1243411677@qq.com
 * annotation:拍照
 */
public class PicturesActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    SurfaceHolder surfaceHolder;
    SurfaceView surfaceView;
    private int mCameraId;//前置1还是后置0
    Button button, button1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pictures);

        surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);

        initCamera();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前相机参数
                Camera.Parameters parameters = mCamera.getParameters();
                // 设置预览大小
                parameters.setPreviewSize(1920, 1080);
                // 设置相片格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                // 设置对焦方式，这里设置自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        // 判断是否对焦成功
                        if (success) {
                            // 拍照 第三个参数为拍照回调
                            mCamera.takePicture(null, null, pc);
                        }
                    }
                });
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCameraDir();
            }
        });
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

    private Camera.PictureCallback pc = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // data为完整数据
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "demo.png";
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length);
                //因为照片有可能是旋转的，这里要做一下处理
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(mCameraId, info);

                Bitmap realBmp = rotaingBitmap(info.orientation,
                        mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT, bitmap);

                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                realBmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                Toast.makeText(PicturesActivity.this, "拍照成功,路径:" + path, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("wqsPicturesActivity", "错误：  " + e.getMessage());
            }
//            mCamera.startPreview();
        }
    };

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


    /**
     * 旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @param scale  是否镜像
     * @return 旋转后的图片
     */
    public static Bitmap rotaingBitmap(int angle, boolean scale, Bitmap bitmap) {
        //bitmap = small(bitmap);   不缩放
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        if (scale) {
            matrix.postScale(-1, 1); //水平镜像
        }
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return returnBm;
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


    private void stopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
