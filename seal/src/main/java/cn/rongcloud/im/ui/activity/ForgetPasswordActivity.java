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
import cn.rongcloud.im.server.response.RestPasswordResponse;
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
 * Created by AMing on 16/2/2.
 * Company RongCloud
 */
@SuppressWarnings("deprecation")
public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener, DownTimerListener {
    private final String TAG = ForgetPasswordActivity.class.getSimpleName();

    private static final int CHECK_PHONE = 31;
    private static final int SEND_CODE = 32;
    private static final int CHANGE_PASSWORD = 33;
    private static final int VERIFY_CODE = 34;
    private static final int CHANGE_PASSWORD_BACK = 1002;
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1000;
    private ClearWriteEditText mPhone, mCode, mPassword1, mPassword2;
    private TextView mCountryNameTv, mCountryCodeTv, changLanguageTv;
    private ImageView mImg_Background;
    private Button mGetCode, mOK;
    private String phone, mCodeToken;
    private boolean available;
    private String mCountryNameCN, mCountryNameEN;
    private String mRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        setHeadVisibility(View.GONE);
        initView();
    }

    private void initView() {
        mPhone = (ClearWriteEditText) findViewById(R.id.forget_phone);
        mCode = (ClearWriteEditText) findViewById(R.id.forget_code);
        mPassword1 = (ClearWriteEditText) findViewById(R.id.forget_password);
        mPassword2 = (ClearWriteEditText) findViewById(R.id.forget_password1);
        mGetCode = (Button) findViewById(R.id.forget_getcode);
        mOK = (Button) findViewById(R.id.forget_button);
        TextView register = (TextView) findViewById(R.id.de_login_register);
        TextView goLogin = (TextView) findViewById(R.id.reg_login);
        mCountryNameTv = (TextView)findViewById(R.id.reg_country_name);
        mCountryCodeTv = (TextView)findViewById(R.id.reg_country_code);
        View selectCountryView = findViewById(R.id.reg_country_select);
        changLanguageTv = findViewById(R.id.chg_lang);

        register.setOnClickListener(this);
        goLogin.setOnClickListener(this);
        mGetCode.setOnClickListener(this);
        mOK.setOnClickListener(this);
        selectCountryView.setOnClickListener(this);
        changLanguageTv.setOnClickListener(this);

        mImg_Background = (ImageView) findViewById(R.id.de_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(ForgetPasswordActivity.this, R.anim.translate_anim);
                mImg_Background.startAnimation(animation);
            }
        }, 200);

        mPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    phone = mPhone.getText().toString().trim();
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

        mCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
//                    AMUtils.onInactive(mContext, mCode);
                    if (available) {
                        mOK.setClickable(true);
                        mOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                    }
                } else {
                    mOK.setClickable(false);
                    mOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                return action.checkPhoneAvailable(region, phone);
            case SEND_CODE:
                return action.sendCode(region, phone);
            case CHANGE_PASSWORD:
                return action.restPassword(mPassword1.getText().toString(), mCodeToken);
            case VERIFY_CODE:
                return action.verifyCode(region, phone, mCode.getText().toString());
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case CHECK_PHONE:
                    CheckPhoneResponse response = (CheckPhoneResponse) result;
                    if (response.getCode() == 200) {
                        if (response.isResult()) {
                            NToast.shortToast(mContext, getString(R.string.phone_unregister));
                            mGetCode.setClickable(false);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                        } else {
                            request(SEND_CODE);
                            mGetCode.setClickable(false);
                            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                        }
                    }
                    break;
                case SEND_CODE:
                    SendCodeResponse scrres = (SendCodeResponse) result;
                    if (scrres.getCode() == 200) {
                        DownTimer downTimer = new DownTimer();
                        downTimer.setListener(this);
                        downTimer.startDown(60 * 1000);
                        available = true;
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
                                request(CHANGE_PASSWORD);
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

                case CHANGE_PASSWORD:
                    RestPasswordResponse response1 = (RestPasswordResponse) result;
                    if (response1.getCode() == 200) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.update_success));
                        Intent data = new Intent();
                        data.putExtra("phone", phone);
                        data.putExtra("password", mPassword1.getText().toString());
                        data.putExtra("region", mRegion);
                        data.putExtra("country", mCountryNameTv.getText());
                        data.putExtra("countryCN", mCountryNameCN);
                        data.putExtra("countryEN", mCountryNameEN);
                        setResult(CHANGE_PASSWORD_BACK, data);
                        this.finish();
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
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_login:
                toLogin();
                break;
            case R.id.de_login_register:
                startActivityForResult(new Intent(this, RegisterActivity.class), 1);
                finish();
                break;
            case R.id.forget_getcode:
                if (TextUtils.isEmpty(mPhone.getText().toString().trim())) {
                    NToast.longToast(mContext, getString(R.string.phone_number_is_null));
                } else {
                    mGetCode.setClickable(false);
                    mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                    request(CHECK_PHONE);
                }
                break;
            case R.id.forget_button:
                if (TextUtils.isEmpty(mPhone.getText().toString())) {
                    NToast.shortToast(mContext, getString(R.string.phone_number_is_null));
                    mPhone.setShakeAnimation();
                    return;
                }

                if (TextUtils.isEmpty(mCode.getText().toString())) {
                    NToast.shortToast(mContext, getString(R.string.code_is_null));
                    mCode.setShakeAnimation();
                    return;
                }

                if (TextUtils.isEmpty(mPassword1.getText().toString())) {
                    NToast.shortToast(mContext, getString(R.string.password_is_null));
                    mPassword1.setShakeAnimation();
                    return;
                }

                if (mPassword1.length() < 6 || mPassword1.length() > 16) {
                    NToast.shortToast(mContext, R.string.passwords_invalid);
                    return;
                }

                if (TextUtils.isEmpty(mPassword2.getText().toString())) {
                    NToast.shortToast(mContext, getString(R.string.confirm_password));
                    mPassword2.setShakeAnimation();
                    return;
                }

                if (!mPassword2.getText().toString().equals(mPassword1.getText().toString())) {
                    NToast.shortToast(mContext, getString(R.string.passwords_do_not_match));
                    return;
                }

                LoadDialog.show(mContext);
                request(VERIFY_CODE);
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
        Intent forgetPwdIntent = new Intent(this, ForgetPasswordActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(loginIntent);
        taskStackBuilder.addNextIntent(forgetPwdIntent);
        taskStackBuilder.startActivities();
        overridePendingTransition(0, 0);
    }

    private void setPushLanguage(final RongIMClient.PushLanguage language) {
        RongIMClient.getInstance().setPushLanguage(language, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                //设置成功也存起来
                RongConfigurationManager.getInstance().setPushLanguage(ForgetPasswordActivity.this, language);
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

    @Override
    public void onTick(long millisUntilFinished) {
        mGetCode.setText(String.valueOf(millisUntilFinished / 1000) + "s");
        mGetCode.setClickable(false);
        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
    }

    @Override
    public void onFinish() {
        mGetCode.setText(R.string.get_code);
        mGetCode.setClickable(true);
        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
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
