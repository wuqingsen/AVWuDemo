package com.demo.camerawu.util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * wuqingsen on 2020-09-19
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private float mAspectRatio;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public int mCameraId;

    public MySurfaceView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                mCamera = Camera.open(mCameraId);
            } catch (Exception e) {
                Log.d("wqs", "camera is not available");
            }
        }
        return mCamera;
    }

    public int getCameraId(){
        return mCameraId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getCameraInstance();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("wqs", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void setAspectRatio(int width, int height) {
        mAspectRatio = (float) width / height;
        getHolder().setFixedSize(width, height);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mAspectRatio == 0) {
            setMeasuredDimension(width, height);
        } else {
            int newW, newH;
            float actualRatio;
            if (width > height) {
                actualRatio = mAspectRatio;
            } else {
                actualRatio = 1 / mAspectRatio;
            }

            if (width < height * actualRatio) {
                newH = height;
                newW = (int) (height * actualRatio);
            } else {
                newW = width;
                newH = (int) (width / actualRatio);
            }
            setMeasuredDimension(newW, newH);

        }
    }

    public void onStop() {
        releaseCamera();
    }

    //销毁摄像头
    private synchronized void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }

    //改变摄像头方向
    public void switchCamera() {
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
        setCameraDisplayOrientation();
        Camera.Parameters parameters = mCamera.getParameters();

        // 这个宽高的设置必须和后面编解码的设置一样，否则不能正常处理
        parameters.setPreviewSize(1920, 1080);
    }

    /**
     * 得到摄像头默认旋转角度后，旋转回来  注意是逆时针旋转
     */
    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        Activity activity = (Activity) getContext();
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
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


}
