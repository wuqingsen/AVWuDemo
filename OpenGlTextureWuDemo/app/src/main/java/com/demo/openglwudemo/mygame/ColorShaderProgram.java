package com.demo.openglwudemo.mygame;

import android.content.Context;

import com.demo.openglwudemo.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static com.demo.openglwudemo.mygame.ShaderProgram.A_COLOR;
import static com.demo.openglwudemo.mygame.ShaderProgram.A_POSITION;
import static com.demo.openglwudemo.mygame.ShaderProgram.U_MATREX;


/**
 * wuqingsen on 2020-08-25
 * Mailbox:1243411677@qq.com
 * annotation:颜色着色器程序
 */
public class ColorShaderProgram extends ShaderProgram{
    private int uMatrixLocation;
    private int aPositionLocation;
    private int aColorLocation;

    public ColorShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader,
                R.raw.simple_fragment_shader);

        uMatrixLocation = glGetUniformLocation(program, U_MATREX);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
    }

    public void setUniforms(float[] matrix) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }
}
