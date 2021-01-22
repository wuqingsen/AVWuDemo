package com.demo.lupingwudemo.audiovideothree;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.demo.lupingwudemo.audiovideo.service.MediaAudioService;
import com.demo.lupingwudemo.recordscreen10.interfaces.MediaProjectionNotificationEngine;

/**
 * wuqingsen on 2020-10-12
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MediaAudioHelper {

    public static final int REQUEST_CODE = 10086;
    private MediaProjectionNotificationEngine notificationEngine;
    private ServiceConnection serviceConnection;
    private MediaAudioService mediaAudioService;

    private static class InstanceHolder {
        private static final MediaAudioHelper instance = new MediaAudioHelper();
    }

    public static MediaAudioHelper getInstance() {
        return InstanceHolder.instance;
    }

    private MediaAudioHelper() {
        super();
    }

    /**
     * 设置 通知引擎
     *
     * @param notificationEngine notificationEngine
     */
    public void setNotificationEngine(MediaProjectionNotificationEngine notificationEngine) {
        this.notificationEngine = notificationEngine;
    }

    /**
     * 启动媒体投影服务
     *
     * @param activity activity
     */
    public void startService(Activity activity) {
        // 绑定服务
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof MediaAudioService.MediaAudioBinder) {
                    mediaAudioService = ((MediaAudioService.MediaAudioBinder) service).getService();
                    mediaAudioService.setNotificationEngine(notificationEngine);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mediaAudioService = null;
            }
        };
        MediaAudioService.bindService(activity, serviceConnection);
    }

    /**
     * 停止媒体投影服务
     *
     * @param context context
     */
    public void stopService(Context context) {
        mediaAudioService = null;

        if (serviceConnection != null) {
            MediaAudioService.unbindService(context, serviceConnection);
            serviceConnection = null;
        }
    }

    /**
     * 显示通知栏
     */
    public void showNotification(){
        mediaAudioService.showNotification();
    }
}
