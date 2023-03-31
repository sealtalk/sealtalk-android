package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.ImageCodeResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.SelectCountryActivity;
import cn.rongcloud.im.ui.activity.SelectDataCenterActivity;
import cn.rongcloud.im.ui.dialog.SecurityKickOutDialog;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.utils.DataCenter;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.LoginViewModel;

public class LoginFragment extends BaseFragment {
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1000;
    private static final int REQUEST_CODE_SELECT_DATA_CENTER = 1001;
    private ClearWriteEditText phoneNumberEdit;
    private ClearWriteEditText verifyCodeEdit;
    private TextView countryNameTv;
    private TextView countryCodeTv;
    private TextView dataCenter;

    private LoginViewModel loginViewModel;
    private Button sendCodeBtn;
    private boolean isRequestVerifyCode = false; // 是否请求成功验证码
    private OnLoginListener loginListener;
    private ImageView imageCode;
    private TextView mRefreshImageCode;
    private ImageCodeResult mImageCodeResult;
    private EditText imageEditText;

    @Override
    protected int getLayoutResId() {
        return R.layout.login_fragment_login;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        phoneNumberEdit = findView(R.id.cet_login_phone);
        verifyCodeEdit = findView(R.id.cet_login_verify_code);
        countryNameTv = findView(R.id.tv_country_name);
        countryCodeTv = findView(R.id.tv_country_code);
        imageCode = findView(R.id.iv_verify_code);
        mRefreshImageCode = (TextView) findView(R.id.tv_refresh_image_code);
        imageEditText = findView(R.id.cet_image_verify_code);
        dataCenter = (TextView) findView(R.id.tv_data_center_name);
        findView(R.id.btn_login, true);
        findView(R.id.ll_country_select, true);
        sendCodeBtn = findView(R.id.cet_login_send_verify_code, true);
        findView(R.id.ll_data_center, true);
        sendCodeBtn.setEnabled(false);
        imageCode.setOnClickListener(this);
        mRefreshImageCode.setOnClickListener(this);
        if (DataCenter.getDataCenterList().size() > 1) {
            findView(R.id.ll_data_center).setVisibility(View.VISIBLE);
        } else {
            findView(R.id.ll_data_center).setVisibility(View.GONE);
        }

        phoneNumberEdit.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 11) {
                            phoneNumberEdit.clearFocus();
                            InputMethodManager imm =
                                    (InputMethodManager)
                                            getContext()
                                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(phoneNumberEdit.getWindowToken(), 0);
                        }
                        if (s.length() > 0 && !isRequestVerifyCode) {
                            sendCodeBtn.setEnabled(true);
                        } else {
                            sendCodeBtn.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
    }

    @Override
    protected void onInitViewModel() {
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        loginViewModel
                .getLoginResult()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                if (resource.status == Status.SUCCESS) {
                                    IMManager.getInstance().resetAfterLogin();

                                    dismissLoadingDialog(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    showToast(R.string.seal_login_toast_success);
                                                    toMain(resource.data);
                                                }
                                            });

                                } else if (resource.status == Status.LOADING) {
                                    showLoadingDialog(R.string.seal_loading_dialog_logining);
                                } else if (resource.status == Status.ERROR) {
                                    int code = resource.code;
                                    SLog.d(
                                            "ss_register_and_login",
                                            "register and login failed = " + code);
                                    dismissLoadingDialog(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    showToast(resource.message);
                                                    if (code == ErrorCode.USER_BLOCKED.getCode()) {
                                                        showSecurityKickOutDialog();
                                                    }
                                                }
                                            });
                                }
                            }
                        });

        loginViewModel
                .getLastLoginUserCache()
                .observe(
                        this,
                        new Observer<UserCacheInfo>() {
                            @Override
                            public void onChanged(UserCacheInfo userInfo) {
                                phoneNumberEdit.setText(userInfo.getPhoneNumber());
                                String region = userInfo.getRegion();
                                if (!region.startsWith("+")) {
                                    region = "+" + region;
                                }
                                countryCodeTv.setText(region);
                                CountryInfo countryInfo = userInfo.getCountryInfo();
                                if (countryInfo != null
                                        && !TextUtils.isEmpty(countryInfo.getCountryName())) {
                                    countryNameTv.setText(countryInfo.getCountryName());
                                }
                                verifyCodeEdit.setText(userInfo.getPassword());
                            }
                        });

        loginViewModel
                .getSendCodeState()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                // 提示
                                if (resource.status == Status.SUCCESS) {
                                    showToast(R.string.seal_login_toast_send_code_success);
                                } else if (resource.status == Status.LOADING) {

                                } else {
                                    showToast(resource.message);
                                    sendCodeBtn.setEnabled(true);
                                }
                            }
                        });

        // 等待接受验证码倒计时， 并刷新及时按钮的刷新
        loginViewModel
                .getCodeCountDown()
                .observe(
                        this,
                        new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer integer) {
                                if (integer > 0) {
                                    sendCodeBtn.setText(integer + "s");
                                    isRequestVerifyCode = true;
                                } else {
                                    // 当计时结束时， 恢复按钮的状态
                                    sendCodeBtn.setEnabled(true);
                                    sendCodeBtn.setText(R.string.seal_login_send_code);
                                    isRequestVerifyCode = false;
                                }
                            }
                        });
        loginViewModel
                .getDataCenterLiveData()
                .observe(
                        this,
                        new Observer<DataCenter>() {
                            @Override
                            public void onChanged(DataCenter center) {
                                if (center.getNameId() != 0) {
                                    dataCenter.setText(center.getNameId());
                                }
                            }
                        });
        loginViewModel
                .getImageCodeResult()
                .observe(
                        this,
                        new Observer<Resource<ImageCodeResult>>() {
                            @Override
                            public void onChanged(Resource<ImageCodeResult> resource) {
                                mImageCodeResult = resource.data;
                                if (resource.status == Status.SUCCESS) {
                                    if (resource.data != null
                                            && !TextUtils.isEmpty(resource.data.getPicCode())) {
                                        convertToBitmap(resource.data);
                                        return;
                                    }
                                } else if (resource.status == Status.ERROR) {
                                    showToast(resource.message);
                                } else if (resource.status == Status.LOADING) {
                                    imageCode.setVisibility(View.GONE);
                                    mRefreshImageCode.setVisibility(View.GONE);
                                    return;
                                }
                                imageCode.setVisibility(View.GONE);
                                mRefreshImageCode.setVisibility(View.VISIBLE);
                            }
                        });
    }

    private void convertToBitmap(ImageCodeResult imageCodeResult) {
        ThreadManager.getInstance()
                .runOnWorkThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    byte[] data =
                                            Base64.decode(
                                                    imageCodeResult.getPicCode(), Base64.NO_WRAP);
                                    Bitmap bitmap =
                                            BitmapFactory.decodeByteArray(data, 0, data.length);
                                    setBitmapToImageCode(bitmap);
                                } catch (Exception e) {
                                    setBitmapToImageCode(null);
                                }
                            }
                        });
    }

    private void setBitmapToImageCode(Bitmap bitmap) {
        ThreadManager.getInstance()
                .runOnUIThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null) {
                                    imageCode.setVisibility(View.VISIBLE);
                                    mRefreshImageCode.setVisibility(View.GONE);
                                    imageCode.setImageBitmap(bitmap);
                                } else {
                                    imageCode.setVisibility(View.GONE);
                                    mRefreshImageCode.setVisibility(View.VISIBLE);
                                }
                            }
                        });
    }

    /** 显示数美踢出对话框 */
    private void showSecurityKickOutDialog() {
        SecurityKickOutDialog dialog = new SecurityKickOutDialog();
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

    @Override
    protected void onClick(View v, int id) {
        switch (id) {
            case R.id.cet_login_send_verify_code:
                String phoneNumber = phoneNumberEdit.getText().toString().trim();
                String countryCode = countryCodeTv.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumberEdit.getText().toString().trim())) {
                    showToast(R.string.seal_login_toast_phone_number_is_null);
                    return;
                }
                if (TextUtils.isEmpty(imageEditText.getText().toString().trim())) {
                    showToast(R.string.image_verification_code_is_null);
                    return;
                }
                // 请求发送验证码时， 禁止手机号改动和获取验证码的按钮改动
                // 请求完成后在恢复原来状态
                sendCodeBtn.setEnabled(false);
                String imageCode = null;
                String imageCodeId = null;
                if (mImageCodeResult != null) {
                    imageCode = imageEditText.getText().toString().trim();
                    imageCodeId = mImageCodeResult.getPicCodeId();
                }
                sendCode(countryCode, phoneNumber, imageCode, imageCodeId);
                break;
            case R.id.btn_login:
                if (loginListener != null && !loginListener.beforeLogin()) {
                    return;
                }
                String phoneStr = phoneNumberEdit.getText().toString().trim();
                String codeStr = verifyCodeEdit.getText().toString().trim();
                String countryCodeStr = countryCodeTv.getText().toString().trim();

                if (TextUtils.isEmpty(phoneStr)) {
                    showToast(R.string.seal_login_toast_phone_number_is_null);
                    phoneNumberEdit.setShakeAnimation();
                    break;
                }

                if (TextUtils.isEmpty(codeStr)) {
                    showToast(R.string.seal_login_toast_code_is_null);
                    verifyCodeEdit.setShakeAnimation();
                    return;
                }

                if (TextUtils.isEmpty(countryCodeStr)) {
                    countryCodeStr = "86";
                } else if (countryCodeStr.startsWith("+")) {
                    countryCodeStr = countryCodeStr.substring(1);
                }

                //                login(countryCodeStr, phoneStr, passwordStr);
                registerAndLogin(countryCodeStr, phoneStr, codeStr);
                break;
            case R.id.ll_country_select:
                // 跳转区域选择界面
                startActivityForResult(
                        new Intent(getActivity(), SelectCountryActivity.class),
                        REQUEST_CODE_SELECT_COUNTRY);
                break;
            case R.id.ll_data_center:
                startActivityForResult(
                        new Intent(getActivity(), SelectDataCenterActivity.class),
                        REQUEST_CODE_SELECT_DATA_CENTER);
                break;
            case R.id.iv_verify_code:
            case R.id.tv_refresh_image_code:
                loginViewModel.getImageCode();
                break;
            default:
                break;
        }
    }

    /**
     * 请求发送验证码
     *
     * @param phoneCode 国家地区的手机区号
     * @param phoneNumber 手机号
     */
    private void sendCode(String phoneCode, String phoneNumber, String picCode, String picCodeId) {
        loginViewModel.sendCode(phoneCode, phoneNumber, picCode, picCodeId);
    }

    /**
     * 登录到 业务服务器，以获得登录 融云 IM 服务器所必须的 token
     *
     * @param region 国家区号
     * @param phone 电话号/帐号
     * @param pwd 密码
     */
    private void login(String region, String phone, String pwd) {
        loginViewModel.login(region, phone, pwd);
    }

    private void registerAndLogin(String region, String phone, String code) {
        loginViewModel.registerAndLogin(region, phone, code);
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
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_COUNTRY) {
                CountryInfo info =
                        data.getParcelableExtra(SelectCountryActivity.RESULT_PARAMS_COUNTRY_INFO);
                countryNameTv.setText(info.getCountryName());
                countryCodeTv.setText(info.getZipCode());
            } else if (requestCode == REQUEST_CODE_SELECT_DATA_CENTER) {
                String dataCenterCode = data.getStringExtra("code");
                if (!TextUtils.isEmpty(dataCenterCode)) {
                    DataCenter dataCenter = DataCenter.getDataCenter(dataCenterCode);
                    if (dataCenter != null) {
                        loginViewModel.changeDataCenter(dataCenter);
                        loginViewModel.saveDataCenter(dataCenter);
                    }
                }
            }
        }
    }

    /**
     * 设置上参数
     *
     * @param phone
     * @param region
     * @param countryName
     */
    public void setLoginParams(String phone, String region, String countryName) {
        phoneNumberEdit.setText(phone);
        countryNameTv.setText(countryName);
        countryCodeTv.setText(region);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        verifyCodeEdit = null;
        loginViewModel.stopCodeCountDown();
    }

    public void setLoginListener(OnLoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public interface OnLoginListener {
        boolean beforeLogin();
    }
}
