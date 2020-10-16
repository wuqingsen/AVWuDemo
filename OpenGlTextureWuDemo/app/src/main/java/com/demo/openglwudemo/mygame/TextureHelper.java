package com.demo.openglwudemo.mygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

/**
 * wuqingsen on 2020-08-24
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class TextureHelper {
    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not generate a new OpenGl texture object.");
            }
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if (bitmap == null) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Resource ID " + resourceId + " Could not be decoded.");
            }
            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        //GL_TEXTURE_2D二维纹理
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        //设置过滤参数
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);//三线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);//双线性过滤

        //加载纹理到openGL
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        //释放资源
        bitmap.recycle();

        //生成MIP贴图
        glGenerateMipmap(GL_TEXTURE_2D);

        //解除绑定，传0就是与当前纹理解除绑定
        glBindTexture(GL_TEXTURE_2D, 0);

        //返回纹理对象ID
        return textureObjectIds[0];
    }
}
