/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package xyz.kymirai.tv.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xyz.kymirai.tv.bean.Item;

public final class ChannelList {

    public interface ResponseHandler {
        void onResponse(Map<String, ArrayList<Item>> map);
    }

    public static void getList(Activity activity, ResponseHandler responseHandler) {
        new Thread(() -> {
            Map<String, ArrayList<Item>> map = new HashMap<>();

            try {
                Document doc = Jsoup.connect("http://v.gdtv.cn/")
                        .timeout(5000) // 设置超时时间
                        .get();
                for (Element element : doc.getElementsByClass("mdnav").get(0).getElementsByTag("li")) {
                    map.put(element.text(), setupMovies(doc.getElementById(element.child(0).attr("href").substring(1))));
                }

            } catch (IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> Toast.makeText(activity, "频道列表加载失败", Toast.LENGTH_SHORT).show());
            }
            activity.runOnUiThread(() -> responseHandler.onResponse(map));
        }).start();
        new Thread(() -> {
            Map<String, ArrayList<Item>> map = new HashMap<>();

            try {
                Document doc = Jsoup.connect("http://star.gdtv.cn/")
                        .timeout(5000) // 设置超时时间
                        .get();
                ArrayList<Item> list = new ArrayList<>();
                for (Element mc : doc.getElementsByClass("mclist").get(0).getElementsByTag("li")) {
                    try {
                        JSONObject jo = new JSONArray(Jsoup.connect("http://www.gdtv.cn/m2o/channel/channel_info.php?id="
                                + Jsoup.connect(mc.child(0).attr("href"))
                                .timeout(5000)
                                .get()
                                .getElementById("m2o_player")
                                .text()
                        )
                                .timeout(5000)
                                .cookie("UM_distinctid", "1")
                                .get()
                                .body()
                                .text()).getJSONObject(0);
                        Object rectangle = jo.getJSONObject("logo").get("rectangle");
                        String url = "";
                        if (rectangle instanceof JSONObject) {
                            JSONObject j = ((JSONObject) rectangle);
                            url = j.getString("host") + j.getString("dir") + j.getString("filepath") + j.getString("filename");
                        } else if (rectangle instanceof String) {
                            url = (String) rectangle;
                        }
                        if (TextUtils.isEmpty(url)) url = "http://www.gdtv.cn/f/images/zbbg.jpg";
                        list.add(
                                Item.build(
                                        mc.text(),
                                        "",
                                        "live:" + jo.getString("m3u8"),
                                        url,
                                        ""
                                )
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                map.put("直播", list);
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> Toast.makeText(activity, "直播列表加载失败", Toast.LENGTH_SHORT).show());
            }

            activity.runOnUiThread(() -> responseHandler.onResponse(map));
        }).start();
    }

    private static ArrayList<Item> setupMovies(Element pd) {
        ArrayList<Item> list = new ArrayList<>();
        for (Element element : pd.getElementsByClass("clearfix").get(0).getElementsByTag("li")) {
            Element tit = element.getElementsByClass("tit").get(0);
            list.add(
                    Item.build(
                            tit.text(),
                            "",
                            tit.child(0).attr("href"),
                            element.getElementsByTag("img").get(0).attr("src"),
                            ""
                    )
            );
        }
        return list;
    }
}