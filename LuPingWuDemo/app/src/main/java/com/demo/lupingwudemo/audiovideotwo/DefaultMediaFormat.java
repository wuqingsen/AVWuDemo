package com.demo.lupingwudemo.audiovideotwo;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;


/**
 * wuqingsen on 2020-06-09
 * Mailbox:1243411677@qq.com
 * annotation:获取默认MediaFormat
 */
@SuppressLint("NewApi")
public class DefaultMediaFormat {

    public static MediaFormat getDefaultVideoFormat(){

        MediaFormat format = MediaFormat.createVideoFormat("video/avc",720 , 1280);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1048000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        return format;
    }

    public static MediaFormat getDefaultAudioFormat(){

        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 16000);
        format.setInteger(MediaFormat.KEY_BIT_RATE,128000);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);
        return format;

    }

}
