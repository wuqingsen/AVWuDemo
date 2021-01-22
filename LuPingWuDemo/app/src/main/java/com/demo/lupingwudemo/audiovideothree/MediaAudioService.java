package com.demo.lupingwudemo.audiovideothree;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.demo.lupingwudemo.audiovideo.MediaAudioHelper;
import com.demo.lupingwudemo.recordscreen10.interfaces.MediaProjectionNotificationEngine;

/**
 * wuqingsen on 2020-10-12
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MediaAudioService extends Service {
    private static final int ID_MEDIA_PROJECTION = MediaAudioHelper.REQUEST_CODE;

    private MediaProjectionNotificationEngine notificationEngine;
    /**
     * 绑定Service
     *
     * @param context           context
     * @param serviceConnection serviceConnection
     */
    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, MediaAudioService.class);
        context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * 解绑Service
     *
     * @param context           context
     * @param serviceConnection serviceConnection
     */
    public static void unbindService(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MediaAudioBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MediaAudioBinder extends Binder {

        public MediaAudioService getService() {
            return MediaAudioService.this;
        }

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
     * 显示通知栏
     */
    public void showNotification() {
        if (notificationEngine == null) {
            return;
        }

        Notification notification = notificationEngine.getNotification();

        startForeground(ID_MEDIA_PROJECTION, notification);
    }

}
