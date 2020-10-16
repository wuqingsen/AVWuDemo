package com.demo.openglwudemo;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.glClear;
import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES10.glViewport;

/**
 * wuqingsen on 2020-08-14
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class FirstOpenGLProjectRender implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //surface被创建调用

        //前三个参数对应红，绿和蓝色。最后一个为透明度
        glClearColor(1.0f,0.0f,0.0f,0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //尺寸发生变化的时候被调用

        //设置窗口尺寸
        glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //绘制一帧时会调用改方法，在里面一定要绘制一些东西，即使是请空屏幕。否则有闪烁效果

        //清空屏幕
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
