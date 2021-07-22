package com.example.minitiktok;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.minitiktok.API.UploadResponse;
import com.example.minitiktok.API.VideoAPI;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.minitiktok.Constants.BASE_URL;
import static com.example.minitiktok.Constants.STUDENT_ID;
import static com.example.minitiktok.Constants.USER_NAME;
import static com.example.minitiktok.Constants.token;


public class UploadActivity extends AppCompatActivity {
    private static final String TAG = "UploadActivity";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final int REQUEST_CODE_COVER_IMAGE = 101;
    private static final String COVER_IMAGE_TYPE = "image/*";
    private static final String VIDEO_TYPE = "video/*";
    private VideoAPI api;
    private Uri coverImageUri;
    private String videoPath;

    private ImageView coverSD;
    private EditText toEditText;
    private EditText contentEditText ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: 开始创建工程");

        initNetwork();
        setContentView(R.layout.upload_activity);
        coverSD = findViewById(R.id.sd_cover);

        findViewById(R.id.btn_cover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile(REQUEST_CODE_COVER_IMAGE, COVER_IMAGE_TYPE, "选择图片");
            }
        });

        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();

            }
        });
        // ------------------------------ 选择视频路径 -------------------------------
        Intent intent=getIntent();
        videoPath =intent.getStringExtra("videoPath");
        Log.d(TAG, "onCreate: 拿到的videoPath"+videoPath);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_COVER_IMAGE == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                coverImageUri = data.getData();
                coverSD.setImageURI(coverImageUri);
                Log.d(TAG, "onActivityResult: 成功设置????");
                if (coverImageUri != null) {
                    Log.d(TAG, "pick cover image " + coverImageUri.toString());
                } else {
                    Log.d(TAG, "uri2File fail " + data.getData());
                }

            } else {
                Log.d(TAG, "file pick fail");
            }
        }
    }

    private void initNetwork() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // 生成api对象
        api = retrofit.create(VideoAPI.class);
    }

    private void getFile(int requestCode, String type, String title) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    private void submit() {

        byte[] coverImageData = readDataFromUri(coverImageUri);
        if (coverImageData == null || coverImageData.length == 0) {
            Toast.makeText(this, "封面不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if ( coverImageData.length >= MAX_FILE_SIZE) {
            Toast.makeText(this, "文件过大", Toast.LENGTH_SHORT).show();
            return;
        }
        // 封面图选择
        MultipartBody.Part _coverPart = MultipartBody.Part.createFormData ("cover_image",
                "cover.png",
                RequestBody.create(MediaType. parse ("multipart/form-data"),
                        coverImageData));
        Toast.makeText(this, "现在选择的图片："+coverImageUri.toString(), Toast.LENGTH_SHORT).show();
        // 视频信息选择
        MultipartBody.Part _videoPart = MultipartBody.Part.createFormData ("video",
                "video.mp4",
                RequestBody.create(MediaType. parse ("multipart/form-data"),
                        new File(videoPath)));
        Toast.makeText(this, "现在选择的视频："+videoPath, Toast.LENGTH_SHORT).show();
        try{
            Log.i("upload","尝试上传");
            Call<UploadResponse> repos = api.submitMessage(STUDENT_ID,USER_NAME,"",_coverPart,_videoPart,token);
            repos.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(final Call<UploadResponse> call, final Response<UploadResponse> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    final UploadResponse repoList = response.body();
                    if (repoList == null) {
                        return;
                    }

                    finish();
                }
                @Override
                public void onFailure(final Call<UploadResponse> call, final Throwable t) {
                    t.printStackTrace();
                }
            });

//            Toast.makeText(this,"试一下行不行",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.i("upload","尝试上传失败");
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
//        finish();
    }
    
    private byte[] readDataFromUri(Uri uri) {
        byte[] data = null;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            data = Util.inputStream2bytes(is);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


}
