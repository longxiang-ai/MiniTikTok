package com.example.minitiktok.ui.data;


import java.util.ArrayList;
import java.util.List;

public class CoverDataSet {

    public static List<CoverData> getData() {
        List<CoverData> result = new ArrayList();
        result.add(new CoverData("让人忘记原唱的歌手", "524.6w","张三"));
        result.add(new CoverData("林丹退役", "433.6w","李四"));
        result.add(new CoverData("你在教我做事？", "357.8w"));
        result.add(new CoverData("投身乡村教育的燃灯者", "333.6w"));
        result.add(new CoverData("暑期嘉年华", "285.6w"));
        result.add(new CoverData("2020年三伏天有40天", "183.2w"));
        result.add(new CoverData("会跟游客合照的老虎", "139.4w"));
        result.add(new CoverData("苏州暴雨", "75.6w"));
        result.add(new CoverData("6月全国菜价上涨", "55w"));
        result.add(new CoverData("猫的第六感有多强", "43w"));
        result.add(new CoverData("IU真好看", "22.2w"));
        return result;
    }

}

