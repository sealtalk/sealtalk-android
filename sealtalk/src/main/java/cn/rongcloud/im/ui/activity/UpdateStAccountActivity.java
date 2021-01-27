package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

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
import cn.rongcloud.im.ui.dialog.StAccountDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;

public class UpdateStAccountActivity extends TitleBaseActivity {
    private ClearWriteEditText updateSAccountCet;
    private UserInfoViewModel userInfoViewModel;
    private UserInfoItemView uivUserInfo;
    private TextView tvTips;
    private TextView titleConfirmTv;
    private boolean isFormatRight;
    private boolean isFirstLetter;
    private final int SAVECONFIRM = 0x1234;
    private final int FORMATERROR = 0x1235;
    private final int ACCOUNTEXIST = 0x1236;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_saccout);
        initView();
        initViewModel();
    }

    private void initView() {
        SealTitleBar sealTitleBar = getTitleBar();
        titleConfirmTv = sealTitleBar.getTvRight();
        titleConfirmTv.setText(R.string.seal_update_staccount_save);
        titleConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFormatRight) {
                    showSaveDialog(SAVECONFIRM);
                } else {
                    showSaveDialog(FORMATERROR);
                }
            }
        });
        sealTitleBar.setTitle(R.string.seal_update_staccount_title);
        setConfirmEnable(false);
        uivUserInfo = findViewById(R.id.uiv_userinfo);
        updateSAccountCet = findViewById(R.id.cet_update_staccount);
        tvTips = findViewById(R.id.tv_tips);
        //设置 hint
        SpannableString ss = new SpannableString(getString(R.string.seal_update_staccout_edittext_hint));
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(12, true);//设置字体大小 true表示单位是sp
        ss.setSpan(sizeSpan, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        updateSAccountCet.setHint(new SpannedString(ss));
        updateSAccountCet.setClearDrawable(getResources().getDrawable(R.drawable.seal_st_account_delete));
        updateSAccountCet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                updateInputTextStatus(s);
            }
        });

    }

    /**
     * 设置可以点击保存按钮（置灰处理）
     *
     * @param isEnable
     */
    private void setConfirmEnable(boolean isEnable) {
        if (isEnable) {
            titleConfirmTv.setClickable(true);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            titleConfirmTv.setClickable(false);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void updateInputTextStatus(Editable s) {
        //第一个不为字母，则提示变为红色提示,等于0的时候正常提示
        if (s.length() == 0) {
            setConfirmEnable(false);
            tvTips.setText(getString(R.string.seal_update_staccount_tips));
            tvTips.setTextColor(getResources().getColor(R.color.seal_setting_item_value));
            isFirstLetter = false;
            isFormatRight = false;
        } else if (s.length() > 0 && !isLetter(s.subSequence(0, 1))) {
            setConfirmEnable(true);
            isFormatRight = false;
            isFirstLetter = false;
            tvTips.setText(getString(R.string.seal_update_staccout_red_tips));
            tvTips.setTextColor(getResources().getColor(R.color.seal_update_name_tips));
        } else {
            setConfirmEnable(true);
            isFirstLetter = true;
            tvTips.setText(getString(R.string.seal_update_staccount_tips));
            tvTips.setTextColor(getResources().getColor(R.color.seal_setting_item_value));
        }
        //长度大于6，并且第一个为字母的情况下，符合规格
        if (s.length() >= 6 && isFirstLetter) {
            isFormatRight = true;
        }
    }

    private void initViewModel() {
        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        userInfoViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> userInfoResource) {
                if (userInfoResource.data != null) {
                    UserInfo info = userInfoResource.data;
                    uivUserInfo.setName(info.getName());
                    ImageLoaderUtils.displayUserPortraitImage(info.getPortraitUri(), uivUserInfo.getHeaderImageView());
                }
            }
        });
        userInfoViewModel.getSetStAccountResult().observe(this, new Observer<Resource<Result>>() {
            @Override
            public void onChanged(Resource<Result> resultResource) {
                if (resultResource.status == Status.SUCCESS) {
                    if (resultResource.data != null) {
                        if (resultResource.data.code == 200) {
                            showToast(R.string.seal_staccount_set_success);
                            finish();
                        } else if (resultResource.data.code == 1000) {
                            showSaveDialog(ACCOUNTEXIST);
                        }
                    }
                }
            }
        });
    }


    private void showSaveDialog(int type) {
        StAccountDialog.Builder builder = new StAccountDialog.Builder();
        switch (type) {
            case SAVECONFIRM:
                builder.setTitleMessage(getString(R.string.seal_staccount_dialog_title));
                //设置账号为蓝色
                String stAccout = updateSAccountCet.getText().toString();
                StringBuffer buffer = new StringBuffer(getString(R.string.seal_staccount_dialog_content));
                int i = buffer.indexOf("@");
                buffer.replace(i, i + 1, stAccout);
                SpannableString spannableString = new SpannableString(buffer.toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.default_clickable_text)), i, i + stAccout.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setContentSpanna(spannableString);
                break;
            case FORMATERROR:
                builder.setTitleMessage(getString(R.string.seal_staccount_dialog_title_format_error));
                builder.setContentMessage(getString(R.string.seal_update_staccout_red_tips));
                builder.setIsOnlyConfirm(true);
                break;
            case ACCOUNTEXIST:
                builder.setTitleMessage(getString(R.string.seal_staccount_dialog_title_format_error));
                builder.setContentMessage(getString(R.string.seal_update_staccout_exit));
                builder.setIsOnlyConfirm(true);
                break;
        }
        StAccountDialog dialog = builder.build();
        builder.setDialogButtonClickListener(new StAccountDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                if (type == SAVECONFIRM) {
                    updateStAccount();
                }
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
            }
        });
        dialog.show(getSupportFragmentManager(), "clear_cache");
    }

    private boolean isLetter(CharSequence s) {
        String all = "^[A-Za-z]+$";
        Pattern pattern = Pattern.compile(all);
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    private void updateStAccount() {
        userInfoViewModel.setStAccount(updateSAccountCet.getText().toString());
    }

}
