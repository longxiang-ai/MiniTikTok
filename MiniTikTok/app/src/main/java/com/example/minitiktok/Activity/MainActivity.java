package com.example.minitiktok.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

import static com.example.minitiktok.Constants.BASE_URL;

public class MainActivity extends AppCompatActivity implements MyVideoAdapter.IOnItemClickListener {
    private final static int PERMISSION_REQUEST_CODE = 1001;
    protected ImageButton btn_post;
    protected ImageButton btn_search;
    protected ImageButton btn_video ;

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private MyVideoAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initButtons();
        //获取实例
        recyclerView = findViewById(R.id.recycler);
        //更改数据时不会变更宽高
        recyclerView.setHasFixedSize(true);
        //设置布局管理器
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        //创建Adapter
        mAdapter = new MyVideoAdapter();
        //设置Adapter每个item的点击事件
        mAdapter.setOnItemClickListener(this);
        //设置Adapter
        recyclerView.setAdapter(mAdapter);

//        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, StaggeredGridLayoutManager.VERTICAL));
        //动画
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(3000);
        recyclerView.setItemAnimator(animator);
        // ----------------------------拉取信息-------------------
        getData(null);
    }

    private void initButtons() {
        btn_post = findViewById(R.id.btn_post);

        btn_post.getBackground().setAlpha(0);
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 动态获取权限
                requestPermission();
                Intent intent = new Intent(MainActivity.this,PostActivity.class);
                Log.d(TAG,"跳转至PostActivity");
                startActivity(intent);
            }
        });
        btn_video = findViewById(R.id.btn_video) ;
        btn_video.getBackground().setAlpha(100);
        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoPostActivity.class) ;
                Log.d(TAG, "onClick: 跳转至VideoPostActivity");
                startActivity(intent);
            }
        });
        btn_search = findViewById(R.id.btn_search);
        btn_search.getBackground().setAlpha(100);
        btn_search.setBackgroundColor(Color.TRANSPARENT) ;
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                Log.d(TAG,"跳转至SearchActivity");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemCLick(int position, VideoMessage data) {
        Log.d(TAG, "onItemCLick: 尝试点击该item");
        Toast.makeText(MainActivity.this, "点击了第" + (position+1) + "条", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,PlayActivity.class);
        // 将被点击的video url传递给PlayActivity
        intent.putExtra("data",data.getVideoUrl());
        startActivity(intent);
    }

    @Override
    public void onItemLongCLick(int position, VideoMessage data) {
        Toast.makeText(MainActivity.this, "长按了第" + (position+1) + "条", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "网络异常" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return result;
    }

    private void recordVideo() {
        PostActivity.startUI(this);
    }

    private void requestPermission() {
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (hasCameraPermission && hasAudioPermission) {
            recordVideo();
        } else {
            List<String> permission = new ArrayList<String>();
            if (!hasCameraPermission) {
                permission.add(Manifest.permission.CAMERA);
            }
            if (!hasAudioPermission) {
                permission.add(Manifest.permission.RECORD_AUDIO);
            }
            ActivityCompat.requestPermissions(this, permission.toArray(new String[permission.size()]), PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermission = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        if (hasPermission) {
            recordVideo();
        } else {
            Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStart(){
        super.onStart();
        getData(null);
    }
}
