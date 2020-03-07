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

package xyz.kymirai.tv.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;

/*
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
public class Item implements Serializable {
    private static long count = 0;
    static final long serialVersionUID = 727566175075960653L;
    private long id;
    private String title;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String url;

    private Item() {
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getBackgroundImageUrl() {
        return bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", backgroundImageUrl='" + bgImageUrl + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}';
    }

    public static Item build(
            String title,
            String description,
            String url,
            String cardImageUrl,
            String backgroundImageUrl) {
        Item item = new Item();
        item.id = count++;
        item.title = title;
        item.description = description;
        item.cardImageUrl = cardImageUrl;
        item.bgImageUrl = backgroundImageUrl;
        item.url = url;
        return item;
    }
}
