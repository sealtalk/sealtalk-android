package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;

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
public class AddFriendFromContactActivity extends TitleAndSearchBaseActivity{
    private SealTitleBar titleBar;
    private AddFriendFromContactFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.new_friend_phone_contact_friend);

        setContentView(R.layout.add_friend_activty_add_from_contact);
        initFragment();

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
    public void onSearch(String keyword) {
        fragment.search(keyword);
    }
}
