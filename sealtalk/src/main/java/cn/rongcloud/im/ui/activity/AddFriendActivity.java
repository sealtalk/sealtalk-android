package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.qrcode.QrCodeDisplayType;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.utils.CheckPermissionUtils;
import cn.rongcloud.im.wx.WXManager;
import io.rong.imkit.RongIM;

/**
 * 添加朋友界面
 */
public class AddFriendActivity extends TitleBaseActivity implements View.OnClickListener {
    private static final String TAG = "AddFriendActivity";
    private static final int REQUEST_PERMISSION_ADD_CONTACT_FREIND = 2001;
    private static final int REQUEST_PERMISSION_INVITE_CONTACT_FRIEND = 2002;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().setTitle(R.string.new_friend_title);

        setContentView(R.layout.main_activity_new_friend);
        initView();
    }

    private void initView() {
        findViewById(R.id.add_friend_tv_search_friend).setOnClickListener(this);
        findViewById(R.id.add_friend_tv_my_qrcode).setOnClickListener(this);
        findViewById(R.id.add_friend_ll_add_from_contact).setOnClickListener(this);
        findViewById(R.id.add_friend_ll_scan).setOnClickListener(this);
        findViewById(R.id.add_friend_ll_add_from_wechat).setOnClickListener(this);
        findViewById(R.id.add_friend_ll_invite_from_contact).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_friend_tv_search_friend:
                toSearchFriend();
                break;
            case R.id.add_friend_tv_my_qrcode:
                showMyQRCode();
                break;
            case R.id.add_friend_ll_add_from_contact:
                addFriendFromContact();
                break;
            case R.id.add_friend_ll_scan:
                toScanQRCode();
                break;
            case R.id.add_friend_ll_add_from_wechat:
                inviteWechatFriend();
                break;
            case R.id.add_friend_ll_invite_from_contact:
                inviteFromContact();
                break;
        }
    }

    /**
     * 通过手机号/SealTalk号查找好友
     */
    private void toSearchFriend() {
        Intent intent = new Intent(this, SearchFriendActivity.class);
        startActivity(intent);
    }

    /**
     * 显示我的二维码
     */
    private void showMyQRCode() {
        Intent qrCodeIntent = new Intent(this, QrCodeDisplayWindowActivity.class);
        qrCodeIntent.putExtra(IntentExtra.STR_TARGET_ID, RongIM.getInstance().getCurrentUserId());
        qrCodeIntent.putExtra(IntentExtra.SERIA_QRCODE_DISPLAY_TYPE, QrCodeDisplayType.PRIVATE);
        startActivity(qrCodeIntent);
    }

    /**
     * 从通讯录添加好友
     */
    private void addFriendFromContact() {
        boolean hasPermissions = CheckPermissionUtils.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_ADD_CONTACT_FREIND);
        if (hasPermissions) {
            Intent intent = new Intent(this, AddFriendFromContactActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 扫一扫
     */
    private void toScanQRCode() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    /**
     * 微信邀请好友
     */
    private void inviteWechatFriend() {
        CommonDialog commonDialog = new CommonDialog.Builder()
                .setTitleText(R.string.new_friend_invite_wechat_friend)
                .setContentMessage(getString(R.string.new_friend_invite_wechat_friend_dialog_content))
                .setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        WXManager.getInstance().inviteToSealTalk();
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {
                    }
                }).build();
        commonDialog.show(getSupportFragmentManager(), null);
    }

    /**
     * 从通讯录邀请好友
     */
    private void inviteFromContact() {
        boolean hasPermissions = CheckPermissionUtils.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_INVITE_CONTACT_FRIEND);
        if (hasPermissions) {
            Intent intent = new Intent(this, InviteFriendFromContactActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 跳转到请求通讯录说明界面
     */
    private void toRequestContactPermission() {
        Intent intent = new Intent(this, RequestContactPermissionActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_ADD_CONTACT_FREIND
                || requestCode == REQUEST_PERMISSION_INVITE_CONTACT_FRIEND) {
            boolean grandResult = true;
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    grandResult = false;
                    break;
                }
            }
            if (grandResult) {
                if (requestCode == REQUEST_PERMISSION_ADD_CONTACT_FREIND) {
                    addFriendFromContact();
                } else if (requestCode == REQUEST_PERMISSION_INVITE_CONTACT_FRIEND) {
                    inviteFromContact();
                }
            } else {
                toRequestContactPermission();
            }
        }
    }
}
