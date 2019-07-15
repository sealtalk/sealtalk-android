package cn.rongcloud.im.ui.activity;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.AppViewModel;
import io.rong.imkit.utilities.LangUtils;

public class ChangeLanguageActivity extends TitleBaseActivity {

    private SettingItemView chineseSiv;
    private SettingItemView englishSiv;
    private AppViewModel appViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);
        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_mine_change_language);

        chineseSiv = findViewById(R.id.siv_chinese);
        chineseSiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //中文
                chineseSiv.setSelected(true);
                englishSiv.setSelected(false);
                changeLanguage(LangUtils.RCLocale.LOCALE_CHINA);
                backToSettingActivity();
            }
        });
        englishSiv =  findViewById(R.id.siv_english);
        englishSiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //英文
                chineseSiv.setSelected(false);
                englishSiv.setSelected(true);
                changeLanguage(LangUtils.RCLocale.LOCALE_US);
                backToSettingActivity();
            }
        });
    }

    /**
     * 初始化Viewmodel
     */
    private void initViewModel() {
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        // 当前app 语音
        appViewModel.getLanguageLocal().observe(this, new Observer<LangUtils.RCLocale>() {
            @Override
            public void onChanged(LangUtils.RCLocale rcLocale) {
                if (rcLocale == LangUtils.RCLocale.LOCALE_US) {
                    chineseSiv.setSelected(false);
                    englishSiv.setSelected(true);
                } else  {
                    chineseSiv.setSelected(true);
                    englishSiv.setSelected(false);
                }
            }
        });
    }


    /**
     * 切换语言
     * @param selectedLocale
     */
    private void changeLanguage(LangUtils.RCLocale selectedLocale){
        if(appViewModel != null) {
            appViewModel.changeLanguage(selectedLocale);
        }
    }


    private void backToSettingActivity() {
        Intent mainActivity = new Intent(ChangeLanguageActivity.this, MainActivity.class);
        mainActivity.putExtra(MainActivity.PARAMS_TAB_INDEX, MainActivity.Tab.ME.getValue());
        Intent settLanguageActivity = new Intent(ChangeLanguageActivity.this, ChangeLanguageActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(ChangeLanguageActivity.this);
        taskStackBuilder.addNextIntent(mainActivity);
        taskStackBuilder.addNextIntent(settLanguageActivity);
        taskStackBuilder.startActivities();
        overridePendingTransition(0, 0);
    }
}
