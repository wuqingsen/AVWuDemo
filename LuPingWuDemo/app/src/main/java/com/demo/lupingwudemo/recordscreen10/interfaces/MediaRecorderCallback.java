package com.demo.lupingwudemo.recordscreen10.interfaces;

import java.io.File;

/**
 * wuqingsen on 2020-10-12
 * Mailbox:1243411677@qq.com
 * annotation:媒体录制回调
 */
public abstract class MediaRecorderCallback {

    /**
     * 成功
     *
     * @param file 录制后的File
     */
    public void onSuccess(File file) {

    }

    /**
     * 失败
     */
    public void onFail() {

    }

}
