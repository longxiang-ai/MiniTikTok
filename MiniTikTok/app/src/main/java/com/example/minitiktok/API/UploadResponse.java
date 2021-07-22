package com.example.minitiktok.API;

import com.example.minitiktok.VideoMessage;
import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("result")
    public VideoMessage videoMessage;
    @SerializedName("success")
    public boolean success;
    @SerializedName("error")
    public String error;
}