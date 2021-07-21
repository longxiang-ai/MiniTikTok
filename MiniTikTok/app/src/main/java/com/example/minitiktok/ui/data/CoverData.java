package com.example.minitiktok.ui.data;

public class CoverData {
    public String title;
    public String poster;
    public String hot;

    public CoverData(String title, String hot,String poster) {
        this.title = title;
        this.poster = poster;
        this.hot = hot;
    }
    public CoverData(String title, String hot) {
        this.title = title;
        this.poster = "王二麻子";
        this.hot = hot;
    }
}
