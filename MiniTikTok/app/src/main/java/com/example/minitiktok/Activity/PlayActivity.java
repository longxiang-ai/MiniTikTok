package com.example.minitiktok.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.minitiktok.Constants;
import com.example.minitiktok.MyVideoAdapter;
import com.example.minitiktok.R;
import com.example.minitiktok.VideoListResponse;
import com.example.minitiktok.VideoMessage;
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

    private RecyclerView recyclerView;
    private MyVideoAdapter mAdapter;
    private ImageButton exit ;
    private LottieAnimationView nice ;

    VideoView videoView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        String VideoUrl=getIntent().getStringExtra("data");

        exitMain();
        watchlist();

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
        //长按点赞
        videoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    nice = findViewById(R.id.nice_view) ;
                    nice.setVisibility(View.VISIBLE);
                    nice.playAnimation();
                    nice.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nice.setVisibility(View.INVISIBLE);
                        }
                    },2000);
                    return true;
                }catch (Exception e)
                {
                    Toast.makeText(PlayActivity.this,"点赞出现问题"+e.toString(),Toast.LENGTH_SHORT).show();;
                    return false;
                }
            }
        });
    }
    //退出当前播放页面到首页
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
    //加载播放界面下方的播放列表
    private void watchlist(){
        recyclerView = findViewById(R.id.play_recycler);
        //更改数据时不会变更宽高
        recyclerView.setHasFixedSize(true);

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
            conn.setRequestProperty("accept", Constants.token);
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
