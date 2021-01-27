package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SimplePhoneContactInfo;
import cn.rongcloud.im.sms.SmsManager;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.fragment.InviteFriendFromContactFragment;
import cn.rongcloud.im.ui.view.SealTitleBar;

/**
 * 从通讯录邀请好友
 */
public class InviteFriendFromContactActivity extends TitleAndSearchBaseActivity {
    private InviteFriendFromContactFragment contactFragment;
    private SealTitleBar titleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.new_friend_invite_phone_friend);
        setContentView(R.layout.invite_friend_activty_add_from_contact);

        initView();
        initFragment();
    }
    private void initView() {
        titleBar.setOnBtnRightClickListener(getString(R.string.seal_send), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteSelectedContacts();
            }
        });
        enableConfirmButton(false);
    }

    private void initFragment(){
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        contactFragment = (InviteFriendFromContactFragment) supportFragmentManager.findFragmentByTag(InviteFriendFromContactFragment.class.getSimpleName());
        if (contactFragment == null) {
            contactFragment = new InviteFriendFromContactFragment();
        }

        // 设置选择通讯录监听
        contactFragment.setOnContactSelectedListener(new InviteFriendFromContactFragment.OnContactSelectedListener() {
            @Override
            public void OnContactSelected(ListItemModel changedModel, int totalSelected) {
                // 设置是否可以点击发送
                if (totalSelected > 0) {
                    enableConfirmButton(true);
                } else {
                    enableConfirmButton(false);
                }
            }
        });

        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if (!contactFragment.isAdded()) {
            fragmentTransaction.add(R.id.fragment_container, contactFragment, InviteFriendFromContactFragment.class.getSimpleName());
        } else {
            fragmentTransaction.show(contactFragment);
        }
        fragmentTransaction.commit();
    }


    @Override
    public void onSearch(String keyword) {
        contactFragment.search(keyword);
    }

    /**
     * 邀请选择的联系人
     */
    private void inviteSelectedContacts() {
        if (contactFragment != null) {
            List<SimplePhoneContactInfo> checkedContactInfo = contactFragment.getCheckedContactInfo();
            List<String> phoneList = new ArrayList<>();
            for (SimplePhoneContactInfo info : checkedContactInfo) {
                phoneList.add(info.getPhone());
            }

            // 跳转到系统发送短信界面
            SmsManager.sendInviteSMS(this, phoneList);
            finish();
        }
    }
}
