package com.demo.lupingwudemo.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.demo.lupingwudemo.MyApplication;
import com.demo.lupingwudemo.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * wuqingsen on 2020-10-12
 * Mailbox:1243411677@qq.com
 * annotation:通知栏工具
 */
public class NotificationHelper {

    private static final String CHANNEL_ID_OTHER = "other";
    private static final String CHANNEL_NAME_OTHER = "其他消息";
    @TargetApi(Build.VERSION_CODES.O)
    private static final int CHANNEL_IMPORTANCE_OTHER = NotificationManager.IMPORTANCE_MIN;

    private static final String CHANNEL_ID_SYSTEM = "system";
    private static final String CHANNEL_NAME_SYSTEM = "系统通知";
    @TargetApi(Build.VERSION_CODES.O)
    private static final int CHANNEL_IMPORTANCE_SYSTEM = NotificationManager.IMPORTANCE_HIGH;

    private static class InstanceHolder {
        private static final NotificationHelper instance = new NotificationHelper();
    }

    public static NotificationHelper getInstance() {
        return InstanceHolder.instance;
    }

    private NotificationHelper() {
        super();
        createChannel();
    }

    /**
     * 创建通知渠道
     */
    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        createChannel(CHANNEL_ID_OTHER, CHANNEL_NAME_OTHER, CHANNEL_IMPORTANCE_OTHER, false);
        createChannel(CHANNEL_ID_SYSTEM, CHANNEL_NAME_SYSTEM, CHANNEL_IMPORTANCE_SYSTEM, true);
    }

    /**
     * 创建通知渠道
     *
     * @param channelId   channelId
     * @param channelName channelName
     * @param importance  importance
     * @param isShowBadge 是否显示角标
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel(String channelId, String channelName, int importance, boolean isShowBadge) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(isShowBadge);
        NotificationManager notificationManager = (NotificationManager)MyApplication.getInstance().getInstance().getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 创建通知栏 Builder
     *
     * @return NotificationCompat.Builder
     */
    public NotificationCompat.Builder create(String channelId) {
        Context context = MyApplication.getInstance();

        return new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
    }

    /**
     * 创建通知栏 Builder
     *
     * @return NotificationCompat.Builder
     */
    public NotificationCompat.Builder createOther() {
        return create(CHANNEL_ID_OTHER);
    }

    /**
     * 创建通知栏 Builder
     *
     * @return NotificationCompat.Builder
     */
    public NotificationCompat.Builder createSystem() {
        return create(CHANNEL_ID_SYSTEM);
    }

    /**
     * 显示通知栏
     *
     * @param id           id
     * @param notification notification
     */
    public void show(int id, Notification notification) {
        Context context =  MyApplication.getInstance();
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, notification);
        }
    }

}
