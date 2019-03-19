package cn.rongcloud.im.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetTokenResponse;
import cn.rongcloud.im.server.response.GetUserInfoByIdResponse;
import cn.rongcloud.im.server.response.LoginResponse;
import cn.rongcloud.im.server.utils.AMUtils;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.common.RLog;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/1/15.
 * Company RongCloud
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = "LoginActivity";
    private static final int LOGIN = 5;
    private static final int GET_TOKEN = 6;
    private static final int SYNC_USER_INFO = 9;
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1000;

    private ImageView mImg_Background;
    private ClearWriteEditText mPhoneEdit, mPasswordEdit;
    private TextView mCountryNameTv, mCountryCodeTv, changLanguageTv;
    private String phoneString;
    private String passwordString;
    private String connectResultId;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String loginToken;
    private String mCountryNameCN, mCountryNameEN;
    private String mRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setHeadVisibility(View.GONE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        initView();
    }

    private void initView() {
        mPhoneEdit = (ClearWriteEditText) findViewById(R.id.de_login_phone);
        mPasswordEdit = (ClearWriteEditText) findViewById(R.id.de_login_password);
        Button mConfirm = (Button) findViewById(R.id.de_login_sign);
        TextView mRegister = (TextView) findViewById(R.id.de_login_register);
        TextView forgetPassword = (TextView) findViewById(R.id.de_login_forgot);
        mCountryNameTv = (TextView)findViewById(R.id.reg_country_name);
        mCountryCodeTv = (TextView)findViewById(R.id.reg_country_code);
        View selectCountryView = findViewById(R.id.reg_country_select);
        changLanguageTv = findViewById(R.id.chg_lang);

        forgetPassword.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        selectCountryView.setOnClickListener(this);
        changLanguageTv.setOnClickListener(this);

        mImg_Background = (ImageView) findViewById(R.id.de_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate_anim);
                mImg_Background.startAnimation(animation);
            }
        }, 200);
        mPhoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    AMUtils.onInactive(mContext, mPhoneEdit);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String oldPhone = sp.getString(SealConst.SEALTALK_LOGING_PHONE, "");
        String oldPassword = sp.getString(SealConst.SEALTALK_LOGING_PASSWORD, "");
        String oldRegion = sp.getString(SealConst.SEALTALK_LOGIN_REGION,"");

        if (!TextUtils.isEmpty(oldPhone)
                && !TextUtils.isEmpty(oldPassword)) {
            mPhoneEdit.setText(oldPhone);
            mPasswordEdit.setText(oldPassword);
        }

        if (getIntent().getBooleanExtra("kickedByOtherClient", false)) {
            final AlertDialog dlg = new AlertDialog.Builder(LoginActivity.this).create();
            dlg.show();
            Window window = dlg.getWindow();
            window.setContentView(R.layout.other_devices);
            TextView text = (TextView) window.findViewById(R.id.ok);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.cancel();
                }
            });
        }

        LangUtils.RCLocale appLocale = LangUtils.getAppLocale(this);
        if (LangUtils.RCLocale.LOCALE_CHINA == appLocale) {
            changLanguageTv.setText(R.string.lang_en);
            String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_CN, "");
            if(!TextUtils.isEmpty(countryName)){
                mCountryNameTv.setText(countryName);
                mCountryCodeTv.setText("+" + oldRegion);
            }
        } else if (LangUtils.RCLocale.LOCALE_US == appLocale) {
            changLanguageTv.setText(R.string.lang_chs);
            String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_EN, "");
            if(!TextUtils.isEmpty(countryName)){
                mCountryNameTv.setText(countryName);
                mCountryCodeTv.setText("+" + oldRegion);
            }
        } else {
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, this);
                changLanguageTv.setText(R.string.lang_en);
                String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_CN, "");
                if(!TextUtils.isEmpty(countryName)){
                    mCountryNameTv.setText(countryName);
                    mCountryCodeTv.setText("+" + oldRegion);
                }
            } else {
                RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, this);
                changLanguageTv.setText(R.string.lang_chs);
                String countryName = sp.getString(SealConst.SEALTALK_LOGIN_COUNTRY_EN, "");
                if(!TextUtils.isEmpty(countryName)){
                    mCountryNameTv.setText(countryName);
                    mCountryCodeTv.setText("+" + oldRegion);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.de_login_sign:
                phoneString = mPhoneEdit.getText().toString().trim();
                passwordString = mPasswordEdit.getText().toString().trim();

                if (TextUtils.isEmpty(phoneString)) {
                    NToast.shortToast(mContext, R.string.phone_number_is_null);
                    mPhoneEdit.setShakeAnimation();
                    return;
                }

//                if (!AMUtils.isMobile(phoneString)) {
//                    NToast.shortToast(mContext, R.string.Illegal_phone_number);
//                    mPhoneEdit.setShakeAnimation();
//                    return;
//                }

                if (TextUtils.isEmpty(passwordString)) {
                    NToast.shortToast(mContext, R.string.password_is_null);
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                if (passwordString.contains(" ")) {
                    NToast.shortToast(mContext, R.string.password_cannot_contain_spaces);
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                LoadDialog.show(mContext);
                editor.putBoolean("exit", false);
                editor.commit();
                String oldPhone = sp.getString(SealConst.SEALTALK_LOGING_PHONE, "");
                request(LOGIN, true);
                break;
            case R.id.de_login_register:
                startActivityForResult(new Intent(this, RegisterActivity.class), 1);
                break;
            case R.id.de_login_forgot:
                startActivityForResult(new Intent(this, ForgetPasswordActivity.class), 2);
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
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(loginIntent);
        taskStackBuilder.startActivities();
        overridePendingTransition(0, 0);
    }

    private void setPushLanguage(final RongIMClient.PushLanguage language) {
        RongIMClient.getInstance().setPushLanguage(language, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                //设置成功也存起来
                RongConfigurationManager.getInstance().setPushLanguage(LoginActivity.this, language);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                RLog.e(TAG, getString(R.string.setting_push_language_error));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && data != null) {
            String phone = data.getStringExtra("phone");
            String password = data.getStringExtra("password");
            String region = data.getStringExtra("region");
            String countryName = data.getStringExtra("country");
            String countryNameCN = data.getStringExtra("countryCN");
            String countryNameEN = data.getStringExtra("countryEN");
            if(!TextUtils.isEmpty(countryNameCN) && !TextUtils.isEmpty(countryNameEN)){
                mCountryNameCN = countryNameCN;
                mCountryNameEN = countryNameEN;
                mCountryCodeTv.setText("+" + region);
                mCountryNameTv.setText(countryName);
            }
            mPhoneEdit.setText(phone);
            mPasswordEdit.setText(password);
        } else if (data != null && requestCode == 1) {
            String phone = data.getStringExtra("phone");
            String password = data.getStringExtra("password");
            String id = data.getStringExtra("id");
            String nickname = data.getStringExtra("nickname");
            String region = data.getStringExtra("region");
            String countryName = data.getStringExtra("country");
            String countryNameCN = data.getStringExtra("countryCN");
            String countryNameEN = data.getStringExtra("countryEN");
            if(!TextUtils.isEmpty(countryNameCN) && !TextUtils.isEmpty(countryNameEN)){
                mCountryNameCN = countryNameCN;
                mCountryNameEN = countryNameEN;
                mCountryCodeTv.setText("+" + region);
                mCountryNameTv.setText(countryName);
            }
            if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(id) && !TextUtils.isEmpty(nickname)) {
                mPhoneEdit.setText(phone);
                mPasswordEdit.setText(password);
                editor.putString(SealConst.SEALTALK_LOGING_PHONE, phone);
                editor.putString(SealConst.SEALTALK_LOGING_PASSWORD, password);
                editor.putString(SealConst.SEALTALK_LOGIN_ID, id);
                editor.putString(SealConst.SEALTALK_LOGIN_NAME, nickname);
                if(!TextUtils.isEmpty(mCountryNameCN) && !TextUtils.isEmpty(mCountryNameEN)){
                    editor.putString(SealConst.SEALTALK_LOGIN_COUNTRY_CN, mCountryNameCN);
                    editor.putString(SealConst.SEALTALK_LOGIN_COUNTRY_EN, mCountryNameEN);
                    editor.putString(SealConst.SEALTALK_LOGIN_REGION, mRegion);
                }
                editor.commit();
            }
        } else if(requestCode == REQUEST_CODE_SELECT_COUNTRY && resultCode == RESULT_OK){
            String zipCode = data.getStringExtra("zipCode");
            String countryName = data.getStringExtra("countryName");
            String countryNameCN = data.getStringExtra("countryNameCN");
            String countryNameEN = data.getStringExtra("countryNameEN");
            mCountryNameCN = countryNameCN;
            mCountryNameEN = countryNameEN;
            mCountryCodeTv.setText(zipCode);
            mCountryNameTv.setText(countryName);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            case LOGIN:
                return action.login(region, phoneString, passwordString);
            case GET_TOKEN:
                return action.getToken();
            case SYNC_USER_INFO:
                return action.getUserInfoById(connectResultId);
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case LOGIN:
                    LoginResponse loginResponse = (LoginResponse) result;
                    if (loginResponse.getCode() == 200) {
                        loginToken = loginResponse.getResult().getToken();
                        if (!TextUtils.isEmpty(loginToken)) {
                            RongIM.connect(loginToken, new RongIMClient.ConnectCallback() {
                                @Override
                                public void onTokenIncorrect() {
                                    NLog.e("connect", "onTokenIncorrect");
                                    reGetToken();
                                }

                                @Override
                                public void onSuccess(String s) {
                                    connectResultId = s;
                                    NLog.e("connect", "onSuccess userid:" + s);
                                    editor.putString(SealConst.SEALTALK_LOGIN_ID, s);
                                    editor.commit();
                                    SealUserInfoManager.getInstance().openDB();
                                    request(SYNC_USER_INFO, true);
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    NLog.e("connect", "onError errorcode:" + errorCode.getValue());
                                }
                            });
                        }
                    } else if (loginResponse.getCode() == 100) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, R.string.phone_or_psw_error);
                    } else if (loginResponse.getCode() == 1000) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, R.string.phone_or_psw_error);
                    }
                    break;
                case SYNC_USER_INFO:
                    GetUserInfoByIdResponse userInfoByIdResponse = (GetUserInfoByIdResponse) result;
                    if (userInfoByIdResponse.getCode() == 200) {
                        if (TextUtils.isEmpty(userInfoByIdResponse.getResult().getPortraitUri())) {
                            userInfoByIdResponse.getResult().setPortraitUri(RongGenerate.generateDefaultAvatar(userInfoByIdResponse.getResult().getNickname(), userInfoByIdResponse.getResult().getId()));
                        }
                        String nickName = userInfoByIdResponse.getResult().getNickname();
                        String portraitUri = userInfoByIdResponse.getResult().getPortraitUri();
                        editor.putString(SealConst.SEALTALK_LOGIN_NAME, nickName);
                        editor.putString(SealConst.SEALTALK_LOGING_PORTRAIT, portraitUri);
                        editor.commit();
                        RongIM.getInstance().refreshUserInfoCache(new UserInfo(connectResultId, nickName, Uri.parse(portraitUri)));
                    }
                    //不继续在login界面同步好友,群组,群组成员信息
                    SealUserInfoManager.getInstance().getAllUserInfo();
                    goToMain();
                    break;
                case GET_TOKEN:
                    GetTokenResponse tokenResponse = (GetTokenResponse) result;
                    if (tokenResponse.getCode() == 200) {
                        String token = tokenResponse.getResult().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                                @Override
                                public void onTokenIncorrect() {
                                    Log.e(TAG, "reToken Incorrect");
                                }

                                @Override
                                public void onSuccess(String s) {
                                    connectResultId = s;
                                    NLog.e("connect", "onSuccess userid:" + s);
                                    editor.putString(SealConst.SEALTALK_LOGIN_ID, s);
                                    editor.commit();
                                    SealUserInfoManager.getInstance().openDB();
                                    request(SYNC_USER_INFO, true);
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode e) {

                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    private void reGetToken() {
        request(GET_TOKEN);
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (!CommonUtils.isNetworkConnected(mContext)) {
            LoadDialog.dismiss(mContext);
            NToast.shortToast(mContext, getString(R.string.network_not_available));
            return;
        }
        switch (requestCode) {
            case LOGIN:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, R.string.login_api_fail);
                break;
            case SYNC_USER_INFO:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, R.string.sync_userinfo_api_fail);
                break;
            case GET_TOKEN:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, R.string.get_token_api_fail);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void goToMain() {
        editor.putString("loginToken", loginToken);
        editor.putString(SealConst.SEALTALK_LOGING_PHONE, phoneString);
        editor.putString(SealConst.SEALTALK_LOGING_PASSWORD, passwordString);
        if(!TextUtils.isEmpty(mCountryNameCN) && !TextUtils.isEmpty(mCountryNameEN)){
            editor.putString(SealConst.SEALTALK_LOGIN_REGION, mRegion);
            editor.putString(SealConst.SEALTALK_LOGIN_COUNTRY_CN, mCountryNameCN);
            editor.putString(SealConst.SEALTALK_LOGIN_COUNTRY_EN, mCountryNameEN);
        }
        editor.commit();
        LoadDialog.dismiss(mContext);
        NToast.shortToast(mContext, R.string.login_success);
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
