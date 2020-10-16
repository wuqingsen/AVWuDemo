package com.demo.openglwudemo;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * wuqingsen on 2020-06-12
 * Mailbox:1243411677@qq.com
 * annotation:三角形
 */
public class Triangle {
    //注释：默认情况下，OpenGL ES采用坐标系，[0,0,0]（X，Y，Z）指定GLSurfaceView框架的中心,
    //[1,1,0]是框架的右上角，[ - 1，-1,0]是框架的左下角。
    //请注意，此图形的坐标以逆时针顺序定义。 绘图顺序非常重要，因为它定义了哪一面是您通常想要绘制的图形的正面

    private FloatBuffer vertexBuffer;
    //每个顶点的坐标数量
    static final int COORDS_PER_VERTEX = 3;
    static float triagleCoords[] = {
            //逆时针的顺序
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };
    //设置颜色与红色，绿色，蓝色和alpha(不透明度)值
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    private int mProgram = 0;

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private int mMVPMatrixHandle;

    //    private final String vertexShaderCode =
//            "attribute vec4 vPosition;" +
//                    "void main() {" +
//                    "  gl_Position = vPosition;" +
//                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = triagleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;//每个顶点4字节

    public Triangle() {
        //初始化形状坐标的顶点字节缓冲区; 坐标值的数目*每个浮点数4字节
        ByteBuffer bb = ByteBuffer.allocateDirect(triagleCoords.length * 4);
        //使用设备硬件的本机字节顺序
        bb.order(ByteOrder.nativeOrder());

        //从ByteBuffer创建浮点缓冲区
        vertexBuffer = bb.asFloatBuffer();
        //将坐标添加到FloatBuffer
        vertexBuffer.put(triagleCoords);
        //设置缓冲区读取第一个坐标
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        //创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();

        //添加顶点着色程序
        GLES20.glAttachShader(mProgram, vertexShader);

        //添加片段着色程序
        GLES20.glAttachShader(mProgram, fragmentShader);

        //创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        //添加程序到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        //获取顶点着色器的vPosition成员的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //准备三角形坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        //获取片段着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        //画一个三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        //禁用顶点数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
