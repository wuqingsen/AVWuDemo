package com.demo.lupingwudemo.recordscreen10.interfaces;

import android.graphics.Bitmap;

/**
 * wuqingsen on 2020-10-12
 * Mailbox:1243411677@qq.com
 * annotation:屏幕截图回调
 */
public abstract class ScreenCaptureCallback {

    /**
     * 成功
     *
     * @param bitmap 截图后的Bitmap
     */
    public void onSuccess(Bitmap bitmap) {

    }

    /**
     * 失败
     */
    public void onFail() {

    }

}
