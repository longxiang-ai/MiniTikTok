package com.example.minitiktok;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoListResponse {
    @SerializedName("feeds")
    public List<VideoMessage> feeds;
    @SerializedName("success")
    public boolean success;
}

