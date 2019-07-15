package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
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
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.fragment.LoginFindPasswordFragment;
import cn.rongcloud.im.ui.fragment.LoginFragment;
import cn.rongcloud.im.ui.fragment.LoginRegisterFragment;
import cn.rongcloud.im.viewmodel.AppViewModel;
import io.rong.imkit.utilities.LangUtils;

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
    private Fragment[] fragments = new Fragment[3];

    private View loginBg;
    private TextView changLang;
    private TextView registerRight;
    private TextView registerLeft;
    private TextView findPassword;
    private TextView toLogin;
    private AppViewModel appViewModel;
    private int currentFragmentIndex = FRAGMENT_LOGIN;// 当前选择 Fragment 下标


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_login);

        // 复原上次选的 Fragment
        if(savedInstanceState != null){
            currentFragmentIndex = savedInstanceState.getInt(BUNDLE_LAST_SELECTED_FRAGMENT, FRAGMENT_LOGIN);
        }

        initView();
        initViewModel();
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

        changLang.setOnClickListener(this);
        registerLeft.setOnClickListener(this);
        registerRight.setOnClickListener(this);
        findPassword.setOnClickListener(this);
        toLogin.setOnClickListener(this);

        //默认是登录界面
        controlBottomView(currentFragmentIndex);

        startBgAnimation();
    }


    private void controlBottomView(int index) {
        switch (index) {
            case FRAGMENT_REGISTER:
                registerLeft.setVisibility(View.GONE);
                registerRight.setVisibility(View.GONE);
                findPassword.setVisibility(View.VISIBLE);
                toLogin.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_FIND_PASSWORD:
                registerLeft.setVisibility(View.VISIBLE);
                registerRight.setVisibility(View.GONE);
                findPassword.setVisibility(View.GONE);
                toLogin.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_LOGIN:
                registerLeft.setVisibility(View.GONE);
                registerRight.setVisibility(View.VISIBLE);
                findPassword.setVisibility(View.VISIBLE);
                toLogin.setVisibility(View.GONE);
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
            if(fragment == null) {
                fragment = new LoginFragment();
            }
        } else if (i == FRAGMENT_REGISTER) {
            fragment = supportFragmentManager.findFragmentByTag(LoginRegisterFragment.class.getCanonicalName());
            if(fragment == null) {
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
            if(fragment == null) {
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
     * 切换语言
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
}
