package com.demo.openglwudemo.mygame;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.demo.openglwudemo.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_FALSE;
import static android.opengl.GLES10.GL_FLOAT;
import static android.opengl.GLES10.GL_LINES;
import static android.opengl.GLES10.GL_TRIANGLES;
import static android.opengl.GLES10.glClear;
import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES10.glDrawArrays;
import static android.opengl.GLES10.glVertexPointer;
import static android.opengl.GLES10.glViewport;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * wuqingsen on 2020-08-14
 * Mailbox:1243411677@qq.com
 * annotation:
 * 在OpenGL中的物体，都要考虑如何用点、直线以及三角形把它组合起来
 */
public class AirHockeyRender implements GLSurfaceView.Renderer {

    private static final int POSITION_CONPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    private Context context;

    //存储链接的程序的ID
    private int program;

    private int uColorLocation;

    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    private static final String A_COLOR  = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE =
            (POSITION_CONPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private int aColorLocation;

    public AirHockeyRender(Context context) {
        this.context = context;

        //定义一个矩形
        float[] tableVertices = {
                0f, 0f,
                0f, 14f,
                9f, 14f,
                9f, 0f
        };

        //定义两个三角形
        //注释：定义三角形，总是以意识真的顺序排列顶点，成为卷曲顺序；使用这种排序方式，可以优化性能
        float[] tableVerticesWithTriangles = {
//                0, 0,
//                -0.5f, -0.5f,
//                0.5f, -0.5f,
//                0.5f, 0.5f,
//                -0.5f, 0.5f,
//                -0.5f, -0.5f,
////
////                //第一个三角形
//////                -0.5f, -0.5f,
//////                0.5f, 0.5f,
//////                -0.5f, 0.5f,
//////
//////                //第二个三角形
//////                -0.5f, -0.5f,
//////                0.5f, -0.5f,
//////                0.5f, 0.5f,
////
//                -0.5f, 0f,
//                0.5f, 0f,
//
//                0f, -0.25f,
//                0f, 0.25f,


                //三角形
                0f,0f,1f,1f,1f,

                -0.5f,-0.5f,0.7f,0.7f,0.7f,
                0.5f,-0.5f,0.7f,0.7f,0.7f,
                0.5f,0.5f,0.7f,0.7f,0.7f,
                -0.5f,0.5f,0.7f,0.7f,0.7f,
                -0.5f,-0.5f,0.7f,0.7f,0.7f,

                //线
                -0.5f,0f,1f,0f,0f,
                0.5f,0f,1f,0f,0f,

                //木槌
                0f,-0.25f,0f,0f,1f,
                0f,0.25f,1f,0f,0f

        };

        vertexData = ByteBuffer
                //分配一块内存
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //surface被创建调用

        //前三个参数对应红，绿和蓝色。最后一个为透明度
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        String vertexShaderSource = TextResoutceReader
                .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResoutceReader
                .readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        //将着色器链接起来
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        //获取属性位置
        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        aColorLocation = glGetAttribLocation(program,A_COLOR);

        //告诉openGL 到哪找到属性a_Position对应的数据
        vertexData.position(0);
        //参数:1属性位置，2每个属性的数据的计数，3数据的类型，4忽略，5忽略，6读取数据(内存)
        glVertexAttribPointer(aPositionLocation, POSITION_CONPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        //寻找数据
        glEnableVertexAttribArray(aPositionLocation);


        //告诉openGL 到哪找到属性a_Position对应的数据
        vertexData.position(POSITION_CONPONENT_COUNT);
        //参数:1属性位置，2每个属性的数据的计数，3数据的类型，4忽略，5忽略，6读取数据(内存)
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        //寻找数据
        glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //尺寸发生变化的时候被调用

        //设置窗口尺寸
        glViewport(0, 0, width, height);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }

        glUseProgram(program);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //绘制一帧时会调用改方法，在里面一定要绘制一些东西，即使是请空屏幕。否则有闪烁效果

        //清空屏幕
        glClear(GL_COLOR_BUFFER_BIT);

        //绘制桌子
//        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);//白色
        //参数:1画三角形，2从顶点数组开头读顶点，3读入6个顶点
        glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        //绘制中心线
//        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);//红色
        glDrawArrays(GLES20.GL_LINES, 6, 2);

        //画两个木槌
//        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 8, 1);
//        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 9, 1);

    }
}
