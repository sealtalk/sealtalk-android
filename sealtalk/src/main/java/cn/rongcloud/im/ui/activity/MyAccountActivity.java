package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import cn.rongcloud.im.utils.log.SLog;

/**
 * 我的账号
 */
public class MyAccountActivity extends TitleBaseActivity implements View.OnClickListener {
    private UserInfoItemView userInfoUiv;
    private SettingItemView nicknameSiv;
    private SettingItemView phonenumberSiv;
    private SettingItemView sAccountSiv;
    private SettingItemView genderSiv;
    private UserInfoViewModel userInfoViewModel;
    private boolean isCanSetStAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        initView();
        initViewModel();
    }


    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_mine_my_account);

        userInfoUiv = findViewById(R.id.uiv_userinfo);
        userInfoUiv.setOnClickListener(this);
        nicknameSiv = findViewById(R.id.siv_nickname);
        nicknameSiv.setOnClickListener(this);
        sAccountSiv = findViewById(R.id.siv_saccount);
        sAccountSiv.setOnClickListener(this);
        phonenumberSiv = findViewById(R.id.siv_phonenumber);
        genderSiv = findViewById(R.id.siv_gender);
        genderSiv.setOnClickListener(this);
    }


    /**
     * 初始化 viewmodel
     */
    private void initViewModel() {
        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        // 用户信息
        userInfoViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> resource) {
                SLog.d("ss_update", "userInfo == " + resource.data);
                if (resource.data != null) {
                    // 减少图片加载次数，改为失败（获取上一次数据库中数据）或成功时加载图片
                    if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                        ImageLoaderUtils.displayUserPortraitImage(resource.data.getPortraitUri(), userInfoUiv.getHeaderImageView());
                    }
                    nicknameSiv.setValue(resource.data.getName());
                    String phoneNumber = TextUtils.isEmpty(resource.data.getPhoneNumber()) ? "" : resource.data.getPhoneNumber();
                    phonenumberSiv.setValue(phoneNumber);
                    isCanSetStAccount = TextUtils.isEmpty(resource.data.getStAccount());
                    if (!isCanSetStAccount) {
                        sAccountSiv.setValue(resource.data.getStAccount());
                    } else {
                        sAccountSiv.setValue(getString(R.string.seal_mine_my_account_notset));
                    }
                    String gender = resource.data.getGender();
                    if (TextUtils.isEmpty(gender) || gender.equals("male")) {
                        gender = getString(R.string.seal_gender_man);
                    } else if (gender.equals("female")) {
                        gender = getString(R.string.seal_gender_female);
                    }
                    genderSiv.setValue(gender);
                }
            }
        });

        // 头像上传结果
        userInfoViewModel.getUploadPortraitResult().observe(this, new Observer<Resource<Result>>() {
            @Override
            public void onChanged(Resource<Result> resource) {
                if (resource.status == Status.SUCCESS) {
                    showToast(R.string.profile_update_portrait_success);
                } else if (resource.status == Status.ERROR) {
                    showToast(R.string.profile_upload_portrait_failed);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uiv_userinfo:
                showSelectPictureDialog();
                break;
            case R.id.siv_nickname:
                Intent intent = new Intent(this, UpdateNameActivity.class);
                startActivity(intent);
                break;
            case R.id.siv_saccount:
                if (isCanSetStAccount) {
                    Intent intentSt = new Intent(this, UpdateStAccountActivity.class);
                    startActivity(intentSt);
                }
                break;
            case R.id.siv_gender:
                Intent intentGender = new Intent(this,UpdateGenderActivity.class);
                startActivity(intentGender);
                break;
            default:
                //DO nothing
                break;
        }
    }

    /**
     * 选择图片的 dialog
     */
    private void showSelectPictureDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(new SelectPictureBottomDialog.OnSelectPictureListener() {
            @Override
            public void onSelectPicture(Uri uri) {
                //上传图片
                uploadPortrait(uri);
            }
        });
        SelectPictureBottomDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "select_picture_dialog");
    }


    /**
     * 上传头像
     *
     * @param uri
     */
    private void uploadPortrait(Uri uri) {
        if (userInfoViewModel != null) {
            userInfoViewModel.uploadPortrait(uri);
        }
    }


}
