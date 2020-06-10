package com.demo.lupingwudemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.lupingwudemo.audiovideo.AudioVideoCodecActivity;
import com.demo.lupingwudemo.audiovideotwo.AudioVideoCodecTwoActivity;
import com.demo.lupingwudemo.utils.PermissionsChecker;
import com.demo.lupingwudemo.video.VideoCodecActivity;

/**
 * wuqingsen on 2020-06-04
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class MainActivity extends AppCompatActivity {
    private PermissionsChecker mPermissionsChecker;
    Button btn_video, btn_avdiovideo, btn_avdiovideo2;

    //定位权限,获取app内常用权限
    String[] permsLocation = {"android.permission.READ_PHONE_STATE"
            , "android.permission.ACCESS_COARSE_LOCATION"
            , "android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.READ_EXTERNAL_STORAGE"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.CAMERA"
            , "android.permission.RECORD_AUDIO"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPermissionsChecker = new PermissionsChecker(MainActivity.this);
        if (mPermissionsChecker.lacksPermissions(permsLocation)) {
            ActivityCompat.requestPermissions(MainActivity.this, permsLocation, 1);
        }
        btn_video = findViewById(R.id.btn_video);
        btn_avdiovideo = findViewById(R.id.btn_avdiovideo);
        btn_avdiovideo2 = findViewById(R.id.btn_avdiovideo2);

        //mediaCodec录屏
        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoCodecActivity.class));
            }
        });
        //mediaCodec录音录屏
        btn_avdiovideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AudioVideoCodecActivity.class));
            }
        });
        //mediaCodec录音录屏2
        btn_avdiovideo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AudioVideoCodecTwoActivity.class));
            }
        });
    }
}
