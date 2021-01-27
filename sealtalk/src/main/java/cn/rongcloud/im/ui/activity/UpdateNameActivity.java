package cn.rongcloud.im.ui.activity;

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
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;

public class UpdateNameActivity extends TitleBaseActivity {

    private ClearWriteEditText updateNameCet;
    private UserInfoViewModel userInfoViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_name);

        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {

        getTitleBar().setTitle(R.string.seal_update_name);
        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_update_name_save_update), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = updateNameCet.getText().toString().trim();
                if (!TextUtils.isEmpty(newName)) {
                    updateName(newName);
                } else {
                    showToast(R.string.seal_update_name_toast_nick_name_can_not_empty);
                    updateNameCet.setShakeAnimation();
                }
            }
        });

        updateNameCet = findViewById(R.id.cet_update_name);
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {

        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        // 用户信息
        userInfoViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> resource) {
                if (resource.data != null) {
                    String name = TextUtils.isEmpty(resource.data.getName()) ? "" : resource.data.getName();
                    updateNameCet.setText(name);
                    int length = name.length();
                    if (length <= 32) {
                        updateNameCet.setSelection(length);
                    }
                }
            }
        });

        // name 修改结果
        userInfoViewModel.getSetNameResult().observe(this, new Observer<Resource<Result>>() {
            @Override
            public void onChanged(Resource<Result> resultResource) {
                if (resultResource.status == Status.SUCCESS) {
                    showToast(R.string.seal_update_name_toast_nick_name_change_success);
                    finish();
                } else if (resultResource.status == Status.ERROR) {
                    //TODO 错误提示
                }
            }
        });
    }

    /**
     * 更新name
     *
     * @param newName
     */
    private void updateName(String newName) {
        if (userInfoViewModel != null) {
            userInfoViewModel.setName(newName);
        }
    }
}
