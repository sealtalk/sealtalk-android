package cn.rongcloud.im.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.ForwardSearchAdapter;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.ui.interfaces.SearchableInterface;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.ForwardSearchViewModel;

/**
 * 搜索。
 */
public class ForwardSearchFragment extends BaseFragment implements SearchableInterface {
    private static final String TAG = "ForwardSearchFragment";
    private ForwardSearchAdapter adapter;
    private ForwardSearchViewModel viewModel;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private String initSearch;
    private OnContactItemClickListener contactListener;
    private OnGroupItemClickListener groupListener;

    @Override
    protected int getLayoutResId() {
        return R.layout.search_fragment_list;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        recyclerView = findView(R.id.rv_contacts);
        emptyView = findView(R.id.tv_empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter == null) {
            createAdapter();
        }
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onInitViewModel() {
        viewModel = createViewModel();
        viewModel.getSearchAll().observe(this, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchModels) {
                SLog.i(TAG, "searchModels.size() = " + searchModels.size());
                if (searchModels == null || searchModels.size() == 0 || (searchModels.size() == 1 && searchModels.get(0).getType() == R.layout.search_fragment_recycler_title_layout)) {
                    emptyView.setVisibility(View.VISIBLE);
                    String empty = String.format(getString(R.string.seal_search_empty), initSearch);
                    int start = empty.indexOf(initSearch);
                    emptyView.setText(CharacterParser.getSpannable(empty, start, start + initSearch.length()));
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (adapter != null) {
                        adapter.updateData(searchModels);
                    }
                }
            }
        });

        if (!TextUtils.isEmpty(initSearch)) {
            search(initSearch);
        }
    }


    /**
     * 创建 viewmodel
     * @return
     */
    public ForwardSearchViewModel createViewModel() {
        boolean isSelect = false;
        Bundle bundle = getArguments();
        if (bundle != null) {
            isSelect = getArguments().getBoolean(IntentExtra.IS_SELECT, false);
        }
        return  ViewModelProviders.of(this, new ForwardSearchViewModel.Factory(isSelect, getActivity().getApplication())).get(ForwardSearchViewModel.class);
    }

    @Override
    public void search(String match) {
        initSearch = match;
        if (viewModel != null) {
            viewModel.search(match);
        }
    }

    @Override
    public void clear() {
        if (adapter != null) {
            adapter.clear();
        }
    }

    /**
     * 群组项被点击了
     * @param groupEntity
     */
    protected void onItemGroupClicked(GroupEntity groupEntity) {
        if (groupListener != null) {
            groupListener.onGroupClicked(groupEntity);
        }
    }

    /**
     * 好友项被点击了
     * @param friendShipInfo
     */
    protected void onItemFriendClicked(FriendShipInfo friendShipInfo) {
        if (contactListener != null) {
            contactListener.onItemContactClick(friendShipInfo);
        }
    }



    /**
     * 设置群组点击项
     * @param listener
     */
    public void setOnGroupItemClickListener(OnGroupItemClickListener listener) {
        this.groupListener = listener;
    }

    /**
     * 设置联系人（好友）点击项
     * @param listener
     */
    public void setOnContactItemClickListener(OnContactItemClickListener listener) {
        this.contactListener = listener;
    }

    /**
     * 设置已经选择的用户
     * @param selectGroupIds
     * @param selectFriendIds
     */
    public void setSelectedIds(List<String> selectGroupIds, List<String> selectFriendIds) {
        if (adapter == null) {
            createAdapter();
        }
        adapter.setSelected(selectGroupIds, selectFriendIds);
    }

    private void createAdapter() {
        adapter = new ForwardSearchAdapter(new OnGroupItemClickListener() {
            @Override
            public void onGroupClicked(GroupEntity groupEntity) {
                onItemGroupClicked(groupEntity);
            }
        }, new OnContactItemClickListener() {
            @Override
            public void onItemContactClick(FriendShipInfo friendShipInfo) {
                onItemFriendClicked(friendShipInfo);
            }
        });
    }
}
