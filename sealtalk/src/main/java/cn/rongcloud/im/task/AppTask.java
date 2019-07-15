package cn.rongcloud.im.task;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.model.ChatRoomResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.service.AppService;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.common.BuildVar;

public class AppTask {

    private AppService appsService;
    private Context context;
    public AppTask(Context context){
        appsService = HttpClientManager.getInstance(context).getClient().createService(AppService.class);
        this.context = context.getApplicationContext();
    }

    /**
     * 获取当前的最新版本
     * @return
     */
    public LiveData<Resource<VersionInfo>> getNewVersion() {
        return new NetworkOnlyResource<VersionInfo, VersionInfo>(){

            @NonNull
            @Override
            protected LiveData<VersionInfo> createCall() {
                return appsService.getNewVersion ();
            }

            @Override
            protected VersionInfo transformRequestType(VersionInfo response) {
                return response;
            }
        }.asLiveData();
    }

    /**
     * SDK 版本号
     *
     * @return
     */
    public String getSDKVersion() {
        return BuildVar.SDK_VERSION;
    }

    /**
     * 获取聊天室
     *
     * @return
     */
    public LiveData<Resource<List<ChatRoomResult>>> getDiscoveryChatRoom(){
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
     * @return
     */
    public LangUtils.RCLocale getLanguageLocal() {

        LangUtils.RCLocale appLocale = RongConfigurationManager.getInstance().getAppLocale(context);
        if (appLocale == LangUtils.RCLocale.LOCALE_AUTO) {
            Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                appLocale = LangUtils.RCLocale.LOCALE_CHINA;
            } else if (systemLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                appLocale = LangUtils.RCLocale.LOCALE_US;
            } else {
                appLocale = LangUtils.RCLocale.LOCALE_CHINA;
            }
        }
        return appLocale;
    }

    /**
     * 设置当前应用的 语音
     * @param selectedLocale
     */
    public boolean changeLanguage(LangUtils.RCLocale selectedLocale) {
        LangUtils.RCLocale appLocale = RongConfigurationManager.getInstance().getAppLocale(context);
        if( selectedLocale == appLocale) {
            return false;
        }

        if (selectedLocale == LangUtils.RCLocale.LOCALE_CHINA) {
            RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_CHINA, context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources resources = context.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                LocaleList localeList = new LocaleList(selectedLocale.toLocale());
                LocaleList.setDefault(localeList);
                config.setLocales(localeList);
                resources.updateConfiguration(config, dm);
            }
            setPushLanguage(RongIMClient.PushLanguage.ZH_CN);
        } else if(selectedLocale == LangUtils.RCLocale.LOCALE_US) {
            RongConfigurationManager.getInstance().switchLocale(LangUtils.RCLocale.LOCALE_US, context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources resources = context.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                LocaleList localeList = new LocaleList(selectedLocale.toLocale());
                LocaleList.setDefault(localeList);
                config.setLocales(localeList);
                resources.updateConfiguration(config, dm);
            }
            setPushLanguage(RongIMClient.PushLanguage.EN_US);
        }

        return true;
    }

    /**
     * 设置 push 的语言
     * @param language
     */
    public void setPushLanguage(RongIMClient.PushLanguage language) {
        RongIMClient.getInstance().setPushLanguage(language, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                //设置成功也存起来
                RongConfigurationManager.getInstance().setPushLanguage(context, language);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    /**
     * 是否是 Debug 模式
     * @return
     */
    public boolean isDebugMode() {
        //TODO 获取是否是 Debug 模式
        return false;
    }
}
