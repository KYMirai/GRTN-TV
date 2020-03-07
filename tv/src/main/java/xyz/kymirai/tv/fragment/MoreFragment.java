package xyz.kymirai.tv.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import xyz.kymirai.tv.bean.Item;
import xyz.kymirai.tv.presenter.CardPresenter;
import xyz.kymirai.tv.utils.ProgramList;

public class MoreFragment extends BrowseFragment {
    int page = 0;
    String url = "";
    boolean finish = false;
    ProgramList.Type type = ProgramList.Type.pp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupUIElements(getActivity().getIntent().getStringExtra("channel"));

        url = getActivity().getIntent().getStringExtra("url");
        loadRows(page++);

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadRows(int page) {
        //url = "http://v.gdtv.cn/";
        if (!finish) {
            ProgramList.getList(getActivity(), url + (type == ProgramList.Type.pp ? "?pp=" + (48 * page) : "index_" + (page + 2) + ".html"), (type, list) -> {
                ArrayObjectAdapter rowsAdapter = getAdapter() == null ? new ArrayObjectAdapter(new ListRowPresenter()) : (ArrayObjectAdapter) getAdapter();
                CardPresenter cardPresenter = new CardPresenter();
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

                for (Item movie : list)
                    listRowAdapter.add(movie);
                HeaderItem header = new HeaderItem(page, "");//getTitle().toString());
                rowsAdapter.add(new ListRow(header, listRowAdapter));
                //}
                if (page == 0) {
                    if (type == ProgramList.Type.nomore) finish = true;
                    this.type = type;
                    setAdapter(rowsAdapter);
                    setSelectedPosition(0);
                    setOnItemViewSelectedListener(new ItemViewSelectedListener());
                }
                if (list.size() < 48) {
                    finish = true;
                }

            });
        }
    }

    private void setupUIElements(String title) {
        setTitle(title);// + "-节目单");
        setHeadersState(HEADERS_DISABLED);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Item) {
                new Thread(() -> {
                    try {
                        Document doc = Jsoup.connect(((Item) item).getUrl())
                                .timeout(5000)
                                .get();
                        Intent it = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(doc.getElementById("m3u8").val());
                        it.setDataAndType(uri, "video/mp4");
                        startActivity(it);
                    } catch (IOException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show());
                    }
                }).start();

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
            if (!finish && getAdapter().size() - getSelectedPosition() < 3) {
                loadRows(page++);
            }
        }
    }
}
