package com.demo.openglwudemo;

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
    }
}
