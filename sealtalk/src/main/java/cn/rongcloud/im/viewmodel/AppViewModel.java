package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.model.ChatRoomResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.utilities.LangUtils;

public class AppViewModel extends AndroidViewModel {
    private final AppTask appTask;
    private String sealTalkVersionName;
    private SingleSourceMapLiveData<Resource<VersionInfo>, Resource<VersionInfo.AndroidVersion>> hasNew;
    private MutableLiveData<String> sdkVersion = new MutableLiveData<>();
    private MutableLiveData<String> sealTalkVersion = new MutableLiveData<>();
    private SingleSourceLiveData<Resource<List<ChatRoomResult>>> chatRoomResultList = new SingleSourceLiveData<>();
    private MutableLiveData<LangUtils.RCLocale> languageLocal = new MutableLiveData<>();
    private MutableLiveData<Boolean> debugMode = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        appTask = new AppTask(application);
        sealTalkVersionName = getSealTalkVersion(application);

        hasNew = new SingleSourceMapLiveData<>(new Function<Resource<VersionInfo>, Resource<VersionInfo.AndroidVersion>>() {
            @Override
            public Resource<VersionInfo.AndroidVersion> apply(Resource<VersionInfo> input) {
                if (input.data != null) {
                    SLog.d("ss_version", "input == " + input);
                    String newVersion = input.data.getAndroidVersion().getVersion();
                    if (sealTalkVersionName != null) {
                        boolean needUpdate = true;
                        try {
                            String[] newVersionCodeArray = newVersion.split("\\.");
                            String[] curVersionCodeArray = sealTalkVersionName.split("\\.");
                            int curVerLen = curVersionCodeArray.length;
                            int newVerLen = newVersionCodeArray.length;
                            String curVer = sealTalkVersionName.replace(".", "");
                            String newVer = newVersion.replace(".", "");
                            if (curVerLen > newVerLen) {
                                //补齐位数
                                StringBuilder sBuilder = new StringBuilder(newVer);
                                for (int i = 0; i < curVerLen - newVerLen; i++) {
                                    sBuilder.append("0");
                                }
                                newVer = sBuilder.toString();
                            } else if (curVerLen < newVerLen) {
                                //补齐位数
                                StringBuilder sBuilder = new StringBuilder(curVer);
                                for (int i = 0; i < newVerLen - curVerLen; i++) {
                                    sBuilder.append("0");
                                }
                                curVer = sBuilder.toString();
                            }
                            needUpdate = Integer.parseInt(newVer) > Integer.parseInt(curVer);
                        } catch (Exception e) {
                            SLog.w(LogTag.API, "compare version error, force to use new version.", e);
                        }
                        if (needUpdate) {
                            return new Resource<VersionInfo.AndroidVersion>(input.status, input.data.getAndroidVersion(), input.code);
                        }
                    }

                }
                return new Resource<VersionInfo.AndroidVersion>(input.status, null, input.code);
            }
        });

        sdkVersion.setValue(getSdkVersion());
        sealTalkVersion.setValue(sealTalkVersionName);
        checkVersion();
        requestChatRoomList();
        // 语言
        languageLocal.setValue(appTask.getLanguageLocal());

        debugMode.setValue(appTask.isDebugMode());
    }

    /**
     * 检测是否有新版本
     *
     * @return
     */
    public LiveData<Resource<VersionInfo.AndroidVersion>> getHasNewVersion() {
        return hasNew;
    }

    /**
     * 获取sdk 版本
     *
     * @return
     */
    public LiveData<String> getSDKVersion() {
        return sdkVersion;
    }

    /**
     * sealtalk 版本
     *
     * @return
     */
    public LiveData<String> getSealTalkVersion() {
        return sealTalkVersion;
    }

    /**
     * 检测版本
     */
    private void checkVersion() {
        hasNew.setSource(appTask.getNewVersion());
    }

    /**
     * 请求聊天室列表
     */
    public void requestChatRoomList() {
        chatRoomResultList.setSource(appTask.getDiscoveryChatRoom());
    }

    /**
     * 获取聊天室列表
     *
     * @return
     */
    public LiveData<Resource<List<ChatRoomResult>>> getChatRoonList() {
        return chatRoomResultList;
    }

    /**
     * 获取SDK版本
     */
    private String getSdkVersion() {
        return appTask.getSDKVersion();
    }

    /**
     * 当前本地语音
     *
     * @return
     */
    public LiveData<LangUtils.RCLocale> getLanguageLocal() {
        return languageLocal;
    }

    /**
     * 获取 SealTalk 版本
     *
     * @param application
     * @return
     */
    private String getSealTalkVersion(Context application) {
        try {
            PackageInfo packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 比较版本
     *
     * @param currentVersion
     * @param newVersion
     * @return
     */
    private boolean hasNewVersion(String currentVersion, String newVersion) {

        currentVersion = currentVersion.replace("\\.", "");
        newVersion = newVersion.replace("\\.", "");

        if (Integer.parseInt(newVersion.toString()) > Integer.parseInt(currentVersion.toString())) {
            return true;
        }
        return false;
    }

    /**
     * 切换语音
     *
     * @param selectedLocale
     */
    public void changeLanguage(LangUtils.RCLocale selectedLocale) {
        if (appTask.changeLanguage(selectedLocale)) {
            languageLocal.postValue(appTask.getLanguageLocal());
        }
    }

    public LiveData<Boolean> getDebugMode() {
        return debugMode;
    }
}
