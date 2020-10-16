package com.demo.openglwudemo.mygame;

import android.content.Context;

import com.demo.openglwudemo.R;

import static android.opengl.GLES20.GL_TEXTURE;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static com.demo.openglwudemo.mygame.ShaderProgram.A_POSITION;
import static com.demo.openglwudemo.mygame.ShaderProgram.A_TEXTURE_COORDINATES;
import static com.demo.openglwudemo.mygame.ShaderProgram.U_MATREX;
import static com.demo.openglwudemo.mygame.ShaderProgram.U_TEXTURE_UNIT;


/**
 * wuqingsen on 2020-08-25
 * Mailbox:1243411677@qq.com
 * annotation:加入纹理着色器程序
 */
public class TextureShaderProgram extends ShaderProgram {
    private int uMatrixLocation;
    private int uTextureUnitLocation;

    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    public TextureShaderProgram(Context context) {
        super(context, R.raw.texture_vertex_shader,
                R.raw.texture_fragment_shader);

        uMatrixLocation = glGetUniformLocation(program, U_MATREX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    //设置uniform并返回属性位置
    public void setUniforms(float[] matrix, int textureId) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        glActiveTexture(GL_TEXTURE);

        glBindTexture(GL_TEXTURE_2D, textureId);

        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
