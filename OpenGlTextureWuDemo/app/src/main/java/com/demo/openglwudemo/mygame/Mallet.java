package com.demo.openglwudemo.mygame;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.demo.openglwudemo.mygame.Constants.BYTES_PER_FLOAT;

/**
 * wuqingsen on 2020-08-25
 * Mailbox:1243411677@qq.com
 * annotation:木槌数据
 */
public class Mallet {
    private static final int POSITION_COMPONENT_COUNT  = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)
            * BYTES_PER_FLOAT;
    private static final float[] VERTEX_DATA ={
            //X,Y,R,G,B,
            0f,-0.4f,0f,0f,1f,
            0f,0.4f,1f,0f,0f
    };

    private VertexArray vertexArray;

    public Mallet(){
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(ColorShaderProgram colorProgram){
        vertexArray.setVertexAttribPointer(
                0,
                colorProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE
        );
        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                colorProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE
        );

    }

    public void draw(){
        glDrawArrays(GL_POINTS,0,2);
    }
}
