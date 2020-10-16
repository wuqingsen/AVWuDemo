package com.demo.lupingwudemo;

import android.app.Application;

/**
 * wuqingsen on 2020-10-12
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MyApplication extends Application {

    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static MyApplication getInstance() {
        return myApplication;
    }
}
