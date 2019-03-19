package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.ImageView;

import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import io.rong.common.RLog;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.utilities.LangUtils.RCLocale;
import io.rong.imlib.RongIMClient;

public class SetLanguageActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = SetLanguageActivity.class.getSimpleName();
    private RCLocale originalLocale;
    private RCLocale selectedLocale;

    private ImageView chineseCheckbox;
    private ImageView englishCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_language);
        setTitle(R.string.setting_language);

        findViewById(R.id.ll_chinese).setOnClickListener(SetLanguageActivity.this);
        findViewById(R.id.ll_english).setOnClickListener(SetLanguageActivity.this);
        chineseCheckbox = (ImageView) findViewById(R.id.img_chinese_checkbox);
        englishCheckbox = (ImageView) findViewById(R.id.img_english_checkbox);


        originalLocale = RongConfigurationManager.getInstance().getAppLocale(this);
        if (originalLocale == RCLocale.LOCALE_CHINA) {
            selectLocale(RCLocale.LOCALE_CHINA);
        } else if (originalLocale == RCLocale.LOCALE_US) {
            selectLocale(RCLocale.LOCALE_US);
        } else {
            //auto值则以系统为标准设定
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                originalLocale = RCLocale.LOCALE_CHINA;
                selectLocale(RCLocale.LOCALE_CHINA);
            } else if (systemLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                originalLocale = RCLocale.LOCALE_US;
                selectLocale(RCLocale.LOCALE_US);
            } else {
                originalLocale = RCLocale.LOCALE_CHINA;
                selectLocale(RCLocale.LOCALE_CHINA);
            }
        }
    }

    private void selectLocale(RCLocale locale) {
        if (locale == RCLocale.LOCALE_CHINA) {
            englishCheckbox.setImageDrawable(null);
            chineseCheckbox.setImageResource(R.drawable.ic_checkbox_full);
            selectedLocale = RCLocale.LOCALE_CHINA;
        } else if (locale == RCLocale.LOCALE_US) {
            chineseCheckbox.setImageDrawable(null);
            englishCheckbox.setImageResource(R.drawable.ic_checkbox_full);
            selectedLocale = RCLocale.LOCALE_US;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_chinese) {
            selectLocale(RCLocale.LOCALE_CHINA);
            changeLanguageAndRestart(selectedLocale);
        } else if (v.getId() == R.id.ll_english) {
            selectLocale(RCLocale.LOCALE_US);
            changeLanguageAndRestart(selectedLocale);
        }
    }

    private void changeLanguageAndRestart(RCLocale selectedLocale){
        if( selectedLocale == originalLocale) return;

        if (selectedLocale == RCLocale.LOCALE_CHINA) {
            RongConfigurationManager.getInstance().switchLocale(RCLocale.LOCALE_CHINA, this);
            App.updateApplicationLanguage();
            setPushLanguage(RongIMClient.PushLanguage.ZH_CN);
        } else if(selectedLocale == RCLocale.LOCALE_US) {
            RongConfigurationManager.getInstance().switchLocale(RCLocale.LOCALE_US, this);
            App.updateApplicationLanguage();
            setPushLanguage(RongIMClient.PushLanguage.EN_US);
        }
        backToSettingActivity();
    }

    private void setPushLanguage(final RongIMClient.PushLanguage language) {
        RongIMClient.getInstance().setPushLanguage(language, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                //设置成功也存起来
                RongConfigurationManager.getInstance().setPushLanguage(SetLanguageActivity.this, language);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                RLog.e(TAG, getString(R.string.setting_push_language_error));
            }
        });
    }

    private void backToSettingActivity() {
        Intent mainActivity = new Intent(SetLanguageActivity.this, MainActivity.class);
        mainActivity.putExtra(MainActivity.INITIAL_TAB_INDEX, MainActivity.TAB_ME_INDEX);

        Intent settLanguageActivity = new Intent(SetLanguageActivity.this, SetLanguageActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(SetLanguageActivity.this);
        taskStackBuilder.addNextIntent(mainActivity);
        taskStackBuilder.addNextIntent(settLanguageActivity);
        taskStackBuilder.startActivities();
        overridePendingTransition(0, 0);
    }
}
