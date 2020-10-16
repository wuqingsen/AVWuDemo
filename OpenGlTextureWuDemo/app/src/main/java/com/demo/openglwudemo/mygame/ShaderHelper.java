package com.demo.openglwudemo.mygame;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * wuqingsen on 2020-08-19
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {

        //创建一个对象
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new shader.");
            }
            //创建失败
            return 0;
        }
        //将着色器源代码上传到着色器对象中
        glShaderSource(shaderObjectId, shaderCode);
        //编译着色器
        glCompileShader(shaderObjectId);

        //检查OpenGl是否能成功编译这个着色器
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        //取出着色器信息日志
        if (LoggerConfig.ON) {
            Log.v(TAG, "Results of compiling source: " + "\n" + shaderCode + "\n" +
                    glGetShaderInfoLog(shaderObjectId));
        }

        //验证编译状态并返回着色器对象ID
        if (compileStatus[0] == 0) {
            //失败则删除
            glDeleteShader(shaderObjectId);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Compilation if shader failed.");
            }
            //创建失败
            return 0;
        }
        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        //新建程序对象
        final int programObjectId = glCreateProgram();

        //创建失败
        if (programObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new program");
            }
            return 0;
        }

        //把顶点着色器和片段着色器附加到程序对象上
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        //将着色器联合起来
        glLinkProgram(programObjectId);

        //检查链接是否成功
        final int[] linksStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linksStatus, 0);

        if (LoggerConfig.ON) {
            Log.w(TAG, "Results of linking program:\n" + glGetProgramInfoLog(programObjectId));
        }

        //验证链接状态，为0链接失败
        if (linksStatus[0] == 0) {
            glDeleteProgram(programObjectId);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Linking if program failed.");
            }
            return 0;
        }

        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        //验证程序
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.w(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        //compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        //Link them into a shader program.
        program = linkProgram(vertexShader,fragmentShader);

        if (LoggerConfig.ON){
            validateProgram(program);
        }

        return program;
    }
}
