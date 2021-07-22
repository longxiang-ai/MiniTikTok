package com.example.minitiktok;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.minitiktok.ui.post.PostFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostActivity extends AppCompatActivity implements SurfaceHolder.Callback{
        private SurfaceView mSurfaceView;
        private Camera mCamera;
        private MediaRecorder mMediaRecorder;
        private SurfaceHolder mHolder;
        private VideoView mVideoView;
        private Button mRecordButton;
        private Button mUploadButton;
        private boolean isRecording = false;
        private static String TAG = "PostActivity";
        private String mp4Path = "";

        public static void startUI(Context context) {
            Intent intent = new Intent(context, PostActivity.class);
            context.startActivity(intent);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.post_activity);
            mSurfaceView = findViewById(R.id.surfaceview);

            mVideoView = findViewById(R.id.videoview);
            mRecordButton = findViewById(R.id.bt_record);
            mUploadButton = findViewById(R.id.bt_upload);
            mHolder = mSurfaceView.getHolder();
            initCamera();
            mHolder.addCallback(this);

            mUploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: 尝试点击uploadButton");
                    Intent intent = new Intent(PostActivity.this,UploadActivity.class);
                    intent.putExtra("videoPath",mp4Path);
                    startActivity(intent);
                }
            });

        }

        @Override
        protected void onResume() {
            super.onResume();
            if (mCamera == null) {
                initCamera();
            }
            mCamera.startPreview();
        }

        @Override
        protected void onPause() {
            super.onPause();
            mCamera.stopPreview();
        }

        private void initCamera() {
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.set("orientation", "portrait");
            parameters.set("rotation", 90);
            mCamera.setParameters(parameters);
            // 判断是否为横屏，而决定是否进行旋转
            DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
            boolean isPortrait = dm.widthPixels < dm.heightPixels;
            if(isPortrait)
            {
                mCamera.setDisplayOrientation(90);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null) {
                return;
            }
            //停止预览效果
            mCamera.stopPreview();
            //重新设置预览效果
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }


        public void record(View view) {
            if (!isRecording) {
                if(prepareVideoRecorder())
                {
                    mRecordButton.setText("停止");
                    mMediaRecorder.start();
                }
            } else {
                // 停止录制
                mRecordButton.setText("录制");
                mUploadButton.setVisibility(View.VISIBLE);
                mRecordButton.setVisibility(View.GONE);
                Toast.makeText(this,"已经将录制的视频文件保存在"+mp4Path,Toast.LENGTH_SHORT).show();
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                try {
                    mMediaRecorder.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();
                mVideoView.setVisibility(View.VISIBLE);

                mVideoView.setVideoPath(mp4Path);
                mVideoView.start();
            }
            isRecording = !isRecording;
        }

        private boolean prepareVideoRecorder() {
            mMediaRecorder = new MediaRecorder();
            // Step 1: Unlock and set camera to MediaRecorder
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            // Step 2: Set sources
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            // Step 4: Set output file
            mp4Path = getOutputMediaPath();
            mMediaRecorder.setOutputFile(mp4Path);
            // Step 5: Set the preview output
            mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
            mMediaRecorder.setOrientationHint(90);
            // Step 6: Prepare configured MediaRecorder
            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException | IOException e) {
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        private void releaseMediaRecorder() {
//            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder=null;
        }

        private String getOutputMediaPath() {
            File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(mediaStorageDir, "IMG_" + timeStamp + ".mp4");
            if (!mediaFile.exists()) {
                mediaFile.getParentFile().mkdirs();
            }
            return mediaFile.getAbsolutePath();
        }
    }
