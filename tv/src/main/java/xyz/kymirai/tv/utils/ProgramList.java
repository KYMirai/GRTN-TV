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
import android.util.Log;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.kymirai.tv.bean.Item;

public final class ProgramList {
    public enum Type {
        pp, html, nomore
    }

    public interface ResponseHandler {
        void onResponse(Type type, @NonNull ArrayList<Item> list);
    }

    public static void getList(Activity activity, String url, ResponseHandler responseHandler) {
        new Thread(() -> {
            //Map<String, ArrayList<Item>> map = new HashMap<>();
            try {
                Document doc = Jsoup.connect(url)
                        .timeout(5000) // 设置超时时间
                        .get();
                Type t = Type.pp;
                final Type type;
                try {
                    t = doc.getElementsByClass("meneame").get(0).getElementsByTag("a").get(0).attr("href").contains("?pp=") ? Type.pp : Type.html;
                } catch (Exception e) {
                    t = Type.nomore;
                } finally {
                    type = t;
                }
                activity.runOnUiThread(() -> responseHandler.onResponse(
                        type,
                        setupItems(doc.getElementsByClass("clearfix").get(1).getElementsByTag("li"))
                        )
                );
                //map.put("program", setupItems(doc.getElementsByClass("clearfix").get(1).getElementsByTag("li")));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }).start();
    }

    private static ArrayList<Item> setupItems(Elements elements) {
        ArrayList<Item> list = new ArrayList<>();
        for (Element element : elements) {
            list.add(
                    Item.build(
                            element.getElementsByClass("cdate").get(0).text(),
                            element.getElementsByTag("a").get(0).attr("title"),
                            element.getElementsByTag("a").get(0).attr("href"),
                            element.getElementsByTag("img").get(0).attr("src"),
                            ""
                    )
            );
        }
        return list;
    }
}