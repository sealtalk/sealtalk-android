package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.SelectCountryActivity;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.viewmodel.LoginViewModel;

public class LoginFragment extends BaseFragment {
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1000;
    private ClearWriteEditText phoneNumberEdit;
    private ClearWriteEditText passwordEdit;
    private TextView countryNameTv;
    private TextView countryCodeTv;

    private LoginViewModel loginViewModel;


    @Override
    protected int getLayoutResId() {
        return R.layout.login_fragment_login;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        phoneNumberEdit = findView(R.id.cet_login_phone);
        passwordEdit = findView(R.id.cet_login_password);
        countryNameTv = findView(R.id.tv_country_name);
        countryCodeTv = findView(R.id.tv_country_code);
        findView(R.id.btn_login, true);
        findView(R.id.ll_country_select, true);

        phoneNumberEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    phoneNumberEdit.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(phoneNumberEdit.getWindowToken(), 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onInitViewModel() {
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(this, new Observer<Resource<String>>() {
            @Override
            public void onChanged(Resource<String> resource) {
                if (resource.status == Status.SUCCESS) {
                    dismissLoadingDialog(new Runnable() {
                        @Override
                        public void run() {
                            showToast(R.string.seal_login_toast_success);
                            toMain(resource.data);
                        }
                    });

                } else if (resource.status == Status.LOADING) {
                    showLoadingDialog(R.string.seal_loading_dialog_logining);
                } else {
                    dismissLoadingDialog(new Runnable() {
                        @Override
                        public void run() {
                            showToast(resource.message);
                        }
                    });
                }
            }
        });

        loginViewModel.getLastLoginUserCache().observe(this, new Observer<UserCacheInfo>() {
            @Override
            public void onChanged(UserCacheInfo userInfo) {
                phoneNumberEdit.setText(userInfo.getPhoneNumber());
                String region = userInfo.getRegion();
                if (!region.startsWith("+")) {
                    region = "+" + region;
                }
                countryCodeTv.setText(region);
                CountryInfo countryInfo = userInfo.getCountryInfo();
                if (countryInfo != null && !TextUtils.isEmpty(countryInfo.getCountryName())) {
                    countryNameTv.setText(countryInfo.getCountryName());
                }
                passwordEdit.setText(userInfo.getPassword());
            }
        });
    }


    @Override
    protected void onClick(View v, int id) {
        switch (id) {
            case R.id.btn_login:
                String phoneStr = phoneNumberEdit.getText().toString().trim();
                String passwordStr = passwordEdit.getText().toString().trim();
                String countryCodeStr = countryCodeTv.getText().toString().trim();

                if (TextUtils.isEmpty(phoneStr)) {
                    showToast(R.string.seal_login_toast_phone_number_is_null);
                    phoneNumberEdit.setShakeAnimation();
                    break;
                }

                if (TextUtils.isEmpty(passwordStr)) {
                    showToast(R.string.seal_login_toast_password_is_null);
                    passwordEdit.setShakeAnimation();
                    return;
                }

                if (passwordStr.contains(" ")) {
                    showToast(R.string.seal_login_toast_password_cannot_contain_spaces);
                    passwordEdit.setShakeAnimation();
                    return;
                }
                if(TextUtils.isEmpty(countryCodeStr)){
                    countryCodeStr = "86";
                }else if(countryCodeStr.startsWith("+")){
                    countryCodeStr = countryCodeStr.substring(1);
                }

                login(countryCodeStr, phoneStr, passwordStr);
                break;
            case R.id.ll_country_select:
                // 跳转区域选择界面
                startActivityForResult(new Intent(getActivity(), SelectCountryActivity.class), REQUEST_CODE_SELECT_COUNTRY);
                break;
            default:
                break;
        }
    }

    /**
     * 登录到 业务服务器，以获得登录 融云 IM 服务器所必须的 token
     *
     * @param region 国家区号
     * @param phone  电话号/帐号
     * @param pwd    密码
     */
    private void login(String region, String phone, String pwd) {
        loginViewModel.login(region, phone, pwd);
    }

    private void toMain(String userId) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(IntentExtra.USER_ID, userId);
        startActivity(intent);
        getActivity().finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE_SELECT_COUNTRY) {
            CountryInfo info = data.getParcelableExtra(SelectCountryActivity.RESULT_PARAMS_COUNTRY_INFO);
            countryNameTv.setText(info.getCountryName());
            countryCodeTv.setText(info.getZipCode());
        }
    }

    /**
     * 设置上参数
     * @param phone
     * @param region
     * @param countryName
     */
    public void setLoginParams(String phone, String region, String countryName) {
        phoneNumberEdit.setText(phone);
        countryNameTv.setText(countryName);
        countryCodeTv.setText(region);
    }
}
