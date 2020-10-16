package com.demo.openglwudemo.mygame;

import android.content.Context;

import static android.opengl.GLES20.glUseProgram;

/**
 * wuqingsen on 2020-08-25
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class ShaderProgram {
    protected static final String U_MATREX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    protected  int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId) {
        program = ShaderHelper.buildProgram(
                TextResoutceReader.readTextFileFromResource(
                        context, vertexShaderResourceId),
                TextResoutceReader.readTextFileFromResource(
                        context, fragmentShaderResourceId));
    }

    public  void useProgram(){
        //Set the current OpenGL shader program to this program.
        glUseProgram(program);
    }
}
