package com.demo.openglwudemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * wuqingsen on 2020-06-12
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        //仅当绘图数据发生变化时才呈现视图
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                //在中线以上扭转旋转方向
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                //将旋转方向反转至中线左侧
                if (x < getHeight() / 2) {
                    dy = dy * -1;
                }

                mRenderer.setmAngle(mRenderer.getmAngle() +
                        ((dx + dy)*TOUCH_SCALE_FACTOR));
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
