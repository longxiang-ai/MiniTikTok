package com.example.minitiktok;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.minitiktok.ui.post.PostFragment;

public class PostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PostFragment.newInstance())
                    .commitNow();
        }
    }
}
