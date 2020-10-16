package com.demo.lupingwudemo.videoscreen;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.demo.lupingwudemo.R;
import com.demo.lupingwudemo.video.EncoderVideoThread;

/**
 * wuqingsen on 2020-10-15
 * Mailbox:1243411677@qq.com
 * annotation:录屏service
 */
public class ScreenRecorderService extends Service {
    private static MediaProjectionManager sMediaProjectionManager;
    private static Intent sData;
    private static boolean isInit;
    private MediaProjection mediaProjection;
    private static EncoderVideoThread mEncoderThread;
    private boolean isStop;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void start(Context context, MediaProjectionManager mMediaProjectionManager, Intent data) {
        sMediaProjectionManager = mMediaProjectionManager;
        sData = data;
        Intent service = new Intent(context, ScreenRecorderService.class);
        service.putExtra("stop", "start");
        context.startForegroundService(service);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void stop(Context context) {
        Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.putExtra("stop", "stop");
        context.startForegroundService(intent);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        isInit = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String stop = intent.getStringExtra("stop");
        if (stop != null && stop.equals("stop")) {
            mEncoderThread.quit();
            stopForeground(true);
            return super.onStartCommand(intent, flags, startId);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            createNotificationChannel();

            mediaProjection = sMediaProjectionManager.getMediaProjection(Activity.RESULT_OK, sData);   //根据返回结果，获取MediaProjection--保存帧数据
            //开启编码线程进行编码
            mEncoderThread = new EncoderVideoThread(mediaProjection);
            mEncoderThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 通知渠道的id
        String id = "my_channel_01";
        // 用户可以看到的通知渠道的名字.
        CharSequence name = getString(R.string.app_name);
//         用户可以看到的通知渠道的描述
        String description = "描述描述";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        mChannel = new NotificationChannel(id, name, importance);

//         配置通知渠道的属性
        mChannel.setDescription(description);
//         设置通知出现时的闪灯（如果 android 设备支持的话）
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
//         设置通知出现时的震动（如果 android 设备支持的话）
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//         最后在notificationmanager中创建该通知渠道 //
        mNotificationManager.createNotificationChannel(mChannel);

        // 为该通知设置一个id
        int notifyID = 1;
        // 通知渠道的id
        String CHANNEL_ID = "my_channel_01";
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(this)
                .setContentTitle("录制屏幕").setContentText(name + "正在录制屏幕内容...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID)
                .build();
        startForeground(1, notification);
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
