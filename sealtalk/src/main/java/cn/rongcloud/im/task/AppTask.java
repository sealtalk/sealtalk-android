package cn.rongcloud.im.task;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.contact.TranslationLanguage;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.ChatRoomResult;
import cn.rongcloud.im.model.ProxyModel;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.net.AppProxyManager;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.SealTalkUrl;
import cn.rongcloud.im.net.service.AppService;
import cn.rongcloud.im.utils.DataCenter;
import cn.rongcloud.im.utils.DataCenterImpl;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import com.google.gson.Gson;
import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.utils.language.LangUtils;
import io.rong.imkit.utils.language.RongConfigurationManager;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.RCIMProxy;
import java.util.List;
import java.util.Locale;

public class AppTask {

    private AppService appsService;
    private Context context;

    public AppTask(Context context) {
        appsService =
                HttpClientManager.getInstance(context).getClient().createService(AppService.class);
        this.context = context.getApplicationContext();
    }

    /**
     * 获取当前的最新版本
     *
     * @return
     */
    public LiveData<Resource<VersionInfo>> getNewVersion() {
        return new NetworkOnlyResource<VersionInfo, VersionInfo>() {

            @NonNull
            @Override
            protected LiveData<VersionInfo> createCall() {
                return appsService.getNewVersion();
            }

            @Override
            protected VersionInfo transformRequestType(VersionInfo response) {
                return response;
            }
        }.asLiveData();
    }

    /** 获取 IMSDK 版本号 */
    public String getSDKVersion() {
        return RongCoreClient.getVersion();
    }

    /**
     * 获取聊天室
     *
     * @return
     */
    public LiveData<Resource<List<ChatRoomResult>>> getDiscoveryChatRoom() {
        return new NetworkOnlyResource<List<ChatRoomResult>, Result<List<ChatRoomResult>>>() {
            @NonNull
            @Override
            protected LiveData<Result<List<ChatRoomResult>>> createCall() {
                return appsService.getDiscoveryChatRoom();
            }
        }.asLiveData();
    }

    /**
     * 获取当前app 的语音设置
     *
     * @return
     */
    public LangUtils.RCLocale getLanguageLocal() {
        LangUtils.RCLocale appLocale =
                RongConfigurationManager.getInstance()
                        .getAppLocale(
                                context); // RongConfigurationManager.getInstance().getAppLocale(context);
        if (appLocale == LangUtils.RCLocale.LOCALE_AUTO) {
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                appLocale = LangUtils.RCLocale.LOCALE_CHINA;
            } else if (systemLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                appLocale = LangUtils.RCLocale.LOCALE_US;
            } else if (systemLocale.getLanguage().equals(new Locale("ar").getLanguage())) {
                appLocale = LangUtils.RCLocale.LOCALE_ARAB;
            } else {
                appLocale = LangUtils.RCLocale.LOCALE_CHINA;
            }
        }
        return appLocale;
    }

    /**
     * 设置当前应用的 语音
     *
     * @param selectedLocale
     */
    public boolean changeLanguage(LangUtils.RCLocale selectedLocale) {
        // todo
        //        LangUtils.RCLocale appLocale =
        // RongConfigurationManager.getInstance().getAppLocale(context);
        //        if (selectedLocale == appLocale) {
        //            return false;
        //        }

        if (selectedLocale == LangUtils.RCLocale.LOCALE_CHINA) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources resources = context.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                LocaleList localeList = new LocaleList(selectedLocale.toLocale());
                LocaleList.setDefault(localeList);
                config.setLocales(localeList);
                resources.updateConfiguration(config, dm);
                // 保存语言状态
                LangUtils.saveLocale(context, selectedLocale);

                //                Resources resources = getResources();
                //                Configuration config = resources.getConfiguration();
                //                DisplayMetrics dm = resources.getDisplayMetrics();
                //                if (language.equals("en")) {
                //                    config.locale = Locale.ENGLISH;
                //                } else {
                //                    config.locale = Locale.SIMPLIFIED_CHINESE;
                //                }
                //                resources.updateConfiguration(config, dm);
            } else {
                RongConfigurationManager.getInstance()
                        .switchLocale(LangUtils.RCLocale.LOCALE_CHINA, context);
            }
            setPushLanguage(RongIMClient.PushLanguage.ZH_CN);
        } else if (selectedLocale == LangUtils.RCLocale.LOCALE_US) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources resources = context.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                LocaleList localeList = new LocaleList(selectedLocale.toLocale());
                LocaleList.setDefault(localeList);
                config.setLocales(localeList);
                resources.updateConfiguration(config, dm);
                LangUtils.saveLocale(context, selectedLocale);
                //                updateResources(context,"en");
            } else {
                RongConfigurationManager.getInstance()
                        .switchLocale(LangUtils.RCLocale.LOCALE_US, context);
            }
            setPushLanguage(RongIMClient.PushLanguage.EN_US);
        } else if (selectedLocale == LangUtils.RCLocale.LOCALE_ARAB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources resources = context.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                LocaleList localeList = new LocaleList(selectedLocale.toLocale());
                LocaleList.setDefault(localeList);
                config.setLocales(localeList);
                resources.updateConfiguration(config, dm);
                LangUtils.saveLocale(context, selectedLocale);
            } else {
                RongConfigurationManager.getInstance()
                        .switchLocale(LangUtils.RCLocale.LOCALE_ARAB, context);
            }
            //            setPushLanguage(RongIMClient.PushLanguage.LOCALE_ARAB);
        }

        return true;
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }

    /**
     * 设置 push 的语言
     *
     * @param language
     */
    public void setPushLanguage(RongIMClient.PushLanguage language) {
        RongIMClient.getInstance()
                .setPushLanguageCode(
                        language.getMsg(),
                        new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                // 设置成功也存起来
                                // todo
                                // RongConfigurationManager.getInstance().setPushLanguage(context,
                                // language);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {}
                        });
    }

    /**
     * 是否是 Debug 模式
     *
     * @return
     */
    public boolean isDebugMode() {
        // TODO 获取是否是 Debug 模式
        return context.getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false);
    }

    public void setTranslationSrcLanguage(String language) {
        context.getSharedPreferences("config", MODE_PRIVATE)
                .edit()
                .putString("translation_src_language", language)
                .apply();
    }

    public void setTranslationTargetLanguage(String language) {
        context.getSharedPreferences("config", MODE_PRIVATE)
                .edit()
                .putString("translation_target_language", language)
                .apply();
    }

    public String getTranslationSrcLanguage() {
        return context.getSharedPreferences("config", MODE_PRIVATE)
                .getString("translation_src_language", TranslationLanguage.LANGUAGE_ZH_CN);
    }

    public String getTranslationTargetLanguage() {
        return context.getSharedPreferences("config", MODE_PRIVATE)
                .getString("translation_target_language", TranslationLanguage.LANGUAGE_EN);
    }

    public void changeDataCenter(DataCenter center) {
        saveDataCenter(center);
        RongCoreClient.getInstance().switchAppKey(center.getAppKey());
        RongCoreClient.setServerInfo(center.getNaviUrl(), null);
        IMCenter.init((Application) context.getApplicationContext(), center.getAppKey(), true);
        // 初始化扩展模块
        IMManager.getInstance().initExtensionModules(context);
        IMManager.getInstance().initMessageAndTemplate();
        SealTalkUrl.DOMAIN = center.getAppServer();
    }

    private void saveDataCenter(DataCenter dataCenter) {
        context.getSharedPreferences("config", MODE_PRIVATE)
                .edit()
                .putString("data_center", dataCenter.getCode())
                .apply();
    }

    private DataCenter getDataCenterByCode() {
        String code =
                context.getSharedPreferences("config", MODE_PRIVATE)
                        .getString("data_center", DataCenterImpl.SINGAPORE.getCode());
        return DataCenterImpl.valueByCode(code);
    }

    public void reInit() {
        IMManager.getInstance().init(((Application) context.getApplicationContext()));
    }

    public void saveProxyMode(ProxyModel proxyModel) {
        if (RongIM.getInstance().getCurrentConnectionStatus()
                        == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED
                || RongIM.getInstance().getCurrentConnectionStatus()
                        == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING
                || RongIM.getInstance().getCurrentConnectionStatus()
                        == RongIMClient.ConnectionStatusListener.ConnectionStatus.SUSPEND) {
            RongIM.getInstance().disconnect();
        }
        SharedPreferences.Editor configSp =
                context.getSharedPreferences("config", MODE_PRIVATE).edit();
        if (proxyModel == null) {
            configSp.remove("app_proxy").apply();
            AppProxyManager.getInstance().setProxy(null);
        } else {
            Gson gson = new Gson();
            String proxyJson = gson.toJson(proxyModel);
            configSp.putString("app_proxy", proxyJson).apply();
            AppProxyManager.getInstance()
                    .setProxy(
                            new RCIMProxy(
                                    proxyModel.getProxyHost(),
                                    proxyModel.getPort(),
                                    proxyModel.getUserName(),
                                    proxyModel.getPassword()));
        }
        IMManager.getInstance().cacheConnectIM();
    }

    public ProxyModel getProxy() {
        String proxy =
                context.getSharedPreferences("config", MODE_PRIVATE).getString("app_proxy", "");
        if (TextUtils.isEmpty(proxy)) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(proxy, ProxyModel.class);
        }
    }

    public DataCenter getCurrentDataCenter() {
        if (isSealChat()) {
            return getDataCenterByCode();
        } else {
            return new DataCenter() {
                @Override
                public String getNaviUrl() {
                    return BuildConfig.SEALTALK_NAVI_SERVER;
                }

                @Override
                public int getNameId() {
                    return R.string.data_center_beijing;
                }

                @Override
                public String getCode() {
                    return null;
                }

                @Override
                public String getAppKey() {
                    return BuildConfig.SEALTALK_APP_KEY;
                }

                @Override
                public String getAppServer() {
                    return BuildConfig.SEALTALK_SERVER;
                }
            };
        }
    }

    public boolean isSealChat() {
        return context.getApplicationInfo().processName.equals("cn.rongcloud.im.sg");
    }
}
