package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.utils.ToastUtils;
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
        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_update_name_save_update),new View.OnClickListener() {
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
        updateNameCet.setFilters(new InputFilter[]{emojiFilter, new InputFilter.LengthFilter(10)});
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
                    String name = TextUtils.isEmpty(resource.data.getName())? "" : resource.data.getName();
                    updateNameCet.setText(name);
                    updateNameCet.setSelection(name.length());
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
     * @param newName
     */
    private void updateName(String newName) {
        if (userInfoViewModel != null) {
            userInfoViewModel.setName(newName);
        }
    }

    /**
     * 表情输入的过滤
     */
    InputFilter emojiFilter = new InputFilter() {
        Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                ToastUtils.showToast("不支持输入表情");
                return "";
            }
            return null;
        }
    };
}
