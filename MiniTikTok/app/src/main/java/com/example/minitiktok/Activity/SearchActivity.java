package com.example.minitiktok.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import static com.example.minitiktok.Constants.BASE_URL;

public class SearchActivity extends AppCompatActivity implements MyVideoAdapter.IOnItemClickListener {
    private static final String TAG = "TAG";

    private RecyclerView recyclerView;
    private Button search ;
    private EditText editText ;
    private MyVideoAdapter mAdapter;
    private ImageView img_no_found;
    private String ExtraValue ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        watchlist(null);
        img_no_found=findViewById(R.id.img_no_found);
        search = findViewById(R.id.search_button) ;
        editText = findViewById(R.id.search_text) ;
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraValue = editText.getText().toString();
                Toast.makeText(SearchActivity.this,"寻找学号："+ExtraValue,Toast.LENGTH_SHORT).show();
                watchlist(ExtraValue);
            }
        });
    }
    //加载搜索到的列表
    private void watchlist(String searchtext){

        recyclerView = findViewById(R.id.search_recycler);
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
        //设置瀑布流效果
        recyclerView.addItemDecoration(new DividerItemDecoration(this, StaggeredGridLayoutManager.VERTICAL));
        //动画
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(3000);
        recyclerView.setItemAnimator(animator);
        // ----------------------------拉取信息-------------------
        getData(searchtext);
    }

    private void getData(String search){
        Log.i("getData","尝试获取Data1");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("getData","尝试获取Data2");
                final VideoListResponse response = getDataFromInternet(search,"student_id");
                if(response != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!response.feeds.isEmpty())
                            {
                                recyclerView.setVisibility(View.VISIBLE);
                                img_no_found.setVisibility(View.GONE);
                            }
                            else
                            {
                                img_no_found.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                Toast.makeText(SearchActivity.this,"很抱歉没有找到",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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

    private VideoListResponse getDataFromInternet(String searchtext , String type ) {
        Log.i("getDataFromInternet","尝试获取Internet Data,user_id="+searchtext);
        String urlStrId , urlStrName, urlStrExtra;
        String urlStr = BASE_URL+"video";
        if ( searchtext!=null )
            urlStr = String.format("%svideo?%s=%s",BASE_URL,type,searchtext) ;
//        String urlStrTar = BASE_URL+"video?"+type+"="+searchtext ;
//        urlStrId = BASE_URL+"video"+"?student_id="+searchtext;
//        urlStrName = BASE_URL+"video"+"?user_name="+searchtext;
//        urlStrExtra = BASE_URL+"video"+"?extra_value="+searchtext;
        Log.i(TAG, "getDataFromInternet: "+urlStr);
        VideoListResponse result = null ;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("accept", Constants.token);
                if (conn.getResponseCode() == 200) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    result = new Gson().fromJson(reader, new TypeToken<VideoListResponse>() {}.getType());
                    reader.close();
                    in.close();
                    Log.i(TAG, "getDataFromInternet: search for "+searchtext+" --- over");
                } else {
                    Exception exception = new Exception("网络未知错误");
                    conn.disconnect();
                    throw exception;
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchActivity.this, "网络异常" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
//        Toast.makeText(SearchActivity.this,"Totally :"+result.feeds.size(),Toast.LENGTH_SHORT).show();
        Log.i(TAG, "getDataFromInternet: totally there are "+result.feeds.size()+"contexts");
        return result;
    }

    public void onItemCLick(int position, VideoMessage data) {
//        Log.d(TAG, "onItemCLick: 尝试点击该item");
        Toast.makeText(SearchActivity.this, "点击了第" + (position+1) + "条", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SearchActivity.this,PlayActivity.class);
        // 将被点击的video url传递给playactivity
        intent.putExtra("data",data.getVideoUrl());
        startActivity(intent);
    }
    public void onItemLongCLick(int position, VideoMessage data){
        Toast.makeText(SearchActivity.this, "长按了第" + (position+1) + "条", Toast.LENGTH_SHORT).show();
    }

}

