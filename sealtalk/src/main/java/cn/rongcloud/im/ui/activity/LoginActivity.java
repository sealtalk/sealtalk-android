package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.fragment.LoginFindPasswordFragment;
import cn.rongcloud.im.ui.fragment.LoginFragment;
import cn.rongcloud.im.ui.fragment.LoginRegisterFragment;
import cn.rongcloud.im.utils.StatusBarUtil;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import io.rong.imkit.utils.language.LangUtils;

/**
 * 登录界面
 * 用户可以在这个界面通过帐号登录到 业务服务器 并从中获取获取到连接 融云IM 服务器 所必须的 token
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final int FRAGMENT_LOGIN = 0;
    private static final int FRAGMENT_REGISTER = 1;
    private static final int FRAGMENT_FIND_PASSWORD = 2;
    private static final String BUNDLE_LAST_SELECTED_FRAGMENT = "last_select_fragment";
    private Fragment[] fragments = new Fragment[1];

    private View loginBg;
    private TextView changLang;
    private TextView registerRight;
    private TextView registerLeft;
    private TextView findPassword;
    private TextView toLogin;
    private AppViewModel appViewModel;
    private int currentFragmentIndex = FRAGMENT_LOGIN;// 当前选择 Fragment 下标
    private TextView registrationTerms;
    private TextView mSealTalkVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.login_activity_login);
        // 复原上次选的 Fragment
        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt(BUNDLE_LAST_SELECTED_FRAGMENT, FRAGMENT_LOGIN);
        }
        initView();
        initViewModel();
    }

    private void initStatusBar() {
        //这里注意下 因为在评论区发现有网友调用setRootViewFitsSystemWindows 里面 winContent.getChildCount()=0 导致代码无法继续
        //是因为你需要在setContentView之后才可以调用 setRootViewFitsSystemWindows
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, false)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x000000);
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        loginBg = findViewById(R.id.iv_login_bg);
        changLang = findViewById(R.id.tv_change_lang);

        registerLeft = findViewById(R.id.tv_register_left);
        registerRight = findViewById(R.id.tv_register_right);
        findPassword = findViewById(R.id.tv_find_passsword);
        toLogin = findViewById(R.id.tv_login);
        mSealTalkVersion = findViewById(R.id.tv_seal_talk_version);
        initRegistrationTerms();

        changLang.setOnClickListener(this);
        registerLeft.setOnClickListener(this);
        registerRight.setOnClickListener(this);
        findPassword.setOnClickListener(this);
        toLogin.setOnClickListener(this);

        //默认是登录界面
        controlBottomView(currentFragmentIndex);

        startBgAnimation();

        // 判断是否被其他用户踢出到此界面
        Intent intent = getIntent();
        boolean isKicked = intent.getBooleanExtra(IntentExtra.BOOLEAN_KICKED_BY_OTHER_USER, false);
        if (isKicked) {
            showKickedByOtherDialog();
        }
    }

    private void initRegistrationTerms() {
        registrationTerms = findViewById(R.id.tv_registration_terms);
        registrationTerms.setText(Html.fromHtml("<font color='#5C6970'>" + getString(R.string.seal_talk_login_bottom_registration_text_front) + "</font>" + "<br>" + "<font color='#5C6970'>" + getString(R.string.seal_talk_login_bottom_registration_text_behand) + "</font>"));

        String text = registrationTerms.getText().toString();
        int index = text.indexOf("\"");
        if (index == -1) {
            index = text.indexOf("⟪");
        }
        if (index == -1) {
            return;
        }
        SpannableString str = new SpannableString(registrationTerms.getText());
        str.setSpan(new NoRefCopySpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(LoginActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.PARAMS_TITLE, getString(R.string.seal_talk_registration_title));
                intent.putExtra(WebViewActivity.PARAMS_URL, "file:///android_asset/agreement_zh.html");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);

            }
        }, index, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        registrationTerms.setText(str);
        registrationTerms.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
        registrationTerms.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
    }

    private void controlBottomView(int index) {
        switch (index) {
            case FRAGMENT_REGISTER:
//                registerLeft.setVisibility(View.GONE);
//                registerRight.setVisibility(View.GONE);
//                findPassword.setVisibility(View.VISIBLE);
//                toLogin.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_FIND_PASSWORD:
//                registerLeft.setVisibility(View.VISIBLE);
//                registerRight.setVisibility(View.GONE);
//                findPassword.setVisibility(View.GONE);
//                toLogin.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_LOGIN:
//                registerLeft.setVisibility(View.GONE);
//                registerRight.setVisibility(View.VISIBLE);
//                findPassword.setVisibility(View.VISIBLE);
//                toLogin.setVisibility(View.GONE);
                break;
        }

        showFragment(index);
        currentFragmentIndex = index;
    }


    private void showFragment(int index) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment;
        for (int i = 0; i < fragments.length; i++) {
            fragment = fragments[i];
            if (fragment == null) {
                fragment = createFragment(i);
                fragments[i] = fragment;
                if (fragment != null && !fragment.isAdded()) {
                    fragmentTransaction.add(R.id.fragment_container, fragment, fragment.getClass().getCanonicalName());
                }
            }
            if (index == i) {
                fragmentTransaction.show(fragment);
            } else {
                fragmentTransaction.hide(fragment);
            }
        }
        fragmentTransaction.commit();
    }

    private Fragment createFragment(int i) {
        Fragment fragment = null;
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (i == FRAGMENT_LOGIN) {
            fragment = supportFragmentManager.findFragmentByTag(LoginFragment.class.getCanonicalName());
            if (fragment == null) {
                fragment = new LoginFragment();
            }
        } else if (i == FRAGMENT_REGISTER) {
            fragment = supportFragmentManager.findFragmentByTag(LoginRegisterFragment.class.getCanonicalName());
            if (fragment == null) {
                LoginRegisterFragment loginRegisterFragment = new LoginRegisterFragment();
                loginRegisterFragment.setOnOnRegisterListener(new LoginRegisterFragment.OnRegisterListener() {
                    @Override
                    public void onRegisterSuccess(String phone, String region, String countryName) {
                        showFragment(FRAGMENT_LOGIN);
                        ((LoginFragment) fragments[FRAGMENT_LOGIN]).setLoginParams(phone, region, countryName);
                    }
                });
                fragment = loginRegisterFragment;
            }
        } else if (i == FRAGMENT_FIND_PASSWORD) {
            fragment = supportFragmentManager.findFragmentByTag(LoginFindPasswordFragment.class.getCanonicalName());
            if (fragment == null) {
                LoginFindPasswordFragment loginFindPasswordFragment = new LoginFindPasswordFragment();
                loginFindPasswordFragment.setOnResetPasswordListener(new LoginFindPasswordFragment.OnResetPasswordListener() {
                    @Override
                    public void onResetPasswordSuccess(String phone, String region, String countryName) {
                        showFragment(FRAGMENT_LOGIN);
                        ((LoginFragment) fragments[FRAGMENT_LOGIN]).setLoginParams(phone, region, countryName);
                    }
                });
                fragment = loginFindPasswordFragment;
            }
        }
        return fragment;
    }

    /**
     * 注册监听 ViewModel
     */
    private void initViewModel() {
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        appViewModel.getLanguageLocal().observe(this, new Observer<LangUtils.RCLocale>() {
            @Override
            public void onChanged(LangUtils.RCLocale rcLocale) {
                if (rcLocale == LangUtils.RCLocale.LOCALE_US) {
                    changLang.setText(R.string.lang_chs);
                } else {
                    changLang.setText(R.string.lang_en);
                }
            }
        });

        AppViewModel appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);

        // sealtalk 版本
        appViewModel.getSealTalkVersion().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String version) {
                mSealTalkVersion.setText(getString(R.string.seal_talk_version_text, version));
            }
        });
    }


    /**
     * 背景微动画
     */
    private void startBgAnimation() {
        Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.seal_login_bg_translate_anim);
        loginBg.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register_left:
            case R.id.tv_register_right:
                controlBottomView(FRAGMENT_REGISTER);
                break;
            case R.id.tv_login:
                controlBottomView(FRAGMENT_LOGIN);
                break;
            case R.id.tv_find_passsword:
                controlBottomView(FRAGMENT_FIND_PASSWORD);
                break;
            case R.id.tv_change_lang:
                // 切换语言操作
                String langValue = changLang.getText().toString();
                if (langValue.equals("EN")) {
                    changeLanguage(LangUtils.RCLocale.LOCALE_US);
                } else {
                    changeLanguage(LangUtils.RCLocale.LOCALE_CHINA);
                }
                restartActivity();
                break;
            default:
                // Do nothing
                break;
        }
    }

    /**
     * 设置切换语言后， 重启activity
     */
    private void restartActivity() {
        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * 显示他人登录对话框
     */
    private void showKickedByOtherDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_login_kick_by_other));
        builder.setIsOnlyConfirm(true);
        builder.isCancelable(false);
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), null);
    }


    /**
     * 切换语言
     *
     * @param locale
     */
    private void changeLanguage(LangUtils.RCLocale locale) {
        if (appViewModel != null) {
            appViewModel.changeLanguage(locale);
        }
    }

    @Override
    public void clearAllFragmentExistBeforeCreate() {
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前选择的小标
        outState.putInt(BUNDLE_LAST_SELECTED_FRAGMENT, currentFragmentIndex);
    }

    /**
     * 不监听登出事件
     *
     * @return
     */
    @Override
    public boolean isObserveLogout() {
        return false;
    }

    public static class NoRefCopySpan extends ClickableSpan {

        @Override
        public void onClick(@NonNull View widget) {

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
        }

    }
}
