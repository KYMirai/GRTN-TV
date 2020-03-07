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

package xyz.kymirai.tv.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import xyz.kymirai.tv.presenter.CardPresenter;
import xyz.kymirai.tv.bean.Item;
import xyz.kymirai.tv.ui.activity.MoreActivity;
import xyz.kymirai.tv.utils.ChannelList;
import xyz.kymirai.tv.R;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;

    //http://www.gdtv.cn/f/images/zbbg.jpg

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();

        loadRows();

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadRows() {
        ChannelList.getList(getActivity(), (map) -> {
            ArrayObjectAdapter rowsAdapter = getAdapter() == null ? new ArrayObjectAdapter(new ListRowPresenter()) : (ArrayObjectAdapter) getAdapter();
            CardPresenter cardPresenter = new CardPresenter(true);

            int i = 0;
            for (Map.Entry<String, ArrayList<Item>> entry : map.entrySet()) {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                List<Item> list = entry.getValue();
                if (list != null)
                    for (Item movie : list)
                        listRowAdapter.add(movie);
                HeaderItem header = new HeaderItem(i++, entry.getKey());
                if (entry.getKey().equals("直播"))
                    rowsAdapter.add(0, new ListRow(header, listRowAdapter));
                else
                    rowsAdapter.add(new ListRow(header, listRowAdapter));
            }

            if (getAdapter() == null) setAdapter(rowsAdapter);
        });
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(getActivity(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title));

        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Item) {
                String url = ((Item) item).getUrl();
                if (url.startsWith("live:")) {
                    String new_url = url.substring(5);
                    Log.i(TAG, "onItemClicked: " + url);
                    new Thread(() -> {
                        try {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            //Uri uri = Uri.parse(new JSONArray(Jsoup.connect("http://star.gdtv.cn/m2o/channel/channel_info.php").timeout(5000).cookie("UM_distinctid", "1").get().body().text()).getJSONObject(0).getString("m3u8"));
                            Uri uri = Uri.parse(new_url);
                            it.setDataAndType(uri, "video/mp4");
                            startActivity(it);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();


                } else {
                    startActivity(new Intent(getActivity(), MoreActivity.class).putExtra("channel", ((Item) item).getTitle()).putExtra("url", url));
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            if (item instanceof Item) {
                mBackgroundUri = ((Item) item).getBackgroundImageUrl();
                startBackgroundTimer();
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(() -> updateBackground(mBackgroundUri));
        }
    }

}
