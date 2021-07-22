package com.example.minitiktok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.example.minitiktok.Constants.BASE_URL;

public class PlayActivity extends AppCompatActivity implements MyVideoAdapter.IOnItemClickListener {

    String Default_Url = "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4";
    private RecyclerView recyclerView;
    private MyVideoAdapter mAdapter;
    private Button exit ;
    private RecyclerView.LayoutManager layoutManager;
    private GridLayoutManager gridLayoutManager;
    VideoView videoView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        String VideoUrl=getIntent().getStringExtra("data");

        exitMain();

        videoView = findViewById(R.id.play_video) ;
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.parse( VideoUrl ));
        videoView.start();
        // 单击播放暂停，再次单击继续播放
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying())
                {
                    videoView.pause();
                }
                else
                {
                    videoView.start();
                }
            }
        });
        watchlist();
    }

    private void exitMain(){
        exit = findViewById(R.id.play_exit) ;
        exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(PlayActivity.this,MainActivity.class) ;
                startActivity(intent);
            }
        });
    }
    private void watchlist(){
        recyclerView = findViewById(R.id.play_recycler);
        //更改数据时不会变更宽高
        recyclerView.setHasFixedSize(true);
        //创建线性布局管理器
        layoutManager = new LinearLayoutManager(this);
        //创建格网布局管理器
        gridLayoutManager = new GridLayoutManager(this, 2);
        //设置布局管理器，瀑布流播放的效果
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        //创建Adapter
        mAdapter = new MyVideoAdapter();
        //设置Adapter每个item的点击事件
        mAdapter.setOnItemClickListener(this);
        //设置Adapter
        recyclerView.setAdapter(mAdapter);
        //瀑布流播放的效果
        recyclerView.addItemDecoration(new DividerItemDecoration(this, StaggeredGridLayoutManager.VERTICAL));
        //动画
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(3000);
        recyclerView.setItemAnimator(animator);
        getData(null);
    }
    public void onItemCLick(int position, VideoMessage data) {
        Toast.makeText(PlayActivity.this, "点击了第" + (position+1) + "条", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PlayActivity.this,PlayActivity.class);
        // 将被点击的video url传递给下一层的playactivity
        intent.putExtra("data",data.getVideoUrl());
        startActivity(intent);
    }
    public void onItemLongCLick(int position, VideoMessage data){
        Toast.makeText(PlayActivity.this, "长按了第" + (position+1) + "条", Toast.LENGTH_SHORT).show();
    }
    private void getData(String studentId){
        Log.i("getData","尝试获取Data1");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("getData","尝试获取Data2");
                final VideoListResponse response = getDataFromInternet(studentId);
                if(response != null)
                {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.setData(response.feeds);
                        }
                    });
                }
            }
        }).start();
    }

    private VideoListResponse getDataFromInternet(String studentId) {
        Log.i("getDataFromInternet","尝试获取Internet Data,StudentID="+studentId);
        String urlStr;
        if (studentId != null)
        {
            urlStr = BASE_URL+"/video"+"?student_id="+studentId;
        }
        else
        {
            urlStr = BASE_URL+"/video";
        }
        VideoListResponse result = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept",Constants.token);
            if (conn.getResponseCode() == 200)
            {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                result = new Gson().fromJson(reader, new TypeToken<VideoListResponse>(){}.getType());
                reader.close();
                in.close();
            }
            else
            {
                Exception exception = new Exception("网络未知错误");
                conn.disconnect();
                throw exception;
            }
            conn.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PlayActivity.this, "网络异常" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return result;
    }
}
