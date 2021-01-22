package com.demo.lupingwudemo.audiovideothree;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * wuqingsen on 2020-06-03
 * Mailbox:1243411677@qq.com
 * annotation:文件处理工具类
 */
public class FileUtils {
    public String getFilePath(){
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"吴庆森录屏.mp4";
        File file = new File(filePath);
        if (file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
            return filePath;
        } catch (IOException e) {
            Log.i("FileUtil","file.createNewFile() error...");
            e.printStackTrace();
        }
        return filePath;
    }
}
