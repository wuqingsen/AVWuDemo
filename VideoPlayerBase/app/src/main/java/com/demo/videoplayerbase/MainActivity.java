package com.demo.videoplayerbase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.kk.taurus.playerbase.config.PlayerLibrary;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.render.IRender;
import com.kk.taurus.playerbase.widget.BaseVideoView;

public class MainActivity extends AppCompatActivity{
    ImageView iv_playVideo;
    BaseVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_playVideo = findViewById(R.id.iv_playVideo);
        videoView = findViewById(R.id.video_view);

        videoView.setOnPlayerEventListener(new OnPlayerEventListener() {
            @Override
            public void onPlayerEvent(int eventCode, Bundle bundle) {

                switch (eventCode) {
                    case PLAYER_EVENT_ON_START:
                        Log.w("wqs", "开始播放");
                        break;
                    case PLAYER_EVENT_ON_RESUME:
                        Log.w("wqs", "恢复播放");
                        break;
                    case PLAYER_EVENT_ON_PAUSE:
                        Log.w("wqs", "暂停播放");
                        break;
                    case PLAYER_EVENT_ON_PLAY_COMPLETE:
                        Log.w("wqs", "播放完成");
                        break;
                }
            }
        });
        iv_playVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("wqs", "点击播放");
                String filePath = Environment.getExternalStorageDirectory() + "/" + "demo.mp4";
                videoView.setDataSource(new DataSource(filePath));
                videoView.setRenderType(IRender.RENDER_TYPE_TEXTURE_VIEW);
                videoView.start();
            }
        });

    }

}
