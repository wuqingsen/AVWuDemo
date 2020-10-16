package com.demo.openglwudemo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * wuqingsen on 2020-06-12
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    //    private Square mSquare;
    private Triangle mTriangle;

    //mMVPMatrix是“模型视图投影矩阵”的缩写
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] mRotationMatrix = new float[16];

    public volatile float mAngle;

    public float getmAngle() {
        return mAngle;
    }

    public void setmAngle(float mAngle) {
        this.mAngle = mAngle;
    }

    // 在View的OpenGL环境被创建的时候调用。
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //1.初始化形状
        mTriangle = new Triangle();
//        mSquare = new Square();
    }

    //每一次View的重绘都会调用
    @Override
    public void onDrawFrame(GL10 gl) {
        //设置摄像头位置(查看矩阵)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        //计算投影和视图转换
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float[] scratch = new float[16];

        //为三角形创建一个旋转变换
        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 0.090f * ((int) time);

        //注意，mMVPMatrix因子*必须是第一*，这样矩阵乘法乘积才正确。
        Matrix.setRotateM(mRotationMatrix,0,mAngle,0,0,-1.0f);
        Matrix.multiplyMM(scratch,0,mMVPMatrix,0,mRotationMatrix,0);

        mTriangle.draw(scratch);
    }

    //屏幕方向转变
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        //这个投影矩阵在onDrawFrame()方法中应用于对象坐标
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    //创建一个顶点着色器类型(GLES20.GL_VERTEX_SHADER)或片段着色器类型(GLES20.GL_FRAGMENT_SHADER)
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        //将源代码添加到着色器并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
