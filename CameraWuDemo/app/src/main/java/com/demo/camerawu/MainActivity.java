package com.demo.camerawu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.camerawu.util.PermissionsChecker;

public class MainActivity extends AppCompatActivity {
    private PermissionsChecker mPermissionsChecker;

    Button btnShowView, btnPictures, btnH264Record, btnH264Play;

    //定位权限,获取app内常用权限
    String[] permsLocation = {"android.permission.READ_PHONE_STATE"
            , "android.permission.ACCESS_COARSE_LOCATION"
            , "android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.READ_EXTERNAL_STORAGE"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.CAMERA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPermissionsChecker = new PermissionsChecker(MainActivity.this);
        if (mPermissionsChecker.lacksPermissions(permsLocation)) {
            ActivityCompat.requestPermissions(MainActivity.this, permsLocation, 1);
        }
        btnShowView = findViewById(R.id.btnShowView);
        btnPictures = findViewById(R.id.btnPictures);
        btnH264Record = findViewById(R.id.btnH264Record);
        btnH264Play = findViewById(R.id.btnH264Play);

        //预览
        btnShowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraShowActivity.class));
            }
        });
        //拍照
        btnPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PicturesActivity.class));
            }
        });
        //录制h264
        btnH264Record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, H264RecordActivity.class));
            }
        });
        //播放h264
        btnH264Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, H264PlayActivity.class));
            }
        });
    }
}
