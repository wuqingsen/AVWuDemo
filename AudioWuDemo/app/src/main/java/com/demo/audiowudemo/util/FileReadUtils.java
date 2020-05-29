package com.demo.audiowudemo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * wuqingsen on 2020-05-29
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class FileReadUtils {

    /**
     * 以字节流读取文件
     *
     * @param path 文件路径
     * @return 字节数组
     */
    public static byte[] getByteStream(String path) {
        // 拿到文件
        File file = new File(path);
        return getByteStream(file);
    }

    /**
     * 以字节流读取文件
     *
     * @param file 文件
     * @return 字节数组
     */
    public static byte[] getByteStream(File file) {
        try {
            // 拿到输入流
            FileInputStream input = new FileInputStream(file);
            // 建立存储器
            byte[] buf = new byte[input.available()];
            // 读取到存储器
            input.read(buf);
            // 关闭输入流
            input.close();
            // 返回数据
            return buf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以字符流读取文件
     *
     * @param path 文件路径
     * @return 字符数组
     */
    public static String getCharacterStream(String path) {
        try {
            // 创建字符流对象
            FileReader reader = new FileReader(path);
            // 创建字符串拼接
            StringBuilder builder = new StringBuilder();
            // 读取一个字符
            int read = reader.read();
            // 能读取到字符
            while (read != -1) {
                // 拼接字符串
                builder.append((char) read);
                // 读取下一个字符
                read = reader.read();
            }
            // 关闭字符流
            reader.close();
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
