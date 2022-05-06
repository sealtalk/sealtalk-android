package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.contact.TranslationLanguage;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.ui.view.SettingItemView;
import java.util.ArrayList;
import java.util.List;

/** @author gusd */
public class TranslationSettingActivity extends TitleBaseActivity {
    private static final String TAG = "TranslationSettingActivity";
    public static List<Pair<String, String>> LANGUAGE_LIST = new ArrayList<>();
    private AppTask appTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_setting);
        initView();
        initData();
    }

    private void initView() {
        findViewById(R.id.siv_translation_src_language)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent =
                                        new Intent(
                                                TranslationSettingActivity.this,
                                                TranslationLanguageListActivity.class);
                                intent.putExtra("type", "src");
                                startActivity(intent);
                            }
                        });

        findViewById(R.id.siv_translation_target_language)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent =
                                        new Intent(
                                                TranslationSettingActivity.this,
                                                TranslationLanguageListActivity.class);
                                intent.putExtra("type", "target");
                                startActivity(intent);
                            }
                        });
        appTask = new AppTask(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((SettingItemView) findViewById(R.id.siv_translation_target_language))
                .setValue(getLanguageNameByCode(appTask.getTranslationTargetLanguage()));
        ((SettingItemView) findViewById(R.id.siv_translation_src_language))
                .setValue(getLanguageNameByCode(appTask.getTranslationSrcLanguage()));
    }

    public String getLanguageNameByCode(String code) {
        for (Pair<String, String> pair : LANGUAGE_LIST) {
            if (pair.first.equals(code)) {
                return pair.second;
            }
        }
        return "";
    }

    private void initData() {
        if (!LANGUAGE_LIST.isEmpty()) {
            return;
        }
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_AF, "南非荷兰语（南非）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_SQ, "阿尔巴尼亚语（阿尔巴尼亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_AM, "阿姆哈拉语（埃塞俄比亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_AR, "阿拉伯语（沙特阿拉伯）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HY, "亚美尼亚语（亚美尼亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_AZ, "阿塞拜疆语（阿塞拜疆）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_EU, "巴斯克语（西班牙）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_BE, "白俄罗斯语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_BN, "孟加拉语（孟加拉）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_BS, "波斯尼亚语（波斯尼亚和黑塞哥维那）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_BG, "保加利亚语（保加利亚)"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MY, "缅甸语（缅甸）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_CA, "加泰罗尼亚语（西班牙）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_CEB, "宿务语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_ZH_CN, "中文普通话（中国简体）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_ZH_TW, "中文粤语（香港繁体)"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_CO, "科西嘉语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HR, "克罗地亚语（克罗地亚"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_CS, "捷克语（捷克共和国）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_DA, "丹麦语（丹麦）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_NL, "荷兰语（荷兰）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_EN, "英语（英国）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_EO, "世界语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_ET, "爱沙尼亚语（爱沙尼亚)"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_TL, "菲律宾语（菲律宾）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_FI, "芬兰语（芬兰）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_FR_FR, "法语（法国）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_FR, "法语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_FY, "弗里斯兰语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_GL, "加利西亚语（西班牙）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KA, "格鲁吉亚语（格鲁吉亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_DE, "德语（德国）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_EL, "希腊语（希腊"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_GU, "古吉拉特语（印度）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HT, "海地克里奥尔语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HA, "豪萨语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HAW, "夏威夷语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_IW, "希伯来语（以色列）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HI, "印地语（印度）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HMN, "苗语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_HU, "匈牙利语（匈牙利）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_IS, "冰岛语（冰岛）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_IG, "伊博语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_ID, "印度尼西亚语（印度尼西亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_GA, "爱尔兰语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_IT, "意大利语（意大利）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_JA, "日语（日本）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_JV, "爪哇语（印度尼西亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KN, "卡纳达语（印度）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KK, "哈萨克语（哈萨克斯坦）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KM, "高棉语（柬埔寨）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_RW, "卢旺达语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KO, "韩语（韩国）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KU, "库尔德语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_KY, "吉尔吉斯语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_LO, "老挝语（老挝）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_LV, "拉脱维亚语（拉脱维亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_LT, "立陶宛语（立陶宛）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_LB, "卢森堡语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MK, "马其顿语（北马其顿）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MG, "马尔加什语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MS, "马来语（马来西亚）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_ML, "马拉雅拉姆语（印度）"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MT, "马耳他语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MI, "毛利语"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MR, "马拉地语（印度)"));
        LANGUAGE_LIST.add(Pair.create(TranslationLanguage.LANGUAGE_MN, "蒙古语（蒙古）"));
    }
}
