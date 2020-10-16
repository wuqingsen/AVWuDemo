package com.demo.lupingwudemo.recordscreencodec;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * @author Yrom
 */
public class ScreenRecorder extends Thread {
    private static final String TAG = "mmm";
    private final HandlerThread mGLThread;
    private final Handler mGLHandler;

    private int mWidth;
    private int mHeight;
    private int mDpi;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private boolean isRun;

    public ScreenRecorder(int width, int height, int dpi, MediaProjection mp) {
        super(TAG);
        mWidth = width;
        mHeight = height;
        mDpi = dpi;
        mMediaProjection = mp;

        mGLThread = new HandlerThread("GLThread");
        mGLThread.start();
        mGLHandler = new Handler(mGLThread.getLooper());
    }

    /**
     * stop task
     */
    public final void quit() {
        isRun = false;
    }

    @Override
    public void run() {
        method1();
//        method2();
    }

    private void method1() {
        isRun = true;
        try {
            ImageReader imageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    imageReader.getSurface(), null, null);
            Log.d(TAG, "created virtual display: " + mVirtualDisplay);

            while (isRun) {
                SystemClock.sleep(30);
                Image image = imageReader.acquireNextImage();

                if (image == null) {
                    continue;
                }
                int width = image.getWidth();
                int height = image.getHeight();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int piexlStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - piexlStride * width;

                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / piexlStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                image.close();

                byte[] nv21 = ImageUtil.getNV21(mWidth, mHeight, bitmap);
                Log.e("wqs", "视频流: "+ nv21);
            }
        } finally {
            release();
        }
    }

    private void release() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }
}