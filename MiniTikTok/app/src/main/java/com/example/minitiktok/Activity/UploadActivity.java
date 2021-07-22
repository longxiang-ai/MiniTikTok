package com.example.minitiktok.Activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.minitiktok.UploadResponse;
import com.example.minitiktok.API.VideoAPI;
import com.example.minitiktok.R;
import com.example.minitiktok.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

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
import static java.lang.Math.max;
import static java.lang.Math.min;


public class UploadActivity extends AppCompatActivity {
    private static final String TAG = "UploadActivity";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final int REQUEST_CODE_COVER_IMAGE = 101;
    private static final String COVER_IMAGE_TYPE = "image/*";
    private static final String VIDEO_TYPE = "video/*";
    private VideoAPI api;
    private Uri coverImageUri = null;
    byte[] coverImageData;
    Bitmap coverImageBitmap;
    private String videoPath;

    private ImageView coverSD;
    private Button BTN_COVER;
    private Button BTN_SUBMIT ;

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

        findViewById(R.id.btn_cover_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coverImageData = getCoverImageAuto();
                coverSD.setImageBitmap(coverImageBitmap);
            }
        });

        //点击提交隐藏按钮和展示加载动画
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
                // --------------- 调整设置逻辑 -----------------
                coverImageData = readDataFromUri(coverImageUri);
                // --------------- 调整设置逻辑 -----------------
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

    private byte[] getCoverImageAuto(){
        MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
        mmdr.setDataSource(videoPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        coverImageBitmap = mmdr.getFrameAtTime();
        coverImageBitmap = zoomBitmap(coverImageBitmap,min(getWindow().getWindowManager().getDefaultDisplay().getWidth()/2,coverImageBitmap.getWidth()),min(400,coverImageBitmap.getHeight()));
        coverImageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }
    private void submit() {
        if (coverImageData == null || coverImageData.length == 0) {
            Toast.makeText(this, "封面不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if ( coverImageData.length >= MAX_FILE_SIZE) {
            Toast.makeText(this, "文件过大", Toast.LENGTH_SHORT).show();
            return;
        }
        // 封面能够提交 显示提交过程动画
        BTN_COVER = findViewById(R.id.btn_cover) ;
        BTN_SUBMIT = findViewById(R.id.btn_submit) ;
        BTN_COVER.setVisibility(View.GONE);
        BTN_SUBMIT.setVisibility(View.GONE);
        View loading = findViewById(R.id.lottie_view) ;
        loading.setVisibility(View.VISIBLE);
        View text = findViewById(R.id.loading) ;
        text.setVisibility(View.VISIBLE);
        // 封面图选择
        MultipartBody.Part _coverPart = MultipartBody.Part.createFormData ("cover_image",
                "cover.png",
                RequestBody.create(MediaType. parse ("multipart/form-data"),
                        coverImageData));
        if (coverImageUri!=null)
            Toast.makeText(this, "现在选择的图片："+coverImageUri.toString(), Toast.LENGTH_SHORT).show();
        else
        {
            Toast.makeText(this, "现在选择的图片：视频首帧", Toast.LENGTH_SHORT).show();
        }
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


                    Toast.makeText(UploadActivity.this,"上传成功！",Toast.LENGTH_SHORT).show();
                    // 上传完成后略微延迟再返回MainActivity，替换原先的finish
                    Handler handler=new Handler();
                    Runnable runnable=new Runnable(){
                        @Override
                        public void run() {
                            Intent intent = new Intent(UploadActivity.this,MainActivity.class);
                            startActivity(intent);
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                }
                @Override
                public void onFailure(final Call<UploadResponse> call, final Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        catch (Exception e)
        {
            Log.i("upload","尝试上传失败");
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
