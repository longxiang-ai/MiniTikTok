package com.example.minitiktok;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayActivity extends AppCompatActivity {

    String Default_Url = "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4";
    VideoView videoView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        videoView = findViewById(R.id.video_default) ;
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.parse( Default_Url));
        videoView.start();

    }

}
