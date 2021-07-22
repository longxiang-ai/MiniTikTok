package com.example.minitiktok.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minitiktok.UploadResponse;
import com.example.minitiktok.API.VideoAPI;
import com.example.minitiktok.R;
import com.example.minitiktok.Util;

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

public class VideoPostActivity extends AppCompatActivity {

    private static final String TAG = "VideoUploadActivity";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final int REQUEST_CODE_COVER_IMAGE = 101;
    private static final int REQUEST_CODE_VIDEO = 102 ;
    private static final String COVER_IMAGE_TYPE = "image/*";
    private static final String VIDEO_TYPE = "video/*";
    private VideoAPI api;
    private Uri coverImageUri;
    private Uri videoUri ;
    private String videoPath;

    private ImageView coverSD ;
    private TextView videoInfo ;
    private Button BTN_COVER ;
    private Button BTN_SUBMIT ;
    private Button BTN_VIDEO ;

    private boolean VIDEO_OK = false ;
    private boolean IMAGE_OK = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNetwork();
        setContentView(R.layout.activity_video_post);

        coverSD = findViewById(R.id.sd_cover2);
        videoInfo = findViewById(R.id.video_info) ;
        Click() ;
    }

    private void Click(){
        BTN_COVER = findViewById(R.id.btn_cover2) ;
        BTN_SUBMIT = findViewById(R.id.btn_submit2) ;
        BTN_VIDEO = findViewById(R.id.btn_context2) ;
        BTN_COVER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile(REQUEST_CODE_COVER_IMAGE, COVER_IMAGE_TYPE, "选择图片");
            }
        });
        BTN_VIDEO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile(REQUEST_CODE_VIDEO, VIDEO_TYPE ,"选择视频");
            }
        });
        BTN_SUBMIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( IMAGE_OK == false ) {
                    Toast.makeText(VideoPostActivity.this,"请选择封面",Toast.LENGTH_SHORT).show();
                    return;
                }else if ( VIDEO_OK == false ) {
                    Toast.makeText(VideoPostActivity.this,"请选择视频",Toast.LENGTH_SHORT).show();
                    return;
                }
                Submit();
//                BTN_COVER.setVisibility(View.GONE);
//                BTN_SUBMIT.setVisibility(View.GONE);
//                View loading = findViewById(R.id.lottie_view2) ;
//                loading.setVisibility(View.VISIBLE);
//                View text = findViewById(R.id.loading2) ;
//                text.setVisibility(View.VISIBLE);
            }
        });
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
                    Toast.makeText(this,"image pick success",Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "uri2File fail " + data.getData());
                }
                IMAGE_OK = true ;
            } else {
                Log.d(TAG, "image file pick fail");
                Toast.makeText(this,"image pick fail",Toast.LENGTH_SHORT).show();
            }
        }else if ( REQUEST_CODE_VIDEO == requestCode ) {
            if (resultCode == Activity.RESULT_OK ){
                videoUri = data.getData() ;
                videoInfo.setText(videoUri.toString());
                if (videoUri != null) {
                    Log.d(TAG, "pick cover image " + videoUri.toString());
                    Toast.makeText(this,"video pick success",Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "uri2File fail " + data.getData());
                }
                VIDEO_OK = true ;
            }else {
                Log.d(TAG, "onActivityResult: video file pick fail");
                Toast.makeText(this,"video pick fail",Toast.LENGTH_SHORT).show();
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


    private void Submit() {
        if ( IMAGE_OK == false ) {
            Toast.makeText(this,"请选择封面",Toast.LENGTH_SHORT).show();
            return;
        }else if ( VIDEO_OK == false ) {
            Toast.makeText(this,"请选择视频",Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] coverImageData = readDataFromUri(coverImageUri);
        byte[] videoData = readDataFromUri(videoUri) ;
        if (coverImageData == null || coverImageData.length == 0) {
            Toast.makeText(this, "封面不存在", Toast.LENGTH_SHORT).show();
            return;
        }if ( coverImageData.length >= MAX_FILE_SIZE) {
            Toast.makeText(this, "封面文件过大", Toast.LENGTH_SHORT).show();
            return;
        }if (videoData == null || videoData.length == 0 ) {
            Toast.makeText(this, "视频不存在", Toast.LENGTH_SHORT).show();
            return;
        }if ( videoData.length >= 100*MAX_FILE_SIZE) {
            Toast.makeText(this, "视频文件过大", Toast.LENGTH_SHORT).show();
            return;
        }
        BTN_COVER.setVisibility(View.GONE);
        BTN_SUBMIT.setVisibility(View.GONE);
        BTN_VIDEO.setVisibility(View.GONE);
        View loading = findViewById(R.id.lottie_view2) ;
        loading.setVisibility(View.VISIBLE);
        View text = findViewById(R.id.loading2) ;
        text.setVisibility(View.VISIBLE);
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
                        videoData));
        Toast.makeText(this, "现在选择的视频："+videoUri.toString(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(VideoPostActivity.this,"上传成功！",Toast.LENGTH_SHORT).show();
                    // 上传完成后略微延迟再返回MainActivity，替换原先的finish
                    Handler handler=new Handler();
                    Runnable runnable=new Runnable(){
                        @Override
                        public void run() {
                            Intent intent = new Intent(VideoPostActivity.this,MainActivity.class);
                            startActivity(intent);
                        }
                    };
                    handler.postDelayed(runnable, 2000);
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
}
