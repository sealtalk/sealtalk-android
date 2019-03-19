package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.CheckPhoneResponse;
import cn.rongcloud.im.server.response.RegisterResponse;
import cn.rongcloud.im.server.response.SendCodeResponse;
import cn.rongcloud.im.server.response.VerifyCodeResponse;
import cn.rongcloud.im.server.utils.AMUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.downtime.DownTimer;
import cn.rongcloud.im.server.utils.downtime.DownTimerListener;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.common.RLog;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imlib.RongIMClient;

/**
 * Created by AMing on 16/1/14.
 * Company RongCloud
 */
@SuppressWarnings("deprecation")
public class RegisterActivity extends BaseActivity implements View.OnClickListener, DownTimerListener {
    private final String TAG = RegisterActivity.class.getSimpleName();
    private final int REQUEST_CODE_SELECT_COUNTRY = 1;

    private static final int CHECK_PHONE = 1;
    private static final int SEND_CODE = 2;
    private static final int VERIFY_CODE = 3;
    private static final int REGISTER = 4;
    private static final int REGISTER_BACK = 1001;
    private ImageView mImgBackground;
    private ClearWriteEditText mPhoneEdit, mCodeEdit, mNickEdit, mPasswordEdit;
    private TextView mCountryNameTv, mCountryCodeTv, changLanguageTv;
    private Button mGetCode, mConfirm;
    private String mPhone, mCode, mNickName, mPassword, mCodeToken;
    private boolean isRequestCode = false;
    private String mCountryNameCN, mCountryNameEN;
    private String mRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setHeadVisibility(View.GONE);
        initView();
    }

    private void initView() {
        mPhoneEdit = (ClearWriteEditText) findViewById(R.id.reg_phone);
        mCodeEdit = (ClearWriteEditText) findViewById(R.id.reg_code);
        mNickEdit = (ClearWriteEditText) findViewById(R.id.reg_username);
        mPasswordEdit = (ClearWriteEditText) findViewById(R.id.reg_password);
        mGetCode = (Button) findViewById(R.id.reg_getcode);
        mConfirm = (Button) findViewById(R.id.reg_button);
        mCountryNameTv = (TextView)findViewById(R.id.reg_country_name);
        mCountryCodeTv = (TextView)findViewById(R.id.reg_country_code);
        View selectCountryView = findViewById(R.id.reg_country_select);
        changLanguageTv = findViewById(R.id.chg_lang);

        mGetCode.setOnClickListener(this);
        mGetCode.setClickable(false);
        mConfirm.setOnClickListener(this);
        selectCountryView.setOnClickListener(this);
        changLanguageTv.setOnClickListener(this);

        TextView goLogin = (TextView) findViewById(R.id.reg_login);
        TextView goForget = (TextView) findViewById(R.id.reg_forget);
        goLogin.setOnClickListener(this);
        goForget.setOnClickListener(this);

        mImgBackground = (ImageView) findViewById(R.id.rg_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.translate_anim);
                mImgBackground.startAnimation(animation);
            }
        }, 200);

        addEditTextListener();

        LangUtils.RCLocale appLocale = LangUtils.getAppLocale(this);
        if (LangUtils.RCLocale.LOCALE_CHINA == appLocale) {
            changLanguageTv.setText(R.string.lang_en);
        } else if (LangUtils.RCLocale.LOCALE_US == appLocale) {
            changLanguageTv.setText(R.string.lang_chs);
        } else {
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, this);
                changLanguageTv.setText(R.string.lang_en);
            } else {
                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, this);
                changLanguageTv.setText(R.string.lang_chs);
            }
        }
    }

    private void addEditTextListener() {
        mPhoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && isBright) {
                    mPhone = s.toString().trim();
                    mGetCode.setClickable(true);
                    mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                } else {
                    mGetCode.setClickable(false);
                    mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCodeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() == 6) {
//                    AMUtils.onInactive(mContext, mCodeEdit);
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 5) {
                    mConfirm.setClickable(true);
                    mConfirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                } else {
                    mConfirm.setClickable(false);
                    mConfirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        String region = mCountryCodeTv.getText().toString();
        if(TextUtils.isEmpty(region)){
            region = "86";
        }else if(region.startsWith("+")){
            region = region.substring(1);
        }
        mRegion = region;
        switch (requestCode) {
            case CHECK_PHONE:
                return action.checkPhoneAvailable(region, mPhone);
            case SEND_CODE:
                return action.sendCode(region, mPhone);
            case VERIFY_CODE:
                return action.verifyCode(region, mPhone, mCode);
            case REGISTER:
                return action.register(mNickName, mPassword, mCodeToken);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CHECK_PHONE:
                    CheckPhoneResponse cprres = (CheckPhoneResponse) result;
                    if (cprres.getCode() == 200) {
                        if (cprres.isResult()) {
                            mGetCode.setClickable(false);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                            request(SEND_CODE);
                        } else {
                            mGetCode.setClickable(false);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                            Toast.makeText(mContext, R.string.phone_number_has_been_registered, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case SEND_CODE:
                    SendCodeResponse scrres = (SendCodeResponse) result;
                    if (scrres.getCode() == 200) {
                        isRequestCode = true;
                        DownTimer downTimer = new DownTimer();
                        downTimer.setListener(this);
                        downTimer.startDown(60 * 1000);
                        NToast.shortToast(mContext, R.string.messge_send);
                    } else if (scrres.getCode() == 5000) {
                        NToast.shortToast(mContext, R.string.message_frequency);
                    } else if(scrres.getCode() == 3102){
                        NToast.shortToast(mContext, R.string.Illegal_phone_number);
                    }
                    break;

                case VERIFY_CODE:
                    VerifyCodeResponse vcres = (VerifyCodeResponse) result;
                    switch (vcres.getCode()) {
                        case 200:
                            mCodeToken = vcres.getResult().getVerification_token();
                            if (!TextUtils.isEmpty(mCodeToken)) {
                                request(REGISTER);
                            } else {
                                NToast.shortToast(mContext, "code token is null");
                                LoadDialog.dismiss(mContext);
                            }
                            break;
                        case 1000:
                            //验证码错误
                            NToast.shortToast(mContext, R.string.verification_code_error);
                            LoadDialog.dismiss(mContext);
                            break;
                        case 2000:
                            //验证码过期
                            NToast.shortToast(mContext, R.string.captcha_overdue);
                            LoadDialog.dismiss(mContext);
                            break;
                    }
                    break;

                case REGISTER:
                    RegisterResponse rres = (RegisterResponse) result;
                    switch (rres.getCode()) {
                        case 200:
                            LoadDialog.dismiss(mContext);
                            NToast.shortToast(mContext, R.string.register_success);
                            Intent data = new Intent();
                            data.putExtra("phone", mPhone);
                            data.putExtra("password", mPassword);
                            data.putExtra("nickname", mNickName);
                            data.putExtra("region", mRegion);
                            data.putExtra("country", mCountryNameTv.getText());
                            data.putExtra("countryCN", mCountryNameCN);
                            data.putExtra("countryEN", mCountryNameEN);
                            data.putExtra("id", rres.getResult().getId());
                            setResult(REGISTER_BACK, data);
                            this.finish();
                            break;
                        case 400:
                            // 错误的请求
                            break;
                        case 404:
                            //token 不存在
                            break;
                        case 500:
                            //应用服务端内部错误
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case CHECK_PHONE:
                Toast.makeText(mContext, mContext.getString(R.string.phone_enable_check_request_failed), Toast.LENGTH_SHORT).show();
                break;
            case SEND_CODE:
                NToast.shortToast(mContext, mContext.getString(R.string.get_verify_code_request_failed));
                break;
            case VERIFY_CODE:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, mContext.getString(R.string.verify_code_enable_check_request_failed));
                break;
            case REGISTER:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, mContext.getString(R.string.register_request_failed));
                break;
        }
    }

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_login:
                toLogin();
                break;
            case R.id.reg_forget:
                startActivity(new Intent(this, ForgetPasswordActivity.class));
                finish();
                break;
            case R.id.reg_getcode:
                if (TextUtils.isEmpty(mPhoneEdit.getText().toString().trim())) {
                    NToast.longToast(mContext, R.string.phone_number_is_null);
                } else {
                    mGetCode.setClickable(false);
                    mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                    request(CHECK_PHONE);
                }
                break;
            case R.id.reg_button:
                mPhone = mPhoneEdit.getText().toString().trim();
                mCode = mCodeEdit.getText().toString().trim();
                mNickName = mNickEdit.getText().toString().trim();
                mPassword = mPasswordEdit.getText().toString().trim();


                if (TextUtils.isEmpty(mNickName)) {
                    NToast.shortToast(mContext, getString(R.string.name_is_null));
                    mNickEdit.setShakeAnimation();
                    return;
                }
                if (mNickName.contains(" ")) {
                    NToast.shortToast(mContext, getString(R.string.name_contain_spaces));
                    mNickEdit.setShakeAnimation();
                    return;
                }

                if (TextUtils.isEmpty(mPhone)) {
                    NToast.shortToast(mContext, getString(R.string.phone_number_is_null));
                    mPhoneEdit.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(mCode)) {
                    NToast.shortToast(mContext, getString(R.string.code_is_null));
                    mCodeEdit.setShakeAnimation();
                    return;
                }
                if (TextUtils.isEmpty(mPassword)) {
                    NToast.shortToast(mContext, getString(R.string.password_is_null));
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                if (mPassword.contains(" ")) {
                    NToast.shortToast(mContext, getString(R.string.password_cannot_contain_spaces));
                    mPasswordEdit.setShakeAnimation();
                    return;
                }

                if (!isRequestCode) {
                    NToast.shortToast(mContext, getString(R.string.not_send_code));
                    return;
                }

                LoadDialog.show(mContext);
                request(VERIFY_CODE, true);

                break;
            case R.id.reg_country_select:
                startActivityForResult(new Intent(this, SelectCountryActivity.class), REQUEST_CODE_SELECT_COUNTRY);
                break;
            case R.id.chg_lang:
                LangUtils.RCLocale appLocale = LangUtils.getAppLocale(this);
                if (LangUtils.RCLocale.LOCALE_CHINA == appLocale) {
                    RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, this);
                    App.updateApplicationLanguage();
                    setPushLanguage(RongIMClient.PushLanguage.EN_US);
                    changLanguageTv.setText(R.string.lang_chs);
                } else if (LangUtils.RCLocale.LOCALE_US == appLocale) {
                    RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, this);
                    App.updateApplicationLanguage();
                    setPushLanguage(RongIMClient.PushLanguage.ZH_CN);
                    changLanguageTv.setText(R.string.lang_en);
                } else{
                    Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
                    if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                        RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, this);
                        App.updateApplicationLanguage();
                        setPushLanguage(RongIMClient.PushLanguage.EN_US);
                        changLanguageTv.setText(R.string.lang_chs);
                    } else {
                        RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, this);
                        App.updateApplicationLanguage();
                        setPushLanguage(RongIMClient.PushLanguage.ZH_CN);
                        changLanguageTv.setText(R.string.lang_en);
                    }
                }
                changeLanguage();
                break;
        }
    }

    private void changeLanguage() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(loginIntent);
        taskStackBuilder.addNextIntent(registerIntent);
        taskStackBuilder.startActivities();
        overridePendingTransition(0, 0);
    }

    private void setPushLanguage(final RongIMClient.PushLanguage language) {
        RongIMClient.getInstance().setPushLanguage(language, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                //设置成功也存起来
                RongConfigurationManager.getInstance().setPushLanguage(RegisterActivity.this, language);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                RLog.e(TAG, getString(R.string.setting_push_language_error));
            }
        });
    }

    private void toLogin(){
        Intent loginIntent = new Intent(this, LoginActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(loginIntent);
        taskStackBuilder.startActivities();
    }

    boolean isBright = true;

    @Override
    public void onTick(long millisUntilFinished) {
        mGetCode.setText(String.valueOf(millisUntilFinished / 1000) + "s");
        mGetCode.setClickable(false);
        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
        isBright = false;
    }

    @Override
    public void onFinish() {
        mGetCode.setText(R.string.get_code);
        mGetCode.setClickable(true);
        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
        isBright = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_SELECT_COUNTRY && resultCode == RESULT_OK){
            String zipCode = data.getStringExtra("zipCode");
            String countryName = data.getStringExtra("countryName");
            String countryNameCN = data.getStringExtra("countryNameCN");
            String countryNameEN = data.getStringExtra("countryNameEN");
            mCountryCodeTv.setText(zipCode);
            mCountryNameTv.setText(countryName);
            mCountryNameCN = countryNameCN;
            mCountryNameEN = countryNameEN;
        }
    }
}
