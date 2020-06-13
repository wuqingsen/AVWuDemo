package com.demo.openglwudemo;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * wuqingsen on 2020-06-12
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }
}
