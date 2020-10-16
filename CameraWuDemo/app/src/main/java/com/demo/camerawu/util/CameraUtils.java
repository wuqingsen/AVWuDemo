package com.demo.camerawu.util;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.view.Display;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.List;

/**
 * wuqingsen on 2020-09-19
 * Mailbox:1243411677@qq.com
 * annotation:
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class CameraUtils {
    public static Camera.Size getSupportSize(View view, int width, int height, List<Camera.Size> allSize){
        Display display = view.getDisplay();
        Point point = new Point();
        display.getSize(point);
        for (Camera.Size size : allSize) {
            if (size.width <= point.y && size.height <= point.x) {
                return size;
            }
        }
        return allSize.get(0);
    }
}
