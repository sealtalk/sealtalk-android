package cn.rongcloud.im.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.SearchAdapter;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnChatItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnMessageRecordClickListener;
import cn.rongcloud.im.ui.interfaces.OnShowMoreClickListener;
import cn.rongcloud.im.ui.interfaces.SearchableInterface;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.viewmodel.SealSearchViewModel;
import cn.rongcloud.im.utils.log.SLog;

public class SearchBaseFragment extends Fragment implements SearchableInterface {
    private static final String TAG = "SearchBaseFragment";
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    protected SealSearchViewModel viewModel;
    protected String initSearch;
    private TextView tvEmpty;

    public void init(OnChatItemClickListener onChatItemClickListener,
                     OnGroupItemClickListener onGroupItemClickListener,
                     OnShowMoreClickListener onShowMoreClickListener,
                     OnContactItemClickListener onContactItemClickListener,
                     OnMessageRecordClickListener onMessageRecordClickListener) {
        searchAdapter = new SearchAdapter(onChatItemClickListener, onGroupItemClickListener, onShowMoreClickListener, onContactItemClickListener, onMessageRecordClickListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment_list, container, false);
        viewModel = ViewModelProviders.of(this).get(SealSearchViewModel.class);
        recyclerView = view.findViewById(R.id.rv_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchAdapter);
        tvEmpty = view.findViewById(R.id.tv_empty_view);
        return view;
    }

    public void updateData(List<SearchModel> data) {
        SLog.i(TAG, "updateData() size: " + data.size());
        if (data == null || data.size() == 0 || (data.size() == 1 && data.get(0).getType() == R.layout.search_fragment_recycler_title_layout)) {
            tvEmpty.setVisibility(View.VISIBLE);
            String empty = String.format(getString(R.string.seal_search_empty), initSearch);
            int start = empty.indexOf(initSearch);
            tvEmpty.setText(CharacterParser.getSpannable(empty, start, start + initSearch.length()));
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (searchAdapter != null) {
                searchAdapter.updateData(data);
            }
        }
    }

    public void search(String search) {
        SLog.i(TAG, "search: " + search);
        initSearch = search;
        if (TextUtils.isEmpty(search)) {
            clear();
        }
    }

    public void clear() {
        searchAdapter.updateData(new ArrayList<>());
    }

    /**
     * @return 上次搜索关键字
     */
    public String getInitSearch() {
        return initSearch;
    }
}
