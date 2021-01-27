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
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;

public class UpdateGenderActivity extends TitleBaseActivity implements View.OnClickListener {

    private SettingItemView manSiv;
    private SettingItemView femaleSiv;
    private UserInfoViewModel userInfoViewModel;
    private final int GENDER_MAN = 0x3444;
    private final int GENDER_FEMALE = 0x3445;
    private int currentType = GENDER_MAN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_gender);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(getString(R.string.seal_mine_my_account_gender));
        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_gender_save), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGender();
            }
        });
        manSiv = findViewById(R.id.siv_gender_man);
        manSiv.setOnClickListener(this);
        femaleSiv = findViewById(R.id.siv_gender_female);
        femaleSiv.setOnClickListener(this);
    }

    private void initViewModel() {
        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        userInfoViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> userInfoResource) {
                if (userInfoResource.data != null) {
                    String gender = userInfoResource.data.getGender();
                    if (TextUtils.isEmpty(gender) || gender.equals("male")) {
                        updateGenderStatus(GENDER_MAN);
                    } else {
                        updateGenderStatus(GENDER_FEMALE);
                    }
                }
            }
        });
        userInfoViewModel.getSetGenderResult().observe(this, new Observer<Resource<Result>>() {
            @Override
            public void onChanged(Resource<Result> resultResource) {
                if (resultResource.status == Status.SUCCESS) {
                    if (resultResource.data != null) {
                        if (resultResource.data.code == 200) {
                            showToast(R.string.seal_gender_set_success);
                            finish();
                        } else {
                            showToast(R.string.seal_gender_set_fail);
                        }
                    }
                }
            }
        });
    }

    private void updateGenderStatus(int type) {
        if (type == GENDER_MAN) {
            manSiv.setRightImageVisibility(View.VISIBLE);
            femaleSiv.setRightImageVisibility(View.GONE);
            currentType = GENDER_MAN;
        } else {
            manSiv.setRightImageVisibility(View.GONE);
            femaleSiv.setRightImageVisibility(View.VISIBLE);
            currentType = GENDER_FEMALE;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_gender_man:
                updateGenderStatus(GENDER_MAN);
                break;
            case R.id.siv_gender_female:
                updateGenderStatus(GENDER_FEMALE);
                break;
        }
    }

    private void setGender() {
        String gender = (currentType == GENDER_MAN) ? "male" : "female";
        userInfoViewModel.setGender(gender);
    }
}
