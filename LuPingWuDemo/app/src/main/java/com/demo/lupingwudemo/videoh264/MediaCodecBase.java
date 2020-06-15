package com.demo.lupingwudemo.videoh264;

import android.media.MediaCodec;

/**
 * wuqingsen on 2020-06-15
 * Mailbox:1243411677@qq.com
 * annotation:
 */

public abstract class MediaCodecBase {

    protected MediaCodec mEncoder;

    protected boolean isRun = false;

    public abstract void prepare();

    public abstract void release();


}
