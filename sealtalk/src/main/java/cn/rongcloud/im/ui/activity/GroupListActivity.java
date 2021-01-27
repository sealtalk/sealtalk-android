package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.fragment.SearchGroupByNameFragment;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static cn.rongcloud.im.ui.view.SealTitleBar.Type.NORMAL;

public class GroupListActivity extends TitleAndSearchBaseActivity implements OnGroupItemClickListener {
    private static final String TAG = "GroupListActivity";
    private SearchGroupByNameFragment searchGroupByNameFragment;
    private FrameLayout groupListContainerFl;
    private TextView emptyTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setType(NORMAL);
        getTitleBar().setTitle(R.string.seal_ac_search_group);
        setContentView(R.layout.activity_group_list);
        groupListContainerFl = findViewById(R.id.fl_content_fragment);
        emptyTv = findViewById(R.id.tv_empty_group_notice);
        searchGroupByNameFragment = new SearchGroupByNameFragment();
        searchGroupByNameFragment.setOnSearchResultListener(new SearchGroupByNameFragment.SearchResultListener() {
            @Override
            public void onSearchResult(String lastKeyWord, List<SearchModel> searchModels) {
                if (TextUtils.isEmpty(lastKeyWord) && (searchModels == null || searchModels.size() == 0)) {
                    emptyTv.setVisibility(View.VISIBLE);
                    groupListContainerFl.setVisibility(View.GONE);
                } else {
                    emptyTv.setVisibility(View.GONE);
                    groupListContainerFl.setVisibility(View.VISIBLE);
                }
            }
        });
        searchGroupByNameFragment.init(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content_fragment, searchGroupByNameFragment);
        transaction.commit();
    }

    @Override
    public void onSearch(String keyword) {
        searchGroupByNameFragment.search(keyword);
    }

    @Override
    public void onGroupClicked(GroupEntity groupEntity) {
        RongIM.getInstance().startConversation(this, Conversation.ConversationType.GROUP, groupEntity.getId(), groupEntity.getName());
    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                searchGroupByNameFragment.search(s.toString());
//            }
//        }, 300);
//    }
}
