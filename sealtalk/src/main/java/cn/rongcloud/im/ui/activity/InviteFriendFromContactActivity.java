package cn.rongcloud.im.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SimplePhoneContactInfo;
import cn.rongcloud.im.qrcode.QRCodeManager;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.fragment.InviteFriendFromContactFragment;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.RongIM;

/**
 * 从通讯录邀请好友
 */
public class InviteFriendFromContactActivity extends TitleBaseActivity implements View.OnClickListener {
    private InviteFriendFromContactFragment contactFragment;
    private TextView searchTv;
    private SealTitleBar titleBar;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.new_friend_invite_phone_friend);
        setContentView(R.layout.invite_friend_activty_add_from_contact);

        handler = new Handler();

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
        enableSendButton(false);

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
                handler.removeCallbacks(searchKeywordRunnable);
                String keyword = s.toString();
                int delay = 500;
                // 当输入空白时立即显示结果
                if(TextUtils.isEmpty(keyword)){
                    delay = 0;
                }
                handler.postDelayed(searchKeywordRunnable, delay);
            }
        });
    }

    /**
     * 触发搜索操作，因为配合 handler 使用，所以单独实现
     */
    private Runnable searchKeywordRunnable = new Runnable() {
        @Override
        public void run() {
            if(titleBar != null && contactFragment != null) {
                String keyword = titleBar.getEtSearch().getText().toString();
                contactFragment.search(keyword);
            }
        }
    };

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
                    enableSendButton(true);
                } else {
                    enableSendButton(false);
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

            sendInviteSMS(phoneList);
            finish();
        }
    }

    /**
     * 跳转到系统发送短信界面
     *
     * @param phoneNumberList
     */
    private void sendInviteSMS(List<String> phoneNumberList) {
        String smsContent = "";
        try {
            StringBuilder phoneBuilder = new StringBuilder();
            for (String number : phoneNumberList) {
                phoneBuilder.append(number).append(";");
            }

            QRCodeManager qrCodeManager = new QRCodeManager(this);
            String myUrl = qrCodeManager.generateUserQRCodeContent(RongIM.getInstance().getCurrentUserId());
            smsContent = getString(R.string.sms_share_invite_friend_content_format, myUrl);

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + Uri.encode(phoneBuilder.toString())));
            // 兼容旧版发送短信加入 address 字段信息
            intent.putExtra("address", phoneBuilder.toString());
            intent.putExtra("sms_body", smsContent);
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            try {
                //获取剪贴板管理器
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("sms", smsContent);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
            } catch (Exception e) {
            }
            ToastUtils.showToast(R.string.new_friend_invite_from_phone_contact_error, Toast.LENGTH_LONG);
        }
    }

    /**
     * 设置发送按钮是否可用
     *
     * @param enable
     */
    private void enableSendButton(boolean enable) {
        TextView titleConfirmTv = titleBar.getTvRight();
        if (enable) {
            titleConfirmTv.setClickable(true);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            titleConfirmTv.setClickable(false);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search:
                showSearch();
                break;
        }
    }

    private void showSearch(){
        setTitleBarType(SealTitleBar.Type.SEARCH);
        searchTv.setVisibility(View.GONE);
    }

    private void showNormalTitle(){
        titleBar.getEtSearch().setText("");
        setTitleBarType(SealTitleBar.Type.NORMAL);
        searchTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        closeSearchOrExit();
    }

    private void closeSearchOrExit(){
        if(titleBar.getType() == SealTitleBar.Type.SEARCH){
            showNormalTitle();
        } else {
            finish();
        }
    }
}
