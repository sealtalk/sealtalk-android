package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.adapter.viewholders.AddFriendFromContactItemViewHolder;
import cn.rongcloud.im.ui.fragment.AddFriendFromContactFragment;
import cn.rongcloud.im.ui.view.SealTitleBar;

/**
 * 从通讯录添加好友
 */
public class AddFriendFromContactActivity extends TitleBaseActivity implements View.OnClickListener {
    private TextView searchTv;
    private SealTitleBar titleBar;
    private AddFriendFromContactFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.new_friend_phone_contact_friend);

        setContentView(R.layout.add_friend_activty_add_from_contact);
        initView();
        initFragment();

    }

    private void initView() {
        searchTv = findViewById(R.id.tv_search);
        searchTv.setOnClickListener(this);

        titleBar.setOnSearchClearTextClickedListener(new SealTitleBar.OnSearchClearTextClickedListener() {
            @Override
            public void onSearchClearTextClicked() {
                showNormalTitle();
            }
        });
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearchOrExit();
            }
        });
        titleBar.addSeachTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString();
                if(fragment != null){
                    fragment.search(keyword);
                }
            }
        });
    }

    private void initFragment(){
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        fragment = (AddFriendFromContactFragment)supportFragmentManager.findFragmentByTag(AddFriendFromContactFragment.class.getSimpleName());
        if(fragment == null){
            fragment = new AddFriendFromContactFragment();
        }
        fragment.setAddFriendClickedListener(new AddFriendFromContactItemViewHolder.OnAddFriendClickedListener() {
            @Override
            public void onAddFriendClicked(String userId) {
                Intent intent = new Intent(AddFriendFromContactActivity.this, UserDetailActivity.class);
                intent.putExtra(IntentExtra.STR_TARGET_ID, userId);
                startActivity(intent);
            }
        });

        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.fragment_container, fragment, AddFriendFromContactFragment.class.getSimpleName());
        } else {
            fragmentTransaction.show(fragment);
        }
        fragmentTransaction.commit();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search:
                showSearch();
                break;
        }
    }

    /**
     * 显示搜索栏
     */
    private void showSearch(){
        setTitleBarType(SealTitleBar.Type.SEARCH);
        searchTv.setVisibility(View.GONE);
    }

    /**
     * 显示标题
     */
    private void showNormalTitle(){
        titleBar.getEtSearch().setText("");
        setTitleBarType(SealTitleBar.Type.NORMAL);
        searchTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        closeSearchOrExit();
    }

    /**
     * 关闭搜索或退出
     */
    private void closeSearchOrExit(){
        if(titleBar.getType() == SealTitleBar.Type.SEARCH){
            showNormalTitle();
        } else {
           finish();
        }
    }
}
